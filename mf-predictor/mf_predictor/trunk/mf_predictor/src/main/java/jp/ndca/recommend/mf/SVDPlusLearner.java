package jp.ndca.recommend.mf;

import jp.ndca.recommend.common.data.DatasetHandler;
import jp.ndca.recommend.common.data.RatingDataset;
import jp.ndca.recommend.common.structure.vector.SparseDotVector;
import jp.ndca.recommend.common.structure.vector.SparseDotVector.Node;
import jp.ndca.recommend.common.util.PrimitiveArrayHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unimi.dsi.fastutil.ints.IntSet;

public class SVDPlusLearner extends SVDLearner {

	private static Logger log = LoggerFactory.getLogger(SVDPlusLearner.class);
	private static double DEFAULT_LAMBDA2 = 0.001;
	
	protected double[][] y;
	protected double[] rR_u;
	
	private double lambda2 = DEFAULT_LAMBDA2;
	public double getLambda2() {
		return lambda2;
	}
	public void setLambda2(double lambda2) {
		this.lambda2 = lambda2;
	}

	
	/**
	 * constractors
	 */
	public SVDPlusLearner(SVDConf conf) {
		this(null, conf);
	}
	public SVDPlusLearner( RatingDataset dataset, SVDConf conf ){
		super( conf );
		double lambda2 = conf.getLambda2();
		if( lambda2 <= 0 )
			throw new IllegalArgumentException("lambda2 need to be non-negative ");
		if( dataset != null )
			setRatingDataset(dataset);
	}

	
	@Override
	public void setRatingDataset( RatingDataset dataset ){
		super.setRatingDataset(dataset);
		rR_u = new double[dataset.size()];
		for( int i = 0 ; i < r.length ; i++ )
			rR_u[i] = 1 / Math.sqrt( r[i].size() );
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
		y = new double[k][itemNum];
		bu = new double[userNum];
		bi = new double[itemNum];
		pastObjectVal = Double.POSITIVE_INFINITY;
		
		IntSet itemIDSet = DatasetHandler.getAllFeatureIDs(r);
		initParameter( itemIDSet );
		log.info( "userNum : " + userNum + ", itemNum : " + itemIDSet.size() + ", maxItemID : " + itemNum );
		
		double gamma = this.gamma;
		
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
					
					// updates
					bu[userID] += gamma * ( error_ui - lambda * bu[userID] );
					bi[itemID] += gamma * ( error_ui - lambda * bi[itemID] );
					
					for( int s = 0 ; s < k ; s++ ){
						
						double userID_s = userFactor[userID][s];
						double s_itemID = itemFactor[s][itemID];
						
						// update userFactor
						userFactor[userID][s] += gamma * ( error_ui * s_itemID - lambda2 * userID_s );
						
						// update itemFactor and item implicit feedback
						double userContext = 0.0d;
						for( int pos = 0 ; pos < size ; pos++ ){
							int item_j = nodes[pos].getIntKey();
							userContext += y[s][item_j];
							// here is item implicit feedback
							y[s][item_j] += gamma * ( error_ui * s_itemID * rR_u[userID] - lambda2 * y[s][item_j] );
						}
						userContext = userID_s + userContext * rR_u[userID] ;
						itemFactor[s][itemID] += gamma * ( error_ui * userContext - lambda2 * s_itemID );
						
					}
				}
			}

			if( testConvergence && isConverged() ){
				log.info("success convergence. end loop time : " + t );
				break;
			}
			gamma *= 0.9;

		}
	}
	

	/**
	 * calculate difference between real item rate and expect one,  by follow
	 * 
	 * r_ui - r'_ui - μ = b_i + b_u + Σ_k I[k][i]* ( U[u][k] + |R(u)|^-0.5 * Σ_j∈R(u) y[j][k] )
	 * 
	 * @param userID
	 * @param itemID
	 * @param rate
	 * @return
	 */
	private double def( int userID,  int itemID, double rate ){
		double score = bu[userID] + bi[itemID];
		SparseDotVector r_u = r[userID];
		int size = r_u.size();
		Node[] nodes = r_u.getNodeArray();
		for( int s = 0 ; s < k ; s++ ){
			double user_s = userFactor[userID][s];
			double sum = 0;
			for( int i = 0 ; i < size ; i++ ){
				int item_j = nodes[i].getIntKey();
				sum += y[s][item_j];
			}
			user_s += sum * rR_u[userID];
			score += user_s * itemFactor[s][itemID];
		}
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

		double regularVal1 = PrimitiveArrayHandler.square(bi) + PrimitiveArrayHandler.square(bu);
		double regularVal2 = 0.0d;
		for( int i = 0 ; i < k ; i++ ){
			for( int userID = 0; userID < userFactor.length ; userID++ )
				regularVal2 += Math.pow(userFactor[userID][i], 2);
			for( int itemID = 0; itemID < itemFactor[0].length ; itemID++ ){
				regularVal2 += Math.pow(itemFactor[i][itemID], 2);
				regularVal2 += Math.pow(y[i][itemID], 2);
			}
		}
		
		double objectVal = lossValue + lambda * regularVal1 + lambda2 * regularVal2;
		log.info( "current : " + objectVal + ", " + "past : " + pastObjectVal );
		log.info( "  - lossValue : " + lossValue + ", " + "regularVal1 : " + regularVal1 + ", " + "regularVal2 : " + regularVal2 );
		double changeRate = ( pastObjectVal - objectVal ) / pastObjectVal;
		if( changeRate  < convergenceRate )
			return true;
		pastObjectVal = objectVal;
		return false;
	}
	
	
	@Override
	public SVDModel createSVDModel(){
		SVDModel model = new SVDModel();
		model.setMu(mu);
		model.setBi(bi);
		model.setBu(bu);
		model.setItemFactor(itemFactor);
		// p_u + Σ_j∈R(u) ( y[s][j] ) / |R(u)|^-0.5
		for( int userID = 0 ; userID < r.length ; userID++ ){
			SparseDotVector r_u = r[userID];
			int size = r_u.size();
			Node[] nodes = r_u.getNodeArray();
			for( int s = 0 ; s < k ; s++ ){
				double sum = 0;
				for( int pos = 0 ; pos < size ; pos++ ){
					int item_j = nodes[pos].getIntKey();
					sum += y[s][item_j];
				}
				userFactor[userID][s] += sum * rR_u[userID];
			}
		}
		model.setUserFactor(userFactor);
		return model;
	}

}
