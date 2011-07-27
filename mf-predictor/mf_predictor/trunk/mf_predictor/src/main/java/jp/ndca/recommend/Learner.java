package jp.ndca.recommend;

import jp.ndca.recommend.common.data.RatingDataset;

public interface Learner {

	public void learn();
	
	public void setRatingDataset( RatingDataset dataset );
	
	public Predictor createPredictor();
	
}
