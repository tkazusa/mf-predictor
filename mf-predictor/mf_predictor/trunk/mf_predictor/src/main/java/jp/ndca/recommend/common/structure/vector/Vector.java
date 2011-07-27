package jp.ndca.recommend.common.structure.vector;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.io.Serializable;
import java.util.Iterator;

public interface Vector extends Serializable, Cloneable{

	public double add(int id, double value);
	
	public Vector clone() throws CloneNotSupportedException;
	
	public double get( int id );

	public int getMaxID();

	public double getMaxValue();
	
	public double norm();
	
	public void put( int id, double value );
	
	public double product(int id, double value);

	public void remove( int id );

	public int size();

	public void trim();
	
	public IntSet keySet();

	public int[] keys();
	
	public Iterator<Int2DoubleMap.Entry> iterator();

}

