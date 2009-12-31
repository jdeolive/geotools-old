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
import javax.media.jai.JAI;
import javax.media.jai.TileCache;

import org.apache.commons.collections.map.LRUMap;
import org.geotools.arcsde.raster.io.TileReader;

import com.sun.media.imageioimpl.common.SimpleRenderedImage;
import com.sun.media.jai.codecimpl.util.DataBufferDouble;

@SuppressWarnings("unchecked")
public class ArcSDETiledRenderedImage extends SimpleRenderedImage {

    private final TileReader tileReader;

    private final SampleModel tileSampleModel;

    public ArcSDETiledRenderedImage(final TileReader tileReader, final ImageTypeSpecifier typeSpec) {
        this.tileReader = tileReader;
        super.colorModel = typeSpec.getColorModel();
        super.sampleModel = typeSpec.getSampleModel();
        super.minX = 0;
        super.minY = 0;
        super.tileGridXOffset = 0;
        super.tileGridYOffset = 0;
        super.tileHeight = tileReader.getTileHeight();
        super.tileWidth = tileReader.getTileWidth();
        super.height = tileReader.getTilesHigh() * tileWidth;
        super.width = tileReader.getTilesWide() * tileHeight;
        this.tileSampleModel = super.sampleModel.createCompatibleSampleModel(tileWidth, tileHeight);
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public SampleModel getSampleModel() {
        return tileSampleModel;// sampleModel;
    }

    @Override
    public int hashCode() {
        final int pyramidLevel = tileReader.getPyramidLevel();
        return 17 * pyramidLevel;
    }

    /**
     * @see java.awt.image.RenderedImage#getTile(int, int)
     */
    public synchronized Raster getTile(final int tileX, final int tileY) {
        // System.err.printf("getTile(%d, %d) %s\n", tileX, tileY, this.toString());

        final boolean useCache = true;
        final TileCache jaiCache = JAI.getDefaultInstance().getTileCache();
        final int realTileX = tileReader.toRealTileX(tileX);
        final int realTileY = tileReader.toRealTileY(tileY);

        if (useCache && jaiCache != null) {
            Raster tile = jaiCache.getTile(this, realTileX, realTileY);
            if (tile != null) {
                // /System.err.println("! GOT TILE FROM TileCache " + realTileX + ", " +
                // realTileX + ", plevel " + tileReader.getPyramidLevel());
                return tile;
            }
        }

        final int xOrigin = tileXToX(tileX);
        final int yOrigin = tileYToY(tileY);

        // final int numBands = tileSampleModel.getNumBands();

        WritableRaster currentTile = Raster.createWritableRaster(tileSampleModel, new Point(
                xOrigin, yOrigin));
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

        if (useCache && jaiCache != null) {
            jaiCache.add(this, realTileX, realTileY, currentTile);
        }

        return currentTile;
    }

}