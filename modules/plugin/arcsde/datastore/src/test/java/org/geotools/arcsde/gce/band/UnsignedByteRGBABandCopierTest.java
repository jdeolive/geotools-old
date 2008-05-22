package org.geotools.arcsde.gce.band;

import java.util.logging.Logger;

import org.geotools.arcsde.gce.RasterTestData;
import org.geotools.util.logging.Logging;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class UnsignedByteRGBABandCopierTest {

    static RasterTestData rasterTestData;

    static Logger LOGGER;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        LOGGER = Logging.getLogger(UnsignedByteRGBABandCopierTest.class.getCanonicalName());
        if (rasterTestData == null) {
            rasterTestData = new RasterTestData();
            rasterTestData.setUp();
            rasterTestData.loadRGBRaster();
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        rasterTestData.tearDown();
    }

}
