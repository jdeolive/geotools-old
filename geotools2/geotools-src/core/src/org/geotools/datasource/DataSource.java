package org.geotools.datasource;

import com.sun.java.util.collections.List;

/** The source of data for Features. Shapefiles, database, etc. are reference through this interface.
 */
public interface DataSource
{
	/** Loads Feature rows for the given Extent from the datasource
	 */
	public List load(Extent ex) throws DataSourceException;
	
	/** Saves the given features to the datasource
	 */
	public void save(List features) throws DataSourceException;
	
	/** gets the Column names (used by FeatureTable) for this DataSource
	 */
	public String [] getColumnNames();
	
	/** Stops this DataSource from loading
	 */
	public void stopLoading();
	
}

