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
 *
 */
package org.geotools.arcsde.raster.io;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.raster.info.RasterCellType;
import org.geotools.arcsde.raster.info.RasterDatasetInfo;
import org.geotools.arcsde.session.Command;
import org.geotools.arcsde.session.ISession;
import org.geotools.arcsde.session.ISessionPool;
import org.geotools.arcsde.session.UnavailableConnectionException;
import org.geotools.util.logging.Logging;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRaster;
import com.esri.sde.sdk.client.SeRasterConstraint;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;

/**
 * Offers an iterator like interface to fetch ArcSDE raster tiles.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @since 2.5.4
 * @version $Id$
 * @source $URL:
 *         http://svn.osgeo.org/geotools/trunk/modules/plugin/arcsde/datastore/src/main/java/org
 *         /geotools/arcsde/gce/NativeTileReader.java $
 */
@SuppressWarnings( { "nls" })
final class NativeTileReader implements TileReader {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    private final RasterDatasetInfo rasterInfo;

    private final long rasterId;

    private final int pyramidLevel;

    private final Rectangle requestedTiles;

    private final ISessionPool sessionPool;

    private ISession session;

    private TileFetchCommand tileFetchCommand;

    private boolean started;

    private final int bitmaskDataLength;

    private final BitmaskToNoDataConverter noData;

    private final int pixelsPerTile;

    private final int tileDataLength;

    private int bitsPerSample;

    private final RasterCellType nativeCellType;

    /**
     * @see DefaultTiledRasterReader#nextRaster()
     */
    private static class QueryRasterCommand extends Command<Void> {

        private SeQuery preparedQuery;

        private SeRow row;

        private final SeRasterConstraint rasterConstraint;

        private final String rasterColumn;

        private final String rasterTable;

        private final long rasterId;

        /**
         * 
         * @param rConstraint
         *            indicates which bands, pyramid level and grid envelope to query
         * @param rasterTable
         *            indicates which raster table to query
         * @param rasterColumn
         *            indicates what raster column in the raster table to query
         * @param rasterId
         *            indicates which raster in the raster catalog to query
         */
        public QueryRasterCommand(final SeRasterConstraint rConstraint, final String rasterTable,
                final String rasterColumn, final long rasterId) {
            this.rasterConstraint = rConstraint;
            this.rasterTable = rasterTable;
            this.rasterColumn = rasterColumn;
            this.rasterId = rasterId;
        }

        @Override
        public Void execute(ISession session, SeConnection connection) throws SeException,
                IOException {

            final SeSqlConstruct sqlConstruct = new SeSqlConstruct(rasterTable);
            /*
             * Filter by the given raster id
             */
            final String rasterIdFilter = rasterColumn + " = " + rasterId;
            sqlConstruct.setWhere(rasterIdFilter);

            final String[] rasterColumns = { rasterColumn };
            preparedQuery = new SeQuery(connection, rasterColumns, sqlConstruct);
            preparedQuery.prepareQuery();
            preparedQuery.execute();

            this.row = preparedQuery.fetch();
            if (row == null) {
                return null;
            }

            preparedQuery.queryRasterTile(rasterConstraint);

            return null;
        }

        public SeQuery getPreparedQuery() {
            return preparedQuery;
        }

        public SeRow getSeRow() {
            return row;
        }
    }

    /**
     * Creates a {@link TileReader} that reads tiles out of ArcSDE for the given {@code
     * preparedQuery} and {@code SeRow} using the given {@code session}, in the native raster
     * format.
     * <p>
     * As for any object that receives a {@link ISession session}, the same rule applies: this class
     * is not responsible of {@link ISession#dispose() disposing} the session, but the calling code
     * is.
     * </p>
     * 
     * @param preparedQuery
     *            the query stream to close when done
     * @param row
     * @param imageDimensions
     *            the image size, x and y are the offsets, width and height the actual width and
     *            height, used to ignore incomming pixel data as appropriate to fit the image
     *            dimensions
     * @param bitsPerSample
     * @param numberOfBands2
     * @param requestedTiles
     */
    NativeTileReader(final ISessionPool sessionPool, final RasterDatasetInfo rasterInfo,
            final long rasterId, final int pyramidLevel, final Rectangle requestedTiles,
            final BitmaskToNoDataConverter noData) {
        this.sessionPool = sessionPool;
        this.rasterInfo = rasterInfo;
        this.rasterId = rasterId;
        this.pyramidLevel = pyramidLevel;
        this.requestedTiles = requestedTiles;
        this.noData = noData;

        final Dimension tileSize = rasterInfo.getTileDimension(rasterId);

        this.pixelsPerTile = tileSize.width * tileSize.height;

        this.nativeCellType = rasterInfo.getNativeCellType();
        this.bitsPerSample = nativeCellType.getBitsPerSample();
        this.tileDataLength = (int) Math
                .ceil(((double) pixelsPerTile * (double) bitsPerSample) / 8D);
        this.bitmaskDataLength = (int) Math.ceil(pixelsPerTile / 8D);
    }

