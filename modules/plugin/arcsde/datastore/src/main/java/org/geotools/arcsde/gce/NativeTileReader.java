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
package org.geotools.arcsde.gce;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.EOFException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.ArcSdeException;
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
import com.esri.sde.sdk.client.SeRasterTile;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.esri.sde.sdk.client.SeStreamOp;

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

    /**
     * {@link SeStreamOp} held to be closed at {@link #dispose()}
     * 
     * @see #execute()
     */
    private SeQuery preparedQuery;

    private SeRasterTile nextTile;

    private boolean started;

    private final int bitmaskDataLength;

    private final BitmaskToNoDataConverter noData;

    private final int pixelsPerTile;

    private final int tileDataLength;

    private int bitsPerSample;

    /**
     * Command to fetch an {@link SeRasterTile tile}
     */
    private static class TileFetchCommand extends Command<SeRasterTile> {

        private final SeRow row;

        public TileFetchCommand(final SeRow row) {
            this.row = row;
        }

        @Override
        public SeRasterTile execute(ISession session, SeConnection connection) throws SeException,
                IOException {
            return row.getRasterTile();
        }

    }

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

        final RasterCellType nativeCellType = rasterInfo.getNativeCellType();
        this.bitsPerSample = nativeCellType.getBitsPerSample();
        this.tileDataLength = (int) Math
                .ceil(((double) pixelsPerTile * (double) bitsPerSample) / 8D);
        this.bitmaskDataLength = (int) Math.ceil(pixelsPerTile / 8D);
    }

    /**
     * @see org.geotools.arcsde.gce.TileReader#getBitsPerSample()
     */
    public int getBitsPerSample() {
        return bitsPerSample;
    }

    /**
     * @see org.geotools.arcsde.gce.TileReader#getPixelsPerTile()
     */
    public int getPixelsPerTile() {
        return pixelsPerTile;
    }

    /**
     * @see org.geotools.arcsde.gce.TileReader#getNumberOfBands()
     */
    public int getNumberOfBands() {
        return rasterInfo.getNumBands();
    }

    /**
     * @see org.geotools.arcsde.gce.TileReader#getTileWidth()
     */
    public int getTileWidth() {
        return rasterInfo.getTileWidth(rasterId);
    }

    /**
     * @see org.geotools.arcsde.gce.TileReader#getTileHeight()
     */
    public int getTileHeight() {
        return rasterInfo.getTileHeight(rasterId);
    }

    /**
     * @see org.geotools.arcsde.gce.TileReader#getTilesWide()
     */
    public int getTilesWide() {
        return requestedTiles.width;
    }

    /**
     * @see org.geotools.arcsde.gce.TileReader#getTilesHigh()
     */
    public int getTilesHigh() {
        return requestedTiles.height;
    }

    /**
     * @see org.geotools.arcsde.gce.TileReader#getBytesPerTile()
     */
    public int getBytesPerTile() {
        return tileDataLength;
    }

    /**
     * @see org.geotools.arcsde.gce.TileReader#hasNext()
     */
    public boolean hasNext() throws IOException {
        if (!started) {
            execute();
            try {
                nextTile = session.issue(tileFetchCommand);
            } catch (IOException e) {
                dispose();
                throw e;
            } catch (RuntimeException e) {
                dispose();
                throw e;
            }
            started = true;
            if (nextTile == null) {
                dispose();
                LOGGER.fine("No tiles to fetch at all, releasing connection");
            }
        }
        return nextTile != null;
    }

    /**
     * Creates and executes the {@link SeQuery} that's used to fetch the required tiles from the
     * specified raster, and stores (as member variables) the {@link SeRow} to fetch the tiles from
     * and the {@link SeQuery} to be closed at the TileReader's disposal
     * 
     * @throws IOException
     */
    private void execute() throws IOException {

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

            int minTileX = requestedTiles.x;
            int minTileY = requestedTiles.y;
            int maxTileX = minTileX + requestedTiles.width - 1;
            int maxTileY = minTileY + requestedTiles.height - 1;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Requesting tiles [x=" + minTileX + "-" + maxTileX + ", y=" + minTileY
                        + "-" + maxTileY + "] from tile range [x=0-"
                        + (rasterInfo.getNumTilesWide(rasterIndex, pyramidLevel) - 1) + ", y=0-"
                        + (rasterInfo.getNumTilesHigh(rasterIndex, pyramidLevel) - 1) + "]");
            }
            // SDEPoint tileOrigin = rAttr.getTileOrigin();

            if (LOGGER.isLoggable(Level.FINE)) {
                Rectangle tiledImageSize = new Rectangle(0, 0, tileWidth * requestedTiles.width,
                        tileHeight * requestedTiles.height);

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

        final boolean transactional = false;
        try {
            this.session = sessionPool.getSession(transactional);
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("Using " + session + " to read raster #" + rasterId + " on Thread "
                        + Thread.currentThread().getName() + ". Tile set: " + requestedTiles);
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

        this.tileFetchCommand = new TileFetchCommand(row);
        this.preparedQuery = queryCommand.getPreparedQuery();
    }

    /**
     * @see org.geotools.arcsde.gce.TileReader#next(byte[])
     */
    public TileInfo next(byte[] tileData) throws IOException {
        if (tileData == null) {
            dispose();
            throw new IllegalArgumentException("tileData is null");
        }

        final SeRasterTile tile;
        final boolean hasNext = hasNext();
        if (hasNext) {
            tile = nextTile();
        } else {
            throw new IllegalStateException("There're no more tiles to fetch");
        }

        try {
            final byte[] bitMaskData = tile.getBitMaskData();

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest(" >> Fetching " + tile + " - bitmask: " + bitMaskData.length);
            }

            assert bitMaskData.length == 0 ? true : bitmaskDataLength == bitMaskData.length;

            final int numPixels = tile.getNumPixels();

            final Long bandId = Long.valueOf(tile.getBandId().longValue());

            if (0 == numPixels) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("tile contains no pixel data, skipping: " + tile);
                }
                noData.setAll(bandId, tileData);
            } else if (pixelsPerTile == numPixels) {

                final byte[] rawTileData = tile.getPixelData();
                //System.out.println("got raw tile data " + rawTileData);
                
                System.arraycopy(rawTileData, 0, tileData, 0, tileDataLength);

                if (bitMaskData.length > 0) {
                    noData.setNoData(bandId, tileData, bitMaskData);
                }

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("returning " + numPixels + " pixels data packaged into "
                            + tileDataLength + " bytes for tile [" + tile.getColumnIndex() + ","
                            + tile.getRowIndex() + "]");
                }
            } else {
                throw new IllegalStateException("Expected pixels per tile == " + pixelsPerTile
                        + " but got " + numPixels + ": " + tile);
            }

            return new TileInfo(bandId, bitMaskData, numPixels);

        } catch (RuntimeException e) {
            dispose();
            throw e;
        }
    }

    private SeRasterTile nextTile() throws IOException {
        if (nextTile == null) {
            dispose();
            throw new EOFException("No more tiles to read");
        }
        SeRasterTile curr = nextTile;

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
            LOGGER.finer("There're no more tiles to fetch");
        }

        return curr;
    }

    /**
     * @see org.geotools.arcsde.gce.TileReader#dispose()
     */
    public void dispose() {
        if (session != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("TileReader disposing " + session + " on Thread "
                        + Thread.currentThread().getName());
            }
            try {
                session.close(this.preparedQuery);
            } catch (Exception e) { 
                LOGGER.log(Level.WARNING, "Closing tile reader's prepared Query", e);
            }
            session.dispose();
            session = null;
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
}
