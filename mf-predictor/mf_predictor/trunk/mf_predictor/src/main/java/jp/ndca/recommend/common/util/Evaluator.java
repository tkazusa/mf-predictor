package jp.ndca.recommend.common.util;

import jp.ndca.recommend.Learner;
import jp.ndca.recommend.Predictor;
import jp.ndca.recommend.common.data.RatingData;
import jp.ndca.recommend.common.data.RatingDataset;
import jp.ndca.recommend.common.structure.vector.Vector;

public class Evaluator{

	/**
	 * cross valudation test
	 * @param trainingDataset
	 * @param testDataset
	 * @param learner
	 */
	public static void cvTest( RatingDataset[] trainingDataset, RatingDataset[] testDataset, Learner learner ){
		if( trainingDataset.length != testDataset.length )
			throw new IllegalArgumentException("test and training need to be the same");
		double mae = 0.0d;
		double rsme = 0.0d;
		for( int i = 0 ; i < trainingDataset.length ; i++ ){
			learner.setRatingDataset( trainingDataset[i] );
			learner.learn();
			double[] evaluations = MAE_RSME(testDataset[i], learner.createPredictor());
			mae += evaluations[0];
			rsme += evaluations[1];
		}
		int size = trainingDataset.length;
		mae /= size;
		rsme /= size;
		System.out.println( "MAE : " + mae );
		System.out.println( "RSME : " + rsme );
	}
	

	private static double[] MAE_RSME( RatingDataset testDataset, Predictor predictor ){
		int totalTestEvalNum = 0;
		double mae = 0;
		double rsme = 0;
		for(  RatingData testData : testDataset ){
			int userID = testData.getDataID();
			// real rating data
			Vector testRates = testData.getVector();
			totalTestEvalNum += testRates.size();
			//calc MAE amd RSME
			for( int itemID : testRates.keySet() ){
				double answerRate = testRates.get(itemID);
				double score = predictor.predict( userID, itemID );
				if( score == -1 )
					throw new IllegalArgumentException("testData has the dataID which there is not in the training data \n userID : " + userID + ", itemID : " + itemID);
				mae += Math.abs( answerRate - score );
				rsme += Math.pow( Math.abs( answerRate - score ), 2 );
			}
		}
		double[] evaluations = new double[2];
		evaluations[0] = mae / totalTestEvalNum;
		evaluations[1] = Math.sqrt( rsme / totalTestEvalNum );
		return evaluations;
	}
	
}