    /**
     * @see org.geotools.arcsde.raster.io.TileReader#getBitsPerSample()
     */
    public int getBitsPerSample() {
        return bitsPerSample;
    }

    /**
     * @see org.geotools.arcsde.raster.io.TileReader#getPixelsPerTile()
     */
    public int getPixelsPerTile() {
        return pixelsPerTile;
    }

    /**
     * @see org.geotools.arcsde.raster.io.TileReader#getNumberOfBands()
     */
    public int getNumberOfBands() {
        return rasterInfo.getNumBands();
    }

    /**
     * @see org.geotools.arcsde.raster.io.TileReader#getTileWidth()
     */
    public int getTileWidth() {
        return rasterInfo.getTileWidth(rasterId);
    }

    /**
     * @see org.geotools.arcsde.raster.io.TileReader#getTileHeight()
     */
    public int getTileHeight() {
        return rasterInfo.getTileHeight(rasterId);
    }

    /**
     * @see org.geotools.arcsde.raster.io.TileReader#getTilesWide()
     */
    public int getTilesWide() {
        return requestedTiles.width;
    }

    /**
     * @see org.geotools.arcsde.raster.io.TileReader#getTilesHigh()
     */
    public int getTilesHigh() {
        return requestedTiles.height;
    }

    /**
     * @see org.geotools.arcsde.raster.io.TileReader#getBytesPerTile()
     */
    public int getBytesPerTile() {
        return tileDataLength;
    }

    /**
     * Creates and executes the {@link SeQuery} that's used to fetch the required tiles from the
     * specified raster, and stores (as member variables) the {@link SeRow} to fetch the tiles from
     * and the {@link SeQuery} to be closed at the TileReader's disposal
     * 
     * @throws IOException
     */
    private void execute() throws IOException {
        final Rectangle requestedTiles = this.requestedTiles;
        this.tileFetchCommand = execute(requestedTiles);
    }

    private TileFetchCommand execute(final Rectangle rasterTiles) throws IOException {

        final int rasterIndex = rasterInfo.getRasterIndex(rasterId);
        final int tileWidth = rasterInfo.getTileWidth(rasterId);
        final int tileHeight = rasterInfo.getTileHeight(rasterId);

        /*
         * Create the raster constraint to query the needed tiles out of the specified raster at the
         * given pyramid level
         */
        final SeRasterConstraint rConstraint;
        try {
            final int numberOfBands;
            numberOfBands = rasterInfo.getNumBands();

            int[] bandsToQuery = new int[numberOfBands];
            for (int bandN = 1; bandN <= numberOfBands; bandN++) {
                bandsToQuery[bandN - 1] = bandN;
            }

            int minTileX = rasterTiles.x;
            int minTileY = rasterTiles.y;
            int maxTileX = minTileX + rasterTiles.width - 1;
            int maxTileY = minTileY + rasterTiles.height - 1;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Requesting tiles [x=" + minTileX + "-" + maxTileX + ", y=" + minTileY
                        + "-" + maxTileY + "] from tile range [x=0-"
                        + (rasterInfo.getNumTilesWide(rasterIndex, pyramidLevel) - 1) + ", y=0-"
                        + (rasterInfo.getNumTilesHigh(rasterIndex, pyramidLevel) - 1) + "]");
            }
            // SDEPoint tileOrigin = rAttr.getTileOrigin();

            if (LOGGER.isLoggable(Level.FINE)) {
                Rectangle tiledImageSize = new Rectangle(0, 0, tileWidth * rasterTiles.width,
                        tileHeight * rasterTiles.height);

                LOGGER.fine("Tiled image size: " + tiledImageSize);
            }

            final int interleaveType = SeRaster.SE_RASTER_INTERLEAVE_BIP;

            rConstraint = new SeRasterConstraint();
            rConstraint.setBands(bandsToQuery);
            rConstraint.setLevel(pyramidLevel);
            rConstraint.setEnvelope(minTileX, minTileY, maxTileX, maxTileY);
            rConstraint.setInterleave(interleaveType);
        } catch (SeException se) {
            throw new ArcSdeException(se);
        }

