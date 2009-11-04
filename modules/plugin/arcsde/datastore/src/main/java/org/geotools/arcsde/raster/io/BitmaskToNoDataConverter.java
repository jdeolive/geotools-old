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

import static org.geotools.arcsde.raster.info.RasterCellType.TYPE_8BIT_U;

import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.geotools.arcsde.raster.info.RasterCellType;
import org.geotools.arcsde.raster.info.RasterDatasetInfo;

/**
 * A helper class to set nodata values directly onto the arcsde tile's returned {@code byte[]}
 * 
 * @author Gabriel Roldan
 * @since 2.5.6
 * @version $Id$
 * 
 */
class BitmaskToNoDataConverter {

    public static final BitmaskToNoDataConverter NO_ACTION_CONVERTER = new BitmaskToNoDataConverter(
            0, 0, null) {

        @Override
        public void setNoData(int sampleN, TileInfo tile) {
            // no action
        }

        @Override
        public void setAll(TileInfo tile) {
            // no action
        }

        @Override
        public void setNoData(TileInfo tile) {
            // no action
        }

    };

    protected final int pixelsPerTile;

    protected final Map<Long, Number> byBandIdNoDataValues;

    protected final int bitsPerSample;

    /**
     * 
     * @param pixelsPerTile
     * @param bitsPerSample
     * @param byBandIdNoDataValues
     */
    private BitmaskToNoDataConverter(final int pixelsPerTile, final int bitsPerSample,
            final Map<Long, Number> byBandIdNoDataValues) {

        this.pixelsPerTile = pixelsPerTile;
        this.bitsPerSample = bitsPerSample;
        this.byBandIdNoDataValues = byBandIdNoDataValues;

    }

    /**
     * Creates a "nodata setter" for the given raster determined by the raster dataset and the
     * raster index inside the dataset
     * 
     * @param rasterInfo
     * @param rasterIndex
     * @return
     */
    public static BitmaskToNoDataConverter getInstance(final RasterDatasetInfo rasterInfo,
            final long rasterId) {

        final int rasterIndex = rasterInfo.getRasterIndex(rasterId);
        final int numBands = rasterInfo.getNumBands();
        final RasterCellType targetType = rasterInfo.getTargetCellType(rasterIndex);

        // Map<Long, byte[]> byBandIdNoDataValues = new HashMap<Long, byte[]>();
        Map<Long, Number> byBandIdNoDataValues = new HashMap<Long, Number>();

        Dimension tileDimension = rasterInfo.getTileDimension(rasterIndex);
        final int samplesPerTile = tileDimension.width * tileDimension.height;

        for (int bandN = 0; bandN < numBands; bandN++) {
            long bandId = rasterInfo.getBand(rasterIndex, bandN).getBandId();
            Number noDataValue = rasterInfo.getNoDataValue(rasterIndex, bandN);
            // byte[] noDataValueBytes = toBytes(noDataValue, targetType);
            byBandIdNoDataValues.put(Long.valueOf(bandId), noDataValue);
        }

        final int bitsPerSample = targetType.getBitsPerSample();
        BitmaskToNoDataConverter noDataSetter;
        if (targetType == TYPE_8BIT_U) {
            noDataSetter = new Unsigned8bitConverter(samplesPerTile, bitsPerSample,
                    byBandIdNoDataValues);
        } else {
            noDataSetter = new BitmaskToNoDataConverter(samplesPerTile, bitsPerSample,
                    byBandIdNoDataValues);
        }

        return noDataSetter;
    }

    static byte[] toBytes(final Number noDataValue, final RasterCellType targetType) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream writer = new DataOutputStream(out);

