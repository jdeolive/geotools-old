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

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

/**
 * An {@link ImageInputStream} that reads ArcSDE raster tiles in a band interleaved order.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @since 2.5.4
 * @version $Id$
 * @source $URL:
 *         http://svn.osgeo.org/geotools/trunk/modules/plugin/arcsde/datastore/src/main/java/org
 *         /geotools/arcsde/gce/ArcSDETiledImageInputStream.java $
 */
final class ArcSDETiledImageInputStream extends ImageInputStreamImpl implements ImageInputStream {

    private final TileReader tileReader;

    private final int tileDataLength;

    private final byte[] currTileData;

    private int currTileDataIndex;

    private int currTileIndex = -1;

    private final int length;

    public ArcSDETiledImageInputStream(final TileReader tileReader) {
        super();
        this.tileReader = tileReader;
        final int bytesPerTile = tileReader.getBytesPerTile();
        this.tileDataLength = bytesPerTile;
        this.currTileData = new byte[bytesPerTile];
        // force load at the first read invocation
        this.currTileDataIndex = tileDataLength;

        final int tilesWide = tileReader.getTilesWide();
        final int tilesHigh = tileReader.getTilesHigh();
        final int numberOfBands = tileReader.getNumberOfBands();

        length = bytesPerTile * tilesWide * tilesHigh * numberOfBands;
    }

    /**
     * Returns the computed lenght of the stream based on the tile dimensions, number of tiles,
     * number of bands, and bits per sample
     */
    @Override
    public long length() {
        return length;
    }

    @Override
    public int read() throws IOException {
        final byte[] data = getTileData();
        if (data == null) {
            return -1;
        }
        byte b = data[currTileDataIndex];
        ++currTileDataIndex;
        ++streamPos;
        return b;
    }

    @Override
    public int read(byte[] buff, int off, int len) throws IOException {
        final byte[] data = getTileData();
        if (data == null) {
            return -1;
        }
        final int available = data.length - currTileDataIndex;
        final int count = Math.min(available, len);
        System.arraycopy(data, currTileDataIndex, buff, off, count);
        currTileDataIndex += count;
        streamPos += count;
        return count;
    }

    /**
     * Fetches a tile from the {@code tileReader} if necessary and returns the current tile data.
     * <p>
     * It is needed to fetch a new tile if {@link #currTileDataIndex} indicates all the current tile
     * data has been already read. If so, {@code currTileDataIndex} is reset to 0. The {@code read}
     * operations are responsible of incrementing {@code currTileDataIndex} depending on how many
     * bytes have been consumed from the tile data returned by this method.
     * </p>
     * 
     * @return {@code null} if there's no more tiles to fetch, the current tile data otherwise
     * @throws IOException
     */
    private byte[] getTileData() throws IOException {
        if (currTileDataIndex == tileDataLength) {
            if (!tileReader.hasNext()) {
                return null;
            }

            currTileDataIndex = 0;
            ++currTileIndex;
            tileReader.next(currTileData);
        }
        return currTileData;
    }

    @Override
    public void close() throws IOException {
        tileReader.dispose();
        super.close();
    }

    // @Override
    public void _seek(final long newPos) throws IOException {
        if (newPos == streamPos) {
            //System.err.println("seek to currPos " + newPos + ". currPos = " + streamPos);
            return;
        }
//        if (newPos < streamPos) {
//            System.err.println("back seek to " + newPos + ". currPos = " + streamPos);
//        }else{
//            System.err.println("Forward seek to " + newPos + ". currPos = " + streamPos);
//        }
        super.seek(newPos);
//        if (newPos >= length) {
//            //System.err.println("---> seek to " + newPos + " > length: " + length);
//            return;
//        }
//
//        // force tile for newPos to be loaded
//        //this.currTileDataIndex = tileDataLength;
//        
//        
//        final long actualPos = ((long)tileDataLength * currTileIndex) + currTileDataIndex;
//        if(newPos > actualPos){
//            System.err.println("---> seek to " + newPos + " > actualPos: " + actualPos);
//            int delta = (int) (newPos - actualPos);
//            int newDataIndex = currTileDataIndex + delta;
//            while(newDataIndex > tileDataLength){
//                // force tile for newPos to be loaded
//                this.currTileDataIndex = tileDataLength;
//                getTileData();
//                newDataIndex -= tileDataLength;
//            }
//            this.currTileDataIndex = newDataIndex;
//        }
    }

    @Override
    public boolean isCached() {
        return true;
    }

    @Override
    public boolean isCachedMemory() {
        return true;
    }
}