        /*
         * Obtain the ISession this tile reader will work with until exhausted
         */
        try {
            if (this.session == null) {
                // lets share connections as we're going to do read only operations
                final boolean transactional = false;
                this.session = sessionPool.getSession(transactional);
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("Using " + session + " to read raster #" + rasterId
                            + " on Thread " + Thread.currentThread().getName() + ". Tile set: "
                            + rasterTiles);
                }
            }
        } catch (UnavailableConnectionException e) {
            // really bad luck..
            throw new RuntimeException(e);
        }

        final String rasterTable = rasterInfo.getRasterTable();
        final String rasterColumn = rasterInfo.getRasterColumns()[0];
        final QueryRasterCommand queryCommand = new QueryRasterCommand(rConstraint, rasterTable,
                rasterColumn, rasterId);

        session.issue(queryCommand);

        final SeRow row = queryCommand.getSeRow();

        final int numberOfBands = getNumberOfBands();
        final SeQuery preparedQuery = queryCommand.getPreparedQuery();
        TileFetchCommand fetchCommand = new TileFetchCommand(preparedQuery, row, pixelsPerTile,
                numberOfBands, nativeCellType);
        return fetchCommand;
    }

    /**
     * @see org.geotools.arcsde.raster.io.TileReader#getTile(int, int)
     */
    public TileInfo[] getTile(final int tileX, final int tileY) throws IOException {

        final TileInfo[] bandTiles = fetchTile(tileX, tileY);

        try {
            final int numberOfBands = getNumberOfBands();
            for (int bandN = 0; bandN < numberOfBands; bandN++) {
                final TileInfo tile = bandTiles[bandN];
                noData.setNoData(tile);
            }

        } catch (RuntimeException e) {
            dispose();
            throw e;
        }

        return bandTiles;
    }

    private int lastTileX = -1;

    private int lastTileY = -1;

    private TileInfo[] fetchTile(final int tileX, final int tileY) throws IOException {

        TileInfo[] tileInfo = null;

        if (isConsecutive(tileX, tileY)) {
            while (lastTileX != tileX || lastTileY != tileY) {
                tileInfo = nextTile();
            }
        } else {
            tileInfo = fetchSingleTile(tileX, tileY);
        }

        return tileInfo;
    }

    /**
     * Executes a separate request to fetch this single tile
     * 
     * @throws IOException
     */
    private TileInfo[] fetchSingleTile(final int tileX, final int tileY) throws IOException {
        LOGGER.info("fetchSingleTile " + tileX + ", " + tileY);
        final int rasterTileX = requestedTiles.x + tileX;
        final int rasterTileY = requestedTiles.y + tileY;
        final int width = 1;
        final int height = 1;
        final Rectangle requestTiles = new Rectangle(rasterTileX, rasterTileY, width, height);

        final TileFetchCommand command = execute(requestTiles);
        final SeQuery query = command.getQuery();
        final TileInfo[] tile;
        try {
            tile = session.issue(command);
            session.close(query);
        } catch (IOException e) {
            session.close(query);
            dispose();
            throw e;
        } catch (RuntimeException e) {
            session.close(query);
            dispose();
            throw e;
        }

        return tile;
    }

    /**
     * Determines whether the tile defined by {@code tileX, tileY} is consecutive to the original
     * request, whether it is exactly the next in the stream or any other one that follows the last
     * tile fetched from the original request.
     */
    private boolean isConsecutive(final int tileX, final int tileY) {
        if (tileX > lastTileX && tileY >= lastTileY) {
            return true;
        }
        if (tileX <= lastTileX && tileY > lastTileY) {
            return true;
        }
        return false;
    }

    private TileInfo[] nextTile() throws IOException {
        if (!started) {
            execute();
            started = true;
        }

        TileInfo[] nextTile;
        try {
            nextTile = session.issue(tileFetchCommand);
        } catch (IOException e) {
            dispose();
            throw e;
        } catch (RuntimeException e) {
            dispose();
            throw e;
        }
        if (nextTile == null) {
            dispose();
            throw new IllegalStateException("There're no more tiles to fetch");
        }

        if (lastTileY == -1) {
            lastTileY = 0;
        }
        lastTileX++;
        if (lastTileX == getTilesWide()) {
            lastTileX = 0;
            lastTileY++;
        }

        if (lastTileX == getTilesWide() - 1 && lastTileY == getTilesHigh() - 1) {
            dispose();
        }
        return nextTile;
    }

    /**
     * @see org.geotools.arcsde.raster.io.TileReader#dispose()
     */
    public void dispose() {
        if (session != null) {
            // System.err.println("TileReader disposing " + session + " on Thread "
            // + Thread.currentThread().getName());
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("TileReader disposing " + session + " on Thread "
                        + Thread.currentThread().getName());
            }
            if (tileFetchCommand != null) {
                try {
                    SeQuery preparedQuery = this.tileFetchCommand.getQuery();
                    session.close(preparedQuery);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Closing tile reader's prepared Query", e);
                }
            }
            tileFetchCommand = null;
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("Disposing " + session + " on thread "
                        + Thread.currentThread().getName());
            }
            session.dispose();
            session = null;
            started = false;
        }
    }

    /**
     * Disposes as to make sure the {@link ISession session} is returned to the pool even if a
     * failing or non careful client left this object hanging around
     * 
     * @see #dispose()
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() {
        dispose();
    }

    public int getMinTileX() {
        return requestedTiles.x;
    }

    public int getMinTileY() {
        return requestedTiles.y;
    }

    public int getPyramidLevel() {
        return pyramidLevel;
    }

    public long getRasterId() {
        return rasterId;
    }
}
