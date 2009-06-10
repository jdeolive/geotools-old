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
import org.geotools.util.logging.Logging;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeRasterTile;
import com.esri.sde.sdk.client.SeRow;

/**
 * Offers an iterator like interface to fetch ArcSDE raster tiles.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @since 2.5.4
 * @version $Id$
 * @source $URL$
 */
@SuppressWarnings( { "nls" })
final class NativeTileReader implements TileReader {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    private final int bitsPerSample;

    private final Rectangle requestedTiles;

    private final Dimension tileSize;

    private final int tileDataLength;

    private final SeRow row;

    private final int pixelsPerTile;

    private final int numberOfBands;

    private SeRasterTile nextTile;

    private boolean started;

    private final int bitmaskDataLength;

    private final BitmaskToNoDataConverter noData;

    /**
     * 
     * @param row
     * @param imageDimensions
     *            the image size, x and y are the offsets, width and height the actual width and
     *            height, used to ignore incomming pixel data as appropriate to fit the image
     *            dimensions
     * @param bitsPerSample
     * @param numberOfBands2
     * @param requestedTiles
     */
    NativeTileReader(final SeRow row, final int bitsPerSample, int numberOfBands,
            final Rectangle requestedTiles, Dimension tileSize,
            final BitmaskToNoDataConverter noData) {
        this.row = row;
        this.bitsPerSample = bitsPerSample;
        this.numberOfBands = numberOfBands;
        this.requestedTiles = requestedTiles;
        this.tileSize = tileSize;
        this.pixelsPerTile = tileSize.width * tileSize.height;
        this.tileDataLength = (int) Math
                .ceil(((double) pixelsPerTile * (double) bitsPerSample) / 8D);
        this.bitmaskDataLength = (int) Math.ceil(pixelsPerTile / 8D);
        this.noData = noData;
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
        return numberOfBands;
    }

    /**
     * @see org.geotools.arcsde.gce.TileReader#getTileWidth()
     */
    public int getTileWidth() {
        return tileSize.width;
    }

    /**
     * @see org.geotools.arcsde.gce.TileReader#getTileHeight()
     */
    public int getTileHeight() {
        return tileSize.height;
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
            try {
                nextTile = row.getRasterTile();
                started = true;
                if (nextTile == null) {
                    LOGGER.fine("No tiles to fetch at all, releasing connection");
                }
            } catch (SeException e) {
                throw new ArcSdeException(e);
            }
        }
        return nextTile != null;
    }

    /**
     * @see org.geotools.arcsde.gce.TileReader#next(byte[])
     */
    public TileInfo next(byte[] tileData) throws IOException {
        if (tileData == null) {
            throw new IllegalArgumentException("tileData is null");
        }

        final SeRasterTile tile;

        if (hasNext()) {
            tile = nextTile();
        } else {
            throw new IllegalStateException("There're no more tiles to fetch");
        }

        final byte[] bitMaskData = tile.getBitMaskData();

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(" >> Fetching " + tile + " - bitmask: " + bitMaskData.length
                    + " has more: " + hasNext());
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
    }

    private SeRasterTile nextTile() throws IOException {
        if (nextTile == null) {
            throw new EOFException("No more tiles to read");
        }
        SeRasterTile curr = nextTile;
        try {
            nextTile = row.getRasterTile();
            if (nextTile == null) {
                LOGGER.finer("There're no more tiles to fetch");
            }
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
        return curr;
    }

}
