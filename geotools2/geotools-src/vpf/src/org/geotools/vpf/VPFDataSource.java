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


import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.geotools.data.AbstractDataSource;
import org.geotools.data.DataSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollectionDefault;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;


/**
 * Class <code>VPFDataSource</code> implements
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: VPFDataSource.java,v 1.12 2003/05/19 20:59:37 kobit Exp $
 */
public class VPFDataSource extends AbstractDataSource implements DataSource {
    /**
     * Describe variable <code>log</code> here.
     *
     */
    private Logger log = Logger.getLogger("org.geotools.vpf");
    /**
     * Describe variable <code>file</code> here.
     *
     */
    private File file = null;
    /**
     * Describe variable <code>dataBase</code> here.
     *
     */
    private VPFDataBase dataBase = null;
    /**
     * Describe variable <code>schema</code> here.
     *
     */
    private FeatureType schema = null;

    /**
     * Creates a new <code>VPFDataSource</code> instance.
     *
     * @param file a <code>File</code> value
     *
     * @exception IOException if an error occurs
     */
    public VPFDataSource(File file) throws IOException {
        this.file = file;
        dataBase = new VPFDataBase(file);
    }

    // Implementation of org.geotools.data.DataSource

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
        FeatureCollection fc = new FeatureCollectionDefault();
        getFeatures(fc, filter);

        return fc;
    }

    // Now mostly handled by AbstractDataSource

    /**
     * Loads features from the datasource into the passed collection, based on
     * the passed filter.  Note that all data sources must support this method
     * at a minimum.
     *
     * @param featureCollection The collection to put the features into.
     * @param query An OpenGIS filter; specifies which features to retrieve.
     *
     * @throws DataSourceException For all data source errors.
     */
    public void getFeatures(FeatureCollection featureCollection, Query query)
        throws DataSourceException {
    }

    /**
     * Data source utility methods.
     *
     * @return DOCUMENT ME!
     */
    /**
     * Retrieves the featureType that features extracted from this datasource
     * will be created with.
     *
     * @return a <code>FeatureType</code> value
     */
    public FeatureType getSchema() {
        return schema;
    }

}
