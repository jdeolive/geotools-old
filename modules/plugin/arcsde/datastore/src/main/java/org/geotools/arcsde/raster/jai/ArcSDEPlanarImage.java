package org.geotools.arcsde.raster.jai;

import java.awt.Point;
import java.awt.image.ColorModel;
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
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.TileCache;

import org.geotools.arcsde.raster.io.TileReader;
import org.geotools.util.logging.Logging;

import com.sun.media.jai.codecimpl.util.DataBufferDouble;
import com.sun.media.jai.util.ImageUtil;

@SuppressWarnings("unchecked")
public class ArcSDEPlanarImage extends PlanarImage {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.raster.jai");

    private TileReader tileReader;

    private final SampleModel tileSampleModel;

    private final BigInteger UID;

    public ArcSDEPlanarImage(TileReader tileReader, int minX, int minY, int width, int height,
            int tileGridXOffset, int tileGridYOffset, SampleModel tileSampleModel,
            ColorModel colorModel) {

        this.tileReader = tileReader;
        this.tileSampleModel = tileSampleModel;

        super.minX = minX;
        super.minY = minY;
        super.width = width;
        super.height = height;
        super.tileGridXOffset = tileGridXOffset;
        super.tileGridYOffset = tileGridYOffset;
        super.tileWidth = tileReader.getTileWidth();
        super.tileHeight = tileReader.getTileHeight();

        super.colorModel = colorModel;
        super.sampleModel = tileSampleModel;
        this.UID = (BigInteger) ImageUtil.generateID(this);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public SampleModel getSampleModel() {
        return sampleModel;
    }

    @Override
    public int hashCode() {
        final int pyramidLevel = tileReader == null ? 1 : tileReader.getPyramidLevel();
        final int rasterId = tileReader == null ? 1 : (int) tileReader.getRasterId();
        return 17 * pyramidLevel + rasterId;
    }

    @Override
    public BigInteger getImageID() {
        return UID;
    }

    private int lastTileX, lastTileY;
    private WritableRaster lastTile;
    
    /**
     * @see java.awt.image.RenderedImage#getTile(int, int)
     */
    @Override
    public Raster getTile(final int tileX, final int tileY) {
        if(lastTileX == tileX && lastTileY == tileY && lastTile != null){
            return lastTile;
        }
        
        final boolean useCache = false;
        final JAI jai = JAI.getDefaultInstance();
        final TileCache jaiCache = jai.getTileCache();

        if (useCache && jaiCache != null) {
            Raster tile = jaiCache.getTile(this, tileX, tileY);
            if (tile != null) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("! GOT TILE FROM TileCache " + tileX + ", " + tileY + ", plevel "
                            + tileReader.getPyramidLevel());
                }
                return tile;
            }
        }

        // System.err.printf("getTile(%d, %d) %s\n", tileX, tileY, this.toString());

        final int xOrigin = tileXToX(tileX);
        final int yOrigin = tileYToY(tileY);

//        if (shallIgnoreTile(tileX, tileY)) {
//            // not a requested tile
//            return createWritableRaster(tileSampleModel, new Point(xOrigin, yOrigin));
//        }
        // final int numBands = tileSampleModel.getNumBands();

        final WritableRaster currentTile;
        currentTile = createWritableRaster(tileSampleModel, new Point(xOrigin, yOrigin));

        final int readerTileX = tileX - tileReader.getMinTileX();
        final int readerTileY = tileY - tileReader.getMinTileY();

        DataBuffer dataBuffer = currentTile.getDataBuffer();
        try {
            switch (tileSampleModel.getDataType()) {
            case DataBuffer.TYPE_BYTE: {
                byte[][] data = ((DataBufferByte) dataBuffer).getBankData();
                tileReader.getTile(tileX, tileY, data);
            }
                break;
            case DataBuffer.TYPE_USHORT: {
                short[][] data = ((DataBufferUShort) dataBuffer).getBankData();
                //tileReader.getTile(readerTileX, readerTileY, data);
                
                tileReader.getTile(tileX, tileY, data);
                
                // TileInfo[] tile = tileReader.getTile(readerTileX, readerTileY, (short[][]) null);
                // short[][] data = new short[tile.length][];
                // int size = 0;
                // for (int i = 0; i < tile.length; i++) {
                // size = tile[i].getNumPixels();
                // data[i] = tile[i].getTileDataAsUnsignedShorts();
                // }
                // dataBuffer = new DataBufferUShort(data, size);
                // currentTile = Raster.createWritableRaster(tileSampleModel, dataBuffer, new Point(
                // xOrigin, yOrigin));
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
            jaiCache.add(this, tileX, tileY, currentTile);
        }
        
        lastTileX = tileX;
        lastTileY = tileY;
        lastTile = currentTile;
        
        return currentTile;
    }

    private boolean shallIgnoreTile(int tx, int ty) {
        int minTileX = tileReader.getMinTileX();
        int minTileY = tileReader.getMinTileY();
        int tilesWide = tileReader.getTilesWide();
        int tilesHigh = tileReader.getTilesHigh();

        return tx < minTileX || ty < minTileY || tx > minTileX + tilesWide
                || ty > minTileY + tilesHigh;
    }

}
