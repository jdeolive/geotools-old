/*
 * MemoryDataSource.java
 *
 * Created on May 2, 2002, 3:20 PM
 */

package org.geotools.data;

/**
 * A memory based datasource
 * @author James
 * @author  iant
 * $Id: MemoryDataSource.java,v 1.1 2002/05/15 15:45:45 jmacgill Exp $
 */

import org.geotools.data.*;
import org.geotools.feature.*;
import org.geotools.datasource.extents.EnvelopeExtent;

public class MemoryDataSource implements DataSource {
    EnvelopeExtent bbox = new EnvelopeExtent();
    /** Creates a new instance of MemoryDataSource */
    public MemoryDataSource() {
    }
    
    /** the feature store */
    private java.util.Vector features = new java.util.Vector();
    
    /** Stops this DataSource from loading
     */
    public void stopLoading() {
        //do nothing
    }
    
    /** Loads Feature rows for the given Extent from the datasource
     * @param ft featureTable to load features into
     * @param ex an extent defining which features to load - null means all features
     * @throws DataSourceException if anything goes wrong
     */
    public void importFeatures(FeatureCollection ft, Extent ex) throws DataSourceException {
        for(int i=0;i<features.size();i++){
            Feature f = (Feature)features.elementAt(i);
            if(ex.containsFeature(f)){
                ft.addFeatures(new Feature[]{f});
            }
        }
    }
    
    /** Saves the given features to the datasource
     * @param ft feature table to get features from
     * @param ex extent to define which features to write - null means all
     * @throws DataSourceException if anything goes wrong or if exporting is not supported
     */
    public void exportFeatures(FeatureCollection ft, Extent ex) throws DataSourceException {
        //do nothing
    }
    
    public void addFeature(Feature f){
        features.addElement(f);
        bbox.combine(new EnvelopeExtent(f.getDefaultGeometry().getEnvelopeInternal()));
    }
    
    /** gets the extent of this data source using the speed of
     * this datasource as set by the parameter.
     * @param quick if true then a quick (and possibly dirty) estimate of
     * the extent is returned. If false then a slow but acurate extent
     * will be returned
     * @return the extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Extent getExtent(boolean speed) {
        return getExtent();
    }
    
    /** gets the extent of this data source using the quick method
     * of knowing what the bounding box is.
     * @return the extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Extent getExtent() {
        return bbox;
    }
    
}
