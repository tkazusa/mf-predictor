package jp.ndca.recommend.plsa;

import jp.ndca.recommend.Predictor;

public class ForcedPLSAPredictor implements Predictor{

	private double[] mu_u;
	private double[] var_u;

	private double[][] p_z_u;		// p(z|u)	: n × z
	private double[][] mu_iz;		// μ_iz	: m × z

	public ForcedPLSAPredictor( ForcedPLSAModel model ){
		this.mu_u  = model.getMu_u();
		this.var_u = model.getVar_u();
		this.mu_iz = model.getMu_iz();
		this.p_z_u = model.getP_z_u();
	}
	
	@Override
	public double predict(int objectID, int itemID) {
		int userID = objectID;
		if( userID < mu_u.length ){
			if( itemID < mu_iz.length ){
				int k = mu_iz[0].length;
				double sum = 0;
				for( int z_id = 0 ; z_id < k ; z_id++ )
					sum += p_z_u[userID][z_id] * mu_iz[itemID][z_id];
				return mu_u[userID] + sum * var_u[userID];
			}
			else
				return mu_u[userID];
		}
		else
			return -1;
	}

}
