package jp.ndca.recommend.common.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.ndca.recommend.common.data.RatingArrayDataset;
import jp.ndca.recommend.common.data.RatingData;
import jp.ndca.recommend.common.data.RatingDataset;
import jp.ndca.recommend.common.structure.vector.Vector;
import jp.ndca.recommend.common.structure.vector.VectorFactory;

import it.unimi.dsi.fastutil.ints.AbstractInt2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class RatingDatasetMaker {

	private Int2ObjectMap<List<Int2DoubleMap.Entry>> data;
	
	public RatingDatasetMaker(){
		Int2ObjectMap<List<Int2DoubleMap.Entry>> _tmp = new Int2ObjectOpenHashMap<List<Int2DoubleMap.Entry>>();
		_tmp.defaultReturnValue(null);
		this.data = _tmp;
	}		
	
	
	public void add( int userID,int itemID, double rating){
		List<Int2DoubleMap.Entry> userRating = data.get(userID);
		if( userRating == null ){
			userRating = new ArrayList<Int2DoubleMap.Entry>();
			data.put( userID, userRating );
		}
		userRating.add( new AbstractInt2DoubleMap.BasicEntry(itemID, rating) );
	}
	
	
	public void refresh(){
		this.data = new Int2ObjectOpenHashMap<List<Int2DoubleMap.Entry>>();
	}

	
	public RatingDataset create(){
		RatingArrayDataset dataset = new RatingArrayDataset();
		Int2ObjectMap.FastEntrySet<List<Int2DoubleMap.Entry>> set = (Int2ObjectMap.FastEntrySet<List<Int2DoubleMap.Entry>>)data.int2ObjectEntrySet();
		int size = set.size();
		Iterator<Int2ObjectMap.Entry<List<Int2DoubleMap.Entry>>> ite = set.fastIterator();
		for( int i = 0 ; i < size ; i++ ){
			Int2ObjectMap.Entry<List<Int2DoubleMap.Entry>> entry = ite.next();
			int userID = entry.getIntKey();
			List<Int2DoubleMap.Entry> ratings = entry.getValue(); // for low memory cost
			Int2DoubleMap map = new Int2DoubleOpenHashMap();
			for( Int2DoubleMap.Entry rating : ratings )
				map.put( rating.getIntKey(), rating.getDoubleValue() );
			Vector vector = VectorFactory.createSparseDotVector(map);
			dataset.add( new RatingData(userID, vector) );
		}
		dataset.sort();
		return dataset;
	}
	
}
