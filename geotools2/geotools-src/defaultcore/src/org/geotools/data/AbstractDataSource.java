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
import org.geotools.feature.FeatureCollectionDefault;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;
import java.util.Set;


/**
 * This class provides a skeletal implementation of the DataSource interface to
 * minimize the effort of required to implement this interface.
 * 
 * <p>
 * To implement a basic datasource, the programmer need only extend this class
 * and provide implementations for the <tt>getFeatures(FeatureCollection
 * collection, Query query)</tt> and <tt>getSchema()</tt> methods.
 * </p>
 * 
 * <p>
 * To implement additional datasource capabilities, such as transactional,
 * rollbacks, abort or Bbox, the programmer must override the appropriate
 * methods (which otherwise throws an UnsupportedOperationException).
 * </p>
 * 
 * <p>
 * In addition to overriding the method, the programmer must also set the
 * metadata correctly.  This can be easily done by setting the field of the
 * AbstractDataSource.MetaDataSupport object before returning it in
 * <tt>createMetaData</tt>
 * </p>
 *
 * @author Chris Holmes, TOPP
 * @version $Id: AbstractDataSource.java,v 1.5 2003/05/19 17:17:14 ianschneider Exp $
 */
public abstract class AbstractDataSource implements DataSource {
    /** the meta data object containing information about this datasource. */
    private DataSourceMetaData metaData;

    /**
     * Loads features from the datasource into the passed collection, based on
     * the passed query.  Note that all data sources must support this method
     * at a minimum.
     *
     * @param collection The collection to put the features into.
     * @param query a datasource query object.  It encapsulates requested
     *        information, such as tableName, maxFeatures and filter.
     *
     * @throws DataSourceException For all data source errors.
     */
    public abstract void getFeatures(FeatureCollection collection, Query query)
        throws DataSourceException;

    /**
     * Loads features from the datasource into the passed collection, based on
     * the passed filter.  Note that all data sources must support this method
     * at a minimum.
     *
     * @param collection The collection to put the features into.
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     *        To get all features use {@link Filter.ALL}
     *
     * @throws DataSourceException For all data source errors.
     */
    public void getFeatures(FeatureCollection collection, Filter filter)
        throws DataSourceException {
        Query query = makeDefaultQuery(filter);
        getFeatures(collection, query);
    }

    /**
     * Creates a query with just the filter.
     *
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     *        To get all features use {@link Filter.ALL}
     *
     * @return A default query with the filter passed in.
     */
    protected Query makeDefaultQuery(Filter filter) {
        return new QueryImpl(filter);
    }

    /**
     * Loads features from the datasource into the returned collection, based
     * on the passed query.
     *
     * @param query a datasource query object.  It encapsulates requested
     *        information, such as tableName, maxFeatures and filter.
     *
     * @return Collection The collection to put the features into.
     *
     * @throws DataSourceException For all data source errors.
     */
    public FeatureCollection getFeatures(Query query)
        throws DataSourceException {
        FeatureCollection collection = new FeatureCollectionDefault();
        getFeatures(collection, query);

        return collection;
    }

    /**
     * Loads features from the datasource into the returned collection, based
     * on the passed filter.
     *
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     *
     * @return Collection The collection to put the features into.
     *
     * @throws DataSourceException For all data source errors.
     */
    public FeatureCollection getFeatures(Filter filter)
        throws DataSourceException {
        FeatureCollection collection = new FeatureCollectionDefault();
        getFeatures(collection, filter);

        return collection;
    }

    /**
     * Loads all features from the datasource into the returned collection.
     * Filter.NONE can also be used to get all features.  Calling this
     * function is equivalent to using {@link Query.ALL}
     *
     * @return Collection The collection to put the features into.
     *
     * @throws DataSourceException For all data source errors.
     */
    public FeatureCollection getFeatures() throws DataSourceException {
        return getFeatures(Query.ALL);
    }

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
    public Set addFeatures(FeatureCollection collection)
        throws DataSourceException, UnsupportedOperationException {
        if (!getMetaData().supportsAdd()) {
            throw new UnsupportedOperationException("This datasource does not" +
                "support addFeatures");
        }

        return null;
    }

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
    public void removeFeatures(Filter filter)
        throws DataSourceException, UnsupportedOperationException {
        if (!getMetaData().supportsRemove()) {
            throw new UnsupportedOperationException("This datasource does not" +
                " support removeFeatures");
        }
    }

