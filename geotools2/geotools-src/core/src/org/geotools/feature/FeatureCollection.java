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
 * @version $Id: FeatureCollection.java,v 1.3 2002/06/04 14:59:25 loxnard Exp $
 * @author  Ian Turton, CCG<br>
 * @author  Rob Hranac, VFNY
 */
public interface FeatureCollection {

    /* ***********************************************************************
     * Managing data source and extents.
     * ***********************************************************************/
    /** 
     * Gets the DataSource being used by this FeatureTable.
     * 
     * @param data The datasource for this feature collection to mediate.
     */
    public void setDataSource(DataSource data);

    /** 
     * Gets the DataSource being used by this FeatureTable.
     * 
     * @return Datasource used by this feature collection.
     */
    public DataSource getDataSource();

    /** 
     * Gets the loaded Extent of this FeatureTable.
     * The Extent of current loaded Features in this table.
     *
     * @param extent The datasource for this feature collection to mediate.
     */
    public void setExtent(Extent extent);

    /** 
     * Gets the loaded Extent of this FeatureTable.
     * The Extent of current loaded Features in this table.
     *
     * @return Datasource used by this feature collection.
     */
    public Extent getExtent();


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
    public Feature[] getFeatures();
    
    /** 
     * Gets the features in the datasource inside the Extent ex.
     * This may trigger a load on the datasource.
     *
     * TODO: given that this may trigger a load, would fetchFeatures be a
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
     * Adds the given List of Features to this FeatureTable.
     *
     * @param features The List of Features to add
     */
    public void addFeatures(Feature[] features);
    
   
    /* ***********************************************************************
     * Managing collection listeners.
     * ***********************************************************************/
    /** 
     * Adds a listener for table events.
     * @param spy The listener to add
     */
    public void addListener(CollectionListener spy);
    
    /** 
     * Removes a listener for table events.
     * @param spy The listener to remove
     */
    public void removeListener(CollectionListener spy);
    
    
}
