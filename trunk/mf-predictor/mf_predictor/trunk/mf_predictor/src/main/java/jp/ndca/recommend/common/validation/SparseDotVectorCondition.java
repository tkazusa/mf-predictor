package jp.ndca.recommend.common.validation;

import jp.ndca.recommend.common.data.RatingData;
import jp.ndca.recommend.common.structure.vector.SparseDotVector;
import jp.ndca.recommend.common.structure.vector.Vector;
import jp.ndca.recommend.common.structure.vector.VectorUtil;


public class SparseDotVectorCondition implements DataStructureCondition, RatingDataCondition{

	private String errorMessage;

	SparseDotVectorCondition(){};

	@Override
	public boolean isTransformable() {
		return true;
	}

	@Override
	public boolean isValidDataStructure(Vector vector) {
		errorMessage = null;
		if( vector instanceof SparseDotVector )
			return true;
		errorMessage = "SparseDotVector error \n Vector is not type of \"SparseDotVector\", but is \"" + vector.getClass() + "\"";
		return false;

	}

	@Override
	public Vector transformDataStructure(Vector vector) {
		return VectorUtil.transformSparseDotVector(vector);
	}

	@Override
	public boolean isValid( RatingData vector ) {
		return isValidDataStructure( vector.getVector() );
	}

	@Override
	public String getError() {
		return errorMessage;
	}

}
