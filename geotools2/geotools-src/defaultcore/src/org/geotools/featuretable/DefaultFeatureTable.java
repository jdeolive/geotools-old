/*
 * DefaultFeatureTable.java
 *
 * Created on March 14, 2002, 2:09 PM
 */

package org.geotools.featuretable;
import org.geotools.datasource.*;

import java.util.Vector;
/**
 *
 * @author  jamesm
 */
public class DefaultFeatureTable implements FeatureTable {

    private Vector listeners = new Vector();
    private Vector rows = new Vector();
    private DataSource datasource;
    private Extent loadedExtent;
    
    /** Creates a new instance of DefaultFeatureTable */
    public DefaultFeatureTable() {
    }
    
    public DefaultFeatureTable(DataSource ds){
        setDataSource(ds);
    }

    /** Adds a listener for table events
     */
    public void addTableChangedListener(TableChangedListener fel) {
        listeners.addElement(fel);
    }
    
    /** Removes a listener for table events
     */
    public void removeTableChangedListener(TableChangedListener fel) {
        listeners.removeElement(fel);
    }
    
    /** get the features in the datasource inside the loadedExtent
     * will not trigger a datasourceload.
     * functionally equivalent to getFeatures(getLoadedExtent());
     * @see #getfeatures(Extent ex)
     */
    public Feature[] getFeatures() {
        Feature[] features = new Feature[rows.size()];
        for(int i=0;i<features.length;i++){
            features[i] = (Feature)rows.elementAt(i);
        }
        return features;
    }
    
    /** 
     * Removes the features from this FeatureTable which fall into the specified extent, notifying TableChangedListeners that the table has changed
     * @param ex The extent defining which features to remove
     */
    public void removeFeatures(Extent ex) {
        //TODO: remove the features
    }
    /** Removes the features from this FeatureTable, notifying TableChangedListeners that the table has changed
     * @param f The Features to remove
     */ 
    public void removeFeatures(Feature[] features) {
        //TODO: remove the features
    }
    
    /** Gets the DataSource being used by this FeatureTable
     */
    public DataSource getDataSource() {
        return datasource;
    }
    
    /** Removes the feature from this FeatureTable, notifying TableChangedListeners that the table has changed
     * @param f The Feature to remove
     */
    public void removeFeature(Feature feature) {
        rows.removeElement(feature);
    }
    
    /** get the features in the datasource inside the Extent ex
     * this may trigger a load on the datasource
     */
    public Feature[] getFeatures(Extent ex) throws DataSourceException {
        //TODO: this diff when not null bit could be more elegent
        Extent toLoad[];
        if(loadedExtent!=null){
         toLoad = loadedExtent.difference(ex);
        }
        else{
            toLoad = new Extent[]{ex};
        }
        
        for(int i=0;i<toLoad.length;i++){
            //TODO: move this code to its own method?
            if(toLoad[i]!=null){
                datasource.importFeatures(this,toLoad[i]);
                if(loadedExtent==null){
                    loadedExtent = toLoad[i];
                }
                else{
                    loadedExtent = loadedExtent.combine(toLoad[i]);
                }
            }
        }
        return getFeatures();
    }
    
    
    /** Adds the given List of Features to this FeatureTable, notifying TableChangedListeners that the table has changed
     * @param features The List of Features to add
     */
    public void addFeature(Feature feature) {
        rows.addElement(feature);
    }
    
    /** Gets the loaded Extent of this FeatureTable
     * The Extent of current loaded Features in this table
     */
    public Extent getLoadedExtent() {
        return loadedExtent;
    }
    
    /** Adds the given List of Features to this FeatureTable
     * @param features The List of Features to add
     */
    public void addFeatures(Feature[] features) {
        for(int i=0;i<features.length;i++){
            if(!rows.contains(features[i])){
                rows.addElement(features);
            }
        }
    }
    
    public void setDataSource(DataSource ds) {
        datasource = ds;
    }
    
}
