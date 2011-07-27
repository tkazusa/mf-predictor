package jp.ndca.recommend.mf;

import java.util.Iterator;

import jp.ndca.recommend.Learner;
import jp.ndca.recommend.common.data.DatasetHandler;
import jp.ndca.recommend.common.data.RatingData;
import jp.ndca.recommend.common.data.RatingDataset;
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
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;

public class SVDLearner implements Learner{

	private static Logger log = LoggerFactory.getLogger(SVDLearner.class);
	
	protected SparseDotVector[] r;
	protected double[][] userFactor;
	protected double[][] itemFactor;
	protected double[] bu;
	protected double[] bi;
	protected double mu;
	protected double pastObjectVal;
	
	protected static RatingDataValidations validations = new RatingDataValidations();

	static{
		validations.addSparseDotVectorCondition();
		validations.addDataSizeCondition(0, Integer.MAX_VALUE);
		validations.addValueRangeCondition(1, Integer.MAX_VALUE);
	}

	protected int k;
	protected int maxLoop;
	protected double gamma;
	protected double lambda;
	protected double initVal;
	protected double convergenceRate;
	protected boolean testConvergence;

	public int getK()			{		return k;				}
	public int getMaxLoop()	{		return maxLoop;		}
	public double getGamma()	{		return gamma;			}
	public double getLambda()	{		return lambda;			}
	public double getInitVal(){		return initVal;		}
	public double getConvergenceRate()
		{		return convergenceRate;		}
	public boolean isTestConvergence()
		{		return testConvergence;		}
	
