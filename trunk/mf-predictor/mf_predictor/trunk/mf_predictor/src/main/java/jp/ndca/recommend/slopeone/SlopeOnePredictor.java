package jp.ndca.recommend.slopeone;

import jp.ndca.recommend.Predictor;
import jp.ndca.recommend.common.structure.vector.Vector;

public class SlopeOnePredictor implements Predictor{

	private double[][] itemMeanDeviation;
	private Vector[] dataset;

	public SlopeOnePredictor( SlopeOneModel model ){
		itemMeanDeviation = model.getItemMeanDeviation();
		dataset = model.getDataset();
	}

	
	@Override
	public double predict(int userID, int itemID ) {
		if( userID < dataset.length ){
			if( itemID < itemMeanDeviation.length ){
				Vector vector = dataset[userID];
				int scoringNum = vector.size();
				double result = 0.0d;
				for( int key : vector.keys() ){
					int refItemID = key;
					double score = vector.get(key);
					result += itemMeanDeviation[itemID][refItemID] + score;
				}
				double expectation = result / (double)scoringNum;
				return expectation;
			}
			else{
				Vector vector = dataset[userID];
				int scoringNum = vector.size();
				double result = 0.0d;
				for( int key : vector.keys() ){
					double score = vector.get(key);
					result += score;
				}
				double expectation = result / (double)scoringNum;
				return expectation;
			}
		}
		else
			return -1;
	}

}