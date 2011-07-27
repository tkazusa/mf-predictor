package jp.ndca.recommend.common.validation;

import jp.ndca.recommend.common.structure.vector.Vector;

public interface DataStructureCondition {

	public boolean isValidDataStructure( Vector vector );

	public boolean isTransformable();

	public Vector transformDataStructure( Vector vector );

}
