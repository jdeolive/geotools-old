/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
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
package org.geotools.data.arcsde;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import java.util.Map;
import java.util.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author Gabriel Rold?n
 * @version $Id: ArcSDEDataStoreFactory.java,v 1.2 2004/04/20 04:45:16 cholmesny Exp $
 */
public class ArcSDEDataStoreFactory implements DataStoreFactorySpi {
    /** package's logger */
    protected static final Logger LOGGER = Logger.getLogger(ArcSDEDataStoreFactory.class.getPackage()
                                                                                        .getName());

    /** friendly factory description */
    private static final String FACTORY_DESCRIPTION = "ESRI(tm) ArcSDE 8.x";
    private static Param[] paramMetadata = new Param[11];

    static {
        paramMetadata[0] = new Param("namespace", String.class,
                "namespace associated to this data store", false);
        paramMetadata[1] = new Param("dbtype", String.class,
                "fixed value. Must be \"arcsde\"", true, "arcsde");
        paramMetadata[2] = new Param("server", String.class,
                "sever name where the ArcSDE gateway is running", true);
        paramMetadata[3] = new Param("port", Integer.class,
                "port number in wich the ArcSDE server is listening for connections.Generally it's 5151",
                true, new Integer(5151));
        paramMetadata[4] = new Param("instance", String.class,
                "database instance name", true);
        paramMetadata[5] = new Param("user", String.class,
                "database user name", true);
        paramMetadata[6] = new Param("password", String.class,
                "database user password", true);

        //optional parameters:
        paramMetadata[7] = new Param("pool.minConnections", Integer.class,
                "Minimun number of open connections", false,
                new Integer(ArcSDEConnectionPool.DEFAULT_CONNECTIONS));
        paramMetadata[8] = new Param("pool.maxConnections", Integer.class,
                "Maximun number of open connections", false,
                new Integer(ArcSDEConnectionPool.DEFAULT_MAX_CONNECTIONS));
        paramMetadata[9] = new Param("pool.increment", Integer.class,
                "Number of connections created on each pool size increment",
                false, new Integer(ArcSDEConnectionPool.DEFAULT_INCREMENT));
        paramMetadata[10] = new Param("pool.timeOut", Integer.class,
                "Number of milliseconds a calling thread should wait for an available connection",
                false, new Integer(ArcSDEConnectionPool.DEFAULT_MAX_WAIT_TIME));
    }

    /** factory of connection pools to different SDE databases */
    private static final ConnectionPoolFactory poolFactory = ConnectionPoolFactory
        .getInstance();

    /**
     * empty constructor
     */
    public ArcSDEDataStoreFactory() {
    }

    /**
     * DOCUMENT ME!
     *
     * @param map DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public DataStore createNewDataStore(java.util.Map map) {
        throw new UnsupportedOperationException(
            "ArcSDE DataStore does not supports the creation of new databases. This should be done through database's specific tools");
    }

    /**
     * crates an SdeDataSource based on connection parameters holded in
     * <code>params</code>.
     * 
     * <p>
     * Expected parameters are:
     * 
     * <ul>
     * <li>
     * <b>dbtype</b>: MUST be <code>"arcsde"</code>
     * </li>
     * <li>
     * <b>server</b>: machine name where ArcSDE is running
     * </li>
     * <li>
     * <b>port</b>: por number where ArcSDE listens for connections on server
     * </li>
     * <li>
     * <b>instance</b>: database instance name to connect to
     * </li>
     * <li>
     * <b>user</b>: database user name with at least reading privileges over
     * SDE instance
     * </li>
     * <li>
     * <b>password</b>: database user password
     * </li>
     * </ul>
     * </p>
     *
     * @param params connection parameters
     *
     * @return a new <code>SdeDataStore</code> pointing to the database defined
     *         by <code>params</code>
     *
     * @throws java.io.IOException if somthing goes wrong creating the
     *         datastore.
     */
    public DataStore createDataStore(Map params) throws java.io.IOException {
        ArcSDEDataStore sdeDStore = null;
        ConnectionConfig config = new ConnectionConfig(params);
        ArcSDEConnectionPool connPool = poolFactory.getPoolFor(config);
        sdeDStore = new ArcSDEDataStore(connPool);

        return sdeDStore;
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
            new ConnectionConfig(params);
        } catch (NullPointerException ex) {
            canProcess = false;
        } catch (IllegalArgumentException ex) {
            canProcess = false;
        }

        return canProcess;
    }

	/**
	 * Test to see if this datastore is available, if it has all the
	 * appropriate libraries to construct a datastore.  This datastore just
	 * returns true for now.  This method is used for gui apps, so as to
	 * not advertise data store capabilities they don't actually have.
	 *
	 * @return <tt>true</tt> if and only if this factory is available to create
	 *         DataStores.
	 *
	 * @task REVISIT: I'm just adding this method to compile, maintainer should
	 *       revisit to check for any libraries that may be necessary for
	 *       datastore creations. ch.
	 */
	public boolean isAvailable() {
		return true;
	}

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public DataStoreFactorySpi.Param[] getParametersInfo() {
        return paramMetadata;
    }
}
