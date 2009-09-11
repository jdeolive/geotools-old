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
 */
package org.geotools.data.ws.protocol.http;

import java.net.URL;

import java.util.Map;
import java.util.logging.Logger;

import org.geotools.util.logging.Logging;

/**
 * Base class for {@link HTTPProtocol} implementations that provides the basic property accessors
 * and a good implementation for the URL factory helper method {@link #createUrl(URL, Map)}
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 * @since 2.6
 * @source $URL$
 * 
 */
public abstract class AbstractHttpProtocol implements HTTPProtocol {

    protected static final Logger LOGGER = Logging.getLogger("org.geotools.data.wfs.protocol.http");

    private boolean tryGzip;

    private String authUsername;

    private String authPassword;

    protected int timeoutMillis = -1;

    public AbstractHttpProtocol() {
        super();
    }

    /**
     * @see HTTPProtocol#isTryGzip()
     */
    public boolean isTryGzip() {
        return this.tryGzip;
    }

    /**
     * @see HTTPProtocol#setTryGzip(boolean)
     */
    public void setTryGzip(boolean tryGzip) {
        this.tryGzip = tryGzip;
    }

    /**
     * @see HTTPProtocol#setAuth(String, String)
     */
    public void setAuth(String username, String password) {
        this.authUsername = username;
        this.authPassword = password;
    }

    /**
     * @see HTTPProtocol#
     */
    public int getTimeoutMillis() {
        return this.timeoutMillis;
    }

    /**
     * @see HTTPProtocol#setTimeoutMillis(int)
     */
    public void setTimeoutMillis(int milliseconds) {
        this.timeoutMillis = milliseconds;
    }
}