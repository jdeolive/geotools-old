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

package org.geotools.feature;

import org.geotools.data.Extent;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataSource;

/**
 * @version $Id: FeatureCollection.java,v 1.7 2003/04/11 22:44:00 ianschneider Exp $
 * @author  Ian Turton, CCG<br>
 * @author  Rob Hranac, VFNY
 */
public interface FeatureCollection extends java.util.Set {

    /* ***********************************************************************
     * Managing data source and extents.
     * ***********************************************************************/
    /** 
     * Gets the DataSource being used by this FeatureCollection.
     * 
     * @param data The datasource for this feature collection to mediate.
     */
    void setDataSource(DataSource data);

    /** 
     * Gets the DataSource being used by this FeatureCollection.
     * 
     * @return Datasource used by this feature collection.
     */
    DataSource getDataSource();

    /** 
     * Gets the loaded Extent of this FeatureCollection.
     * The Extent of current loaded Features in this table.
     *
     * @param extent The datasource for this feature collection to mediate.
     */
    void setExtent(Extent extent);

    /** 
     * Gets the loaded Extent of this FeatureCollection.
     * The Extent of current loaded Features in this table.
     *
     * @return Datasource used by this feature collection.
     */
    Extent getExtent();


    /* ***********************************************************************
     * Managing features via the datasource.
     * ***********************************************************************/
    /** 
     * Gets the features in the datasource inside the loadedExtent.  Will not 
     * trigger a datasourceload.  Functionally equivalent to
     * getFeatures(getLoadedExtent());
     *
     * @see #getFeatures(Extent ex)
     */
    Feature[] getFeatures();
    
    /** 
     * Gets the features in the datasource inside the Extent ex.
     * This may trigger a load on the datasource.
     *
     * @task REVISIT: given that this may trigger a load, would fetchFeatures be a
     *                more suitable name?
     *
     * @param boundary The extent in which to load features
     * @return An array of all the features that fall within the boundary
     * @throws DataSourceException if anything went wrong during the fetching
     *         or construction of the requested features
     */
    Feature[] getFeatures(Extent boundary) 
        throws DataSourceException;
    
    /** 
     * Removes the features from this FeatureCollection, notifying 
     * CollectionListeners that the table has changed.
     * @param features The Features to remove
     */
    void removeFeatures(Feature[] features);

    /** 
     * Removes the features from this FeatureCollection which fall into the
     * specified extent, notifying CollectionListeners that the collection 
     * has changed.
     * @param extent The extent defining which features to remove
     */
    void removeFeatures(Extent extent);

    /** 
     * Adds the given List of Features to this FeatureCollection.
     *
     * @param features The List of Features to add
     */
    void addFeatures(Feature[] features);
    
   
    /* ***********************************************************************
     * Managing collection listeners.
     * ***********************************************************************/
    /** 
     * Adds a listener for table events.
     * @param spy The listener to add
     */
    void addListener(CollectionListener spy);
    
    /** 
     * Removes a listener for table events.
     * @param spy The listener to remove
     */
    void removeListener(CollectionListener spy);
    
    
}
