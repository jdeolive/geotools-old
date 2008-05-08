package org.geotools.arcsde.gce.band;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.geotools.arcsde.gce.RasterTestData;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.Session;
import org.geotools.util.logging.Logging;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRaster;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterConstraint;
import com.esri.sde.sdk.client.SeRasterTile;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;

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
