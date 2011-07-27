package jp.ndca.recommend.mf;

import java.io.Serializable;

public class SVDModel implements Serializable{

	private static final long serialVersionUID = -4430533826965528353L;
	
	protected double mu; 
	protected double[] bu; 
	protected double[] bi; 
	protected double[][] userFactor; 
	protected double[][] itemFactor;
	
	public double getMu() 			{		return mu;			}
	public double[] getBu()			{		return bu;			}
	public double[] getBi()			{		return bi;			}
	public double[][] getUserFactor() {	return userFactor;		}
	public double[][] getItemFactor() {	return itemFactor;		}
	
	public void setMu(double mu) 
	{			this.mu = mu;					}
	public void setBu(double[] bu)
	{			this.bu = bu;					}
	public void setBi(double[] bi)
	{			this.bi = bi;					}
	public void setUserFactor(double[][] userFactor)
	{			this.userFactor = userFactor;				}
	public void setItemFactor(double[][] itemFactor)
	{			this.itemFactor = itemFactor;				}
	
}
