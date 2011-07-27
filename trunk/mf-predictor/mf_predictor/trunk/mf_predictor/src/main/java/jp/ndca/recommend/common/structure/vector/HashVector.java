package jp.ndca.recommend.common.structure.vector;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap.FastEntrySet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import jp.ndca.recommend.common.map.Int2DoubleExtOpenHashMap;

public class HashVector implements Vector{

	private static final long serialVersionUID = 1292807595669368782L;
	
	protected HashMapAdaptor hashMap;
	
	protected HashVector(HashMapAdaptor map){
		this.hashMap = map;
	}

	protected HashVector( Map<Integer, ? extends Number> data ){
		this.hashMap = new DoubleHashMap(data);
	}
	
	@Override
	public double get( int id ) {
		return hashMap.get(id);
	}
	@Override
	public void remove(int id) {
		hashMap.remove(id);
	}
	@Override
	public void put(int id, double value) {
		hashMap.put(id, value);
	}
	@Override
	public double add(int id, double value) {
		return hashMap.add( id, value );
	}
	@Override
	public double product(int id, double value) {
		return hashMap.product(id, value);
	}
	@Override
	public int size() {
		return hashMap.size();
	}
	@Override
	public IntSet keySet() {
		return hashMap.keySet();
	}
	@Override
	public int[] keys() {
		return hashMap.keys();
	}
	@Override
	public int getMaxID() {
		return hashMap.getMaxID();
	}
	@Override
	public double getMaxValue() {
		return hashMap.getMaxValue();
	}
	public void setDefaultValue( double defaultValue ){
		hashMap.defaultReturnValue(defaultValue);
	}
	@Override
	public ObjectIterator<Int2DoubleMap.Entry> iterator() {
		return hashMap.iterator();
	}
	@Override
	public HashVector clone() throws CloneNotSupportedException{
		return new HashVector( hashMap.clone() );
	}

	@Override
	public double norm(){
		double score = 0.0d;
		ObjectIterator<Int2DoubleMap.Entry> it = hashMap.iterator();
		int size = hashMap.size();
		for( int i = 0 ; i < size ; i++ ) {
			Int2DoubleMap.Entry entry = it.next();
			double val = entry.getDoubleValue();
			score += val * val;
		}
		return Math.sqrt(score);
	}

	@Override
	public void trim() {
		hashMap.trim();
	}		
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		int size = hashMap.size();
		Iterator<Int2DoubleMap.Entry> ite = hashMap.iterator();
		for( int i = 0 ; i < size ; i++ ){
			Int2DoubleMap.Entry entry = ite.next();
			sb.append( entry.getIntKey() + ":" + entry.getDoubleValue() + " " );
		}
		return sb.substring( 0, sb.length() - 1 );
	}
	
	interface HashMapAdaptor{

		public double get( int id );
		
		public void remove(int id);
		
		public void put( int id, double value );
		
		public double add( int id, double value );
		
		public double product(int id, double value);
		
		public int size();
		
		public IntSet keySet();
		
		public int[] keys();
		
		public int getMaxID();
		
		public double getMaxValue();
		
		public ObjectIterator<Int2DoubleMap.Entry> iterator();
		
		public void defaultReturnValue( double defailtValue );
		
		public HashMapAdaptor clone();
		
		public void trim();
		
	}

	class DoubleHashMap implements HashMapAdaptor{

		private Int2DoubleExtOpenHashMap map;
		
		public DoubleHashMap( Map<Integer, ? extends Number> data ){
			this.map = new Int2DoubleExtOpenHashMap( data.size(), 0.5f );
			for( Entry<Integer,? extends Number> entry : data.entrySet() )
				map.put( entry.getKey().intValue(), entry.getValue().doubleValue() );
			map.trim();
		}
		
		@Override
		public double get( int id ){
			return map.get(id);
		}
		@Override
		public void put(int id, double value) {
			map.put(id, value);
		}
		@Override
		public void remove(int id) {
			map.remove(id);
		}
		@Override
		public double add(int id, double value) {
			return map.add(id, value);
		}
		@Override
		public double product(int id, double value) {
			return map.product(id, value);
		}
		@Override
		public int size() {
			return map.size();
		}
		@Override
		public IntSet keySet() {
			return map.keySet();
		}
		@Override
		public int[] keys() {
			int size = map.size();
			int[] keys = new int[map.size()];
			Int2DoubleMap.FastEntrySet entrySet = map.int2DoubleEntrySet();
			ObjectIterator<Int2DoubleMap.Entry> ite = entrySet.fastIterator();
			for( int i = 0 ; i < size ; i++ ) {
				Int2DoubleMap.Entry entry = ite.next();
				keys[i] = entry.getIntKey();
			}
			return keys;
		}
		@Override
		public int getMaxID(){
			int maxID = Integer.MIN_VALUE;
			FastEntrySet entrySet = (Int2DoubleMap.FastEntrySet) map.int2DoubleEntrySet();
			ObjectIterator<Int2DoubleMap.Entry> ite = entrySet.fastIterator();
			int size = entrySet.size();
			for( int i = 0 ; i < size ; i++ ) {
				int id = ite.next().getIntKey();
				if( maxID < id )
					maxID = id;
			}
			return maxID;
		}
		@Override
		public double getMaxValue() {
			double maxValue = Double.NEGATIVE_INFINITY;
			FastEntrySet entrySet = (Int2DoubleMap.FastEntrySet) map.int2DoubleEntrySet();
			ObjectIterator<Int2DoubleMap.Entry> ite = entrySet.fastIterator();
			int size = entrySet.size();
			for( int i = 0 ; i < size ; i++ ) {
				Int2DoubleMap.Entry entry = ite.next();
				double value = entry.getDoubleValue();
				if( maxValue < value )
					maxValue = value;
			}
			return maxValue;
		}
		@Override
		public ObjectIterator<Int2DoubleMap.Entry> iterator() {
			return map.int2DoubleEntrySet().iterator();
		}
		@Override
		public void defaultReturnValue(double defaultValue) {
			map.defaultReturnValue(defaultValue);
		}
		@Override
		public DoubleHashMap clone() {
			return new DoubleHashMap(map);
		}
		@Override
		public void trim() {
			map.trim();
		}
	}

}