    /**
     * Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     *
     * @throws DataSourceException If modificaton is not supported, if the
     *         attribute and object arrays are not eqaul length, or if the
     *         object types do not match the attribute types.
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    public void modifyFeatures(AttributeType[] type, Object[] value,
        Filter filter)
        throws DataSourceException, UnsupportedOperationException {
        if (!getMetaData().supportsModify()) {
            throw new UnsupportedOperationException("This datasource does not" +
                " support modifyFeatures");
        }
    }

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
     * @throws UnsupportedOperationException if the addFeatures method is not
     *         supported by this datasource.
     */
    public void modifyFeatures(AttributeType type, Object value, Filter filter)
        throws DataSourceException, UnsupportedOperationException {
        AttributeType[] singleType = { type };
        Object[] singleVal = { value };
        modifyFeatures(singleType, singleVal, filter);
    }

    /**
     * Deletes the all the current Features of this datasource and adds the new
     * collection.  Primarily used as a convenience method for file
     * datasources.
     *
     * @param collection - the collection to be written
     *
     * @throws DataSourceException DOCUMENT ME!
     * @throws UnsupportedOperationException if the setFeatures method is not
     *         supported by this datasource.
     */
    public void setFeatures(FeatureCollection collection)
        throws DataSourceException, UnsupportedOperationException {
        if (!getMetaData().supportsSetFeatures()) {
            throw new UnsupportedOperationException("This datasource does not" +
                " support setFeatures");
        }
    }

    /**
     * Makes all transactions made since the previous commit/rollback
     * permanent.  This method should be used only when auto-commit mode has
     * been disabled.   If autoCommit is true then this method does nothing.
     *
     * @throws DataSourceException if there are any datasource errors.
     *
     * @see #setAutoCommit(boolean)
     */
    public void commit() throws DataSourceException {
        //Does nothing, as default datasource is in auto commit mode, it
        //commits after every transaction.
    }

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
    public void rollback()
        throws DataSourceException, UnsupportedOperationException {
        if (!getMetaData().supportsRollbacks()) {
            throw new UnsupportedOperationException("This datasource does not" +
                " support rollbacks");
        }
    }

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
     * @throws DataSourceException DOCUMENT ME!
     * @throws UnsupportedOperationException DOCUMENT ME!
     *
     * @see #setAutoCommit(boolean)
     */
    public void setAutoCommit(boolean autoCommit)
        throws DataSourceException, UnsupportedOperationException {
        if (!getMetaData().supportsRollbacks()) {
            throw new UnsupportedOperationException("This datasource does not" +
                " support rollbacks");
        }
    }

    /**
     * Retrieves the current autoCommit mode for the current DataSource.  If
     * the datasource does not implement setAutoCommit, then this method
     * should always return true.
     *
     * @return <tt>true</tt>, as datasources are autoCommit by default.  If
     *         setAutoCommit is implemented then this method should be
     *         overridden.
     *
     * @throws DataSourceException if a datasource access error occurs.
     *
     * @see #setAutoCommit(boolean)
     */
    public boolean getAutoCommit() throws DataSourceException {
        return true;
    }

    /**
     * Gets the DatasSourceMetaData object associated with this datasource.
     * This is the preferred way to find out which of the possible datasource
     * interface methods are actually implemented, query the
     * DataSourceMetaData about which methods the datasource supports.
     *
     * @return metadata about this datasource.
     */
    public final DataSourceMetaData getMetaData() {
        if (metaData == null) {
            metaData = createMetaData();
        }

        return metaData;
    }

    /**
     * Creates the a metaData object.  This method should be overridden in any
     * subclass implementing any functions beyond getFeatures, so that clients
     * recieve the proper information about the datasource's capabilities.
     * 
     * <p></p>
     *
     * @return the metadata for this datasource.
     *
     * @see #MetaDataSupport
     */
    protected DataSourceMetaData createMetaData() {
        return new MetaDataSupport();
    }

    /**
     * Retrieves the featureType that features extracted from this datasource
     * will be created with.
     *
     * @return the schema of features created by this datasource.
     *
     * @task REVISIT: Our current FeatureType model is not yet advanced enough
     *       to handle multiple featureTypes.  Should getSchema take a
     *       typeName now that  a query takes a typeName, and thus DataSources
     *       can now support multiple types? Or just wait until we can
     *       programmatically make powerful enough schemas?
     */
    public abstract FeatureType getSchema() throws DataSourceException;

