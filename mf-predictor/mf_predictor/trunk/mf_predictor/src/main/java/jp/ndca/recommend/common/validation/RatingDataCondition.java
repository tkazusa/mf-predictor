package jp.ndca.recommend.common.validation;

import jp.ndca.recommend.common.data.RatingData;

public interface RatingDataCondition {

	public boolean isValid( RatingData data );

	public String getError();

}
