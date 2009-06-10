package org.geotools.arcsde.gce;

import java.io.IOException;

interface TileReader {

    public class TileInfo {
        private Long bandId;

        private byte[] bitmaskData;

        private int numPixelsRead;

        public TileInfo(Long bandId, byte[] bitMaskData, int numPixelsRead) {
            this.bandId = bandId;
            this.bitmaskData = bitMaskData;
            this.numPixelsRead = numPixelsRead;
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
    public abstract TileInfo next(byte[] tileData) throws IOException;

}