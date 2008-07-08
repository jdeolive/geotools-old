/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gce.imagemosaic.jdbc;

import org.geotools.geometry.GeneralEnvelope;

import java.awt.Rectangle;

import java.io.IOException;

import java.sql.SQLException;

import java.util.concurrent.LinkedBlockingQueue;

interface JDBCAccess {
    public abstract void startTileDecoders(Rectangle pixelDimension,
        GeneralEnvelope requestEnvelope, ImageLevelInfo info,
        LinkedBlockingQueue<Object> tileQueue) throws IOException;

    public ImageLevelInfo getLevelInfo(int level);

    public int getNumOverviews();

    public void initialize() throws SQLException, IOException;
}
