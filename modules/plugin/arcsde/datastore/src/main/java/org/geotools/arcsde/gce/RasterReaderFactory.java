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

import org.geotools.arcsde.session.ArcSDEConnectionPool;
import org.geotools.arcsde.session.ArcSDEPooledConnection;

/**
 * 
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.7
 */
public class RasterReaderFactory {

    private final ArcSDEConnectionPool connectionPool;

    public RasterReaderFactory(final ArcSDEConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public TiledRasterReader create(final RasterDatasetInfo rasterInfo) throws IOException {

        ArcSDEPooledConnection conn = connectionPool.getConnection();

        TiledRasterReader rasterReader = new DefaultTiledRasterReader(conn, rasterInfo);

        return rasterReader;
    }

}
