package jp.ndca.recommend.common.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * basic dataset by array structure
 * 
 * @author hattori_tsukasa
 *
 */
public class RatingArrayDataset implements RatingDataset{

	private static final long serialVersionUID = -700158976178954689L;

	public boolean isDataSorted = true;
	
	private List<RatingData> dataList;
	public List<RatingData> getDataList() {
		return dataList;
	}
	public void setDataList(List<RatingData> dataList) {
		Collections.sort(dataList);
		isDataSorted = true;
		this.dataList = dataList;
	}
	
	/**
	 * constractor
	 */
	public RatingArrayDataset(){
		this.dataList = new ArrayList<RatingData>();
	}
	public RatingArrayDataset( List<RatingData> dataList ){
		Collections.sort(dataList);
		this.dataList = dataList;
	}


	/**
	 * add data At tail end of "dataList".
	 */
	@Override
	public void add( RatingData data ){
		dataList.add(data);
		isDataSorted = false;
	}
	
	@Override
	public RatingData fastGet( int dataID ){
		return dataList.get(dataID);
	}
	
	/**
	 * get RatingData with dataID </br>
	 * 
	 * @param dataID
	 * @return
	 */
	@Override
	public RatingData get( int dataID ){
		if(!isDataSorted)
			sort();
		int min = 0;
		int max = dataList.size() - 1;
		while( min <= max ){
			int middle = ( max + min ) / 2;
			RatingData data = dataList.get(middle);
			int middleIndex = data.getDataID();
			if( middleIndex < dataID )
				min = ++middle;
			else if( dataID < middleIndex )
				max = --middle;
			else
				return data;
		}
		return null;
	}
	
	public void set( int i, RatingData data ){
		dataList.set(i, data);
		isDataSorted = false;
	}
	
	public void sort(){	
		if(!isDataSorted)
			Collections.sort(dataList);
	}
	
	public void sort( Comparator<RatingData> comp){
		Collections.sort(dataList, comp);
		isDataSorted = false;
	}
	
	public void shuffle(){
		Collections.shuffle(dataList);
		isDataSorted = false;
	}
	
	@Override
	public int size(){
		return dataList.size();
	}

	@Override
	public RatingDataset clone() throws CloneNotSupportedException{
		RatingArrayDataset dataset = new RatingArrayDataset();
		for( int i = 0 ; i < dataList.size() ; i++ ){
			RatingData data = fastGet(i);
			dataset.add( new RatingData(data.getDataID(), data.getVector().clone() ) );
		}
		dataset.sort();
		return dataset;
	}
	
	@Override
	public Iterator<RatingData> iterator() {
		return dataList.iterator();
	}
	
	@Override
	public void trim() {
		for( RatingData data : dataList)
			data.getVector().trim();
	}
	
}
