package jp.ndca.recommend.plsa;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import jp.ndca.recommend.Learner;
import jp.ndca.recommend.Predictor;
import jp.ndca.recommend.common.data.DatasetAnalyzer;
import jp.ndca.recommend.common.data.DatasetHandler;
import jp.ndca.recommend.common.data.RatingData;
import jp.ndca.recommend.common.data.RatingDataset;
import jp.ndca.recommend.common.map.Int2DoubleExtOpenHashMap;
import jp.ndca.recommend.common.structure.vector.SparseDotVector;
import jp.ndca.recommend.common.structure.vector.SparseDotVector.Node;
import jp.ndca.recommend.common.structure.vector.Vector;
import jp.ndca.recommend.common.structure.vector.VectorFactory;
import jp.ndca.recommend.common.util.PrimitiveArrayHandler;
import jp.ndca.recommend.common.validation.DatasetValidator;
import jp.ndca.recommend.common.validation.RatingDataValidations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;


/**
 * continuous forced model and this model use Gaussian PLSA.
 * 
 * @author hattori_tsukasa
 *
 */
public class ForcedPLSALearner implements Learner {

	private static Logger log = LoggerFactory.getLogger( ForcedPLSALearner.class );
	private final static double DEFAULT_SMOOTHING_PARAM = 2;
	private final static double DEFAULT_CONVERGENCE_RATE = 0.0001;
	private final static double DEFAULT_VALUE = 0.5;
	private final static double DEFAULT_ETA = 0.8;
	private final static double DEFAULT_MIN_VAR = 0.1;
	private final static int DEFAULT_K = 10;
	private final static int DEFAULT_THRED_NUM = 1;
	private final static int DEFAULT_MAX_ITERATION = 30;
	
	protected static RatingDataValidations validations = new RatingDataValidations();
	static{
		validations.addSparseDotVectorCondition();
		validations.addDataSizeCondition(1, Integer.MAX_VALUE);
	}

	private int threadNum = DEFAULT_THRED_NUM;
	public int getThreadNum() {
		return threadNum;
	}
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
		
	private int maxIteration = DEFAULT_MAX_ITERATION;
	public int getMaxIteration() {
		return maxIteration;
	}
	public void setMaxIteration(int maxIteration) {
		this.maxIteration = maxIteration;
	}
	
	private double eta = DEFAULT_ETA;
	public double getEta() {
		return eta;
	}
	public void setEta(double eta) {
		this.eta = eta;
	}
	
	private double minVar = DEFAULT_MIN_VAR;
	public double getMinVar() {
		return minVar;
	}
	public void setMinVar(double minVar) {
		this.minVar = minVar;
	}
	
	private int k = DEFAULT_K;
	public int getK() {
		return k;
	}
	public void setK( int k ){
		this.k = k;	
	}
	
	private double initValue = DEFAULT_VALUE;
	public double getInitValue() {
		return initValue;
	}
	public void setInitValue(double initValue) {
		this.initValue = initValue;
	}
	
	private double smoothingParam = DEFAULT_SMOOTHING_PARAM;
	public double getSmoothingParam() {
		return smoothingParam;
	}
	public void setSmoothingParam(double smoothingParam) {
		this.smoothingParam = smoothingParam;
	}

	private boolean testConvergence;
	public boolean isTestConvergence() {
		return testConvergence;
	}
	public void setTestConvergence(boolean testConvergence) {
		this.testConvergence = testConvergence;
	}
	
	private double convergenceRate = DEFAULT_CONVERGENCE_RATE;
	public double getConvergenceRate() {
		return convergenceRate;
	}
	public void setConvergenceRate(double convergenceRate) {
		this.convergenceRate = convergenceRate;
	}
	
	private int n;
	private int m;
	private double[] mu_u;
	private double[] var_u;
	private SparseDotVector[] userItem;
	private Int2ObjectMap<SparseDotVector> itemUser;
	private double preQValue;
	private double preQDiff;
	
