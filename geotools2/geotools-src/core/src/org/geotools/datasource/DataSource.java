package org.geotools.datasource;


import org.geotools.featuretable.*;
/** The source of data for Features. Shapefiles, database, etc. are referenced through this interface.
 *  @version $Id: DataSource.java,v 1.5 2002/03/14 12:16:12 ianturton Exp $
 */
public interface DataSource
{
	/** Loads Feature rows for the given Extent from the datasource
   * @param ft featureTable to load features into
   * @param ex an extent defining which features to load - null means all features
   * @throws DataSourceException if anything goes wrong
   */
	public void importFeatures(FeatureTable ft, Extent ex) throws DataSourceException;
	
	/** Saves the given features to the datasource
   * @param ft feature table to get features from
   * @param ex extent to define which features to write - null means all
   * @throws DataSourceException if anything goes wrong or if exporting is not supported
   */
	public void exportFeatures(FeatureTable ft, Extent ex) throws DataSourceException;
	
	/** Stops this DataSource from loading
	 */
	public void stopLoading();
	
}

