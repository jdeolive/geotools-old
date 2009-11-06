/**
 * 
 */
package org.geotools.arcsde.raster.jai;

import java.awt.Point;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;

import javax.imageio.ImageTypeSpecifier;

import org.apache.commons.collections.map.LRUMap;
import org.geotools.arcsde.raster.io.TileReader;

import com.sun.media.imageioimpl.common.SimpleRenderedImage;
import com.sun.media.jai.codecimpl.util.DataBufferDouble;

@SuppressWarnings("unchecked")
class ArcSDETiledRenderedImage extends SimpleRenderedImage {

    private final TileReader tileReader;

    private final SampleModel tileSampleModel;

    public ArcSDETiledRenderedImage(final TileReader tileReader, final ImageTypeSpecifier typeSpec) {
        this.tileReader = tileReader;
        super.colorModel = typeSpec.getColorModel();
        super.sampleModel = typeSpec.getSampleModel();
        super.height = tileReader.getTilesHigh() * tileReader.getTileHeight();
        super.width = tileReader.getTilesWide() * tileReader.getTileWidth();
        super.minX = 0;
        super.minY = 0;
        super.tileGridXOffset = 0;
        super.tileGridYOffset = 0;
        super.tileHeight = tileReader.getTileHeight();
        super.tileWidth = tileReader.getTileWidth();
        this.tileSampleModel = super.sampleModel.createCompatibleSampleModel(tileWidth, tileHeight);

    }

    /**
     * @see java.awt.image.RenderedImage#getTile(int, int)
     */
    public Raster getTile(final int tileX, final int tileY) {
        // System.err.printf("getTile(%d, %d) %s\n", tileX, tileY, this.toString());

        final TileKey key = newKey(tileX, tileY);
        WritableRaster currentTile = getCached(key);
        if (currentTile != null) {
            return currentTile;
        }

        final int xOrigin = tileXToX(tileX);
        final int yOrigin = tileYToY(tileY);

        // final int numBands = tileSampleModel.getNumBands();

        currentTile = Raster.createWritableRaster(tileSampleModel, new Point(xOrigin, yOrigin));
        final DataBuffer dataBuffer = currentTile.getDataBuffer();
        try {
            switch (tileSampleModel.getDataType()) {
            case DataBuffer.TYPE_BYTE: {
                byte[][] data = ((DataBufferByte) dataBuffer).getBankData();
                tileReader.getTile(tileX, tileY, data);
            }
                break;
            case DataBuffer.TYPE_USHORT: {
                short[][] data = ((DataBufferUShort) dataBuffer).getBankData();
                tileReader.getTile(tileX, tileY, data);
            }
                break;
            case DataBuffer.TYPE_SHORT: {
                short[][] data = ((DataBufferShort) dataBuffer).getBankData();
                tileReader.getTile(tileX, tileY, data);
            }
                break;
            case DataBuffer.TYPE_INT: {
                int[][] data = ((DataBufferInt) dataBuffer).getBankData();
                tileReader.getTile(tileX, tileY, data);
            }
                break;
            case DataBuffer.TYPE_FLOAT: {
                float[][] data = ((DataBufferFloat) dataBuffer).getBankData();
                tileReader.getTile(tileX, tileY, data);
            }
                break;
            case DataBuffer.TYPE_DOUBLE: {
                double[][] data = ((DataBufferDouble) dataBuffer).getBankData();
                tileReader.getTile(tileX, tileY, data);
            }
                break;
            default:
                throw new IllegalStateException("Unrecognized DataBuffer type: "
                        + dataBuffer.getDataType());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // TileInfo[] tileInfo;
        // try {
        // tileInfo = tileReader.getTile(tileX, tileY);
        // } catch (IOException e) {
        // e.printStackTrace();
        // throw new RuntimeException(e);
        // }

        // for (int bandN = 0; bandN < numBands; bandN++) {
        // TileInfo bandData = tileInfo[bandN];
        // bandData.fill(dataBuffer, bandN);
        // }

        cache(key, currentTile);

        return currentTile;
    }

    private TileKey newKey(final int tileX, final int tileY) {
        TileKey tileKey = new TileKey(tileX, tileY);
        return tileKey;
    }

    private LRUMap cache;

    private void cache(TileKey key, WritableRaster tile) {
        if (cache == null) {
            // int tilesWide = tileReader.getTilesWide();
            // int maxCacheSize = Math.min(10, tilesWide);
            cache = new LRUMap(5);
        }
        cache.put(key, tile);
    }

    private WritableRaster getCached(TileKey key) {
        WritableRaster tile = cache == null ? null : (WritableRaster) cache.get(key);
        return tile;
    }

    private static class TileKey {
        private int tileX, tileY;

        public TileKey(int tileX, int tileY) {
            this.tileX = tileX;
            this.tileY = tileY;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof TileKey)) {
                return false;
            }
            TileKey t = (TileKey) o;
            return tileX == t.tileX && tileY == t.tileY;
        }

        @Override
        public int hashCode() {
            return 17 ^ tileX * tileY;
        }

        public int getTileX() {
            return tileX;
        }

        public int getTileY() {
            return tileY;
        }

    }
}