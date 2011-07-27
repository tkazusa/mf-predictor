package jp.ndca.recommend.common.structure.vector;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.Map;

import jp.ndca.recommend.common.structure.vector.SparseDotVector.Node;

public class VectorFactory {
	
	// ### 
	// ### create SparseDotVector
	// ###
	public static SparseDotVector createSparseDotVector( Int2DoubleMap data ){
		int size = data.size();
		Node[] nodes = new Node[ size ];
		Int2DoubleMap.FastEntrySet entrySet = (Int2DoubleMap.FastEntrySet)data.int2DoubleEntrySet();
		ObjectIterator<Int2DoubleMap.Entry> ite = entrySet.fastIterator();
		for( int i = 0 ; i < size ; i++ ) {
			Int2DoubleMap.Entry entry = ite.next();
			nodes[i] = Node.create( entry.getIntKey(), entry.getDoubleValue() );
		}
		return new SparseDotVector( nodes, size );
	}

	// ### 
	// ### create HashVector
	// ###
	public static HashVector createHashVector( Map<Integer, ? extends Number> data ){
		return new HashVector( data);
	}
	
}