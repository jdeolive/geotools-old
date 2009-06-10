package org.geotools.arcsde.gce;

import static org.geotools.arcsde.gce.RasterCellType.TYPE_16BIT_U;
import static org.geotools.arcsde.gce.RasterCellType.TYPE_1BIT;
import static org.geotools.arcsde.gce.RasterCellType.TYPE_4BIT;
import static org.geotools.arcsde.gce.RasterCellType.TYPE_8BIT_U;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.geotools.arcsde.gce.BitmaskToNoDataConverter.Unsigned8bitConverter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.Test;

public class BitmaskToNoDataConverterTest {

    @Test
    public void testGetInstance8BitU() {
        RasterDatasetInfo rasterInfo;
        BitmaskToNoDataConverter noData;

        int noDataValue;
        double statsMin;
        double statsMax;

        noDataValue = 0;
        statsMin = 1;
        statsMax = 255;
        rasterInfo = createRasterInfo(TYPE_8BIT_U, noDataValue, statsMin, statsMax);
        noData = BitmaskToNoDataConverter.getInstance(rasterInfo, 0);
        assertNotNull(noData);

        noDataValue = 255;
        statsMin = 0;
        statsMax = 255;
        rasterInfo = createRasterInfo(TYPE_8BIT_U, noDataValue, statsMin, statsMax);
        noData = BitmaskToNoDataConverter.getInstance(rasterInfo, 0);
        assertNotNull(noData);
    }

    @Test
    public void testGetInstance1Bit() {
        RasterDatasetInfo rasterInfo;
        BitmaskToNoDataConverter noData;

        int noDataValue;
        double statsMin;
        double statsMax;

        noDataValue = 0;
        statsMin = 0;
        statsMax = 1;
        rasterInfo = createRasterInfo(TYPE_1BIT, noDataValue, statsMin, statsMax);
        try {
            noData = BitmaskToNoDataConverter.getInstance(rasterInfo, 0);
            fail("Expected UOE, noDataValue == 0 is non valid");
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        noDataValue = 2;
        statsMin = 0;
        statsMax = 1;
        rasterInfo = createRasterInfo(TYPE_1BIT, noDataValue, statsMin, statsMax);
        noData = BitmaskToNoDataConverter.getInstance(rasterInfo, 0);
        assertNotNull(noData);
        // make sure promotion from 1 to 8 bit is being taking place here and hence we got a 8-bit-u
        // no-data setter
        assertTrue(noData instanceof Unsigned8bitConverter);
    }

    @Test
    public void testGetInstance4Bit() {
        RasterDatasetInfo rasterInfo;
        BitmaskToNoDataConverter noData;

        int noDataValue;
        double statsMin;
        double statsMax;

        statsMin = TYPE_4BIT.getSampleValueRange().getMinimum();
        statsMax = TYPE_4BIT.getSampleValueRange().getMaximum();
        noDataValue = 0;

        rasterInfo = createRasterInfo(TYPE_4BIT, noDataValue, statsMin, statsMax);
        try {
            noData = BitmaskToNoDataConverter.getInstance(rasterInfo, 0);
            fail("Expected UOE, noDataValue == 0 is non valid");
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }

        noDataValue = (int) (statsMax + 1);

        rasterInfo = createRasterInfo(TYPE_4BIT, noDataValue, statsMin, statsMax);
        noData = BitmaskToNoDataConverter.getInstance(rasterInfo, 0);
        assertNotNull(noData);
        // make sure promotion from 4 to 8 bit is being taking place here and hence we got a 8-bit-u
        // no-data setter
        assertTrue(noData instanceof Unsigned8bitConverter);
    }

    @Test
    public void testGetInstance16BitU() {
        RasterDatasetInfo rasterInfo;
        BitmaskToNoDataConverter noData;

        int noDataValue;
        double statsMin;
        double statsMax;

        statsMin = TYPE_16BIT_U.getSampleValueRange().getMinimum();
        statsMax = TYPE_16BIT_U.getSampleValueRange().getMaximum() - 1;
        noDataValue = (int) TYPE_16BIT_U.getSampleValueRange().getMaximum();

        rasterInfo = createRasterInfo(TYPE_16BIT_U, noDataValue, statsMin, statsMax);
        noData = BitmaskToNoDataConverter.getInstance(rasterInfo, 0);
        assertNotNull(noData);

        statsMax = TYPE_16BIT_U.getSampleValueRange().getMaximum();
        noDataValue = (int) TYPE_16BIT_U.getSampleValueRange().getMaximum() + 1;

        rasterInfo = createRasterInfo(TYPE_16BIT_U, noDataValue, statsMin, statsMax);
        noData = BitmaskToNoDataConverter.getInstance(rasterInfo, 0);
        assertNotNull(noData);
        // make sure promotion from 16 to 32 bit is being taking place here and hence we got a
        // 32-bit-u
        // no-data setter
        assertTrue(noData instanceof Unsigned8bitConverter);
    }

    private RasterDatasetInfo createRasterInfo(RasterCellType nativeType, Number noDataValue,
            double statsMin, double statsMax) {

        RasterDatasetInfo datasetInfo = new RasterDatasetInfo();

        List<RasterInfo> datasetRasters = new ArrayList<RasterInfo>();
        RasterInfo rasterInfo = new RasterInfo(128, 128);
        datasetRasters.add(rasterInfo);

        rasterInfo.addPyramidLevel(0, new ReferencedEnvelope(), new Point(), new Point(), 10, 10,
                new Dimension(100, 100));
        List<RasterBandInfo> bands = new ArrayList<RasterBandInfo>();
        RasterBandInfo bandInfo = new RasterBandInfo();
        bands.add(bandInfo);

        bandInfo.bandId = 1L;
        // the native type
        bandInfo.cellType = nativeType;
        // the target type will be determined based on the native type bounds and the band's
        // statistics
        bandInfo.noDataValue = noDataValue;
        bandInfo.statsMin = statsMin;
        bandInfo.statsMax = statsMax;

        rasterInfo.setBands(bands);

        datasetInfo.setPyramidInfo(datasetRasters);

        return datasetInfo;
    }

    @Test
    public void testIsNoData() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetNoData() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetAll() {
        fail("Not yet implemented");
    }

    @Test
    public void testSet() {
        fail("Not yet implemented");
    }

}
