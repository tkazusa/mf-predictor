package jp.ndca.recommend.common.map;

import java.util.Iterator;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;

public class Int2DoubleExtOpenHashMap extends Int2DoubleOpenHashMap{

	private static final long serialVersionUID = -8075347710007751664L;

	public Int2DoubleExtOpenHashMap(int expected, float f){
		super( expected, f );
	}
	public Int2DoubleExtOpenHashMap(){
		super();
	}

	@Override
	public double add( int k, double v ){
		int pos;
        for( pos = HashCommon.murmurHash3(k) & mask; used[pos]; pos = pos + 1 & mask )
			if( k == key[pos] ){
				value[pos] += v;
				return value[pos];
			}
		used[pos] = true;
		key[pos] = k;
		value[pos] = v; // original is : defRetValue + v
		if(++size >= maxFill)
			rehash( HashCommon.arraySize( size+1, f ) );
		return value[pos];
	}

	public double product( int k, double v ){
		int pos;
        for(pos = HashCommon.murmurHash3(k) & mask; used[pos]; pos = pos + 1 & mask)
			if( k == key[pos] ){
				value[pos] *= v;
				return value[pos];
			}
		return 0.0d;
	}

	public double totalValue(){
		double resultValue = 0.0d;
		Iterator<Int2DoubleMap.Entry> ite = int2DoubleEntrySet().fastIterator();
		for(int i=0 ; i < size ; i++ ){
			Int2DoubleMap.Entry entry = ite.next();
			resultValue += entry.getDoubleValue();
		}
		return resultValue;
	}

}
