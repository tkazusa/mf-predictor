package jp.ndca.recommend.ramdom;

import java.io.IOException;

import jp.ndca.recommend.common.data.RatingDataset;
import jp.ndca.recommend.common.util.Evaluator;
import jp.ndca.test.MovieLensDataHandler;

import org.junit.Test;

public class RandomPredictorTest {

	@Test
	public void cvTest() throws NumberFormatException, IOException{

		RatingDataset[] trainingDataset = MovieLensDataHandler.get5FolodTrainingData();
		RatingDataset[] testDataset = MovieLensDataHandler.get5FolodTestData();

		System.out.println("learn start!");
		long start = System.currentTimeMillis();
		Evaluator.cvTest(trainingDataset, testDataset, new RandomLearner() );
		long end = System.currentTimeMillis();
		System.out.println( (end - start) + " ms");
		
		// MAE  : 1.7175747820236886
		// RSME : 2.101887646263455
		// 56 ms
		
	}
	
}
