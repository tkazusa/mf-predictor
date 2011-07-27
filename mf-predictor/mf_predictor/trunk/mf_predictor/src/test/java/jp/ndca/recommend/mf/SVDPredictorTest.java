package jp.ndca.recommend.mf;

import java.io.IOException;

import jp.ndca.recommend.common.data.RatingDataset;
import jp.ndca.recommend.common.util.Evaluator;
import jp.ndca.test.MovieLensDataHandler;

import org.junit.Test;

public class SVDPredictorTest {

	@Test
	public void cvTest() throws NumberFormatException, IOException{

		RatingDataset[] trainingDataset = MovieLensDataHandler.get5FolodTrainingData();
		RatingDataset[] testDataset = MovieLensDataHandler.get5FolodTestData();

		SVDConf conf = new SVDConf();
		conf.setK(100);
		conf.setGamma(0.0065);
		conf.setLambda(0.080);
		conf.setTestConvergence(false);
		conf.setMaxLoop(60);

		System.out.println("learn start!");
		long start = System.currentTimeMillis();
		Evaluator.cvTest(trainingDataset, testDataset, new SVDLearner(conf) );
		long end = System.currentTimeMillis();
		System.out.println( (end - start) + " ms");
		
		// MAE  : 0.7141107454308787
		// RSME : 0.9057304891818632	
		// 13187 ms
		
	}

}
