package jp.ndca.recommend.common.data;

import java.io.Serializable;

public interface RatingDataset extends Serializable, Cloneable, Iterable<RatingData>{

	public void add( RatingData data );
	
	public RatingData get( int dataID );
	
	public RatingData fastGet( int dataID );
	
	public int size();
	
	public void trim();
	
}