    /**
     * Stops this DataSource from loading.
     *
     * @throws UnsupportedOperationException always, as it's not yet supported.
     *
     * @task REVISIT: this needs serious thought.  See geotools IRC from 5 may,
     *       2003.
     */
    public void abortLoading() throws UnsupportedOperationException {
        if (!getMetaData().supportsAbort()) {
            throw new UnsupportedOperationException("This datasource does not" +
                " support abortLoading");
        }
    }

    /**
     * Gets the bounding box of this datasource using the default speed of this
     * datasource as set by the implementer.
     *
     * @return The bounding box of the datasource or null if unknown and too
     *         expensive for the method to calculate.
     *
     * @throws DataSourceException DOCUMENT ME!
     * @throws UnsupportedOperationException DOCUMENT ME!
     *
     * @task REVISIT: Consider changing return of getBbox to Filter once
     *       Filters can be unpacked
     */
    public Envelope getBbox() throws DataSourceException {
        if (!getMetaData().supportsGetBbox()) {
            throw new UnsupportedOperationException("This datasource does not" +
                " support getBbox");
        }

        return null;
    }

    /**
     * Gets the bounding box of this datasource using the speed of this
     * datasource as set by the parameter.
     *
     * @param speed If true then a quick (and possibly dirty) estimate of the
     *        extent is returned. If false then a slow but
     *        accurate extent                will be returned
     *
     * @return The extent of the datasource or null if unknown and too
     *         expensive for the method to calculate.
     *
     * @throws RuntimeException DOCUMENT ME!
     *
     * @task REVISIT:Consider changing return of getBbox to Filter once Filters
     *       can be unpacked
     * @deprecated users can use <tt>DataSourceMetaData.hasFastBbox()</tt> to
     *             check if the loading of the bounding box
     *             will take a long                     time.
     * @task TODO: remove this.
     */
    public Envelope getBbox(boolean speed) {
        try {
            return getBbox();
        } catch (DataSourceException e) {
            throw new RuntimeException(
                "Error in getBbox.  This method should not" +
                " be used any more, use getBbox()");
        }
    }

     /**
     * Sets the schema that features extrated from this datasource will be
     * created with.  This allows the user to obtain the attributes he wants,
     * by calling getSchema and then creating a new schema using the
     * attributeTypes from the currently used schema.
     *
     * @param schema the new schema to be used to create features.
     *
     * @throws DataSourceException DOCUMENT ME!
     *
     * @deprecated Use the properties of the query object to accomplish the
     *             same functionality.
     * @task TODO: remove this.  I'm not sure how to do the deprecation right.
     */
    public void setSchema(FeatureType schema) throws DataSourceException {
    }

    /**
     * This support class allows the subclasses of AbstractDataSource to easily
     * implement the appropriate metadata object.  The return of correct
     * metadata for a new datasource implementation is vitally important, as
     * it allows client applications to plug in the new datasource and use all
     * the available  functionality.
     * 
     * <p>
     * When implementing a new datasource that supports operations that
     * AbstractDataSource does not, the programmer should simply override the
     * createMetaData method to  make a new MetaDataSupport and then use the
     * setter methods to put the correct metadata in.  Only those that should
     * be set to true need be added, as the  AbstractDataSource defaults all
     * to false.
     * </p>
     *
     * @author Chris Holmes, TOPP
     * @author Ian Schneider
     */
    protected static final class MetaDataSupport implements DataSourceMetaData {
        private boolean supportsAdd = false;
        private boolean supportsRemove = false;
        private boolean supportsModify = false;
        private boolean supportsRollbacks = false;
        private boolean supportsSet = false;
        private boolean supportsAbort = false;
        private boolean supportsGetBbox = false;
        private boolean hasFastBbox = false;

        /**
         * No argument constructor.
         */
        public MetaDataSupport() {
        }

        /**
         * Retrieves whether this datasource supports addFeatures.
         *
         * @return <tt>true</tt> the addFeatures method is supported,
         *         <tt>false</tt> otherwise.
         */
        public boolean supportsAdd() {
            return supportsAdd;
        }

        /**
         * Sets whether this datasource supports addFeatures.
         *
         * @param support if this operation should be supported.
         */
        public void setSupportsAdd(boolean support) {
            this.supportsAdd = support;
        }

