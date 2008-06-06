/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.coverage.grid;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import javax.media.jai.JAI;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.operation.MathTransform;

import org.geotools.referencing.CRS;
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.crs.DefaultProjectedCRS;
import org.geotools.referencing.operation.DefaultMathTransformFactory;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.coverage.processing.Operations;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.factory.Hints;


/**
 * Visual test of the "Resample" operation. A remote sensing image is projected from a fitted
 * coordinate system to a geographic one.
 *
 * @source $URL$
 * @version $Id$
 * @author Remi Eve
 * @author Martin Desruisseaux (IRD)
 */
public final class ResampleTest extends GridCoverageTest {
    /**
     * Set to {@code true} if the test case should show the projection results
     * in a windows. This flag is set to {@code true} if the test is run from
     * the command line through the {@code main(String[])} method. Otherwise
     * (for example if it is run from Maven), it is left to {@code false}.
     *
     * @todo Consider setting to {@link TestData#isInteractive}.
     */
    private static boolean SHOW = false;

    /**
     * Small number for comparaisons.
     */
    private static final double EPS = 1E-6;

    /**
     * The source grid coverage, to be initialized by {@link #setUp}.
     * Contains 8-bits indexed color model for a PNG image, with categories.
     */
    private GridCoverage2D coverage;

    /**
     * An other source coverage initialized by {@link #setUp}.
     * Contains indexed color model for a GIF image, without categories.
     */
    private GridCoverage2D indexedCoverage;

    /**
     * An other source coverage initialized by {@link #setUp}.
     * Contains indexed color model for a GIF image, without categories.
     */
    private GridCoverage2D indexedCoverageWithTransparency;

    /**
     * An other source coverage initialized by {@link #setUp}.
     * Contains float values.
     */
    private GridCoverage2D floatCoverage;

    /**
     * Constructs a test case with the given name.
     */
    public ResampleTest(final String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        coverage                        = GridCoverageExamples.getExample(0);
        indexedCoverage                 = GridCoverageExamples.getExample(2);
        indexedCoverageWithTransparency = GridCoverageExamples.getExample(3);
        floatCoverage                   = GridCoverageExamples.getExample(4);
    }

    /**
     * Applies an operation on the specified coverage.
     * This is invoked before to run the tests defined in the super-class.
     */
    @Override
    protected GridCoverage2D transform(final GridCoverage2D coverage) {
        return (GridCoverage2D) Operations.DEFAULT.resample(coverage, getProjectedCRS(coverage));
    }

    /**
     * Returns a projected CRS for test purpose.
     */
    private static CoordinateReferenceSystem getProjectedCRS(final GridCoverage2D coverage) {
        try {
            final GeographicCRS  base = (GeographicCRS) coverage.getCoordinateReferenceSystem();
            final Ellipsoid ellipsoid = base.getDatum().getEllipsoid();
            final DefaultMathTransformFactory factory = new DefaultMathTransformFactory();
            final ParameterValueGroup parameters = factory.getDefaultParameters("Oblique_Stereographic");
            parameters.parameter("semi_major").setValue(ellipsoid.getSemiMajorAxis());
            parameters.parameter("semi_minor").setValue(ellipsoid.getSemiMinorAxis());
            parameters.parameter("central_meridian").setValue(5);
            parameters.parameter("latitude_of_origin").setValue(-5);
            final MathTransform mt;
            try {
                mt = factory.createParameterizedTransform(parameters);
            } catch (FactoryException exception) {
                fail(exception.getLocalizedMessage());
                return null;
            }
            return new DefaultProjectedCRS("Stereographic", base, mt, DefaultCartesianCS.PROJECTED);
        } catch (NoSuchIdentifierException exception) {
            fail(exception.getLocalizedMessage());
            return null;
        }
    }

    /**
     * Projects the specified image to the specified CRS.
     * The result will be displayed in a window if {@link #SHOW} is set to {@code true}.
     * @param show
     *
     * @return The operation name which was applied on the image, or {@code null} if none.
     */
    public static String projectTo(final GridCoverage2D            coverage,
                                    final CoordinateReferenceSystem targetCRS,
                                    final GridGeometry2D            geometry, boolean show)
    {
        return projectTo(coverage, targetCRS, geometry, null, true,show);
    }

    /**
     * Tests the "Resample" operation with an identity transform.
     */
    public void testIdentity() {
        assertEquals("Lookup", projectTo(coverage, coverage.getCoordinateReferenceSystem(), null, null, true, SHOW));
        assertNull(projectTo(indexedCoverage, indexedCoverage.getCoordinateReferenceSystem(), null, null, true, SHOW));
        assertNull(projectTo(indexedCoverageWithTransparency, indexedCoverageWithTransparency.getCoordinateReferenceSystem(), null, null, true, SHOW));
        assertNull(projectTo(floatCoverage, floatCoverage.getCoordinateReferenceSystem(), null, null, true, SHOW));
    }

    /**
     * Tests the "Resample" operation with a "Crop" transform.
     */
    public void testCrop() {
        final GridGeometry2D g1,g2;
        g1 = new GridGeometry2D(new GeneralGridRange(new Rectangle(50,50,100,100)), (MathTransform)null, null);
        g2 = new GridGeometry2D(new GeneralGridRange(new Rectangle(50,50,200,200)), (MathTransform)null, null);
        assertEquals("Crop",   projectTo(coverage,        null, g2, null, false, SHOW));
        assertEquals("Lookup", projectTo(coverage,        null, g2, null, true,  SHOW));
        assertEquals("Crop",   projectTo(indexedCoverage, null, g1, null, false, SHOW));
        assertEquals("Crop",   projectTo(indexedCoverageWithTransparency, null, g1, null, false, SHOW));
        assertEquals("Crop",   projectTo(floatCoverage, null, g1,
                new Hints(Hints.COVERAGE_PROCESSING_VIEW, ViewType.PHOTOGRAPHIC), true, SHOW));
    }

