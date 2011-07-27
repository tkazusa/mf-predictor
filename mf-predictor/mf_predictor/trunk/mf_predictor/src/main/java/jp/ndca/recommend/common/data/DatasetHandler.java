package jp.ndca.recommend.common.data;

import java.util.Iterator;

import jp.ndca.recommend.common.structure.vector.Vector;
import jp.ndca.recommend.common.structure.vector.VectorUtil;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class DatasetHandler {

	/**
	 * average all feature values 
	 * @param dataset
	 * @return
	 */
	public static double aveFeatureValues( RatingDataset dataset ){
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
	
	
	/**
	 * normalize
	 * @param dataset
	 * @return
	 */
	public static RatingArrayDataset normalize( RatingDataset dataset ){
		RatingArrayDataset newDataset = new RatingArrayDataset();
		for( RatingData data : dataset )
			newDataset.add( new RatingData( data.getDataID(), VectorUtil.normalize( data.getVector() ) ) );
		newDataset.sort();
		return newDataset;
	}
	
	
	/**
	 * get all data IDs in the dataset.
	 * @param dataset
	 * @return
	 */
	public static IntSet getAllDataIDs( RatingDataset dataset ) {
		IntSet set = new IntOpenHashSet();
		for( RatingData RatingData : dataset )
			set.add( RatingData.getDataID() );
		return set;
	}
	
	
	/**
	 * get all vector's feature IDs in the dataset.
	 * @param dataset
	 * @return
	 */
	public static IntSet getAllFeatureIDs( RatingDataset dataset ) {
		IntSet set = new IntOpenHashSet();
		for( RatingData data : dataset ){
			Vector vector = data.getVector();
			for( int id : vector.keys() )
				set.add( id );
		}
		return set;
	}

	public static <K extends Vector> IntSet getAllFeatureIDs( K[] vectors ) {
		IntSet set = new IntOpenHashSet();
		for( Vector vector : vectors ){
			for( int id : vector.keys() )
				set.add( id );
		}
		return set;
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
		for( RatingData RatingData : dataset ){
			Vector vector = RatingData.getVector();
			double maxValue = vector.getMaxValue();
			if( datasetMaxValue < maxValue )
				datasetMaxValue = maxValue;
		}
		return datasetMaxValue;
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
