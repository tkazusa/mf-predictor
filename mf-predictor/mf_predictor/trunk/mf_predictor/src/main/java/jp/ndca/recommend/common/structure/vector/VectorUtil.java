package jp.ndca.recommend.common.structure.vector;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jp.ndca.recommend.common.structure.vector.SparseDotVector.Node;

public class VectorUtil {
	
	/**
	 * component adding.
	 * @param vector
	 * @return
	 */
	public static Vector add( Vector vector1, Vector vector2 ){
		Int2DoubleOpenHashMap map = new Int2DoubleOpenHashMap();
		int size = vector1.size();
		Iterator<Int2DoubleMap.Entry> ite = vector1.iterator();
		for( int i = 0 ; i < size ; i++ ){
			Int2DoubleMap.Entry entry = ite.next();
			map.add( entry.getIntKey(), entry.getDoubleValue() );
		}
		size = vector2.size();
		ite = vector2.iterator();
		for( int i = 0 ; i < size ; i++ ){
			Int2DoubleMap.Entry entry = ite.next();
			map.add( entry.getIntKey(), entry.getDoubleValue() );
		}
		return VectorFactory.createSparseDotVector(map);
	}
	
	/**
	 * tf
	 * @param vector
	 * @return
	 */
	public static Vector tf( Vector vector ){
		if( vector instanceof HashVector ){
			return tf( (HashVector)vector );
		}
		else if( vector instanceof SparseDotVector ){
			return tf( (SparseDotVector)vector );
		}
		else
			throw new IllegalArgumentException( "unknown vector type !" );
	}

	public static HashVector tf( HashVector vector ){
		double total = 0;
		int size = vector.size();
		ObjectIterator<Int2DoubleMap.Entry> ite = vector.iterator();
		for( int i = 0 ; i < size ; i++ ){
			Int2DoubleMap.Entry entry = ite.next();
			total += entry.getDoubleValue();
		}
		Int2DoubleOpenHashMap map = new Int2DoubleOpenHashMap();
		ite = vector.iterator();
		for( int i = 0 ; i < size ; i++ ){
			Int2DoubleMap.Entry entry = ite.next();
			int index = entry.getIntKey();
			double value = entry.getDoubleValue();
			map.put( index, value / total );
		}
		return VectorFactory.createHashVector( map );
	}

	public static SparseDotVector tf( SparseDotVector vector ){
		double total = 0;
		int size = vector.size();
		Node[] nodes = vector.getNodeArray();
		for( int i = 0 ; i < size ; i++ ){
			Node ndoe = nodes[i];
			double value = ndoe.getDoubleValue();
			total += value;
		}
		Int2DoubleOpenHashMap map = new Int2DoubleOpenHashMap();
		for( int i = 0 ; i < size ; i++ ){
			Node node = nodes[i];
			map.put( node.getIntKey(), node.getDoubleValue() / total );
		}
		return VectorFactory.createSparseDotVector( map );
	}


	/**
	 * normalize
	 * @param vector
	 * @return
	 */
	public static Vector normalize( Vector vector ){
		if( vector instanceof HashVector ){
			return normalize( (HashVector)vector );
		}
		else if( vector instanceof SparseDotVector ){
			return normalize( (SparseDotVector)vector );
		}
		else
			throw new IllegalArgumentException( "unknown vector type !" );
	}

	public static HashVector normalize( HashVector vector ){
		double norm = vector.norm();
		int size = vector.size();
		Int2DoubleOpenHashMap map = new Int2DoubleOpenHashMap();
		ObjectIterator<Int2DoubleMap.Entry> ite = vector.iterator();
		for( int i = 0 ; i < size ; i++ ){
			Int2DoubleMap.Entry entry = ite.next();
			int index = entry.getIntKey();
			double value = entry.getDoubleValue();
			map.put( index, value / norm );
		}
		return VectorFactory.createHashVector( map );
	}

	public static SparseDotVector normalize( SparseDotVector vector ){
		double norm = vector.norm();
		int size = vector.size();
		Int2DoubleOpenHashMap map = new Int2DoubleOpenHashMap();
		Node[] nodes = vector.getNodeArray();
		for( int i = 0 ; i < size ; i++ ){
			Node node = nodes[i];
			map.put( node.getIntKey(), node.getDoubleValue() / norm );
		}
		return VectorFactory.createSparseDotVector( map );
	}


	public static double cosine( SparseDotVector v1, SparseDotVector v2 ){
		double dot = SparseDotVector.dot(v1, v2);
		if( dot == 0 )
			return 0;
		return dot / ( v1.norm() * v2.norm() );
	}


	public static HashVector transformHashVector( Vector vector ){
		if( vector instanceof HashVector )
			return (HashVector)vector;
		else if( vector instanceof SparseDotVector ){
			Map<Integer, Double> map = new HashMap<Integer, Double>();
			for( int key : vector.keys() )
				map.put( key, vector.get(key) );
			return VectorFactory.createHashVector(map);
		}
		else
			throw new IllegalArgumentException("unknown vector type.");
	}


	public static SparseDotVector transformSparseDotVector( Vector vector ){
		if( vector instanceof SparseDotVector )
			return (SparseDotVector)vector;
		else if( vector instanceof HashVector ){
			Int2DoubleMap map = new Int2DoubleOpenHashMap();
			int size = vector.size();
			ObjectIterator<Int2DoubleMap.Entry> ite = ((HashVector)vector).iterator();
			for( int i = 0 ; i < size ; i++ ){
				Int2DoubleMap.Entry entry = ite.next();
				map.put( entry.getIntKey(), entry.getDoubleValue() );
			}
			return VectorFactory.createSparseDotVector(map);
		}
		else
			throw new IllegalArgumentException("unknown vector type.");
	}


	public static SparseDotVector toSparseDotVectorFromString( String line ){
		Int2DoubleMap map = new Int2DoubleOpenHashMap();
		String[] keyValues = line.split(" ");
		for( String part : keyValues ){
			String[] strs = part.split(":");
			if( strs.length != 2 )
				throw new IllegalArgumentException("This string is not proper format for vector string !");
			int key = Integer.parseInt( strs[0] );
			double value = Double.valueOf( strs[1] );
			map.put( key, value );
		}
		return VectorFactory.createSparseDotVector(map);
	}

	public static HashVector toHashVectorFromString( String line ){
		Int2DoubleMap map = new Int2DoubleOpenHashMap();
		String[] keyValues = line.split(" ");
		for( String part : keyValues ){
			String[] strs = part.split(":");
			if( strs.length != 2 )
				throw new IllegalArgumentException("This string is not proper format for vector string !");
			int key = Integer.parseInt( strs[0] );
			double value = Double.valueOf( strs[1] );
			map.put( key, value );
		}
		return VectorFactory.createHashVector(map);
	}


}
