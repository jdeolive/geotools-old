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

package org.geotools.vpf;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.DataSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataSourceMetaData;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollectionDefault;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;


/**
 * Class VPFDataSource.java is responsible for
 * 
 * <p>
 * Created: Fri Mar 28 13:02:00 2003
 * </p>
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version 1.0.0
 */
public class VPFDataSource implements DataSource {
    protected Logger log = Logger.getLogger("org.geotools.vpf");
    protected File file = null;
    protected VPFDataBase dataBase = null;
    protected FeatureType schema = null;

    /**
     * Creates a new VPFDataSource object.
     *
     * @param file DOCUMENT ME!
     */
    public VPFDataSource(File file)
        throws IOException {
        this.file = file;
        dataBase = new VPFDataBase(file);
    }

    // Implementation of org.geotools.data.DataSource

    /**
     * Describe <code><code>getFeatures</code></code> method here.
     *
     * @param filter a <code><code>Filter</code></code> value
     *
     * @return a <code><code>FeatureCollection</code></code> value
     *
     * @exception DataSourceException if an error occurs
     */
    public FeatureCollection getFeatures(Filter filter)
        throws DataSourceException {
        FeatureCollection fc = new FeatureCollectionDefault();
        getFeatures(fc, filter);

        return fc;
    }

    /**
     * Describe <code><code>getFeatures</code></code> method here.
     *
     * @param featureCollection a <code><code>FeatureCollection</code></code>
     *        value
     * @param filter a <code><code>Filter</code></code> value
     *
     * @exception DataSourceException if an error occurs
     */
    public void getFeatures(FeatureCollection featureCollection,
                            Filter filter)
        throws DataSourceException {}

    /**
     * Describe <code><code>addFeatures</code></code> method here.
     *
     * @param featureCollection a <code><code>FeatureCollection</code></code>
     *        value
     *
     * @return DOCUMENT ME!
     *
     * @exception DataSourceException if an error occurs
     */
    public Set addFeatures(FeatureCollection featureCollection)
        throws DataSourceException {
        throw new DataSourceException(
            "Modification of features is not yet supported by this datasource"
        );
    }

    /**
     * Describe <code><code>removeFeatures</code></code> method here.
     *
     * @param filter a <code><code>Filter</code></code> value
     *
     * @exception DataSourceException if an error occurs
     */
    public void removeFeatures(Filter filter) throws DataSourceException {
        throw new DataSourceException(
            "Removal of features is not yet supported by this datasource"
        );
    }

    /**
     * Describe <code><code>modifyFeatures</code></code> method here.
     *
     * @param attributeTypeArray an <code><code>AttributeType[]</code></code>
     *        value
     * @param objectArray an <code><code>Object[]</code></code> value
     * @param filter a <code><code>Filter</code></code> value
     *
     * @exception DataSourceException if an error occurs
     */
    public void modifyFeatures(
        AttributeType[] attributeTypeArray,
        Object[] objectArray,
        Filter filter
    ) throws DataSourceException {
        throw new DataSourceException(
            "Modification of features is not yet supported by this datasource"
        );
    }

    /**
     * Describe <code><code>modifyFeatures</code></code> method here.
     *
     * @param attributeType an <code><code>AttributeType</code></code> value
     * @param object an <code><code>Object</code></code> value
     * @param filter a <code><code>Filter</code></code> value
     *
     * @exception DataSourceException if an error occurs
     */
    public void modifyFeatures(
        AttributeType attributeType,
        Object object,
        Filter filter
    ) throws DataSourceException {
        throw new DataSourceException(
            "Modification of features is not yet supported by this datasource"
        );
    }

    /**
     * Describe <code><code>abortLoading</code></code> method here.
     */
    public void abortLoading() {}

    /**
     * Describe <code><code>getBbox</code></code> method here.
     *
     * @param flag a <code><code>boolean</code></code> value
     *
     * @return an <code><code>Envelope</code></code> value
     */
    public Envelope getBbox(boolean flag) {
        return getBbox();
    }

    /**
     * Describe <code><code>getBbox</code></code> method here.
     *
     * @return an <code><code>Envelope</code></code> value
     */
    public Envelope getBbox() {
        return new Envelope(dataBase.getMinX(), dataBase.getMaxX(),
                            dataBase.getMinY(), dataBase.getMaxY());
    }

    /**
     * Begins a transaction(add, remove or modify) that does not commit as each
     * modification call is made.  If an error occurs during a transaction
     * after this method has been called then the datasource should rollback:
     * none of the transactions performed after this method was called should
     * go through.
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public void startMultiTransaction() throws DataSourceException {
        throw new DataSourceException("multi transactions not supported");
    }

    /**
     * Ends a transaction after startMultiTransaction has been called.  Similar
     * to a commit call in sql, it finalizes all of the transactions called
     * after a startMultiTransaction.
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public void endMultiTransaction() throws DataSourceException {
        throw new DataSourceException("multi transactions not supported");
    }

    /**
     * Data source utility methods.
     *
     * @return DOCUMENT ME!
     */
    /**
     * Gets the DatasSourceMetaData object associated with this datasource.
     * This is the preferred way to find out which of the possible datasource
     * interface methods are actually implemented, query the
     * DataSourceMetaData about which methods the datasource supports.
     *
     * @return DOCUMENT ME!
     */
    public DataSourceMetaData getMetaData() {
        return new DataSourceMetaData() {
                public boolean supportsTransactions() {
                    return false;
                }

                public boolean supportsMultiTransactions() {
                    return false;
                }

                public boolean supportsSetFeatures() {
                    return false;
                }

                public boolean supportsSetSchema() {
                    return false;
                }

                public boolean supportsAbort() {
                    return false;
                }

                public boolean supportsGetBbox() {
                    return false;
                }
            };
    }

    /**
     * Deletes the all the current Features of this datasource and adds the new
     * collection.  Primarily used as a convenience method for file
     * datasources.
     *
     * @param collection - the collection to be written
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public void setFeatures(FeatureCollection collection)
        throws DataSourceException {
        throw new DataSourceException("set feature not supported");
    }

    /**
     * Retrieves the featureType that features extracted from this datasource
     * will be created with.
     *
     * @return DOCUMENT ME!
     */
    public FeatureType getSchema() {
        return schema;
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
     */
    public void setSchema(FeatureType schema) throws DataSourceException {
        this.schema = schema;
    }
}


// VPFDataSource
