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

import org.geotools.data.DataSourceException;


/**
 * Exception thrown when a free SDE connection can't be obtained after the
 * calling thread was waiting an available connection for
 * <code>SdeConnectionPool instance's getMaxWaitTime()</code> milliseconds
 *
 * @task REVISIT: sure there are a better exception to use in somewhere... just
 * take a look since it seems not very wise to have it here... may be in
 * current jdbc package.
 * @author Gabriel Roldán
 * @version 0.1
 */
public class UnavailableConnectionException extends DataSourceException {
    /**
     * Creates a new UnavailableConnectionException object.
     *
     * @param usedConnections DOCUMENT ME!
     * @param config DOCUMENT ME!
     */
    public UnavailableConnectionException(int usedConnections,
        SdeConnectionConfig config) {
        super("The maximun of " + usedConnections + " to " + config.toString()
            + " has been reached");
    }
}
