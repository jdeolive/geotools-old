package org.geotools.featuretable;



public interface FeatureTable {
    public void setDataSource(DataSource ds);
    
    /** Gets the loaded Extent of this FeatureTable
     * The Extent of current loaded Features in this table
     */
    public Extent getLoadedExtent();
    
    /** get the features in the datasource inside the loadedExtent
     *  will not trigger a datasourceload. 
     *  functionally equivalent to getFeatures(getLoadedExtent());
     * @see #getfeatures(Extent ex)
     */
    public Feature[] getFeatures();
    
    /** get the features in the datasource inside the Extent ex
     *  this may trigger a load on the datasource
     */
    public Feature[] getFeatures(Extent ex) throws DataSourceException;
    
    /** Adds a listener for table events
     */
    public void addTableChangedListener(TableChangedListener fel);
    
    /** Removes a listener for table events
     */
    public void removeTableChangedListener(TableChangedListener fel);
    

    /** Gets the DataSource being used by this FeatureTable
     */
    public DataSource getDataSource();
    
    /** Removes the feature from this FeatureTable, notifying TableChangedListeners that the table has changed
     * @param f The Features to remove
     */
    public void removeFeature(Feature feature);
    public void removeFeatures(Feature[] features);
    public void removeFeatures(Extent ex);

    
    /** Adds the given List of Features to this FeatureTable, notifying TableChangedListeners that the table has changed
     * @param features The List of Features to add
     */
    public void addFeature(Feature feature);
    
    /** Adds the given List of Features to this FeatureTable
     * @param features The List of Features to add
     */
    public void addFeatures(Feature[] features);
    
}