    /**
     * Tests the "Resample" operation with a stereographic coordinate system.
     */
    public void testStereographic() {
        assertEquals("Warp", projectTo(coverage,getProjectedCRS(coverage), null,null,true,SHOW));
    }

    /**
     * Tests the "Resample" operation with a stereographic coordinate system.
     */
    public void testsNad83() throws FactoryException {
        final Hints photo = new Hints(Hints.COVERAGE_PROCESSING_VIEW, ViewType.PHOTOGRAPHIC);
        final CoordinateReferenceSystem crs = CRS.parseWKT(
                "GEOGCS[\"NAD83\"," +
                  "DATUM[\"North_American_Datum_1983\"," +
                    "SPHEROID[\"GRS 1980\",6378137,298.257222101,AUTHORITY[\"EPSG\",\"7019\"]]," +
                    "TOWGS84[0,0,0,0,0,0,0],AUTHORITY[\"EPSG\",\"6269\"]]," +
                  "PRIMEM[\"Greenwich\",0, AUTHORITY[\"EPSG\",\"8901\"]]," +
                  "UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9108\"]]," +
                  "AXIS[\"Lat\",NORTH]," +
                  "AXIS[\"Long\",EAST]," +
                  "AUTHORITY[\"EPSG\",\"4269\"]]");
        assertEquals("Warp", projectTo(indexedCoverage, crs, null, null, false, SHOW));
        assertEquals("Warp", projectTo(indexedCoverageWithTransparency, crs, null, null, false, SHOW));
        assertEquals("Warp", projectTo(floatCoverage, crs, null, photo, true, SHOW));
    }

    /**
     * Tests the "Resample" operation with an "Affine" transform.
     */
    public void testAffine() {
        final Hints photo = new Hints(Hints.COVERAGE_PROCESSING_VIEW, ViewType.PHOTOGRAPHIC);
        performAffine(coverage,                        null, true,  "Lookup",     "Affine", SHOW);
        performAffine(indexedCoverage,                 null, true,  "Lookup",     "Affine", SHOW);
        performAffine(indexedCoverageWithTransparency, null, false, "BandSelect", "Affine", SHOW);
        performAffine(floatCoverage,                  photo, false, "Lookup",     "Affine", SHOW);
    }

    /**
     * Tests <var>X</var>,<var>Y</var> translation in the {@link GridGeometry} after
     * a "Resample" operation.
     */
    public void testTranslation() throws NoninvertibleTransformException {
        doTranslation(coverage);
        doTranslation(indexedCoverage);
        doTranslation(indexedCoverageWithTransparency);
    }

    /**
     * Performs a translation using the "Resample" operation.
     *
     * @param grid the {@link GridCoverage2D} to apply the translation on.
     */
    private void doTranslation(GridCoverage2D grid) throws NoninvertibleTransformException {
        final int    transX =  -253;
        final int    transY =  -456;
        final double scaleX =  0.04;
        final double scaleY = -0.04;
        final ParameterBlock block = new ParameterBlock().
                addSource(grid.getRenderedImage()).
                add((float) transX).
                add((float) transY);
        RenderedImage image = JAI.create("Translate", block);
        assertEquals("Incorrect X translation", transX, image.getMinX());
        assertEquals("Incorrect Y translation", transY, image.getMinY());
        /*
         * Create a grid coverage from the translated image but with the same envelope.
         * Consequently, the 'gridToCoordinateSystem' should be translated by the same
         * amount, with the opposite sign.
         */
        final AffineTransform expected = getAffineTransform(grid);
        grid = CoverageFactoryFinder.getGridCoverageFactory(null).create("Translated",
                image, grid.getEnvelope(), grid.getSampleDimensions(),
                new GridCoverage2D[]{grid}, grid.getProperties());
        expected.translate(-transX, -transY);
        assertEquals(expected, getAffineTransform(grid),EPS);
        /*
         * Apply the "Resample" operation with a specific 'gridToCoordinateSystem' transform.
         * The envelope is left unchanged. The "Resample" operation should compute automatically
         * new image bounds.
         */
        final AffineTransform at = AffineTransform.getScaleInstance(scaleX, scaleY);
        final MathTransform   tr = ProjectiveTransform.create(at);
        final GridGeometry2D geometry = new GridGeometry2D(null, tr, null);
        grid = (GridCoverage2D) Operations.DEFAULT.resample(grid,
                grid.getCoordinateReferenceSystem(), geometry, null);
        assertEquals(at, getAffineTransform(grid));
        image = grid.getRenderedImage();
        expected.preConcatenate(at.createInverse());
        final Point point = new Point(transX, transY);
        assertSame(point, expected.transform(point, point)); // Round toward neareast integer
        assertEquals("Incorrect X translation", point.x, image.getMinX());
        assertEquals("Incorrect Y translation", point.y, image.getMinY());
    }

    /**
     * Inherited test disabled for this suite.
     *
     * @todo Investigate why this test fails.
     */
    @Override
    public void testSerialization() {
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(ResampleTest.class);
    }

    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        SHOW = true;
        org.geotools.util.logging.Logging.GEOTOOLS.forceMonolineConsoleOutput(AbstractProcessor.OPERATION);
        junit.textui.TestRunner.run(suite());
    }
}
