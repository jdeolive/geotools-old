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
 * @author Ray Gallagher
 * @author Rob Hranac, TOPP
 * @author Chris Holmes, TOPP
 * @version $Id: DataSource.java,v 1.16 2003/09/04 22:45:58 cholmesny Exp $
 */
public interface DataSource {
    /**
     * Loads features from the datasource into the passed collection, based on
     * the passed query.  Note that all data sources must support this method
     * at a minimum.
     *
     * @param collection The collection to put the features into.
     * @param query a datasource query object.  It encapsulates requested
     *        information, such as typeName, maxFeatures and filter.
     *
     * @throws DataSourceException For all data source errors.
     *
     * @see Query
     */
    void getFeatures(FeatureCollection collection, Query query)
        throws DataSourceException;

    /**
     * Loads features from the datasource into the passed collection, based on
     * the passed filter.
     *
     * @param collection The collection to put the features into.
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     *
     * @throws DataSourceException For all data source errors.
     */
    void getFeatures(FeatureCollection collection, Filter filter)
        throws DataSourceException;

    /**
     * Loads features from the datasource into the returned collection, based
     * on the passed query.
     *
     * @param query a datasource query object.  It encapsulates requested
     *        information, such as typeName, maxFeatures and filter.
     *
     * @return Collection The collection to put the features into.
     *
     * @throws DataSourceException For all data source errors.
     *
     * @see Query
     */
    FeatureCollection getFeatures(Query query) throws DataSourceException;

    /**
     * Loads features from the datasource into the returned collection, based
     * on the passed filter.
     *
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     *        <tt>null</tt> is not allowed, use Filter.NONE instead.
     *
     * @return Collection The collection to put the features into.
     *
     * @throws DataSourceException For all data source errors.
     */
    FeatureCollection getFeatures(Filter filter) throws DataSourceException;

    /**
     * Loads all features from the datasource into the returned collection.
     * Filter.NONE can also be used to get all features.  Calling this
     * function is equivalent to using {@link Query#ALL}
     *
     * @return Collection The collection to put the features into.
     *
     * @throws DataSourceException For all data source errors.
     */
    FeatureCollection getFeatures() throws DataSourceException;

    /**
     * Adds all features from the passed feature collection to the datasource.
     *
     * @param collection The collection from which to add the features.
     *
     * @return the FeatureIds of the newly added features.
     *
     * @throws DataSourceException if anything goes wrong.
     * @throws UnsupportedOperationException if the addFeatures method is not
     *         supported by this datasource.
     */
    Set addFeatures(FeatureCollection collection)
        throws DataSourceException, UnsupportedOperationException;

    /**
     * Removes all of the features specificed by the passed filter from the
     * collection.
     *
     * @param filter An OpenGIS filter; specifies which features to remove.
     *
     * @throws DataSourceException If anything goes wrong.
     * @throws UnsupportedOperationException if the removeFeatures method is
     *         not supported by this datasource.
     */
    void removeFeatures(Filter filter)
        throws DataSourceException, UnsupportedOperationException;

    /**
     * Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     *
     * @throws DataSourceException if the attribute and object arrays are not
     *         eqaul length, if the object types do not match the attribute
     *         types, or if there are backend errors.
     * @throws UnsupportedOperationException if the modifyFeatures method is
     *         not supported by this datasource.
     */
    void modifyFeatures(AttributeType[] type, Object[] value, Filter filter)
        throws DataSourceException, UnsupportedOperationException;

    /**
     * Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.  A convenience
     * method for single attribute modifications.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     *
     * @throws DataSourceException If modificaton is not supported, if the
     *         object type do not match the attribute type.
     * @throws UnsupportedOperationException if the modifyFeatures method is
     *         not supported by this datasource.
     */
    void modifyFeatures(AttributeType type, Object value, Filter filter)
        throws DataSourceException, UnsupportedOperationException;

