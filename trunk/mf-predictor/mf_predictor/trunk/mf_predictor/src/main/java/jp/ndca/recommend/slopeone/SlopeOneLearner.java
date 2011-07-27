package jp.ndca.recommend.slopeone;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import jp.ndca.recommend.Learner;
import jp.ndca.recommend.common.data.DatasetHandler;
import jp.ndca.recommend.common.data.RatingDataset;
import jp.ndca.recommend.common.structure.vector.HashVector;
import jp.ndca.recommend.common.validation.DatasetValidator;
import jp.ndca.recommend.common.validation.RatingDataValidations;

public class SlopeOneLearner implements Learner{

	private static Logger log = LoggerFactory.getLogger(SlopeOneLearner.class);

	private static final int DEFAULT_CORENUM = 1;

	// dev_ij ( item : i, j )
	private double[][] itemMeanDeviation;

	// id = itemID, →　[ userIDs : Set<Integer> ]
	private Object[] itemInvertedIndex;

	private HashVector[] dataset;

	private int coreNum = DEFAULT_CORENUM;

	public int getCoreNum() {
		return coreNum;
	}
	public void setCoreNum(int coreNum) {
		this.coreNum = coreNum;
	}

	protected static RatingDataValidations validations = new RatingDataValidations();

	static{
		validations.addHashVectorCondition();
		validations.addDataSizeCondition(0, Integer.MAX_VALUE);
		validations.addValueRangeCondition(1, Integer.MAX_VALUE);
	}
	
	/**
	 * constratcor
	 * @param dataset
	 */
	public SlopeOneLearner(){};
	
	public SlopeOneLearner( RatingDataset dataset ){
		setRatingDataset(dataset);
	}

	@Override
	public void setRatingDataset( RatingDataset dataset ) {
		DatasetValidator.check( dataset, validations );
		this.dataset =  new HashVector[dataset.size()];
		for( int i = 0 ; i < dataset.size() ; i++ )
			this.dataset[i] = (HashVector)dataset.fastGet(i).getVector();
	}

	private int maxItemSize;

	@Override
	public void learn() {

		/**
		 * make InvertedIndex
		 */
		maxItemSize = DatasetHandler.getMaxFeatureID(dataset) + 1;
		itemInvertedIndex = new Object[maxItemSize];
		itemMeanDeviation = new double[maxItemSize][maxItemSize];

		for( int userID = 0 ; userID < dataset.length ; userID++ ){
			HashVector vector = dataset[userID];
			ObjectIterator<Int2DoubleMap.Entry> ite = vector.iterator();
			int size = vector.size();
			for( int i = 0 ; i < size ; i++ ){
				int itemID = ite.next().getIntKey();
				IntSet index = (IntSet)itemInvertedIndex[itemID];
				if( index == null ){
					index = new IntOpenHashSet();
					itemInvertedIndex[itemID] = index;
				}
				index.add(userID);
			}
		}

		//transform array from set.
		int noUserItemNum = 0;
		for( int i = 0 ; i < maxItemSize ; i++ ){
			IntSet refSet = (IntSet)itemInvertedIndex[i];
			if( refSet == null ){
				noUserItemNum++;
				continue;
			}
			int[] data = new int[refSet.size()];
			int j = 0;
			for( int id : refSet ){
				data[j++] = id;
			}
			Arrays.sort(data);
			itemInvertedIndex[i] = data;
		}
		log.info( " Number of Item with no relationship with users : " + noUserItemNum );

		////////////////////////

		class Executer implements Runnable{

			Queue<Integer> labelQueue;

			public Executer( Queue<Integer> labelQueue ){
				this.labelQueue = labelQueue;
			}

			@Override
			public void run() {

				//calculate dev
				while( true ){

					Integer i = labelQueue.poll();
					if( i == null )
						break;

					int[] refSet = (int[])itemInvertedIndex[i];
					if( refSet == null )
						continue;
					for( int j = i+1 ; j < maxItemSize ; j++ ){
						int[] candidateSet = (int[])itemInvertedIndex[j];
						if( candidateSet == null )
							continue;

						IntSet coOccurrenceUsers = new IntOpenHashSet();
						int s = 0;
						int r = 0;
						while( s < refSet.length && r < candidateSet.length ){
							int userID_s = refSet[s];
							int userID_r = candidateSet[r];
							if( userID_s == userID_r ){
								coOccurrenceUsers.add(userID_r);
								s++; r++;
							}
							else if ( userID_s < userID_r )
								s++;
							else
								r++;
						}

						double dev = 0.0d;
						for( int coOccurrenceUser : coOccurrenceUsers ){
							HashVector vector = dataset[coOccurrenceUser];
							double rate_i = vector.get(i);
							double rate_j = vector.get(j);
							dev += ( rate_i - rate_j );
						}

						if( coOccurrenceUsers.size() != 0 )
							dev /= (double)coOccurrenceUsers.size();

						itemMeanDeviation[i][j] = dev;
						itemMeanDeviation[j][i] = -dev;
					}
				}
			}
		}

		Queue<Integer> labelQueue = new ConcurrentLinkedQueue<Integer>();
		for( int dataID = 0 ; dataID < maxItemSize ; dataID++ )
			labelQueue.add( dataID );

		Thread[] threads = new Thread[coreNum];
		for( int i = 0 ; i < coreNum ; i++ ){
			threads[i] = new Thread( new Executer(labelQueue) );
			threads[i].start();
		}

		for( Thread thread : threads ){
			try {
				if( thread != null )
					thread.join();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public SlopeOneModel createSlopeOneModel(){
		if( itemMeanDeviation == null )
			throw new RuntimeException("learning is not processed !!");
		return new SlopeOneModel(maxItemSize, itemMeanDeviation, dataset);
	}

	@Override
	public SlopeOnePredictor createPredictor() {
		SlopeOneModel model = createSlopeOneModel();
		return new SlopeOnePredictor(model);
	}

}
