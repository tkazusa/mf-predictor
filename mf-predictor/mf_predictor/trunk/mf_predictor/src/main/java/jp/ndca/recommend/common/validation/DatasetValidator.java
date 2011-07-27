package jp.ndca.recommend.common.validation;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unimi.dsi.fastutil.ints.IntSet;

import jp.ndca.recommend.common.data.DatasetHandler;
import jp.ndca.recommend.common.data.RatingArrayDataset;
import jp.ndca.recommend.common.data.RatingData;
import jp.ndca.recommend.common.data.RatingDataset;
import jp.ndca.recommend.common.structure.vector.Vector;

public class DatasetValidator {

	private static Logger log = LoggerFactory.getLogger(DatasetValidator.class);

	public static void check( RatingDataset dataset, RatingDataValidations validations ){

		if( dataset instanceof RatingArrayDataset ) 
			( (RatingArrayDataset)dataset ).sort();
		IntSet dataIDSet = DatasetHandler.getAllDataIDs(dataset);
		Integer max = Collections.max(dataIDSet);
		int size = dataIDSet.size();
		if( size != max.intValue() + 1 )
			throw new IllegalArgumentException("dataIDs are not sequential IDs." );

		int transformNum = 0;
		for( RatingData data : dataset ){
			for( RatingDataCondition validation : validations.getDataConditions() ){
				if( !validation.isValid(data) )
					if( validation instanceof DataStructureCondition && ((DataStructureCondition)validation).isTransformable() ){
						Vector vector = data.getVector();
						vector = ((DataStructureCondition)validation).transformDataStructure(vector);
						data.setVector(vector);
						transformNum++;
					}
					else{
						String errorMessage = validation.getError();
						throw new IllegalArgumentException( "data with dataID \"( " + data.getDataID() + " )\"  has an error. \n " + errorMessage );
					}
			}
		}
		if( 0 < transformNum )
			log.info(" data structure transformation time : " + transformNum );
	}

}
