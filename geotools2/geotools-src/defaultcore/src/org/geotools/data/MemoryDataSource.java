/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.geotools.data;

/**
 * A memory-based datasource.
 *
 * @version $Id: MemoryDataSource.java,v 1.3 2002/07/11 16:33:34 loxnard Exp $
 * @author James Macgill, CCG
 * @author Ian Turton, CCG
 */

import org.geotools.feature.*;
import org.geotools.datasource.extents.EnvelopeExtent;

/**
 * A special datasource implementation which provides access to
 * features stored in memory.  Unlike other datasources, it is not
 * connected to any external resource.  Instead, all of the features 
 * it provides are stored internally.
 * It is very useful for testing and development when a datasource
 * with only a limited number of features is required.  It may also be
 * adapted in future to act as a cache for other datasources.
 */
public class MemoryDataSource implements DataSource {
    EnvelopeExtent bbox = new EnvelopeExtent();
    /**
     * Creates a new instance of MemoryDataSource.
     */
    public MemoryDataSource() {
    }
    
    /**
     * The feature store.
     */
    private java.util.Vector features = new java.util.Vector();
    
    /**
     * Stops this DataSource from loading.
     */
    public void stopLoading() {
        //do nothing
    }
    
    /**
     * Loads Feature rows for the given Extent from the datasource.
     * @param ft featureTable to load features into
     * @param ex an extent defining which features to load - null means all
     * @throws DataSourceException if anything goes wrong
     */
    public void importFeatures(FeatureCollection ft, Extent ex) throws DataSourceException {
        for (int i = 0; i < features.size(); i++){
            Feature f = (Feature) features.elementAt(i);
            if (ex.containsFeature(f)){
                ft.addFeatures(new Feature[]{f});
            }
        }
    }
    
    /**
     * Saves the given features to the datasource.
     * @param ft feature table to get features from
     * @param ex an extent defining which features to write - null means all
     * @throws DataSourceException if anything goes wrong or if exporting is
     * not supported.
     */
    public void exportFeatures(FeatureCollection ft, Extent ex) throws DataSourceException {
        //do nothing
    }
    
    /**
     * Adds a new feature to the list of those stored within the datasource.
     * The default geometry of the feature will be used to extend the bounding
     * box of the datasource.
     *
     * @param f The feature to add 
     */
    public void addFeature(Feature f){
        features.addElement(f);
        bbox.combine(new EnvelopeExtent(f.getDefaultGeometry().getEnvelopeInternal()));
    }
    
    /**
     * Gets the extent of this datasource using the speed of
     * this datasource as set by the parameter.
     * @param speed if true then a quick (and possibly dirty) estimate of
     * the extent is returned. If false then a slow but accurate extent
     * will be returned.
     * @return the extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Extent getExtent(boolean speed) {
        return getExtent();
    }
    
    /**
     * Gets the extent of this data source using the quick method
     * of knowing what the bounding box is.
     * @return the extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Extent getExtent() {
        return bbox;
    }
    
}