	private double[][] p_z_u;		// p(z|u)	: n × z
	private double[][] mu_iz;		// μ_iz	: m × z
	private double[][] var_iz;	// σ^2_iz 	: m × z


	/**
	 * constractor
	 * @param dataset
	 */
	public ForcedPLSALearner(){};

	public ForcedPLSALearner( RatingDataset dataset ){
		setRatingDataset(dataset);
	}

	@Override
	public void setRatingDataset( RatingDataset dataset ) {
		DatasetValidator.check( dataset, validations );
		normalize(dataset);
	}
	
	private void normalize( RatingDataset dataset ){
		int dataSize = dataset.size();
		mu_u = new double[dataSize];
		var_u = new double[dataSize];
		userItem = new SparseDotVector[dataSize];
		Int2ObjectMap<Int2DoubleExtOpenHashMap> itemuser = new Int2ObjectOpenHashMap<Int2DoubleExtOpenHashMap>();
		double mu = DatasetAnalyzer.aveValue(dataset);
		double sigma = DatasetAnalyzer.varValue(dataset, mu);
		log.info("mu all : " + mu);
		log.info("var all : " + sigma);
		
		for( int dataID = 0 ; dataID < dataset.size() ; dataID++ ){
			RatingData data = dataset.fastGet(dataID);
			Vector vec = data.getVector();
			int size = vec.size();
			
			// μ_u = ( Σ_<r,u,i> r_ui + q * mu ) / ( N + q )   q : smoothing parameter
			double vCount = 0;
			Iterator<Int2DoubleMap.Entry> ite = vec.iterator();
			for( int j = 0 ; j < size ; j++ ){
				Int2DoubleMap.Entry entry = ite.next();
				vCount += entry.getDoubleValue();
			}
			mu_u[dataID] = ( vCount + smoothingParam * mu ) / ( size + smoothingParam );
			
			// σ^2_u = ( Σ_<r,u,i> ( r_ui - μ_u )^2 + q * sigma ) / ( N + q )   q : smoothing parameter
			// r' = ( r - μ_u ) / σ^2_u
			Int2DoubleExtOpenHashMap map = new Int2DoubleExtOpenHashMap();
			double varCount = 0;
			ite = vec.iterator();
			for( int j = 0 ; j < size ; j++ ){
				
				Int2DoubleMap.Entry entry = ite.next();
				int featureID = entry.getIntKey();
				double diff = entry.getDoubleValue() - mu_u[dataID];
				varCount += diff * diff;
				
				// for user-item index
				map.put(featureID, diff);
				// for item-user index
				Int2DoubleExtOpenHashMap reverseMap = itemuser.get(featureID);
				if( reverseMap == null ){
					reverseMap = new Int2DoubleExtOpenHashMap();
					itemuser.put(featureID, reverseMap);
				}
				reverseMap.add(dataID, diff);
				
			}
			var_u[dataID] = ( varCount + smoothingParam * sigma ) / ( size + smoothingParam );
			
			// r' make
			IntSet set = map.keySet();
			for( int id : set )
				map.product(id, 1 / var_u[dataID]);
			userItem[dataID] = VectorFactory.createSparseDotVector(map);
		}
		
		itemUser = new Int2ObjectOpenHashMap<SparseDotVector>();
		int itemSize = itemuser.size();
		Iterator<Int2ObjectMap.Entry<Int2DoubleExtOpenHashMap>> ite = itemuser.int2ObjectEntrySet().iterator();
		for( int i = 0 ; i < itemSize ; i++ ){
			Int2ObjectMap.Entry<Int2DoubleExtOpenHashMap> entry =ite.next();
			int itemID = entry.getIntKey();
			SparseDotVector vec = VectorFactory.createSparseDotVector( entry.getValue() );
			itemUser.put(itemID, vec);
		}
		
	}

	private double beta;
	
