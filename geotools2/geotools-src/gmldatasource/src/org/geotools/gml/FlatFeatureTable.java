/*
 * DefaultFeatureTable.java
 *
 * Created on March 14, 2002, 2:09 PM
 */

package org.geotools.gml;

import java.util.*;

import org.geotools.datasource.*;
import org.geotools.datasource.extents.*;
import org.geotools.featuretable.*;


/**
 * <p>Describes a flat feature table, which is a simplification of the allowed
 * GML feature collections.  We define a <code>FlatFeatureTable</code> as 
 * feature collection with the following properties:<ul>
 * <li>Each Feature in the collection is a FlatFeature.
 * <li>Features are homogeneous (ie. a FlatFeatureTable can only store a single
 * feature type.</ul>
 *
 * @author Rob Hranac, Vision for New York
 */
public class FlatFeatureTable implements FeatureTable {


    /** Gives the bounding box for this FeatureTable */
    private DataSource dataSource;

    /** Gives the bounding box for this FeatureTable */
    private EnvelopeExtent boundedBy = new EnvelopeExtent();

    /** A list of the features in this feature table */
    private String name = new String();

    /** A list of the features in this feature table */
    private String description = new String();
    
    /** Data about the features in this table */
    //private FlatFeatureMetadata featureMetadata;
    
    /** A list of the features in this feature table */
    private Vector features = new Vector();
    

    /** 
		 * Sets the data source for this feature table.
     */
    public FlatFeatureTable(DataSource ds) {
				this.dataSource = ds;
		}
    

    /** 
		 * Sets the data source for this feature table.
     */
    public void setDataSource(DataSource ds) {
				this.dataSource = ds;
		}
    

    /** 
		 * Gets the current loaded extent of this feature table.  
     */
    public Extent getLoadedExtent() {
				
				return boundedBy;
		}
    

    /** 
		 * 
		 *
     * @see #getfeatures(Extent ex)
     */
    public Feature[] getFeatures() {
				
				try {
						dataSource.importFeatures(this, boundedBy);
				} catch (DataSourceException e) {
				}

				// convert features to array for return
				// HACK ALERT - I KNOW THERE IS A BETTER WAY TO DO THIS; FIX
				Feature[] typedFeatures = new Feature[features.size()];
				for ( int i = 0; i < features.size() ; i++ )
						typedFeatures[i] = (Feature) features.get(i);


				return typedFeatures;
		}
    

    /** 
		 * get the features in the datasource inside the Extent ex
     * this may trigger a load on the datasource
     */
    public Feature[] getFeatures(Extent ex)
				throws DataSourceException {

				dataSource.importFeatures(this, ex);
				return (Feature[]) features.toArray();
		}
    

    /** Adds a listener for table events
     */
    public void addTableChangedListener(TableChangedListener fel) {
		}
    
    /** Removes a listener for table events
     */
    public void removeTableChangedListener(TableChangedListener fel) {
		}
    

    /** Gets the DataSource being used by this FeatureTable
     */
    public DataSource getDataSource() {
				return dataSource;
		}
    

    /** Removes the feature from this FeatureTable, notifying TableChangedListeners that the table has changed
     * @param feature The Feature to remove
     */
    public void removeFeature(Feature feature) {
		}


    /** Removes the features from this FeatureTable, notifying TableChangedListeners that the table has changed
     * @param f The Features to remove
     */
    public void removeFeatures(Feature[] features) {
		}
    

    /** Removes the features from this FeatureTable which fall into the specified extent, notifying TableChangedListeners that the table has changed
     * @param ex The extent defining which features to remove
     */
    public void removeFeatures(Extent ex) {
		}

    
    /** Adds the given List of Features to this FeatureTable, notifying TableChangedListeners that the table has changed
     * @param features The List of Features to add
     */
    public void addFeature(Feature feature) {
		}
    

    /** Adds the given List of Features to this FeatureTable
     * @param features The List of Features to add
     */
    public void addFeatures(Feature[] features) {
				
				this.features = new Vector( java.util.Arrays.asList(features) );
		}
    
		/*
		private class DynamicFeature () {

				private FlatFeature feature;
				private Listener listener;

				public DynamicFeature( feature ) {
						this.feature = feature;
						this.listener = null;
				}

		}
		*/
}
