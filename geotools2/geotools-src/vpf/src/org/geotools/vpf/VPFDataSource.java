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
 * Class <code>VPFDataSource</code> implements 
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: VPFDataSource.java,v 1.8 2003/04/17 21:18:02 kobit Exp $
 */
public class VPFDataSource implements DataSource {
    protected Logger log = Logger.getLogger("org.geotools.vpf");
    protected File file = null;
    protected VPFDataBase dataBase = null;
    protected FeatureType schema = null;

    /**
	 * Creates a new <code>VPFDataSource</code> instance.
	 *
	 * @param file a <code>File</code> value
	 * @exception IOException if an error occurs
	 */
	public VPFDataSource(File file)
        throws IOException {
        this.file = file;
        dataBase = new VPFDataBase(file);
    }

    // Implementation of org.geotools.data.DataSource

    /**
     * Loads features from the datasource into the returned collection, based on
     * the passed filter.
     *
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @return Collection The collection to put the features into.
     * @throws DataSourceException For all data source errors.
     */
    public FeatureCollection getFeatures(Filter filter)
        throws DataSourceException {
        FeatureCollection fc = new FeatureCollectionDefault();
        getFeatures(fc, filter);

        return fc;
    }

    /**
     * Loads features from the datasource into the passed collection, based on
     * the passed filter.  Note that all data sources must support this method
     * at a minimum.
     *
     * @param featureCollection The collection to put the features into.
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @throws DataSourceException For all data source errors.
     */
    public void getFeatures(FeatureCollection featureCollection,
                            Filter filter)
        throws DataSourceException {}

    /**
     * Adds all features from the passed feature collection to the datasource.
     *
     * @param featureCollection The collection from which to add the features.
     * @return the FeatureIds of the newly added features.
     * @throws DataSourceException If anything goes wrong or if exporting is
     * not supported.
     */
    public Set addFeatures(FeatureCollection featureCollection)
        throws DataSourceException {
        throw new DataSourceException("Modification of features is not yet supported by this datasource");
    }

    /**
     * Removes all of the features specificed by the passed filter from the
     * collection.
     *
     * @param filter An OpenGIS filter; specifies which features to remove.
     * @throws DataSourceException If anything goes wrong or if deleting is
     * not supported.
     */
    public void removeFeatures(Filter filter) throws DataSourceException {
        throw new DataSourceException("Removal of features is not yet supported by this datasource");
    }

    /**
     * Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.
     *
     * @param attributeTypeArray The attributes to modify.
     * @param objectArray The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     * @throws DataSourceException If modificaton is not supported, if
     * the attribute and object arrays are not eqaul length, or if the object
     * types do not match the attribute types.
     */
    public void modifyFeatures(AttributeType[] attributeTypeArray,
                               Object[] objectArray, Filter filter)
        throws DataSourceException {
        throw new DataSourceException("Modification of features is not yet supported by this datasource");
    }

    /**
     * Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.  A convenience
     * method for single attribute modifications.
     *
     * @param attributeType The attributes to modify.
     * @param object The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     * @throws DataSourceException If modificaton is not supported, if
     * the object type do not match the attribute type.
     */
    public void modifyFeatures(AttributeType attributeType,
                               Object object, Filter filter)
        throws DataSourceException {
        throw new DataSourceException("Modification of features is not yet supported by this datasource");
    }

    /**
     * Stops this DataSource from loading.
     */
    public void abortLoading() {}

    /**
     * Gets the bounding box of this datasource using the speed of 
     * this datasource as set by the parameter.
     *
     * @param flag If true then a quick (and possibly dirty) estimate of
     * the extent is returned. If false then a slow but accurate extent
     * will be returned
     * @return The extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     * @task REVISIT:Consider changing return of getBbox to Filter once Filters can be unpacked
     */
    public Envelope getBbox(boolean flag) {
        return getBbox();
    }

    /**
     * Gets the bounding box of this datasource using the default speed of 
     * this datasource as set by the implementer. 
     *
     * @return The bounding box of the datasource or null if unknown and too
     * expensive for the method to calculate.
     * @task REVISIT: Consider changing return of getBbox to Filter once Filters
     * can be unpacked
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

    /**************************************************
      Data source utility methods.
     **************************************************/

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
    } //	public Iterator iterator(){getFeatures().iterator();}
	
} // VPFDataSource
