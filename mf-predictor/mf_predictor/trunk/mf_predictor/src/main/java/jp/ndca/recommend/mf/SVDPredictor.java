package jp.ndca.recommend.mf;

import jp.ndca.recommend.Predictor;

public class SVDPredictor implements Predictor{

	protected int k;
	protected double mu;
	protected double[] bu;
	protected double[] bi;
	protected double[][] userFactor;
	protected double[][] itemFactor;
	
	public SVDPredictor( SVDModel model ){
		mu = model.getMu();
		bi = model.getBi();
		bu = model.getBu();
		userFactor = model.getUserFactor();
		itemFactor = model.getItemFactor();
		if( userFactor[0].length != itemFactor.length )
			throw new IllegalArgumentException("itemFactor and userFactor have a different factor dimension !");
		k = userFactor[0].length;
	}
	
	@Override
	public double predict(int userID, int itemID ) {
		if( userID < bu.length ){
			if( itemID < bi.length ){
				double score = mu + bu[userID] + bi[itemID];
				for( int i = 0 ; i < k ; i++)
					score += userFactor[userID][i] * itemFactor[i][itemID];
		 		return score;
			}
			else{
				double score = mu + bu[userID];
		 		return score;
			}
		}
		else
			return -1;
	}

}
