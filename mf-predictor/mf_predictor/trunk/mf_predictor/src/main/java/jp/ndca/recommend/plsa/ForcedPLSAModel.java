package jp.ndca.recommend.plsa;

import java.io.Serializable;

public class ForcedPLSAModel implements Serializable{

	private static final long serialVersionUID = -412792732235733708L;
	
	private double[] mu_u;
	private double[] var_u;

	private double[][] p_z_u;		// p(z|u)	: n × z
	private double[][] mu_iz;		// μ_iz	: m × z
	
	
	public double[] getMu_u()		{		return mu_u;			}
	public double[] getVar_u()	{		return var_u;			}
	public double[][] getMu_iz()	{		return mu_iz;			}
	public double[][] getP_z_u()	{		return p_z_u;			}
	
	public void setVar_u(double[] var_u)
	{		this.var_u = var_u;			}
	public void setMu_u(double[] mu_u)
	{		this.mu_u = mu_u;			}
	public void setMu_iz(double[][] mu_iz)
	{		this.mu_iz = mu_iz;			}
	public void setP_z_u(double[][] p_z_u)
	{		this.p_z_u = p_z_u;			}
	
	
	public ForcedPLSAModel( double[] mu_u, double[] var_u, double[][] p_z_u, double[][] mu_iz ){
		this.mu_u  = mu_u;
		this.var_u = var_u;
		this.p_z_u = p_z_u;
		this.mu_iz = mu_iz;
	}
	
}
