package jp.ndca.recommend.slopeone;

import java.io.IOException;

import jp.ndca.recommend.common.data.RatingDataset;
import jp.ndca.recommend.common.util.Evaluator;
import jp.ndca.test.MovieLensDataHandler;

import org.junit.Test;

public class SlopeOnePredictorTest {

	@Test
	public void cvTest() throws NumberFormatException, IOException{

		RatingDataset[] trainingDataset = MovieLensDataHandler.get5FolodTrainingData();
		RatingDataset[] testDataset = MovieLensDataHandler.get5FolodTestData();
		
		System.out.println("learn start!");
		long start = System.currentTimeMillis();
		Evaluator.cvTest(trainingDataset, testDataset, new SlopeOneLearner() );
		long end = System.currentTimeMillis();
		System.out.println( (end - start) + " ms");
		
		// MAE  : 0.7424379689586948
		// RSME : 0.9426361811518429		
		// 6843 ms
		
	}

}