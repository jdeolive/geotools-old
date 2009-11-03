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

import java.io.IOException;

/**
 * Offers an iterator like interface to read ArcSDE raster tiles into a {@code byte[]}
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @since 2.5.4
 * @version $Id$
 * @source $URL:
 *         http://svn.osgeo.org/geotools/trunk/modules/plugin/arcsde/datastore/src/main/java/org
 *         /geotools/arcsde/gce/TileReader.java $
 */
public interface TileReader {

    public class TileInfo {
        private final long bandId;

        private final byte[] bitmaskData;

        private final int numPixelsRead;

        private final byte[] tileDta;

        private final int columnIndex;

        private final int rowIndex;

        public TileInfo(long bandId, int colIndex, int rowIndex, int numPixelsRead,
                byte[] tileData, byte[] bitMaskData) {
            this.bandId = bandId;
            this.columnIndex = colIndex;
            this.rowIndex = rowIndex;
            this.numPixelsRead = numPixelsRead;
            this.tileDta = tileData;
            this.bitmaskData = bitMaskData;
        }

        public Long getBandId() {
            return bandId;
        }

        public byte[] getBitmaskData() {
            return bitmaskData;
        }

        public int getNumPixelsRead() {
            return numPixelsRead;
        }

        public byte[] getTileData() {
            return tileDta;
        }

        public int getColumnIndex() {
            return columnIndex;
        }

        public int getRowIndex() {
            return rowIndex;
        }
    }

    /**
     * @return number of bits per sample
     */
    public abstract int getBitsPerSample();

    /**
     * @return number of samples per tile
     */
    public abstract int getPixelsPerTile();

    /**
     * @return numbre of bands being fetched
     */
    public abstract int getNumberOfBands();

    /**
     * @return number of pixels per tile over the X axis
     */
    public abstract int getTileWidth();

    /**
     * @return number of pixels per tile over the Y axis
     */
    public abstract int getTileHeight();

    /**
     * @return number of tiles being fetched over the X axis
     */
    public abstract int getTilesWide();

    /**
     * @return number of tiles being fetched over the Y axis
     */
    public abstract int getTilesHigh();

    /**
     * @return number of bytes in the raw pixel content of a tile, not taking into account any
     *         trailing bitmask data.
     */
    public abstract int getBytesPerTile();

    /**
     * @return whether there are more tiles to fetch
     * @throws IOException
     */
    public abstract boolean hasNext() throws IOException;

    /**
     * Fetches a tile and fills {@code tileData} with its raw pixel data packaged as bytes according
     * to the number of bits per sample
     * 
     * @param tileData
     *            a possibly {@code null} array where to store the next tile data. If {@code null} a
     *            new byte[] of length {@link #getBytesPerTile()} will be allocated and filled up
     *            with the raw tile pixel data.
     * @return the bitmask data, or an empty array if the tile is full
     * @throws IOException
     * @throws {@link IllegalArgumentException} if tileData is not null and its size is less than
     *         {@link #getBytesPerTile()}
     */
    public abstract TileInfo next() throws IOException;

    /**
     * Disposes any resource being held by this TileReader, making the TileReader unusable and the
     * behaviour of {@link #hasNext()} and {@link #next} unpredictable
     */
    public abstract void dispose();

}