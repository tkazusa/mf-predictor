package jp.ndca.recommend.ramdom;

import jp.ndca.recommend.Learner;
import jp.ndca.recommend.Predictor;
import jp.ndca.recommend.common.data.RatingDataset;

public class RandomLearner implements Learner{

	@Override
	public void learn() {
	}

	@Override
	public void setRatingDataset(RatingDataset dataset) {
	}

	@Override
	public Predictor createPredictor() {
		return new RandomPredictor();
	}

}
