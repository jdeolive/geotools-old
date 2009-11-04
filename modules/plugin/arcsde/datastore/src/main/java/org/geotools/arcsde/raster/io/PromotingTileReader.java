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

    public boolean hasNext() throws IOException {
        return nativeReader.hasNext();
    }

    public TileInfo[] next() throws IOException {
        final int numberOfBands = getNumberOfBands();
        final TileInfo[] promotedBandInfo = new TileInfo[numberOfBands];

        try {
            final TileInfo[] nativeBandInfo = nativeReader.next();

            for (int bandN = 0; bandN < numberOfBands; bandN++) {
                TileInfo nativeData = nativeBandInfo[bandN];
                TileInfo promotedData = promoter.promote(nativeData);
                setNoData(promotedData);
                promotedBandInfo[bandN] = promotedData;
            }
        } catch (IOException e) {
            dispose();
            throw e;
        } catch (RuntimeException e) {
            dispose();
            throw e;
        }
        return promotedBandInfo;
        //
        // final byte[] nativeTileData = tileInfo.getTileData();
        // final byte[] tileData = new byte[getBytesPerTile()];
        // try {
        // final byte[] bitmaskData = tileInfo.getBitmaskData();
        // final boolean hasNoDataPixels = bitmaskData.length > 0;
        // final Long bandId = tileInfo.getBandId();
        //
        // final int numPixelsRead = tileInfo.getNumPixelsRead();
        // if (numPixelsRead == 0) {
        // noData.setAll(bandId, tileData);
        // } else {
        // final int numSamples = getPixelsPerTile();
        // assert numPixelsRead == numSamples;
        //
        // for (int sampleN = 0; sampleN < numSamples; sampleN++) {
        // if (hasNoDataPixels && noData.isNoData(sampleN, bitmaskData)) {
        // noData.setNoData(bandId, sampleN, tileData);
        // } else {
        // promoter.promote(sampleN, nativeTileData, tileData);
        // }
        // }
        // }
        //
        // } catch (RuntimeException e) {
        // dispose();
        // throw e;
        // }
        //
        // TileInfo promotedTileInfo = new TileInfo(tileInfo.getBandId(), tileInfo.getColumnIndex(),
        // tileInfo.getRowIndex(), tileInfo.getNumPixelsRead(), tileData, tileInfo
        // .getBitmaskData());
        // return promotedTileInfo;
    }

    private void setNoData(TileInfo tileInfo) {
        final byte[] bitmaskData = tileInfo.getBitmaskData();
        final boolean hasNoDataPixels = bitmaskData.length > 0;

        if (hasNoDataPixels) {
            final int numPixelsRead = tileInfo.getNumPixelsRead();
            if (numPixelsRead == 0) {
                noData.setAll(tileInfo);
            } else {
                final int numSamples = getPixelsPerTile();
                assert numPixelsRead == numSamples;

                for (int sampleN = 0; sampleN < numSamples; sampleN++) {
                    if (noData.isNoData(sampleN, bitmaskData)) {
                        noData.setNoData(sampleN, tileInfo);
                    }
                }
            }
        }
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
            }

            UnsupportedOperationException exception = new UnsupportedOperationException(
                    "Promoting from " + source + " to " + target + " not yet implemented");
            LOGGER.log(Level.WARNING, "Can't promote", exception);
            throw exception;
        }
    }

    private static final class UcharToUshort extends SampleDepthPromoter {

        // @Override
        // public void promote(int sampleN, byte[] nativeTileData, byte[] tileData) {
        // int pixArrayOffset = 2 * sampleN;
        // tileData[pixArrayOffset] = 0;
        // tileData[pixArrayOffset + 1] = (byte) ((nativeTileData[sampleN] >>> 0) & 0xFF);
        // }

        @Override
        public TileInfo promote(final TileInfo nativeData) {

            short[] promotedPixels = nativeData.getTileDataAsUnsignedShorts();

            TileInfo promotedTileInfo = new TileInfo(nativeData.getBandId(), nativeData
                    .getColumnIndex(), nativeData.getRowIndex(), nativeData.getNumPixelsRead(),
                    nativeData.getBitmaskData());

            promotedTileInfo.setTileData(promotedPixels);

            return promotedTileInfo;
        }
    }

    private static final class OneBitToUchar extends SampleDepthPromoter {

        // @Override
        // public void promote(int sampleN, byte[] nativeTileData, byte[] tileData) {
        // int pixArrayOffset = sampleN / 8;
        // int bit = sampleN % 8;
        // int _byte = nativeTileData[pixArrayOffset];
        // byte ucharvalue = (byte) ((_byte >> (7 - bit)) & 0x01);
        // tileData[sampleN] = ucharvalue;
        // }

        @Override
        public TileInfo promote(TileInfo nativeData) {
            // no need to promote, should be already stored as bytes
            return nativeData;
        }
    }

    private static final class ShortToInt extends SampleDepthPromoter {

        // @Override
        // public void promote(int sampleN, byte[] nativeTileData, byte[] tileData) {
        // int pixArrayOffset = 4 * sampleN;
        //
        // tileData[pixArrayOffset] = 0;
        // tileData[pixArrayOffset + 1] = 0;
        // tileData[pixArrayOffset + 1] = (byte) ((nativeTileData[sampleN] >>> 8) & 0xFF);
        // tileData[pixArrayOffset + 1] = (byte) ((nativeTileData[sampleN] >>> 0) & 0xFF);
        // }

        @Override
        public TileInfo promote(TileInfo nativeData) {

            int[] promotedPixels = nativeData.getTileDataAsIntegers();

            TileInfo promotedTileInfo = new TileInfo(nativeData.getBandId(), nativeData
                    .getColumnIndex(), nativeData.getRowIndex(), nativeData.getNumPixelsRead(),
                    nativeData.getBitmaskData());

            promotedTileInfo.setTileData(promotedPixels);

            return promotedTileInfo;
        }
    }

    /**
     * @see org.geotools.arcsde.raster.io.TileReader#dispose()
     */
    public void dispose() {
        this.nativeReader.dispose();
    }
}