    /**
     * Deletes the all the current Features of this datasource and adds the new
     * collection.  Primarily used as a convenience method for file
     * datasources.
     *
     * @param collection - the collection to be written
     *
     * @throws DataSourceException if there are any datasource errors.
     * @throws UnsupportedOperationException if the setFeatures method is not
     *         supported by this datasource.
     */
    void setFeatures(FeatureCollection collection)
        throws DataSourceException, UnsupportedOperationException;

    /**
     * Makes all transactions made since the previous commit/rollback
     * permanent.  This method should be used only when auto-commit mode has
     * been disabled.   If autoCommit is true then this method does nothing.
     *
     * @throws DataSourceException if there are any datasource errors.
     *
     * @see #setAutoCommit(boolean)
     */
    void commit() throws DataSourceException;

    /**
     * Undoes all transactions made since the last commit or rollback. This
     * method should be used only when auto-commit mode has been disabled.
     * This method should only be implemented if
     * <tt>setAutoCommit(boolean)</tt>  is also implemented.
     *
     * @throws DataSourceException if there are problems with the datasource.
     * @throws UnsupportedOperationException if the rollback method is not
     *         supported by this datasource.
     *
     * @see #setAutoCommit(boolean)
     */
    void rollback() throws DataSourceException, UnsupportedOperationException;

    /**
     * Sets this datasources auto-commit mode to the given state. If a
     * datasource is in auto-commit mode, then all its add, remove and modify
     * calls will be executed  and committed as individual transactions.
     * Otherwise, those calls are grouped into a single transaction  that is
     * terminated by a call to either the method commit or the method
     * rollback.  By default, new datasources are in auto-commit mode.
     *
     * @param autoCommit <tt>true</tt> to enable auto-commit mode,
     *        <tt>false</tt> to disable it.
     *
     * @throws DataSourceException if there are problems with the datasource.
     * @throws UnsupportedOperationException if rollbacks are not supported by
     *         this datasource.
     *
     * @see #setAutoCommit(boolean)
     */
    void setAutoCommit(boolean autoCommit)
        throws DataSourceException, UnsupportedOperationException;

    /**
     * Retrieves the current autoCommit mode for the current DataSource.  If
     * the datasource does not implement setAutoCommit, then this method
     * should always return true.
     *
     * @return the current state of this datasource's autoCommit mode.
     *
     * @throws DataSourceException if a datasource access error occurs.
     *
     * @see #setAutoCommit(boolean)
     */
    boolean getAutoCommit() throws DataSourceException;

    /**
     * Gets the DatasSourceMetaData object associated with this datasource.
     * This is the preferred way to find out which of the possible datasource
     * interface methods are actually implemented, query the
     * DataSourceMetaData about which methods the datasource supports.
     *
     * @return metadata about this datasource.
     */
    DataSourceMetaData getMetaData();

    /**
     * Retrieves the featureType that features extracted from this datasource
     * will be created with.
     *
     * @return the schema of features created by this datasource.
     *
     * @throws DataSourceException if there are any problems getting the
     *         schema.
     *
     * @task REVISIT: Our current FeatureType model is not yet advanced enough
     *       to handle multiple featureTypes.  Should getSchema take a
     *       typeName now that  a query takes a typeName, and thus DataSources
     *       can now support multiple types? Or just wait until we can
     *       programmatically make powerful enough schemas?
     */
    FeatureType getSchema() throws DataSourceException;

    /**
     * Stops this DataSource from loading.
     *
     * @task REVISIT: this needs serious thought.  See geotools IRC from 5 may,
     *       2003.
     */
    void abortLoading();

    /**
     * Gets the bounding box of this datasource using the default speed of this
     * datasource as set by the implementor.  In order to determine if the
     * datasource is able to return a bounds relatively quickly use {@link
     * DataSourceMetaData.fastBbox()} to check if a quick bounds is possible.
     *
     * @return The bounding box of the datasource or null if unknown and too
     *         expensive for the method to calculate.
     *
     * @throws DataSourceException if there are errors getting the bounding
     *         box.
     */
    Envelope getBounds() throws DataSourceException;
}
