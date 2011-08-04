package jp.ndca.recommend.common.structure.vector;

import java.io.Serializable;
import java.util.Arrays;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

/**
 * Vector for learning and classifier.
 * This vector does't be predicted to update, add and remove self-compositions.
 *
 * @author Hattori Tsukasa
 *
 */
public class SparseDotVector implements Vector{

	private static final long serialVersionUID = 5139352187360817728L;

	protected Node[] nodes;

	protected int size;

	protected SparseDotVector( Node[] nodes, int size ){
		super();
		if( 0 < size )
			Arrays.sort( nodes, 0, size );
		this.nodes = nodes;
		this.size = size;
 	}

	@Override
	public double norm(){
		double score = 0.0d;
		for( int i = 0 ; i < size ; i++ ){
			double val = nodes[i].getDoubleValue();
			score += val * val;
		}
		return Math.sqrt(score);
	}

	public Node[] getNodeArray() {
		return nodes;
	}

	public int size(){
		return size;
	}

	@Override
	public void trim(){
		if( size < nodes.length )
			nodes = Arrays.copyOf(nodes, size);
	}

	public static double dot( SparseDotVector v1, SparseDotVector v2 ){

		int size1 = v1.size();
		int size2 = v2.size();

		double score = 0;
		for( int i = 0, j = 0 ; i < size1 && j < size2 ; ){

			Node node1 = v1.nodes[i];
			Node node2 = v2.nodes[j];

			int id1 = node1.getIntKey();
			int id2 = node2.getIntKey();

			if( id1 < id2 )
				i++;
			else if( id2 < id1 )
				j++;
			else{
				score += node1.getDoubleValue() * node2.getDoubleValue();
				i++;
				j++;
			}
		}
		return score;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for( int i = 0 ; i < size ; i++ )
			sb.append( nodes[i].getIntKey() + ":" + nodes[i].getDoubleValue() + " " );
		return sb.substring( 0, sb.length() - 1 );
	}
	
	@Override
	public double get( int id ) {
		if( size == 0 )
			return 0;
		int min = 0;
		int max = size - 1;
		while( min <= max ){
			int middle = ( max + min ) / 2;
			Node node = nodes[middle];
			int middleIndex = node.getIntKey();
			if( middleIndex < id )
				min = ++middle;
			else if( id < middleIndex )
				max = --middle;
			else
				return node.getDoubleValue();
		}
		return 0.0d;
	}

	@Override
	public void put(int id, double value ) {
		int min = 0;
		int max = size - 1;
		while( min <= max ){
			int middle = ( max + min ) / 2;
			Node node = nodes[middle];
			int middleIndex = node.getIntKey();
			if( middleIndex < id )
				min = ++middle;
			else if( id < middleIndex )
				max = --middle;
			else{
				node.setValue(value);
				return;
			}
		}
		int index = 0;
		if( 0 <= max )
			index = ++max;

		Node node = Node.create( id, value );
		ensureCapacity( size + 1 );
		System.arraycopy( nodes, index, nodes, index+1, size-index );
		nodes[index] = node;
		size++;
	}

	@Override
	public double add(int id, double value) {
		int min = 0;
		int max = size - 1;
		while( min <= max ){
			int middle = ( max + min ) / 2;
			Node node = nodes[middle];
			int middleIndex = node.getIntKey();
			if( middleIndex < id )
				min = ++middle;
			else if( id < middleIndex )
				max = --middle;
			else{
				node.add(value);
				return node.getDoubleValue();
			}
		}
		int index = 0;
		if( 0 <= max )
			index = ++max;
		
		Node node = Node.create( id, value );
		ensureCapacity( size + 1 );
		System.arraycopy( nodes, index, nodes, index+1, size-index );
		nodes[index] = node;
		size++;
		return value;
	}

	@Override
	public double product( int id, double value ) {
		int min = 0;
		int max = size - 1;
		while( min <= max ){
			int middle = ( max + min ) / 2;
			Node node = nodes[middle];
			int middleIndex = node.getIntKey();
			if( middleIndex < id )
				min = ++middle;
			else if( id < middleIndex )
				max = --middle;
			else{
				node.product(value);
				return node.getDoubleValue();
			}
		}
		return 0.0d;
	}

