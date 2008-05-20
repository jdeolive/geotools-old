package org.geotools.arcsde.gce.imageio;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.gce.ArcSDEPyramid;
import org.geotools.arcsde.gce.RasterTestData;
import org.geotools.arcsde.pool.Session;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterColumn;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;

public class OneByteBandNoColormapReaderTest {

    static RasterTestData rasterTestData;

    static HashMap<String, Object> readerProps;

    static Logger LOGGER = Logging.getLogger(OneByteBandNoColormapReaderTest.class
            .getCanonicalName());

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        rasterTestData = new RasterTestData();
        rasterTestData.setUp();
        rasterTestData.loadOneByteGrayScaleRaster();

        Session session = null;
        SeQuery q = null;
        ArcSDEPyramid pyramid;
        SeRow r;
        String tableName;
        try {

            // Set up a pyramid and readerprops for the sample three-band imagery
            session = rasterTestData.getTestData().getConnectionPool().getConnection();
            tableName = rasterTestData.getGrayScaleOneByteRasterTableName();
            q = Session.issueCreateAndExecuteQuery(session, new String[] { "RASTER" },
                    new SeSqlConstruct(tableName));
            r = q.fetch();
            SeRasterAttr rattrThreeBand = r.getRaster(0);
            q.close();

            SeRasterColumn rcol = session.createSeRasterColumn(rattrThreeBand.getRasterColumnId());

            CoordinateReferenceSystem crs = CRS.parseWKT(rcol.getCoordRef()
                    .getCoordSysDescription());
            pyramid = new ArcSDEPyramid(rattrThreeBand, crs);

            readerProps = new HashMap<String, Object>();
            readerProps.put(ArcSDERasterReaderSpi.PYRAMID, pyramid);
            readerProps.put(ArcSDERasterReaderSpi.RASTER_TABLE, tableName);
            readerProps.put(ArcSDERasterReaderSpi.RASTER_COLUMN, "RASTER");
        } catch (SeException se) {
            LOGGER.log(Level.SEVERE, se.getSeError().getErrDesc(), se);
            throw se;
        } finally {
            if (q != null)
                q.close();
            if (session != null) {
                session.close();
            }
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        rasterTestData.tearDown();
    }

}