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

public class OneBitBandCopierTest {

    static RasterTestData rasterTestData;

    static Logger LOGGER;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        LOGGER = Logging.getLogger(OneBitBandCopierTest.class.getCanonicalName());
        if (rasterTestData == null) {
            rasterTestData = new RasterTestData();
            rasterTestData.setUp();
            rasterTestData.load1bitRaster();
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        rasterTestData.tearDown();
    }

    @Test
    public void testLiveOneBitAlignedRasterTile() throws Exception {
        final String tableName = rasterTestData.get1bitRasterTableName();

        Session session = null;
        try {
            ArcSDEConnectionPool pool = rasterTestData.getTestData().getConnectionPool();

            session = pool.getSession();
            SeQuery q = session.createAndExecuteQuery(new String[] { "RASTER" },
                    new SeSqlConstruct(tableName));
            SeRow r = q.fetch();
            SeRasterAttr rAttr = r.getRaster(0);

            int[] bands = new int[] { 1 };
            SeRasterConstraint rConstraint = new SeRasterConstraint();
            rConstraint.setBands(bands);
            rConstraint.setLevel(0);
            rConstraint.setEnvelope(0, 0, 0, 0);
            rConstraint.setInterleave(SeRaster.SE_RASTER_INTERLEAVE_BSQ);

            q.queryRasterTile(rConstraint);

            BufferedImage fromSdeImage = new BufferedImage(128, 128, BufferedImage.TYPE_BYTE_BINARY);
            ArcSDERasterBandCopier bandCopier = ArcSDERasterBandCopier.getInstance(rAttr
                    .getPixelType(), rAttr.getTileWidth(), rAttr.getTileHeight());

            SeRasterTile rTile = r.getRasterTile();
            for (int i = 0; i < bands.length; i++) {
                bandCopier.copyPixelData(rTile, fromSdeImage.getRaster(), 0, 0, i);
                rTile = r.getRasterTile();
            }

            // ImageIO.write(fromSdeImage, "PNG", new File("/tmp/"
            // + Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));
            final File originalRasterFile = org.geotools.test.TestData.file(null, rasterTestData
                    .getRasterTestDataProperty("sampledata.onebitraster"));
            BufferedImage originalImage = ImageIO.read(originalRasterFile);

            // Well, now we have an image tile. Does it have what we expect on it?
            Assert.assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(
                    fromSdeImage, originalImage.getSubimage(0, 0, 128, 128)));

        } catch (SeException se) {
            LOGGER.log(Level.SEVERE, se.getSeError().getErrDesc(), se);
        } finally {
            if (session != null)
                session.close();
        }
    }

    @Test
    public void testLiveOneBitUnalignedRasterTile() throws Exception {
        final String tableName = rasterTestData.get1bitRasterTableName();

        Session session = null;
        try {
            ArcSDEConnectionPool pool = rasterTestData.getTestData().getConnectionPool();

            session = pool.getSession();
            SeQuery q = session.createAndExecuteQuery(new String[] { "RASTER" },
                    new SeSqlConstruct(tableName));
            SeRow r = q.fetch();
            SeRasterAttr rAttr = r.getRaster(0);

            int[] bands = new int[] { 1 };
            SeRasterConstraint rConstraint = new SeRasterConstraint();
            rConstraint.setBands(bands);
            rConstraint.setLevel(0);
            rConstraint.setEnvelope(0, 0, 0, 0);
            rConstraint.setInterleave(SeRaster.SE_RASTER_INTERLEAVE_BSQ);

            q.queryRasterTile(rConstraint);

            final int targetImgWidth = 67, targetImgHeight = 67;
            final int imgxstart = 38, imgystart = 31;

            BufferedImage fromSdeImage = new BufferedImage(targetImgWidth, targetImgHeight,
                    BufferedImage.TYPE_BYTE_BINARY);
            ArcSDERasterBandCopier bandCopier = ArcSDERasterBandCopier.getInstance(rAttr
                    .getPixelType(), rAttr.getTileWidth(), rAttr.getTileHeight());

            SeRasterTile rTile = r.getRasterTile();
            for (int i = 0; i < bands.length; i++) {
                bandCopier.copyPixelData(rTile, fromSdeImage.getRaster(), imgxstart, imgystart, i);
                rTile = r.getRasterTile();
            }

            final File originalRasterFile = org.geotools.test.TestData.file(null, rasterTestData
                    .getRasterTestDataProperty("sampledata.onebitraster"));
            BufferedImage originalImage = ImageIO.read(originalRasterFile);
            BufferedImage subImage = originalImage.getSubimage(imgxstart, imgystart,
                    targetImgWidth, targetImgHeight);

            // Well, now we have an image tile. Does it have what we expect on it?
            Assert.assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(
                    fromSdeImage, subImage));

        } catch (SeException se) {
            LOGGER.log(Level.SEVERE, se.getSeError().getErrDesc(), se);
        } finally {
            if (session != null)
                session.close();
        }
    }

}