	public void setK(int k)
	{			this.k = k;							}
	public void setMaxLoop(int maxLoop)
	{			this.maxLoop = maxLoop;				}
	public void setGamma(double gamma)
	{			this.gamma = gamma;					}
	public void setLambda(double lambda)
	{			this.lambda = lambda;				}
	public void setInitVal(double initVal)
	{			this.initVal = initVal;				}
	public void setConvergenceRate(double convergenceRate)
	{		this.convergenceRate = convergenceRate;			}
	public void setTestConvergence(boolean testConvergence)
	{		this.testConvergence = testConvergence;			}

	
	/**
	 * constructor 
	 * please cause that argument-dataset's condition change by constructor process.
	 * 
	 * @param dataset
	 */
	public SVDLearner( SVDConf conf ){
		this( null, conf );
	}
	public SVDLearner( RatingDataset dataset, SVDConf conf ){
		int k = conf.getK();
		if( k <= 0 )
			throw new IllegalArgumentException("k need to be non-negative ");
		this.k = k;
		
		double gamma = conf.getGamma();
		if( gamma <= 0 )
			throw new IllegalArgumentException("gamma need to be non-negative ");
		this.gamma = gamma;
		
		double lambda = conf.getLambda();
		if( lambda <= 0 )
			throw new IllegalArgumentException("lambda need to be non-negative ");
		this.lambda = lambda;
		
		int maxLoop = conf.getMaxLoop();
		if( maxLoop <= 0 )
			throw new IllegalArgumentException("maxLoop need to be non-negative ");
		this.maxLoop = maxLoop;
		
		double initValue = conf.getInitVal();
		if( initValue < 0 )
			throw new IllegalArgumentException("initValue need to be non-negative ");
		if( initValue == 0 )
			log.info("initValue is 0. So this is not latent analyzed learning recommender !");
		this.initVal = initValue;
		
		if( dataset != null )
			setRatingDataset(dataset);
	}
	
	
	@Override
	public void setRatingDataset( RatingDataset dataset ) {
		DatasetValidator.check( dataset, validations );
		int userNum	= dataset.size();
		r = new SparseDotVector[userNum];
		mu = DatasetHandler.aveFeatureValues(dataset);
		log.info( "rating average : " + mu );
		// initialization r
		for( int userID = 0 ; userID < userNum ; userID++ ){
			RatingData data = dataset.fastGet(userID);
			Vector vector = data.getVector();
			Int2DoubleMap map = new Int2DoubleOpenHashMap();
			Iterator<Int2DoubleMap.Entry> ite = vector.iterator();
			int size = vector.size();
			for(int j = 0 ; j < size ; j++ ){
				Int2DoubleMap.Entry entry = ite.next();
				int itemID = entry.getIntKey();
				double rating = entry.getDoubleValue();
				double r = rating - mu;
				map.put(itemID, r);
			}
			r[userID] = VectorFactory.createSparseDotVector(map);
		}
	}

	
	private void initParameter( IntSet itemIDSet ){
		for( int i = 0 ; i < k ; i++ ){
			for( int userID = 0 ; userID < r.length ; userID++ )
				userFactor[userID][i] = initVal * Math.random();
			for( int itemID : itemIDSet )
				itemFactor[i][itemID] = initVal * Math.random();
		}
	}
	
	
	@Override
	public void learn() {
		
		int userNum	= r.length;
		int itemNum	= DatasetHandler.getMaxFeatureID(r)+1;
		userFactor = new double[userNum][k];
		itemFactor = new double[k][itemNum];
		bu = new double[userNum];
		bi = new double[itemNum];
		pastObjectVal = Double.POSITIVE_INFINITY;
		
		IntSet itemIDSet = DatasetHandler.getAllFeatureIDs(r);
		initParameter( itemIDSet );
		log.info( "userNum : " + userNum + ", itemNum : " + itemIDSet.size() + ", maxItemID : " + itemNum );

		for( int t = 0 ; t < maxLoop ; t++ ){
			for( int userID = 0 ; userID < userNum ; userID++ ){
				SparseDotVector ratingVector = r[userID];
				int size = ratingVector.size();
				Node[] nodes = ratingVector.getNodeArray();
				for( int position = 0 ; position < size ; position++ ){
					Node node = nodes[position];
					int itemID = node.getIntKey();
					double rate = node.getDoubleValue(); // r - mu
					
					// estimate error 
					double error_ui = def( userID, itemID, rate );
					
					// cache1
					double gamma_error_ui = gamma * error_ui;
					double gamma_lambda = gamma * lambda;
					
					// updates
					bu[userID] += gamma_error_ui - gamma_lambda * bu[userID];
					bi[itemID] += gamma_error_ui - gamma_lambda * bi[itemID];
					
					for( int s = 0 ; s < k ; s++ ){
						double userID_s = userFactor[userID][s];
						double s_itemID = itemFactor[s][itemID];
						userFactor[userID][s] += gamma_error_ui * s_itemID - gamma_lambda * userID_s;
						itemFactor[s][itemID] += gamma_error_ui * userID_s - gamma_lambda * s_itemID;
					}
				}
			}
			
			if( testConvergence && isConverged() ){
				log.info("success convergence. end loop time : " + t );
				break;
			}
			
		}
		
	}

	
	/**
	 * calculate difference between real item rate and expected one, as follow
	 * 
	 * r_ui - μ - r'_ui = b_i + b_u + Σ_k U[u][k]I[k][i]
	 * 
	 * @param userID
	 * @param itemID
	 * @param rate
	 * @return
	 */
	private double def( int userID,  int itemID, double rate ){
		double score = bu[userID] + bi[itemID];
		for( int i = 0 ; i < k ; i++ )
			score += userFactor[userID][i] * itemFactor[i][itemID];
		return rate - score;
	}
	
	
	private boolean isConverged(){
		
		// calculate objectValue
		double lossValue = 0;
		for( int userID = 0 ; userID < r.length ; userID++ ){
			SparseDotVector ratingVector = r[userID];
			int size = ratingVector.size();
			Node[] nodes = ratingVector.getNodeArray();
			for( int position = 0 ; position < size ; position++ ){
				Node node = nodes[position];
				int itemID = node.getIntKey();
				double rate = node.getDoubleValue();
				double value = def( userID, itemID, rate );
				lossValue += Math.pow( value , 2);
			}
		}

		double regularVal = PrimitiveArrayHandler.square(bi) + PrimitiveArrayHandler.square(bu);
		for( int i = 0 ; i < k ; i++ ){
			for( int userID = 0; userID < userFactor.length ; userID++ )
				regularVal += Math.pow(userFactor[userID][i], 2);
			for( int itemID = 0; itemID < itemFactor[0].length ; itemID++ )
				regularVal += Math.pow(itemFactor[i][itemID], 2);
		}
		
		double objectVal = lossValue + lambda * regularVal;
		log.info( "current : " + objectVal + ", " + "past : " + pastObjectVal );
		double changeRate = ( pastObjectVal - objectVal ) / pastObjectVal;
		if( changeRate  < convergenceRate )
			return true;
		pastObjectVal = objectVal;
		return false;
	}
	
	
	public SVDModel createSVDModel(){
		SVDModel model = new SVDModel();
		model.setMu(mu);
		model.setBi(bi);
		model.setBu(bu);
		model.setItemFactor(itemFactor);
		model.setUserFactor(userFactor);
		return model;
	}

	
	@Override
	public SVDPredictor createPredictor() {
		SVDModel model = createSVDModel();
		return new SVDPredictor(model);
	}

}
