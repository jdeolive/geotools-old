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
package org.geotools.data.sde;

import com.esri.sde.sdk.client.*;
import org.geotools.data.DataSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataSourceFactorySpi;
import java.util.*;
import java.util.logging.*;


/**
 * DataSourceFactory for geotools2's ArcSDE datasources
 *
 * @author Gabriel Roldán
 * @version 0.1
 */
public class SdeDatasourceFactory implements DataSourceFactorySpi {
    /** package's logger */
    protected static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.sde");

    /** friendly factory description */
    private static final String FACTORY_DESCRIPTION = "ArcSDE 8.x";

    /** factory of connection pools to different SDE databases */
    private static final SdeConnectionPoolFactory poolFactory = SdeConnectionPoolFactory
        .getInstance();

    /**
     * empty constructor
     */
    public SdeDatasourceFactory() {
    }

    /**
     * crates an SdeDataSource based on connection parameters holded in
     * <code>params</code>.
     *
     * <p>
     * Expected parameters are:
     *
     * <ul>
     * <li><b>dbtype</b>: MUST be <code>"arcsde"</code>
     * <li><b>server</b>: machine name where ArcSDE is running
     * <li><b>port</b>: por number where ArcSDE listens for connections on server
     * <li><b>instance</b>: database instance name to connect to
     * <li><b>user</b>: database user name with at least reading privileges over
     * SDE instance
     * <li><b>password</b>: database user password
     * <li><b>table</b>: wich featureclass to work on
     * </ul>
     * </p>
     *
     * @param params where the new<code>SdeDataSource</code> will get ArcSDE
     *        connections from
     *
     * @return a new <code>SdeDataSource</code> for <code>tableName</code>'s
     *         ArcSDE feature class
     *
     * @throws DataSourceException if somthing goes wrong creating the
     *         datasource. For example, if <code>tableName</code> does not
     *         exists in the SDE catalog or the user has no reading
     *         privilegies over it
     */
    public DataSource createDataSource(Map params) throws DataSourceException {
        try { //check for ArcSDE dependencies
            Class.forName("com.esri.sde.sdk.client.SeConnection");
        } catch (ClassNotFoundException ex) {
            throw new DataSourceException("ArcSDE dependencies not found", ex);
        }

        if (params == null) {
            throw new DataSourceException(
                "null is not valid as method argument");
        }

        SdeConnectionConfig connectionConfig = null;
        SdeDataSource dataSource = null;

        try {
            connectionConfig = new SdeConnectionConfig(params);
        } catch (IllegalArgumentException ex) {
            throw new DataSourceException("Argument list is invalid", ex);
        } catch (NullPointerException ex) {
            throw new DataSourceException("At least one argument is missing", ex);
        }

        String tableName = (String) params.get(SdeConnectionConfig.TABLE_NAME_PARAM);

        if ((tableName == null) || (tableName.length() == 0)) {
            throw new DataSourceException("parameter table was not specified");
        }

        SdeConnectionPool connectionPool = poolFactory.getPoolFor(connectionConfig);
        dataSource = new SdeDataSource(connectionPool, tableName);

        return dataSource;
    }


    /**
     * A human friendly name for this data source factory
     *
     * @return this factory's description
     */
    public String getDescription() {
        return FACTORY_DESCRIPTION;
    }

    /**
     * DOCUMENT ME!
     *
     * @param params
     *
     * @return
     */
    public boolean canProcess(Map params) {
        boolean canProcess = true;

        try {
            new SdeConnectionConfig(params);
        } catch (NullPointerException ex) {
            canProcess = false;
        } catch (IllegalArgumentException ex) {
            canProcess = false;
        }

        return canProcess;
    }

    /**
     * makes a query to the sde backend for available SDE layers in the
     * database specified in <code>params</code> and returns it as an array of
     * its corresponding <code>SdeDataSource</code>'s
     *
     * @param params same params used in <code>canProcess</code> to query the
     *        database for available layers
     *
     * @return an array with an sde datasource setted up for each available
     *         layer in the SDE database
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public SdeDataSource[] getAvailableLayers(Map params)
        throws DataSourceException {
        SdeConnectionConfig dbConfig = new SdeConnectionConfig(params);
        SeConnection sdeConn = null;
        SdeConnectionPool pool = null;
        List dataSourcesList = new LinkedList();
        SdeDataSource[] dataSources = new SdeDataSource[0];

        try {
            pool = poolFactory.getPoolFor(dbConfig);
            sdeConn = pool.getConnection();

            Vector sdeLayers = sdeConn.getLayers();
            pool.release(sdeConn);

            SeLayer sdeLayer;
            int nLayers = sdeLayers.size();

            SdeDataSource sdeds;

            for (int i = 0; i < nLayers; i++) {
                sdeLayer = (SeLayer) sdeLayers.get(i);

                try {
                    sdeds = new SdeDataSource(pool, sdeLayer.getQualifiedName());
                    dataSourcesList.add(sdeds);
                } catch (SeException seEx) {
                    LOGGER.warning("can't get " + sdeLayer.getName()
                        + "'s qualified name: " + seEx.getMessage());
                } catch (DataSourceException dsEx) {
                    LOGGER.warning("can't create SdeDataSouce for "
                        + sdeLayer.getName() + ": " + dsEx.getMessage());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                pool.release(sdeConn);
            } catch (Exception ex) {
                LOGGER.warning("can't release connection " + sdeConn + ": "
                    + ex.getMessage());
            }
        }

        dataSources = (SdeDataSource[]) dataSourcesList.toArray(dataSources);

        return dataSources;
    }
}
