package jp.ndca.recommend.common.data;

import java.io.Serializable;

import jp.ndca.recommend.common.structure.vector.Vector;

public class RatingData implements Comparable<RatingData>, Serializable{

	private static final long serialVersionUID = 6959552577897829502L;

	private int dataID;

	private Vector vector;

	public RatingData(){};

	public RatingData( int dataID, Vector vector ){
		this.dataID = dataID;
		this.vector = vector;
	}

	public int getDataID() {
		return dataID;
	}

	public Vector getVector() {
		return vector;
	}

	public Vector setVector(Vector value) {
		Vector old = vector;
		vector  = value;
		return old;
	}

	@Override
	public int compareTo(RatingData o) {
		int id1 = getDataID();
		int id2 = o.getDataID();
		if( id1 < id2 )
			return -1;
		else if( id2 < id1 )
			return  1;
		return 0;
	}

}
