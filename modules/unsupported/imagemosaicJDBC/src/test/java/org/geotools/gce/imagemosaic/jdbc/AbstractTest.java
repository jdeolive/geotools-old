package org.geotools.gce.imagemosaic.jdbc;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;

import org.geotools.factory.Hints;

import org.geotools.geometry.GeneralEnvelope;

import org.geotools.referencing.CRS;

import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.awt.Color;
import java.awt.Rectangle;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public abstract class AbstractTest extends TestCase {
    protected static String OUTPUTDIR_BASE = "target";

    public AbstractTest(String test) {
        super(test);
    }

    protected String getOutPutDir() {
        return OUTPUTDIR_BASE + File.separator + getSubDir();
    }

    protected void initOutputDir() {
        File dir = new File(getOutPutDir());

        if (dir.exists()) {
            File[] files = dir.listFiles();

            if (files != null) {
                for (File f : files)
                    f.delete();
            }
        } else {
            dir.mkdir();
        }
    }

    public void testDrop() {
        try {
            getJDBCSetup().dropAll();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    public void testCreate() {
        initOutputDir();

        try {
            getJDBCSetup().createAll();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    protected JDBCAccess getJDBCAccess() {
        return JDBCAccessFactory.JDBCAccessMap.get(getJDBCSetup().getConfigUrl()
                                                       .toString());
    }

    public void testCreateJoined() {
        JDBCAccess access = getJDBCAccess();

        for (int i = 0; i <= access.getNumOverviews(); i++) {
            ImageLevelInfo li = access.getLevelInfo(i);
            li.setTileTableName(li.getSpatialTableName());
        }

        try {
            getJDBCSetup().createAllJoined();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    protected void imageMosaic(String name, String configUrl,
            GeneralEnvelope envelope, int width, int heigth ) throws IOException {
    	imageMosaic(name,configUrl,envelope,width,heigth,null,null);
    }

    protected void imageMosaic(String name, String configUrl,
        GeneralEnvelope envelope, int width, int heigth, Color bColor,CoordinateReferenceSystem crs)
        throws IOException {
        //Hints hints = new Hints(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM,CRS.parseWKT(M34_PRJ));
        Hints hints = null;
        if (crs !=null)
        	hints = new Hints(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM,crs);
        
        AbstractGridFormat format = (AbstractGridFormat) GridFormatFinder.findFormat(configUrl);
        ImageMosaicJDBCReader reader = (ImageMosaicJDBCReader) format.getReader(configUrl,
                hints);

        ParameterValue gg = AbstractGridFormat.READ_GRIDGEOMETRY2D.createValue();

        if (envelope == null) {
            envelope = reader.getOriginalEnvelope();
        } else if (envelope.getCoordinateReferenceSystem() == null) {
            envelope.setCoordinateReferenceSystem(reader.getOriginalEnvelope()
                                                        .getCoordinateReferenceSystem());
        }

        gg.setValue(new GridGeometry2D(
                new GeneralGridRange(new Rectangle(0, 0, width, heigth)),
                envelope));

        final ParameterValue outTransp = ImageMosaicJDBCFormat.OUTPUT_TRANSPARENT_COLOR.createValue();
        outTransp.setValue(bColor == null ? ImageMosaicJDBCFormat.OUTPUT_TRANSPARENT_COLOR.getDefaultValue() : bColor);

        GridCoverage2D coverage = (GridCoverage2D) reader.read(new GeneralParameterValue[] {
                    gg, outTransp
                });

        ImageIO.write(coverage.getRenderedImage(), "tif",
            new File(getOutPutDir() + File.separator + name + ".tif"));
    }

    protected abstract String getSubDir();

    protected abstract JDBCSetup getJDBCSetup();

    public void testImage1() {
        doTestImage1("image1");
    }

    public void testImage1Joined() {
        doTestImage1("image1_joined");
    }

    public void testFullExtent() {
        doFullExtent("fullExtent");
    }

    public void testFullExtentJoined() {
        doFullExtent("fullExtentJoined");
    }

    public void testNoData() {
        doNoData("nodData");
    }

    public void testNoDataJoined() {
        doNoData("noDataJoined");
    }

    public void testPartial() {
        doPartial("partial");
    }

    public void testPartialJoined() {
        doPartial("partialJoined");
    }

    public void testVienna() {
        doVienna("vienna");
    }

    public void testViennaJoined() {
        doVienna("viennaJoined");
    }

    public void testViennaEnv() {
        doViennaEnv("viennaEnv");
    }

    public void testViennaEnvJoined() {
        doViennaEnv("viennaEnvJoined");
    }

    private void doVienna(String name) {
        GeneralEnvelope env = new GeneralEnvelope(new double[] { 608000, 472000 },
                new double[] { 642000, 496000 });

        try {
            env.setCoordinateReferenceSystem(CRS.decode("EPSG:31287"));
            imageMosaic(name, getJDBCSetup().getConfigUrl(), env, 500, 500);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    private void doViennaEnv(String name) {
        GeneralEnvelope env = new GeneralEnvelope(new double[] { 568000, 432000 },
                new double[] { 682000, 536000 });

        try {
            env.setCoordinateReferenceSystem(CRS.decode("EPSG:31287"));
            imageMosaic(name, getJDBCSetup().getConfigUrl(), env, 500, 500);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    private void doTestImage1(String name) {
        GeneralEnvelope env = new GeneralEnvelope(new double[] { 500000, 480000 },
                new double[] { 600000, 530000 });

        try {
            env.setCoordinateReferenceSystem(CRS.decode("EPSG:31287"));
            imageMosaic(name, getJDBCSetup().getConfigUrl(), env, 500, 250);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    private void doFullExtent(String name) {
        JDBCAccess access = getJDBCAccess();
        ImageLevelInfo li = access.getLevelInfo(access.getNumOverviews());

        double scale = li.getEnvelope().getWidth() / 400;
        GeneralEnvelope env = new GeneralEnvelope(new double[] {
                    li.getExtentMinX(), li.getExtentMinY()
                }, new double[] { li.getExtentMaxX(), li.getExtentMaxY() });

        try {
            env.setCoordinateReferenceSystem(CRS.decode("EPSG:31287"));
            imageMosaic(name, getJDBCSetup().getConfigUrl(), env, 400,
                (int) (li.getEnvelope().getHeight() / scale));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    private void doNoData(String name) {
        JDBCAccess access = getJDBCAccess();
        ImageLevelInfo li = access.getLevelInfo(access.getNumOverviews());

        GeneralEnvelope env = new GeneralEnvelope(new double[] {
                    li.getExtentMaxX() + 1000, li.getExtentMaxY() + 1000
                },
                new double[] {
                    li.getExtentMaxX() + 2000, li.getExtentMaxY() + 2000
                });

        try {
            env.setCoordinateReferenceSystem(CRS.decode("EPSG:31287"));
            imageMosaic(name, getJDBCSetup().getConfigUrl(), env, 400, 400);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    private void doPartial(String name) {
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
            imageMosaic(name, getJDBCSetup().getConfigUrl(), env, 400, 400);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
