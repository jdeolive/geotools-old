package org.geotools.feature;

import org.geotools.data.Extent;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataSource;

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
     * @param extent The datasource for this feature collection to mediate.
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
     *
     * TODO: givin that this may trigger a load, would fetchFeatures be a
     * TODO: more suitable name?
     *
     * @param boundary The extent in which to load features
     * @return An array of all the features that fall within the boundary
     * @throws DataSourceException if anything went wrong during the fetching
     *         or construction of the requested features
     */
    public Feature[] getFeatures(Extent boundary) 
        throws DataSourceException;
    
    /** 
     * Removes the features from this FeatureCollection, notifying 
     * CollectionListeners that the table has changed.
     * @param features The Features to remove
     */
    public void removeFeatures(Feature[] features);

    /** 
     * Removes the features from this FeatureCollection which fall into the
     * specified extent, notifying CollectionListeners that the collection 
     * has changed.
     * @param extent The extent defining which features to remove
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
     * @param spy The listener to add
     */
    public void addListener(CollectionListener spy);
    
    /** 
     * Removes a listener for table events
     * @param spy The listener to remove
     */
    public void removeListener(CollectionListener spy);
    
    
}
