package jp.ndca.recommend.common.data;

import java.util.Iterator;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

import jp.ndca.recommend.common.structure.vector.Vector;


public class DatasetAnalyzer {

	
	/**
	 * average all feature values 
	 * @param dataset
	 * @return
	 */
	public static double aveValue( RatingDataset dataset ){
		double ave = 0;
		int featureNum = 0;
		for( RatingData data : dataset ){
			Vector vector = data.getVector();
			Iterator<Int2DoubleMap.Entry> ite = vector.iterator();
			int size = vector.size();
			featureNum += size;
			for( int i = 0 ; i < size ; i++ ){
				Int2DoubleMap.Entry entry = ite.next();
				ave += entry.getDoubleValue();
			}
		}
		return ave / (double)featureNum;
	}
	
	public static <K extends Vector> double aveValue( K[] dataset ){
		double ave = 0;
		int featureNum = 0;
		for( Vector vector : dataset ){
			Iterator<Int2DoubleMap.Entry> ite = vector.iterator();
			int size = vector.size();
			featureNum += size;
			for( int i = 0 ; i < size ; i++ ){
				Int2DoubleMap.Entry entry = ite.next();
				ave += entry.getDoubleValue();
			}
		}
		return ave / (double)featureNum;
	}
	
	public static double varValue( RatingDataset dataset ){
		double ave = aveValue(dataset);
		return varValue( dataset, ave );
	}
	
	public static double varValue( RatingDataset dataset, double ave ){
		double var = 0;
		int featureNum = 0;
		for( RatingData data : dataset ){
			Vector vector = data.getVector();
			Iterator<Int2DoubleMap.Entry> ite = vector.iterator();
			int size = vector.size();
			featureNum += size;
			for( int i = 0 ; i < size ; i++ ){
				Int2DoubleMap.Entry entry = ite.next();
				double diff = ave - entry.getDoubleValue();
				var += diff * diff;
			}
		}
		return var / (double)featureNum;
	}
	
	
	/**
	 * get MaxID in the dataset
	 * @param dataset
	 * @return
	 */
	public static int getMaxFeatureID( RatingDataset dataset ) {
		int datasetMaxID = Integer.MIN_VALUE;
		for( RatingData data : dataset ){
			Vector vector = data.getVector();
			int maxID = vector.getMaxID();
			if( datasetMaxID < maxID )
				datasetMaxID = maxID;
		}
		return datasetMaxID;
	}
	
	public static <K extends Vector> int getMaxFeatureID( Iterable<K> dataset ){
		int maxIndex = 0;
		for( Vector vector : dataset){
			if( vector == null )
				continue;
			int index = vector.getMaxID();
			if( maxIndex < index )
				maxIndex = index;
		}
		return maxIndex;
	}
	
	public static <K extends Vector> int getMaxFeatureID( K[] dataset ){
		int maxIndex = 0;
		for( Vector vector : dataset){
			if( vector == null )
				continue;
			int index = vector.getMaxID();
			if( maxIndex < index )
				maxIndex = index;
		}
		return maxIndex;
	}


	/**
	 * get MaxValue
	 * @param dataset
	 * @return
	 */
	public static double getMaxValue( RatingDataset dataset ) {
		double datasetMaxValue = Double.NEGATIVE_INFINITY;
		for( RatingData basicData : dataset ){
			Vector vector = basicData.getVector();
			double maxValue = vector.getMaxValue();
			if( datasetMaxValue < maxValue )
				datasetMaxValue = maxValue;
		}
		return datasetMaxValue;
	}

	public static <K extends Vector> double getMaxValue( Iterable<K> dataset ){
		double maxValue = 0;
		for( Vector vector : dataset){
			if( vector == null )
				continue;
			double value = vector.getMaxValue();
			if( maxValue < value )
				maxValue = value;
		}
		return maxValue;
	}

	public static <K extends Vector> double getMaxValue( K[] dataset ){
		double maxValue = 0;
		for( Vector vector : dataset){
			if( vector == null )
				continue;
			double value = vector.getMaxValue();
			if( maxValue < value )
				maxValue = value;
		}
		return maxValue;
	}

}
