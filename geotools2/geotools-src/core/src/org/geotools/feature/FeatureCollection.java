package org.geotools.feature;

import org.geotools.data.*;
import org.geotools.datasource.extents.*;

/**
 * 
 *
 * @author  Ian Turton, CCG<br>
 * @author  Rob Hranac, VFNY
 */
public interface FeatureCollection {

    /* ************************************************************************
     * Managing data source and extents.
     * ************************************************************************/
    /** 
     * Gets the DataSource being used by this FeatureTable
     * 
     * @param data The datasource for this feature collection to mediate.
     */
    public void setDataSource(DataSource data);

    /** 
     * Gets the DataSource being used by this FeatureTable
     * 
     * @return Datasource used by this feature collection.
     */
    public DataSource getDataSource();

    /** 
     * Gets the loaded Extent of this FeatureTable
     * The Extent of current loaded Features in this table
     *
     * @param boundary The datasource for this feature collection to mediate.
     */
    public void setExtent(Extent extent);

    /** 
     * Gets the loaded Extent of this FeatureTable
     * The Extent of current loaded Features in this table
     *
     * @return Datasource used by this feature collection.
     */
    public Extent getExtent();


    /* ************************************************************************
     * Managing features via the datasource.
     * ************************************************************************/
    /** 
     * Get the features in the datasource inside the loadedExtent will not 
     * trigger a datasourceload.  functionally equivalent to getFeatures(getLoadedExtent());
     *
     * @see #getFeatures(Extent ex)
     */
    public Feature[] getFeatures();
    
    /** 
     * get the features in the datasource inside the Extent ex
     * this may trigger a load on the datasource
     */
    public Feature[] getFeatures(Extent boundary) 
        throws DataSourceException;
    
    /** 
     * Removes the features from this FeatureTable, notifying TableChangedListeners that the table has changed
     * @param f The Features to remove
     */
    public void removeFeatures(Feature[] features);

    /** Removes the features from this FeatureTable which fall into the specified extent, notifying TableChangedListeners that the table has changed
     * @param ex The extent defining which features to remove
     */
    public void removeFeatures(Extent extent);

    /** 
     * Adds the given List of Features to this FeatureTable
     *
     * @param features The List of Features to add
     */
    public void addFeatures(Feature[] features);
    
   
    /* ************************************************************************
     * Managing collection listeners.
     * ************************************************************************/
    /** 
     * Adds a listener for table events
     */
    public void addListener(CollectionListener spy);
    
    /** Removes a listener for table events
     */
    public void removeListener(CollectionListener spy);
    
    
}
