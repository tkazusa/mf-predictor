package jp.ndca.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import jp.ndca.recommend.common.data.RatingDataset;
import jp.ndca.recommend.common.util.RatingDatasetMaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MovieLensDataHandler {

	private static Logger log = LoggerFactory.getLogger(MovieLensDataHandler.class);
	private static String encode = "utf-8";
	
	public static RatingDataset[] get5FolodTrainingData() throws IOException{
		int foldNum = 5;
		RatingDataset[] trainingDataset = new RatingDataset[foldNum];
		RatingDatasetMaker datasetMaker = new RatingDatasetMaker();
		for( int i = 0 ; i < foldNum ; i++ ){
			String learningPath = "u" + (i+1) + ".base";
			log.info(learningPath);
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream( learningPath );
			BufferedReader br = new BufferedReader ( new InputStreamReader(is, encode) );
			while( br.ready() ){
				String line = br.readLine();
				String[] splits = line.split("\t");
				if( splits.length != 4 )
					throw new IllegalArgumentException("data format error");
				int userID = Integer.parseInt( splits[0] ) - 1;
				int itemID = Integer.parseInt( splits[1] );
				int rating = Integer.parseInt( splits[2] );
				datasetMaker.add(userID, itemID, rating);
			}
			br.close();
	
			trainingDataset[i] = datasetMaker.create();
			datasetMaker.refresh();
		}
		return trainingDataset;
	}
	
	public static RatingDataset[] get5FolodTestData() throws IOException{
		int foldNum = 5;
		RatingDataset[] trainingDataset = new RatingDataset[foldNum];
		RatingDatasetMaker datasetMaker = new RatingDatasetMaker();
		for( int i = 0 ; i < foldNum ; i++ ){
			String learningPath = "u" + (i+1) + ".test";
			log.info(learningPath);
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream( learningPath );
			BufferedReader br = new BufferedReader ( new InputStreamReader(is, encode) );
			while( br.ready() ){
				String line = br.readLine();
				String[] splits = line.split("\t");
				if( splits.length != 4 )
					throw new IllegalArgumentException("data format error");
				int userID = Integer.parseInt( splits[0] ) - 1;
				int itemID = Integer.parseInt( splits[1] );
				int rating = Integer.parseInt( splits[2] );
				datasetMaker.add(userID, itemID, rating);
			}
			br.close();
	
			trainingDataset[i] = datasetMaker.create();
			datasetMaker.refresh();
		}
		return trainingDataset;
	}

	
}