    protected void ensureCapacity(int minCapacity) {
    	int oldCapacity = nodes.length;
    	if ( oldCapacity < minCapacity ) {
    		int newCapacity = (oldCapacity * 3) / 2 + 1;
    		if (newCapacity < minCapacity)
    			newCapacity = minCapacity;
    		// minCapacity is usually close to size, so this is a win:
    		nodes = Arrays.copyOf(nodes, newCapacity);
    	}
    }

	@Override
	public void remove( int id ) {
		int min = 0;
		int max = size - 1;
		while( min <= max ){
			int middle = ( max + min ) / 2;
			Node node = nodes[middle];
			int middleIndex = node.getIntKey();
			if( middleIndex < id )
				min = ++middle;
			else if( id < middleIndex )
				max = --middle;
			else{
				System.arraycopy( nodes, middle+1, nodes, middle, size - (middle+1 ) );
				nodes[--size] = null;
				return;
			}
		}
	}

	@Override
	public IntSet keySet(){
		IntSet set = new IntOpenHashSet();
		for( int i = 0 ; i < size ; i++ )
			set.add( nodes[i].getIntKey() );
		return set;
	}
	
	@Override
	public int[] keys() {
		int[] keys = new int[size];
		for( int i = 0 ; i < size ; i++ )
			keys[i] = nodes[i].getIntKey();
		return keys;
	}
	
	@Override
	public int getMaxID() {
		return nodes[size-1].getIntKey();
	}
	
	@Override
	public double getMaxValue() {
		double maxValue = Double.NEGATIVE_INFINITY;
		for( int i = 0 ; i < size ; i++ ){
			 Node node = nodes[i];
			double value = node.getDoubleValue();
			if( maxValue < value )
				maxValue = value;
		}
		return maxValue;
	}

	@Override
	public ObjectIterator<Int2DoubleMap.Entry> iterator() {
		return new SparseDotIterator(size);
	}

	@Override
	public SparseDotVector clone() throws CloneNotSupportedException{
		Node[] newNodes = new Node[size];
		for( int i = 0 ; i < size ; i++ )
			newNodes[i] = nodes[i].clone();
		return new SparseDotVector( newNodes, size );
	}
	
	public abstract static class Node 
		implements Int2DoubleMap.Entry, Comparable<Node>, Serializable,Cloneable{
		private static final long serialVersionUID = 588148430998910917L;
		public abstract void product(double value);
		public abstract void add(double value);
		public abstract void setKey(int index);
		public abstract Node clone() throws CloneNotSupportedException;
		public static Node create(int index, double value ){
			return new IntDoubleNode(index, value);
		}
	}

	
	public static class IntDoubleNode extends Node{
		private static final long serialVersionUID = -7571548333804370463L;
		public int index;
		public double value;
		
		public IntDoubleNode( int index, double value ){
			this.index = index;
			this.value = value;
		}
		
		@Override
		public Integer getKey()		{	return index;		}
		@Override
		public int getIntKey()			{	return index;		}
		@Override
		public Double getValue()		{	return value;		}
		@Override
		public double getDoubleValue(){	return value;		}
		@Override
		public void setKey(int index)	{	this.index = index;		}
		@Override
		public double setValue(double value)	{
			double old = this.value;
			this.value = value;	
			return old;
		}
		@Override
		public Double setValue(Double value) {
			return setValue( value.doubleValue() );
		}
		@Override
		public void product(double value)		{	this.value *= value;	}
		@Override
		public void add(double value)			{	this.value += value;	}
		@Override
		public int compareTo( Node o ) { // ascending oerder
			int o_index = o.getIntKey();
			if( this.index < o_index )			return -1;
			else if( o_index < this.index )	return  1;
			else								return  0;
		}
		@Override
		public Node clone() throws CloneNotSupportedException{
			return new IntDoubleNode( index, value );
		}
	}

	
	class SparseDotIterator implements ObjectIterator<Int2DoubleMap.Entry>{
		int i = 0;
		int size;
		SparseDotIterator(int size){
			this.size = size;
		}
		@Override
		public boolean hasNext() {
			return i < size;
		}
		@Override
		public Int2DoubleMap.Entry next() {
			return nodes[i++];
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		@Override
		public int skip(int i) {
			return 0;
		}
	}

}