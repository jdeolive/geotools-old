/*
 * FeatureCollectionDefault.java
 *
 * Created on March 14, 2002, 2:09 PM
 */
package org.geotools.feature;

import java.util.*;
import org.geotools.data.*;
import org.geotools.datasource.extents.*;

/**
 * The default feature collection holds and passes out features promiscuously
 * to requesting clients.  It does not guarantee that features are of a certain
 * type or that they follow a specific schema. 
 * 
 * @author  James MacGill, CCG<br>
 * @author  Rob Hranac, VFNY<br>
 */
public class FeatureCollectionDefault implements FeatureCollection {

    /* Internal feature storage list */
    private List features = new Vector();

    /* Internal listener storage list */
    private List listeners = new Vector();

    /* Pointer to the data source */
    private DataSource data;

    /* The currently loaded extent */
    private Extent loadedExtent;
    
    /** 
     * Creates a new instance of DefaultFeatureTable
     *
     */
    public FeatureCollectionDefault() {
    }
    
    /** 
     * Creates a new instance of DefaultFeatureTable
     *
     * @param data 
     */
    public FeatureCollectionDefault(DataSource data){
        setDataSource(data);
    }



    /* ************************************************************************
     * Managing data source and extents.
     * ************************************************************************/
    /** 
     * Creates a new instance of DefaultFeatureTable
     *
     * @param data 
     */
    public void setDataSource(DataSource data) {
        this.data = data;
    }

    /** 
     * Creates a new instance of DefaultFeatureTable
     *
     * @return 
     */
    public DataSource getDataSource() {
        return this.data;
    }


    /** Gets the loaded Extent of this FeatureTable
     * The Extent of current loaded Features in this table
     */
    public void setExtent(Extent extent) {
        this.loadedExtent = extent;
    }
    

    /** Gets the loaded Extent of this FeatureTable
     * The Extent of current loaded Features in this table
     */
    public Extent getExtent() {
        return this.loadedExtent;
    }
    


    /* ************************************************************************
     * Managing collection listeners.
     * ************************************************************************/
    /** 
     * Adds a listener for table events
     */
    public void addListener(CollectionListener spy) {
        listeners.add(spy);
    }
    
    /** Removes a listener for table events
     */
    public void removeListener(CollectionListener spy) {
        listeners.remove(spy);
    }
    

    /* ************************************************************************
     * Managing features via the datasource.
     * ************************************************************************/
    /** 
     * get the features in the datasource inside the loadedExtent
     * will not trigger a datasourceload.
     * functionally equivalent to getFeatures(getLoadedExtent());
     *
     * @see #getfeatures(Extent ex)
     */
    public Feature[] getFeatures() {
        return (Feature[]) features.toArray(new Feature[features.size()]);
    }
    

    /** 
     * get the features in the datasource inside the Extent ex
     * this may trigger a load on the datasource
     */
    public Feature[] getFeatures(Extent ex) 
        throws DataSourceException {

        // TODO: 2
        // Replace this idiom with a loadedExtent = loadedExtent.or(extent)
        //  idiom.  I think?
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
                System.out.println("loading "+i);
                data.importFeatures(this,toLoad[i]);
                if(loadedExtent==null){
                    loadedExtent = toLoad[i];
                }
                else{
                    loadedExtent = loadedExtent.combine(toLoad[i]);
                }
            }
        }
        System.out.println("calling getfeatures");
        return getFeatures();
    }
    
    

    /** 
     * Removes the features from this FeatureTable which fall into the specified
     * extent, notifying TableChangedListeners that the table has changed
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

    /** 
     * Adds the given List of Features to this FeatureTable
     *
     * @param features The List of Features to add
     */
    public void addFeatures(Feature[] features) {
        this.features.addAll( Arrays.asList(features) );
    }
    
    
}
