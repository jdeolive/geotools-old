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

import com.vividsolutions.jts.geom.Envelope;

import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;

import org.geotools.filter.Filter;

import java.util.Set;


/**
 * The source of data for Features. Shapefiles, databases, etc. are referenced
 * through this interface.
 *
 * @version $Id: DataSource.java,v 1.8 2003/04/14 21:36:32 jmacgill Exp $
 * @author Ray Gallagher
 * @author Rob Hranac, TOPP
 * @author Chris Holmes, TOPP
 */
public interface DataSource {
    /**************************************************
     * Feature retrieval methods.                     *
     **************************************************/
    /**
     * Loads features from the datasource into the passed collection, based on
     * the passed filter.  Note that all data sources must support this method
     * at a minimum.
     *
     * @param collection The collection to put the features into.
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @throws DataSourceException For all data source errors.
     */
    void getFeatures(FeatureCollection collection, Filter filter)
        throws DataSourceException;

    /**
     * Loads features from the datasource into the returned collection, based on
     * the passed filter.
     *
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @return Collection The collection to put the features into.
     * @throws DataSourceException For all data source errors.
     */
    FeatureCollection getFeatures(Filter filter) throws DataSourceException;

    /**************************************************
     * Data source modification methods.              *
     **************************************************/
    /**
     * Adds all features from the passed feature collection to the datasource.
     *
     * @param collection The collection from which to add the features.
     * @return the FeatureIds of the newly added features.
     * @throws DataSourceException If anything goes wrong or if exporting is
     * not supported.
     */
    Set addFeatures(FeatureCollection collection) throws DataSourceException;

    /**
     * Removes all of the features specificed by the passed filter from the
     * collection.
     *
     * @param filter An OpenGIS filter; specifies which features to remove.
     * @throws DataSourceException If anything goes wrong or if deleting is
     * not supported.
     */
    void removeFeatures(Filter filter) throws DataSourceException;

    /**
     * Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     * @throws DataSourceException If modificaton is not supported, if
     * the attribute and object arrays are not eqaul length, or if the object
     * types do not match the attribute types.
     */
    void modifyFeatures(AttributeType[] type, Object[] value, Filter filter)
        throws DataSourceException;

    /**
     * Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.  A convenience
     * method for single attribute modifications.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     * @throws DataSourceException If modificaton is not supported, if
     * the object type do not match the attribute type.
     */
    void modifyFeatures(AttributeType type, Object value, Filter filter)
        throws DataSourceException;

    /**
     * Deletes the all the current Features of this datasource and adds the
     * new collection.  Primarily used as a convenience method for file
     * datasources.
     * @param collection - the collection to be written
     */
    void setFeatures(FeatureCollection collection) throws DataSourceException;

    /**
     * Begins a transaction(add, remove or modify) that does not commit as
     * each modification call is made.  If an error occurs during a transaction
     * after this method has been called then the datasource should rollback:
     * none of the transactions performed after this method was called should
     * go through.
     */
    void startMultiTransaction() throws DataSourceException;

    /**
     * Ends a transaction after startMultiTransaction has been called.  Similar
     * to a commit call in sql, it finalizes all of the transactions called
     * after a startMultiTransaction.
     */
    void endMultiTransaction() throws DataSourceException;

    /**************************************************
      Data source utility methods.
     **************************************************/
    /**
     * Gets the DatasSourceMetaData object associated with this datasource.
     * This is the preferred way to find out which of the possible datasource
     * interface methods are actually implemented, query the DataSourceMetaData
     * about which methods the datasource supports.
     */
    DataSourceMetaData getMetaData();

    /**
     * Retrieves the featureType that features extracted from this datasource
     * will be created with.
     */
    FeatureType getSchema();

    /**
     * Sets the schema that features extrated from this datasource will be
     * created with.  This allows the user to obtain the attributes he wants,
     * by calling getSchema and then creating a new schema using the
     * attributeTypes from the currently used schema.
     * @param schema the new schema to be used to create features.
     */
    void setSchema(FeatureType schema) throws DataSourceException;

    /**
     * Stops this DataSource from loading.
     */
    void abortLoading();

    /**
     * Gets the bounding box of this datasource using the default speed of
     * this datasource as set by the implementer.
     *
     * @return The bounding box of the datasource or null if unknown and too
     * expensive for the method to calculate.
     * @task REVISIT: Consider changing return of getBbox to Filter once Filters
     * can be unpacked
     */
    Envelope getBbox();

    /**
     * Gets the bounding box of this datasource using the speed of
     * this datasource as set by the parameter.
     *
     * @param speed If true then a quick (and possibly dirty) estimate of
     * the extent is returned. If false then a slow but accurate extent
     * will be returned
     * @return The extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     * @task REVISIT:Consider changing return of getBbox to Filter once Filters can be unpacked
     */
    Envelope getBbox(boolean speed);
}
