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

import static org.geotools.arcsde.raster.info.RasterCellType.TYPE_16BIT_S;
import static org.geotools.arcsde.raster.info.RasterCellType.TYPE_16BIT_U;
import static org.geotools.arcsde.raster.info.RasterCellType.TYPE_1BIT;
import static org.geotools.arcsde.raster.info.RasterCellType.TYPE_32BIT_S;
import static org.geotools.arcsde.raster.info.RasterCellType.TYPE_32BIT_U;
import static org.geotools.arcsde.raster.info.RasterCellType.TYPE_64BIT_REAL;
import static org.geotools.arcsde.raster.info.RasterCellType.TYPE_8BIT_S;
import static org.geotools.arcsde.raster.info.RasterCellType.TYPE_8BIT_U;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.raster.info.RasterCellType;
import org.geotools.util.logging.Logging;

/**
 * A {@link TileReader} decorator
 * 
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.6
 */
final class PromotingTileReader implements TileReader {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    private final TileReader nativeReader;

    private final RasterCellType targetType;

    private final SampleDepthPromoter promoter;

    private final BitmaskToNoDataConverter noData;

    public PromotingTileReader(final TileReader nativeTileReader, final RasterCellType sourceType,
            final RasterCellType targetType, final BitmaskToNoDataConverter noData) {

        this.nativeReader = nativeTileReader;
        this.targetType = targetType;
        this.noData = noData;
        this.promoter = SampleDepthPromoter.createFor(sourceType, targetType);
        LOGGER.fine("Using sample depth promoting tile reader, from " + sourceType + " to "
                + targetType);
    }

    public int getBitsPerSample() {
        return targetType.getBitsPerSample();
    }

    public int getBytesPerTile() {
        double pixelsPerTile = getPixelsPerTile();
        double bitsPerSample = getBitsPerSample();
        int bytesPerTile = (int) Math.floor((pixelsPerTile * bitsPerSample) / 8D);
        return bytesPerTile;
    }

    public int getNumberOfBands() {
        return nativeReader.getNumberOfBands();
    }

    public int getPixelsPerTile() {
        return nativeReader.getPixelsPerTile();
    }

    public int getTileHeight() {
        return nativeReader.getTileHeight();
    }

    public int getTileWidth() {
        return nativeReader.getTileWidth();
    }

    public int getTilesHigh() {
        return nativeReader.getTilesHigh();
    }

    public int getTilesWide() {
        return nativeReader.getTilesWide();
    }

    /**
     * @see org.geotools.arcsde.raster.io.TileReader#getTile(int, int)
     */
    // public TileInfo[] getTile(final int tileX, final int tileY) throws IOException {
    // final int numberOfBands = getNumberOfBands();
    // final TileInfo[] promotedBandInfo = new TileInfo[numberOfBands];
    //
    // try {
    // final TileInfo[] nativeBandInfo = nativeReader.getTile(tileX, tileY);
    //
    // for (int bandN = 0; bandN < numberOfBands; bandN++) {
    // TileInfo nativeData = nativeBandInfo[bandN];
    //
    // TileInfo promotedData = promoter.promote(nativeData);
    //
    // nativeBandInfo[bandN] = null;// release early release often...
    //
    // noData.setNoData(promotedData);
    // promotedBandInfo[bandN] = promotedData;
    // }
    // } catch (IOException e) {
    // dispose();
    // throw e;
    // } catch (RuntimeException e) {
    // dispose();
    // throw e;
    // }
    // return promotedBandInfo;
    // }

    private TileInfo[] setNoData(TileInfo[] tileInfos) {
        final int numberOfBands = getNumberOfBands();
        final TileInfo[] promotedBandInfo = new TileInfo[numberOfBands];
        try {
            for (int bandN = 0; bandN < numberOfBands; bandN++) {
                TileInfo nativeData = tileInfos[bandN];
                TileInfo promotedData = promoter.promote(nativeData);
                noData.setNoData(promotedData);
                promotedBandInfo[bandN] = promotedData;
            }
        } catch (RuntimeException e) {
            dispose();
            throw e;
        }
        return promotedBandInfo;
    }

    public TileInfo[] getTile(int tileX, int tileY, byte[][] data) throws IOException {
        TileInfo[] tileInfos = nativeReader.getTile(tileX, tileY, data);
        return setNoData(tileInfos);
    }

    public TileInfo[] getTile(int tileX, int tileY, short[][] data) throws IOException {
        TileInfo[] tileInfos = nativeReader.getTile(tileX, tileY, data);
        return setNoData(tileInfos);
    }

    public TileInfo[] getTile(int tileX, int tileY, int[][] data) throws IOException {
        TileInfo[] tileInfos = nativeReader.getTile(tileX, tileY, data);
        return setNoData(tileInfos);
    }

