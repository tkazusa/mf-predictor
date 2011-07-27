package jp.ndca.recommend.slopeone;

import java.io.Serializable;

import jp.ndca.recommend.common.structure.vector.HashVector;
import jp.ndca.recommend.common.structure.vector.Vector;

public class SlopeOneModel implements Serializable{

	private static final long serialVersionUID = -3964443864097118265L;

	public SlopeOneModel( int maxItemID, double[][] itemMeanDeviation, HashVector[] dataset){
		this.itemMeanDeviation = itemMeanDeviation;
		this.dataset = dataset;
		this.maxItemID = maxItemID;
	}

	private double[][] itemMeanDeviation;
	private HashVector[] dataset;
	private int maxItemID;

	public double[][] getItemMeanDeviation() {
		return itemMeanDeviation;
	}
	public Vector[] getDataset() {
		return dataset;
	}
	public int getMaxItemID() {
		return maxItemID;
	}

	public void setItemMeanDeviation(double[][] itemMeanDeviation) {
		this.itemMeanDeviation = itemMeanDeviation;
	}
	public void setDataset(HashVector[] dataset) {
		this.dataset = dataset;
	}
	public void setMaxItemID(int maxItemID) {
		this.maxItemID = maxItemID;
	}

}
