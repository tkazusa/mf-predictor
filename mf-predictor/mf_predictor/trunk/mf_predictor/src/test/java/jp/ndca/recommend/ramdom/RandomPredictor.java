package jp.ndca.recommend.ramdom;

import jp.ndca.recommend.Predictor;

public class RandomPredictor implements Predictor{

	@Override
	public double predict(int userID, int itemID) {
		return ( 5 * Math.random() );
	}

}
