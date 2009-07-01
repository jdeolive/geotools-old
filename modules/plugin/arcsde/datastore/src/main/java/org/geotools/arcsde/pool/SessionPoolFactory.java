/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.arcsde.pool;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * {@link SessionPool} factory.
 * 
 * @author Gabriel Roldan
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/unsupported/arcsde/datastore/src/main
 *         /java/org/geotools/arcsde/pool/SessionPoolFactory.java $
 * @version $Id$
 */
public class SessionPoolFactory {
    /** package logger */
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.arcsde.pool");

    /** singleton pool factory */
    private static final SessionPoolFactory singleton = new SessionPoolFactory();

    /**
     * Creates a new SdeConnectionPoolFactory object.
     */
    private SessionPoolFactory() {
        // intentionally blank
    }

    /**
     * Returns a connection pool factory instance
     * 
     * @return the connection pool factory singleton
     */
    public synchronized static SessionPoolFactory getInstance() {
        return singleton;
    }

    /**
     * Creates a connection pool factory for the given connection parameters.
     * 
     * @param config
     *             contains the connection parameters and pool preferences
     * @return a pool for the given connection parameters, wether it already existed or had to be
     *         created.
     * @throws IOException
     *             if the pool needs but can't be created
     */
    public synchronized SessionPool createPool(ArcSDEConnectionConfig config) throws IOException {
        SessionPool pool;

        // the new pool will be populated with config.minConnections
        // connections
        if (config.getMaxConnections() != null && config.getMaxConnections() == 1) {
            // engage experimental single connection mode!
            pool = new ArcSDEConnectionReference(config);
        } else {
            pool = new SessionPool(config);
        }

        return pool;
    }
}