	@Override
	public void learn() {
		
		log.info("start");
		if( threadNum < 1 )
			throw new IllegalArgumentException("it can't not learn. threadNum is bigger than 1 or even 0");

		beta = 1.0d;
		n = userItem.length;
		m = DatasetAnalyzer.getMaxFeatureID(userItem) + 1;
		init();

		log.info( "k : " + k);
		log.info( "n : " + n);
		log.info( "m : " + m);
		preQValue = Double.NEGATIVE_INFINITY;
		preQDiff  = Double.POSITIVE_INFINITY;
		
		// EM Step
		int step = 0;
		while( step < maxIteration ){
			
			// update p(z|u)
			double[][] new_p_z_u = update_P_z_u();
			
			Object[] resulBox = updateGaussianParam();
			
			// update μ_yz
			double[][] new_mu_iz = (double[][])resulBox[0];

			// update σ^2_xz
			double[][] new_var_iz = (double[][])resulBox[1];

			double previousVal = preQValue;
			
			// anealing
			double object_diff = objectDiff( step, previousVal );
			if( step != 0 && object_diff < preQDiff * 0.5 ){
				beta *= eta;
				log.info("beta : " + beta);
				preQDiff = object_diff;
			}
			
			if( testConvergence && object_diff / Math.abs(previousVal) < convergenceRate )
				break;
			
			p_z_u  = new_p_z_u;
			mu_iz  = new_mu_iz;
			var_iz = new_var_iz;
			step++;

		}

	}
	
	
	/**
	 * initialize EM parameters
	 *
	 */
	private void init(){
		
		p_z_u  = new double[n][k];
		mu_iz  = new double[m][k];
		var_iz = new double[m][k];
		
		for( int userID = 0 ; userID < n ; userID++ ){
			double total = 0;
			for( int z_id = 0 ; z_id < k ; z_id++ ){
				p_z_u[userID][z_id] = Math.random();
				total += p_z_u[userID][z_id];
			}
			for( int z_id = 0 ; z_id < k ; z_id++ )
				p_z_u[userID][z_id] /= total;
		}
		
		IntSet itemIDs = DatasetHandler.getAllFeatureIDs(userItem);
		for( int itemID : itemIDs ){
			for( int z_id = 0 ; z_id < k ; z_id++ )
				var_iz[itemID][z_id] = initValue * Math.random() + minVar;
		}
		
	}
	
	
	/**
	 * calculate updated " p(z|u)  // size : k × n "
	 * @param x_id
	 * @param z_id
	 * @return
	 */
	public double[][] update_P_z_u(){

		class Calculator implements Runnable{
			
			private AtomicInteger userIndex;
			private double[][] resultBox; // n × k
			
			public Calculator( AtomicInteger userIndex, double[][] resultBox ){
				this.userIndex = userIndex;
				this.resultBox = resultBox;
			}
			
			@Override
			public void run() {
				int n = userItem.length;
				while(true){
					int userID = userIndex.getAndIncrement();
					if( n <= userID  )
						break;
					resultBox[userID] = getNew_P_z_u( userID );
				}
			}
			
			private double[] getNew_P_z_u( int userID ){

				// p( z | userID )
				double[] result = new double[k];
				
				// cache make firstly
				int z_id = 0;
				double numerator = 0;
				SparseDotVector vec = userItem[userID];
				Node[] node = vec.getNodeArray();
				int itemNum = vec.size();
				double[] sumCache = new double[itemNum];
				for( int i = 0 ; i < itemNum ; i++ ){
					int itemID = node[i].getIntKey();
					double r = node[i].getDoubleValue();
					double up = getP_r_iz( itemID, z_id, r );
					double under = 0.0;
					for( int zz_id = 0 ; zz_id < k ; zz_id++ ){
						double val = getP_r_iz( itemID, zz_id, r ) * p_z_u[userID][zz_id];
						under += Math.pow(val, beta);
					}
					sumCache[i] = under;
					numerator += Math.pow(up, beta) / under;
				}
				result[z_id] = Math.pow( p_z_u[userID][z_id], beta ) * numerator;
				
				// calculate with cache
				for( z_id = 1 ; z_id < k ; z_id++ ){
					numerator = 0;
					for( int i = 0 ; i < itemNum ; i++ ){
						int itemID = node[i].getIntKey();
						double r = node[i].getDoubleValue();
						double up = getP_r_iz( itemID, z_id, r );
						double under = sumCache[i];
						numerator += Math.pow(up, beta) / under;
					}
					result[z_id] = Math.pow( p_z_u[userID][z_id], beta ) * numerator;
				}
				PrimitiveArrayHandler.normalize(result);
				return result;
			}

		}
		
		double[][] resultBox = new double[n][k];
		Thread[] threads = new Thread[threadNum];
		AtomicInteger userIndex = new AtomicInteger();
		for( int i = 0 ; i < threadNum ; i++ ){
			threads[i] = new Thread( new Calculator( userIndex, resultBox ) );
			threads[i].start();
		}
		
		for( int i = 0 ; i < threadNum ; i++ ){
			try {
				threads[i].join();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return resultBox;
		
	}
	

	/**
	 * update gausian parameters
	 * 
	 * ・ μ_iz   : average on regular distribution
	 * ・ σ^2_iz : variance on regular distribution
	 *  ( where  0 < i < m, 0 < z < k )
	 * @return
	 */
	public Object[] updateGaussianParam(){
		
		class Calculator implements Runnable{

			private ConcurrentLinkedQueue<Integer> itemQueue;
			private double[][] muResultBox;
			private double[][] varResultBox;
			
			public Calculator( ConcurrentLinkedQueue<Integer> itemQueue, double[][] muResultBox, double[][] varResultBox ){
				this.itemQueue = itemQueue;
				this.muResultBox = muResultBox;
				this.varResultBox = varResultBox;
			}
			
			@Override
			public void run() {
				while( true ){
					Integer itemID = itemQueue.poll();
					if( itemID == null )
						break;
					Object[] resultBox = getNewGaussianParameter( itemID );
					muResultBox[itemID] = (double[])resultBox[0];
					varResultBox[itemID] = (double[])resultBox[1]; 
				}
			}
			
			private Object[] getNewGaussianParameter( int itemID ){
				SparseDotVector vec = itemUser.get(itemID);
				Node[] nodes = vec.getNodeArray();
				int userNum = vec.size();
				if( userNum == 1 )
					return oneUserExecute( nodes, itemID );
				else
					return multiUserExecute( nodes, itemID, userNum );
			}
			
			private Object[] multiUserExecute( Node[] nodes, int itemID, int userNum ){
				
				double[] result_mu  = new double[k]; // μ_z,itemID
				double[] result_var = new double[k]; // σ^2_z,itemID

				int z_id = 0;
				double up_mu  = 0.0d;
				double up_var = 0.0d;
				double under  = 0.0d;
				double[] sumCache = new double[userNum];
				
				// cache make firstly
				for( int i = 0 ; i < userNum ; i++ ){
					Node node = nodes[i];
					int userID = node.getIntKey();
					double r = node.getDoubleValue();
					double diff = Math.pow( r - mu_iz[itemID][z_id], 2 );
					double p = getP_r_iz(itemID, z_id, r) * p_z_u[userID][z_id];
					
					double sum = 0;
					for( int zz_id = 0 ; zz_id < k ; zz_id++ ){
						double val = getP_r_iz( itemID, zz_id, r ) * p_z_u[userID][zz_id];
						sum += Math.pow(val, beta);
					}
					sumCache[i] = sum;
					p = Math.pow(p, beta);
					up_mu  += r * p / sum;
					up_var += diff * p / sum;
					under  += p / sum;
				}
				result_mu[z_id] = up_mu / under;
				double var = up_var / under;
				result_var[z_id] = ( minVar < var ) ? var : minVar;
				
				
				// calculate with cache
				for( z_id = 1 ; z_id < k ; z_id++ ){
					up_mu  = 0.0d;
					up_var = 0.0d;
					under  = 0.0d;
					for( int i = 0 ; i < userNum ; i++ ){
						Node node = nodes[i];
						int userID = node.getIntKey();
						double r = node.getDoubleValue();
						double diff = Math.pow( r - mu_iz[itemID][z_id], 2 );
						double p = getP_r_iz(itemID, z_id, r) * p_z_u[userID][z_id];
						double sum = sumCache[i];
						p = Math.pow(p, beta);
						up_mu  += r * p / sum;
						up_var += diff * p / sum;
						under  += p / sum;
					}
					result_mu[z_id] = up_mu / under;
					var = up_var / under;
					result_var[z_id] = minVar < var ? var : minVar;
				
				}
				return new Object[]{result_mu, result_var};

			}
			
			private Object[] oneUserExecute( Node[] nodes, int itemID ){
				double[] result_mu  = new double[k];
				double[] result_var = new double[k];
				for( int z_id = 0 ; z_id < k ; z_id++ ){
					Node node = nodes[0];
					double r = node.getDoubleValue();
					double var = Math.pow( r - mu_iz[itemID][z_id], 2 );
					result_mu[z_id] = r;
					result_var[z_id] = ( minVar < var ) ? var : minVar;
				}
				return new Object[]{result_mu, result_var};
			}
			
		}
		
		double[][] muResultBox  = new double[m][k];
		double[][] varResultBox = new double[m][k];
		Thread[] threads = new Thread[threadNum];
		IntSet itemIDSet = itemUser.keySet();
		ConcurrentLinkedQueue<Integer> itemQueue = new ConcurrentLinkedQueue<Integer>(itemIDSet);
		for( int i = 0 ; i < threadNum ; i++ ){
			threads[i] = new Thread( new Calculator( itemQueue, muResultBox, varResultBox ) );
			threads[i].start();
		}
		
		for( int i = 0 ; i < threadNum ; i++ ){
			try {
				threads[i].join();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return new Object[]{muResultBox, varResultBox };

	}
	
	private static final double coeff = 2 * Math.PI;
	private static final double tinyProb = 0.0001;
	
	//TODO using cache better at loop-start
	private double getP_r_iz( int itemID, int z_id, double r ){
		double sigma2 = var_iz[itemID][z_id];
		double val = 1.0 / Math.sqrt( coeff * sigma2 ) * Math.exp( - 0.5 * Math.pow( r - mu_iz[itemID][z_id], 2 ) / sigma2 );
		if( val == 0 )
			return tinyProb;
		if( 1 < val )
			return 1;
		return  val;
	}
	

	private double objectDiff( int t, double previousValue ){
		preQValue = objectValue();
		log.info( t + " current objectvalue : " + preQValue );
		return preQValue - previousValue;
	}
	
	/**
	 *  calculation ObjectValue :
	 *  
	 *   ObjectValue = -Σ_<r,u,i> log { Σ_z p(r,u,i,z) }
	 *   			 = -Σ_<r,u,i> log { Σ_z p(r|i,z) * p(z|u) * p(u,i) }
	 *   			 = -Σ_<r,u,i> log {  Σ_z p(r|i,z) * p(z|u) } + const
	 * @return
	 */
	public double objectValue(){
		int n = userItem.length;
		double value = 0;
		for( int userID = 0 ;  userID < n ; userID++ ){
			SparseDotVector vec = userItem[userID];
			int size = vec.size();
			Node[] nodes = vec.getNodeArray();
			for( int i = 0 ; i < size ; i++ ){
				Node node = nodes[i];
				int itemID = node.getIntKey();
				double r = node.getDoubleValue();
				double p = 0;
				for( int z_id = 0 ; z_id < k ; z_id++ )
					p += getP_r_iz( itemID, z_id, r ) * p_z_u[userID][z_id];
				value += Math.log(p);
			}
		}
		return value;
	}
	
	public ForcedPLSAModel crateModel(){
		return new ForcedPLSAModel(mu_u, var_u, p_z_u, mu_iz );
	}
	
	@Override
	public Predictor createPredictor() {
		ForcedPLSAModel model = crateModel();
		return new ForcedPLSAPredictor(model);
	}
	
}