    public TileInfo[] getTile(int tileX, int tileY, float[][] data) throws IOException {
        TileInfo[] tileInfos = nativeReader.getTile(tileX, tileY, data);
        return setNoData(tileInfos);
    }

    public TileInfo[] getTile(int tileX, int tileY, double[][] data) throws IOException {
        TileInfo[] tileInfos = nativeReader.getTile(tileX, tileY, data);
        return setNoData(tileInfos);
    }

    /**
     * 
     * @author Gabriel Roldan
     */
    private static abstract class SampleDepthPromoter {

        public abstract TileInfo promote(TileInfo nativeData);

        public static SampleDepthPromoter createFor(final RasterCellType source,
                final RasterCellType target) {

            if (source == TYPE_1BIT && target == RasterCellType.TYPE_8BIT_U) {
                return new OneBitToUchar();
            } else if (source == TYPE_8BIT_U && target == TYPE_16BIT_U) {
                return new UcharToUshort();
            } else if (source == TYPE_16BIT_S && target == TYPE_32BIT_S) {
                return new ShortToInt();
            } else if (source == TYPE_8BIT_S && target == TYPE_16BIT_S) {
                return new ByteToShort();
            } else if (source == TYPE_16BIT_U && target == TYPE_32BIT_U) {
                return new UShortToUInt();
            } else if (source == TYPE_32BIT_U && target == TYPE_64BIT_REAL) {
                return new UIntToDouble();
            } else if (source == TYPE_32BIT_S && target == TYPE_64BIT_REAL) {
                return new IntToDouble();
            }

            UnsupportedOperationException exception = new UnsupportedOperationException(
                    "Promoting from " + source + " to " + target + " not yet implemented");
            LOGGER.log(Level.WARNING, "Can't promote", exception);
            throw exception;
        }
    }

    private static final class UcharToUshort extends SampleDepthPromoter {
        @Override
        public TileInfo promote(final TileInfo nativeData) {

            short[] promotedPixels = nativeData.getTileDataAsUnsignedShorts();
            nativeData.setTileData(promotedPixels);

            return nativeData;
        }
    }

    private static final class OneBitToUchar extends SampleDepthPromoter {

        @Override
        public TileInfo promote(TileInfo nativeData) {
            // no need to promote, should be already stored as bytes
            return nativeData;
        }
    }

    private static final class ShortToInt extends SampleDepthPromoter {

        @Override
        public TileInfo promote(TileInfo nativeData) {

            int[] promotedPixels = nativeData.getTileDataAsIntegers();
            nativeData.setTileData(promotedPixels);

            return nativeData;
        }
    }

    /**
     * Promotes signed byte to signed short tile data
     * 
     * @author Gabriel Roldan
     */
    private static final class ByteToShort extends SampleDepthPromoter {
        @Override
        public TileInfo promote(TileInfo nativeData) {

            short[] promotedPixels = nativeData.getTileDataAsShorts();
            nativeData.setTileData(promotedPixels);
            return nativeData;
        }
    }

    /**
     * Promotes unsigned short to unsigned int tile data
     * 
     * @author Gabriel Roldan
     */
    private static final class UShortToUInt extends SampleDepthPromoter {
        @Override
        public TileInfo promote(TileInfo nativeData) {

            int[] promotedPixels = nativeData.getTileDataAsIntegers();
            nativeData.setTileData(promotedPixels);

            return nativeData;
        }
    }

    /**
     * Promotes unsigned int to double tile data
     * 
     * @author Gabriel Roldan
     */
    private static final class UIntToDouble extends SampleDepthPromoter {
        @Override
        public TileInfo promote(TileInfo nativeData) {

            double[] promotedPixels = nativeData.getTileDataAsDoubles();
            nativeData.setTileData(promotedPixels);

            return nativeData;
        }
    }

    /**
     * Promotes int to double tile data
     * 
     * @author Gabriel Roldan
     */
    private static final class IntToDouble extends SampleDepthPromoter {
        @Override
        public TileInfo promote(TileInfo nativeData) {

            double[] promotedPixels = nativeData.getTileDataAsDoubles();
            nativeData.setTileData(promotedPixels);

            return nativeData;
        }
    }

    /**
     * @see org.geotools.arcsde.raster.io.TileReader#dispose()
     */
    public void dispose() {
        this.nativeReader.dispose();
    }

    public int getMinTileX() {
        return nativeReader.getMinTileX();
    }

    public int getMinTileY() {
        return nativeReader.getMinTileY();
    }

    public int getPyramidLevel() {
        return nativeReader.getPyramidLevel();
    }

    public long getRasterId() {
        return nativeReader.getRasterId();
    }

    public int toRealTileX(final int tileX) {
        return nativeReader.toRealTileX(tileX);
    }

    public int toRealTileY(final int tileY) {
        return nativeReader.toRealTileY(tileY);
    }
}