        /**
         * Retrieves whether this datasource supports removeFeatures.
         *
         * @return <tt>true</tt> the removeFeatures method is supported,
         *         <tt>false</tt> otherwise.
         */
        public boolean supportsRemove() {
            return supportsRemove;
        }

        /**
         * Sets whether this datasource supports removeFeatures.
         *
         * @param support if this operation should be supported.
         */
        public void setSupportsRemove(boolean support) {
            this.supportsRemove = support;
        }

        /**
         * Retrieves whether this datasource supports removeFeatures.
         *
         * @return <tt>true</tt> the modifyFeatures method is supported,
         *         <tt>false</tt> otherwise.
         */
        public boolean supportsModify() {
            return supportsModify;
        }

        /**
         * Sets whether this datasource supports modifyFeatures.
         *
         * @param support if this operation should be supported.
         */
        public void setSupportsModify(boolean support) {
            this.supportsModify = support;
        }

        /**
         * Retrieves whether this datasource implements the
         * setAutoCommit(boolean) and rollback() methods of the DataSource
         * Interface.
         *
         * @return <tt>true</tt> if the rollback methods are supported,
         *         <tt>false</tt> otherwise.
         *
         * @see DataSource#setAutoCommit(boolean)
         * @see DataSource#rollback()
         */
        public boolean supportsRollbacks() {
            return supportsRollbacks;
        }

        /**
         * Sets whether this datasource supports rollbacks.
         *
         * @param support if this operation should be supported.
         */
        public void setSupportsRollbacks(boolean support) {
            this.supportsRollbacks = support;
        }

        /**
         * Retrieves whether the datasource supports the {@link
         * DataSource#setFeatures(FeatureCollection) setFeatures} operation.
         * operation.
         *
         * @return <tt>true</tt> if the setFeatures method is supported,
         *         <tt>false</tt> otherwise.
         */
        public boolean supportsSetFeatures() {
            return supportsSet;
        }

        /**
         * Sets whether this datasource supports setFeatures.
         *
         * @param support if this operation should be supported.
         */
        public void setSupportsSetFeatures(boolean support) {
            this.supportsSet = support;
        }

        /**
         * Retrieves whether this datasource supports the {@link
         * DataSource#abortLoading() abortLoading} operation.
         *
         * @return <tt>true</tt> if the abortLoading method is supported,
         *         <tt>false</tt> otherwise.
         */
        public boolean supportsAbort() {
            return supportsAbort;
        }

        /**
         * Sets whether this datasource supports abort.
         *
         * @param support if this operation should be supported.
         */
        public void setSupportsAbort(boolean support) {
            this.supportsAbort = support;
        }

        /**
         * Retrieves whether this datasource returns meaningful results when
         * getBBox is called.
         *
         * @return <tt>true</tt> if the getBbox method is supported,
         *         <tt>false</tt> otherwise.
         */
        public boolean supportsGetBbox() {
            return supportsGetBbox;
        }

        /**
         * Sets whether this datasource supports getBbox.
         *
         * @param support if this operation should be supported.
         */
        public void setSupportsGetBbox(boolean support) {
            this.supportsGetBbox = support;
        }

        /**
         * Retrieves whether the getBbox operation of the datasource will
         * return relatively quickly.  Programmers who care about the speed of
         * calculating the bounding box should query this method before
         * calling getBbox.
         *
         * @return <tt>true</tt> if a getBbox call will return quickly,
         *         <tt>false</tt> otherwise.
         */
        public boolean hasFastBbox() {
            return hasFastBbox;
        }

        /*
         * Sets whether this datasource has a fast bbox.
         */
        public void setFastBbox(boolean fast) {
            this.hasFastBbox = fast;
        }

        /**
         * Override of toString.  Prints a string representation of this
         * object.
         *
         * @return a string representation of the metadata.
         */
        public String toString() {
            return "supportsAdd: " + supportsAdd + "\n" + "supportsRemove: " +
            supportsRemove + "\n" + "supportsModify: " + supportsModify + "\n" +
            "supportsSetFeatures: " + supportsSet + "\n" +
            "supportsRollbacks: " + supportsRollbacks + "\n" +
            "supportsAbort: " + supportsAbort + "\n" + "supportsGetBbox: " +
            supportsGetBbox + "\n" + "hasFastBbox: " + hasFastBbox;
        }
    }
}