        try {
            switch (targetType) {
            case TYPE_16BIT_S:
                writer.writeShort(noDataValue.intValue());
                break;
            case TYPE_16BIT_U:
                writer.writeShort(noDataValue.intValue());
                break;
            case TYPE_32BIT_REAL:
                writer.writeFloat(noDataValue.floatValue());
                break;
            case TYPE_32BIT_S:
                writer.writeInt(noDataValue.intValue());
                break;
            case TYPE_32BIT_U:
                writer.writeInt(noDataValue.intValue());
                break;
            case TYPE_64BIT_REAL:
                writer.writeDouble(noDataValue.doubleValue());
                break;
            case TYPE_8BIT_S:
                writer.writeByte(noDataValue.byteValue());
                break;
            case TYPE_8BIT_U:
                writer.writeByte(noDataValue.intValue());
                break;
            default:
                throw new UnsupportedOperationException(
                        "No no-data converter exists for sample type " + targetType);
            }
        } catch (IOException e) {
            throw new RuntimeException("Can't happen!", e);
        }

        byte[] no_data_bytes = out.toByteArray();
        return no_data_bytes;
    }

    /**
     * Returns whether the sample N in the bitmask byte array is marked as a no-data pixel
     */
    protected final boolean isNoData(int sampleN, byte[] bitmaskData) {
        boolean isNoData = ((bitmaskData[sampleN / 8] >> (7 - (sampleN % 8))) & 0x01) == 0x00;
        return isNoData;
    }

    /**
     * Sets all the samples of {@code tileData} marked as no-data pixel in {@code bitmaskData} to
     * the no-data value for band {@code bandId}
     */
    public void setNoData(final TileInfo tileInfo) {
        final byte[] bitmaskData = tileInfo.getBitmaskData();
        final int numPixelsRead = tileInfo.getNumPixelsRead();
        final boolean hasNoDataPixels = bitmaskData.length > 0;

        if (numPixelsRead == 0) {
            setAll(tileInfo);
        } else if (hasNoDataPixels) {
            final int numSamples = tileInfo.getNumPixels();
            assert numPixelsRead == numSamples;

            for (int sampleN = 0; sampleN < numSamples; sampleN++) {
                if (isNoData(sampleN, bitmaskData)) {
                    setNoData(sampleN, tileInfo);
                }
            }
        }
    }

    /**
     * Sets all the samples in {@code tileData} to the no-data value for the band {@code bandId}
     * <p>
     * Default implementation is to call {@link #setNoData(Long, int, byte[])} as many times as
     * number of samples in a tile. Subclasses may override to optimize.
     * </p>
     */
    protected void setAll(final TileInfo tile) {
        for (int sampleN = 0; sampleN < pixelsPerTile; sampleN++) {
            setNoData(sampleN, tile);
        }
    }

    /**
     * Sets the sample N for the band {@code bandId} on {@code tileData} to the no-data value
     */
    protected void setNoData(final int sampleN, final TileInfo tileData) {
        final Long bandId = tileData.getBandId();
        // byte[] noData = byBandIdNoDataValues.get(bandId);
        Number noData = byBandIdNoDataValues.get(bandId);
        // int pixArrayOffset = (sampleN * bitsPerSample) / 8;
        // System.arraycopy(noData, 0, tileData, pixArrayOffset, noData.length);
        tileData.setValue(sampleN, noData);
    }

    /**
     * A subclass that provides some optimization for the case where the target cell type is
     * {@link RasterCellType#TYPE_8BIT_U}
     */
    static final class Unsigned8bitConverter extends BitmaskToNoDataConverter {

        public Unsigned8bitConverter(final int samplesPerTile, final int bitsPerSample,
                final Map<Long, Number> byBandIdNoDataValues) {
            super(samplesPerTile, bitsPerSample, byBandIdNoDataValues);
        }

        /**
         * Overrides to use the faster {@link Arrays#fill(byte[], byte)} method rather than calling
         * {@link #setNoData(Long, int, byte[])} {@code samplesPerTile} times
         */
        @Override
        public void setAll(final TileInfo tileData) {
            final Long bandId = tileData.getBandId();
            byte noDataValue = byBandIdNoDataValues.get(bandId).byteValue();
            byte[] data = tileData.getTileDataAsBytes();
            Arrays.fill(data, noDataValue);
        }

        @Override
        public void setNoData(int sampleN, TileInfo tileData) {
            final Long bandId = tileData.getBandId();
            final byte noDataValue = byBandIdNoDataValues.get(bandId).byteValue();
            byte[] data = tileData.getTileDataAsBytes();
            data[sampleN] = noDataValue;
        }
    }

}
