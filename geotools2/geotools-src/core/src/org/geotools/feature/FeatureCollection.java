/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.feature;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.DataSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.Extent;


/**
 * Represents a collection of features.  Previously was tied to a datasource,
 * but we are attempting to move away from that in the interface.
 * Implementations may choose to tie to a datasource, but also may not.
 *
 * @author Ian Turton, CCG<br>
 * @author Rob Hranac, VFNY
 * @version $Id: FeatureCollection.java,v 1.10 2003/07/02 18:33:02 jmacgill Exp $
 *
 * @task REVISIT: get rid of datasource reference, get rid of extents.
 */
public interface FeatureCollection extends java.util.Set {
    /* ***********************************************************************
     * Managing data source and extents.
     * ***********************************************************************/

    /**
     * Gets the DataSource being used by this FeatureCollection.
     *
     * @param data The datasource for this feature collection to mediate.
     *
     * @deprecated FeatureCollections should not contain a reference to the datasource which
     * created them so this method will be removed in the near future.
     * @task REVISIT: remove the datasource reference?  Some feature
     *       collections will have references to their datasource, but not
     *       all, as the loading when needed hasn't really worked.
     */
    void setDataSource(DataSource data);

    /** Gets the DataSource being used by this FeatureCollection.
     * @return Datasource used by this feature collection.
     * @task REVISIT: remove the datasource reference?  Some feature
     *       collections will have references to their datasource, but not
     *       all, as the loading when needed hasn't really worked.
     * @deprecated FeatureCollections should not contain a reference to the datasource which
     * created them so this method will be removed in the near future.
     */
    DataSource getDataSource();

    /**
     * Gets the loaded Extent of this FeatureCollection. The Extent of current
     * loaded Features in this table.
     *
     * @param extent The datasource for this feature collection to mediate.
     *
     * @deprecated setting the bounding box should be done according to
     *             implementation.  Other extents were never really used. Some
     *             implementations will figure out the bounding box based on
     *             the features added, some will allow others to set it.  But
     *             it should not be part of the public interface.
     */
    void setExtent(Extent extent);

    /**
     * Gets the loaded Extent of this FeatureCollection. The Extent of current
     * loaded Features in this table.
     *
     * @return Datasource used by this feature collection.
     *
     * @deprecated use {@link #getBoundingBox} instead.  The only extent ever
     *             used was an envelope extent, so this makes it more clear.
     *             It also fits with gml, FeatureCollections print their
     *             extent.
     */
    Extent getExtent();

    /**
     * Gets the bounding box for the features in this feature collection.
     *
     * @return the envelope of the geometries contained by this feature
     *         collection.
     */
    Envelope getBoundingBox();

    /* ***********************************************************************
     * Managing features via the datasource.
     * ***********************************************************************/

    /** Gets the features in the datasource inside the loadedExtent.  Will not
     * trigger a datasourceload.  Functionally equivalent to
     * getFeatures(getLoadedExtent());
     * @see #getFeatures(Extent ex)
     * @deprecated FeatureCollection now extends Set and this mechanism for obtaining an array of
     * features is no longer supported.
     * Either obtain an itterator from the FeatureCollection or use the toArray method
     * instead.
     */
    Feature[] getFeatures();

    /**
     * Gets the features in the datasource inside the Extent ex. This may
     * trigger a load on the datasource.
     *
     * @param boundary The extent in which to load features
     *
     * @return An array of all the features that fall within the boundary
     *
     * @throws DataSourceException if anything went wrong during the fetching
     *         or construction of the requested features
     *
     * @task REVISIT: given that this may trigger a load, would fetchFeatures
     *       be a more suitable name?
     * @task REVISIT: change to getFeatures(Envelope)?  We want to get rid of
     *       extents, not trigger a load for the interface.
     */
    Feature[] getFeatures(Extent boundary) throws DataSourceException;

    /**
     * Removes the features from this FeatureCollection, notifying
     * CollectionListeners that the table has changed.
     *
     * @param features The Features to remove
     */
    void removeFeatures(Feature[] features);

    /**
     * Removes the features from this FeatureCollection which fall into the
     * specified extent, notifying CollectionListeners that the collection has
     * changed.
     *
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
     *
     * @param spy The listener to add
     */
    void addListener(CollectionListener spy);

    /**
     * Removes a listener for table events.
     *
     * @param spy The listener to remove
     */
    void removeListener(CollectionListener spy);
}
