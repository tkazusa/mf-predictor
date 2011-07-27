package jp.ndca.recommend.common.validation;

import jp.ndca.recommend.common.data.RatingData;
import jp.ndca.recommend.common.structure.vector.Vector;

public class DataSizeCondition implements RatingDataCondition{

	private static final int DEFAULT_MIN_SIZE = 0;

	private static final int DEFAULT_MAX_SIZE = Integer.MAX_VALUE;

	private int minSize = DEFAULT_MIN_SIZE;

	private int maxSize = DEFAULT_MAX_SIZE;

	private String errorMessage;

	DataSizeCondition(){};

	DataSizeCondition(int minSize, int maxSize){
		this.minSize = minSize;
		this.maxSize = maxSize;
	}

	public int getMinSize() {
		return minSize;
	}
	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}


	public int getMaxSize() {
		return maxSize;
	}
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}


	public boolean isValid( RatingData data ){
		Vector vector = data.getVector();
		int size = vector.size();
		if( size < minSize || maxSize < size ){
			errorMessage = "LengthCondition error \n Vector size must be bwtween " + minSize + " from " + maxSize + ", but is " + size;
			return false;
		}
		return true;
	}

	@Override
	public String getError() {
		return errorMessage;
	}

}
