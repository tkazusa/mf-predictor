package jp.ndca.recommend.mf;

import java.io.IOException;

import jp.ndca.recommend.common.data.RatingDataset;
import jp.ndca.recommend.common.util.Evaluator;
import jp.ndca.test.MovieLensDataHandler;

import org.junit.Test;

public class SVDPlusPredictorTest {
	
	@Test
	public void cvTest() throws NumberFormatException, IOException{

		RatingDataset[] trainingDataset = MovieLensDataHandler.get5FolodTrainingData();
		RatingDataset[] testDataset = MovieLensDataHandler.get5FolodTestData();

		SVDConf conf = new SVDConf();
		conf.setK(100);
		conf.setGamma ( 0.027 );
		conf.setLambda( 0.08 );
		conf.setLambda2(0.06 );
		
		conf.setTestConvergence(false);
		conf.setMaxLoop(60);

		System.out.println("learn start!");
		long start = System.currentTimeMillis();
		Evaluator.cvTest(trainingDataset, testDataset, new SVDPlusLearner(conf) );
		long end = System.currentTimeMillis();
		System.out.println( (end - start) + " ms");
		
		// MAE  : 0.7109803625861721
		// RSME : 0.9029319172457528
		// time : 1724436 ms
	}

}
