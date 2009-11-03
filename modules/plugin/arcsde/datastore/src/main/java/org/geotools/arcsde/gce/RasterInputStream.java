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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.ImageInputStream;

import org.geotools.arcsde.gce.TileReader.TileInfo;

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
final class RasterInputStream extends InputStream {

    private final TileReader tileReader;

    private final int tileDataLength;

    private byte[] currTileData;

    private int currTileDataIndex;

    private final int length;

    private long streamPos;

    ByteArrayInputStream in;
    byte[] buf;

    public RasterInputStream(final TileReader tileReader) {
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

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            while (tileReader.hasNext()) {
                TileInfo next = tileReader.next();
                out.write(next.getTileData());
            }
            out.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        buf = out.toByteArray();
        this.in = new ByteArrayInputStream(buf);

        if (length != buf.length) {
            throw new IllegalStateException(length + " != " + buf.length);
        }
    }

    @Override
    public int read() throws IOException {
        return in.read();
        // final byte[] data = getTileData();
        // if (data == null) {
        // return -1;
        // }
        // byte b = data[currTileDataIndex];
        // ++currTileDataIndex;
        // return b;
    }

    @Override
    public int read(byte[] buff, int off, int len) throws IOException {
        return in.read(buff, off, len);
        // // System.err.println("read " + len + ", pos = " + streamPos + ", length = " + length);
        // final byte[] data = getTileData();
        // if (data == null) {
        // return -1;
        // }
        // final int available = data.length - currTileDataIndex;
        // final int count = Math.min(available, len);
        // System.arraycopy(data, currTileDataIndex, buff, off, count);
        // currTileDataIndex += count;
        // streamPos += count;
        // if (streamPos == length) {
        // close();
        // }
        // return count;
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
            TileInfo tileInfo = tileReader.next();
            currTileData = tileInfo.getTileData();
        }
        return currTileData;
    }

    @Override
    public void close() throws IOException {
        tileReader.dispose();
    }

    @Override
    public int available() throws IOException {
        return in.available();
        // return tileDataLength - currTileDataIndex;
    }

    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
        // System.err.println("mark at " + readlimit);
    }

    @Override
    public boolean markSupported() {
        return in.markSupported();
        // return false;
    }

    @Override
    public long skip(long n) throws IOException {
        return in.skip(n);
        // return super.skip(n);
    }
}