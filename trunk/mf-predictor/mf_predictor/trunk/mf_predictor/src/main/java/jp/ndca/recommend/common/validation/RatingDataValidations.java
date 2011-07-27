package jp.ndca.recommend.common.validation;

import java.util.ArrayList;
import java.util.List;

public class RatingDataValidations {

	protected List<RatingDataCondition> dataConditions = new ArrayList<RatingDataCondition>();

	public RatingDataValidations addHashVectorCondition(){
		dataConditions.add( new HashVectorCondition() );
		return this;
	}

	public RatingDataValidations addSparseDotVectorCondition(){
		dataConditions.add( new SparseDotVectorCondition() );
		return this;
	}

	public RatingDataValidations addDataSizeCondition( int minLength, int maxLength ){
		dataConditions.add( new DataSizeCondition(minLength, maxLength) );
		return this;
	}

	public RatingDataValidations addValueRangeCondition( double min, double max ){
		dataConditions.add( new ValueRangeCondition( min, max) );
		return this;
	}

	public List<RatingDataCondition> getDataConditions(){
		return dataConditions;
	}

}
