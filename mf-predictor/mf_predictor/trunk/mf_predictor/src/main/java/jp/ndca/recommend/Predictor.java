package jp.ndca.recommend;

/**
 * Rating Predictor
 * @author hattori_tsukasa
 *
 */
public interface Predictor {

	abstract public double predict( int userID, int itemID );

}

