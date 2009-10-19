/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2009, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.arcsde.gce;

import java.io.IOException;

import org.geotools.arcsde.session.ISession;
import org.geotools.arcsde.session.ISessionPool;
import org.geotools.arcsde.session.UnavailableConnectionException;

/**
 * 
 * @author Gabriel Roldan
 * 
 * @source $URL:
 *         http://svn.osgeo.org/geotools/trunk/modules/plugin/arcsde/datastore/src/main/java/org
 *         /geotools/arcsde/gce/RasterReaderFactory.java $
 * @version $Id$
 * @since 2.5.7
 */
public class RasterReaderFactory {

    private final ISessionPool sessionPool;

    public RasterReaderFactory(final ISessionPool connectionPool) {
        this.sessionPool = connectionPool;
    }

    public TiledRasterReader create(final RasterDatasetInfo rasterInfo) throws IOException {

        ISession conn;
        try {
            conn = sessionPool.getSession(false);
        } catch (UnavailableConnectionException e) {
            throw new RuntimeException(e);
        }

        TiledRasterReader rasterReader = new DefaultTiledRasterReader(conn, rasterInfo);

        return rasterReader;
    }

}
