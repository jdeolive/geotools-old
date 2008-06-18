package org.geotools.gce.imagemosaic.jdbc;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.dbcp.DriverConnectionFactory;
import org.geotools.geometry.GeneralEnvelope;

import org.geotools.referencing.CRS;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.awt.Color;


public class H2Test extends AbstractTest {
    public static String EPSG_31287_TOWGS84 = "PROJCS[\"MGI / Austria Lambert\",GEOGCS[\"MGI\", DATUM[\"Militar-Geographische Institut\"," +
        "SPHEROID[\"Bessel 1841\", 6377397.155, 299.1528128, AUTHORITY[\"EPSG\",\"7004\"]], " +
        "TOWGS84[577.326,90.129,463.919,5.137,1.474,5.297,2.4232],AUTHORITY[\"EPSG\",\"6312\"]]," +
        "PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]], UNIT[\"degree\", 0.017453292519943295]," +
        "AXIS[\"Geodetic longitude\", EAST], AXIS[\"Geodetic latitude\", NORTH], AUTHORITY[\"EPSG\",\"4312\"]]," +
        "PROJECTION[\"Lambert Conic Conformal (2SP)\", AUTHORITY[\"EPSG\",\"9802\"]], PARAMETER[\"central_meridian\", 13.333333333333334]," +
        "PARAMETER[\"latitude_of_origin\", 47.5], PARAMETER[\"standard_parallel_1\", 49.0], PARAMETER[\"false_easting\", 400000.0]," +
        "PARAMETER[\"false_northing\", 400000.0], PARAMETER[\"standard_parallel_2\", 46.0], UNIT[\"m\", 1.0]," +
        "AXIS[\"Easting\", EAST], AXIS[\"Northing\", NORTH], AUTHORITY[\"EPSG\",\"31287\"]] ";

    public H2Test(String test) {
        super(test);
    }

    public static Test suite() {
    	
    	
        TestSuite suite = new TestSuite();
    	
    	H2Test test = new H2Test("");
    	if (test.checkPreConditions()==false) return suite;
    	       
        suite.addTest(new H2Test("testDrop"));
        suite.addTest(new H2Test("testCreate"));
        suite.addTest(new H2Test("testImage1"));
        suite.addTest(new H2Test("testFullExtent"));
        suite.addTest(new H2Test("testNoData"));
        suite.addTest(new H2Test("testPartial"));
        suite.addTest(new H2Test("testVienna"));
        suite.addTest(new H2Test("testViennaEnv"));
        suite.addTest(new H2Test("testOutputTransparentColor"));
        suite.addTest(new H2Test("testReproject1"));
        suite.addTest(new H2Test("testDrop"));
        suite.addTest(new H2Test("testCreateJoined"));
        suite.addTest(new H2Test("testImage1Joined"));
        suite.addTest(new H2Test("testFullExtentJoined"));
        suite.addTest(new H2Test("testNoDataJoined"));
        suite.addTest(new H2Test("testPartialJoined"));
        suite.addTest(new H2Test("testViennaJoined"));
        suite.addTest(new H2Test("testViennaEnvJoined"));
        suite.addTest(new H2Test("testDrop"));

        return suite;
    }

    @Override
    protected String getSubDir() {
        return "h2";
    }

    @Override
    protected JDBCSetup getJDBCSetup() {
        return H2Setup.Singleton;
    }

    public void testReproject1() {
        JDBCAccess access = getJDBCAccess();
        ImageLevelInfo li = access.getLevelInfo(access.getNumOverviews());

        GeneralEnvelope env = new GeneralEnvelope(new double[] {
                    li.getExtentMaxX() - 200000, li.getExtentMaxY() - 200000
                },
                new double[] {
                    li.getExtentMaxX() + 200000, li.getExtentMaxY() + 200000
                });

        try {
            CoordinateReferenceSystem source = CRS.parseWKT(EPSG_31287_TOWGS84);
            env.setCoordinateReferenceSystem(source);

            CoordinateReferenceSystem target = CRS.decode("EPSG:4326");
            MathTransform t = CRS.findMathTransform(source, target);
            GeneralEnvelope tenv = CRS.transform(t, env);
            tenv.setCoordinateReferenceSystem(target);
            imageMosaic("partialgreen_reprojected",
                getJDBCSetup().getConfigUrl(), tenv, 400, 400, Color.GREEN,
                source);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    public void testOutputTransparentColor() {
        JDBCAccess access = getJDBCAccess();
        ImageLevelInfo li = access.getLevelInfo(access.getNumOverviews());

        GeneralEnvelope env = new GeneralEnvelope(new double[] {
                    li.getExtentMaxX() - 200000, li.getExtentMaxY() - 200000
                },
                new double[] {
                    li.getExtentMaxX() + 200000, li.getExtentMaxY() + 200000
                });

        try {
            env.setCoordinateReferenceSystem(CRS.decode("EPSG:31287"));
            imageMosaic("partialgreen", getJDBCSetup().getConfigUrl(), env,
                400, 400, Color.GREEN, null);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    public void setUp() throws Exception {
        // No fixture check needed
    }
    String getFixtureId() {
        return null;
    }

}
