package jp.ndca.recommend.common.validation;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

import java.util.Iterator;

import jp.ndca.recommend.common.data.RatingData;
import jp.ndca.recommend.common.structure.vector.Vector;

public class ValueRangeCondition implements RatingDataCondition{

	private String errorMessage;

	ValueRangeCondition(){}

	@Override
	public String getError() {
		return errorMessage;
	}

	double minValue = Double.NEGATIVE_INFINITY;
	double maxValue = Double.POSITIVE_INFINITY;

	public double getMinValue() {
		return minValue;
	}
	public double getMaxValue() {
		return maxValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}


	public ValueRangeCondition(double minValue, double maxValue){
		this.maxValue = maxValue;
		this.minValue = minValue;
	}

	@Override
	public boolean isValid( RatingData data ) {
		errorMessage = null;
		Vector vector = data.getVector();
		Iterator<Int2DoubleMap.Entry> ite = vector.iterator();
		int size = vector.size();;
		for( int i = 0 ; i < size ; i++ ){
			Int2DoubleMap.Entry entry = ite.next();
			double value = entry.getDoubleValue();
			if( value < minValue || maxValue < value ){
				errorMessage = "ValueRangeCondition error \n" + i + " th value in a Vector is out of interval " +
							   "( from " + minValue + " to " + maxValue + "). \n" +
							   "the value is actually " + value;
				return false;
			}
		}
		return true;
	}

}
