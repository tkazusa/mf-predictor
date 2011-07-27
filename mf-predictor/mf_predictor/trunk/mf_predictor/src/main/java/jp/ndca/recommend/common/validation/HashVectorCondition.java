package jp.ndca.recommend.common.validation;

import jp.ndca.recommend.common.data.RatingData;
import jp.ndca.recommend.common.structure.vector.HashVector;
import jp.ndca.recommend.common.structure.vector.Vector;
import jp.ndca.recommend.common.structure.vector.VectorUtil;

public class HashVectorCondition implements DataStructureCondition, RatingDataCondition{

	private String errorMessage;

	HashVectorCondition(){};

	@Override
	public boolean isTransformable() {
		return true;
	}

	@Override
	public boolean isValidDataStructure(Vector vector) {
		errorMessage = null;
		if( vector instanceof HashVector )
			return true;
		errorMessage = "HashVectorCondition error \n Vector is not type of \"HashVector\", but is \"" + vector.getClass() + "\"";
		return false;

	}

	@Override
	public Vector transformDataStructure(Vector vector) {
		return VectorUtil.transformHashVector(vector);
	}

	@Override
	public boolean isValid( RatingData data ) {
		return isValidDataStructure( data.getVector() );
	}

	@Override
	public String getError() {
		return errorMessage;
	}

}
