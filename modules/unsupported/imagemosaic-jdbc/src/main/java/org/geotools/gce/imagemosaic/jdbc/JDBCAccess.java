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
