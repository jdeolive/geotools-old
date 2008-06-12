/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.coverage.grid;

import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.coverage.processing.DefaultProcessor;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.crs.DefaultDerivedCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.Classes;


/**
 * Tests the {@link GridCoverage2D} implementation.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 */
public class GridCoverageTest extends TestCase {
    /**
     * Run the suite from the command line.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(GridCoverageTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public GridCoverageTest(final String name) {
        super(name);
    }

    /**
     * Tests the construction and access to a grid coverage.
     *
     * @throws IOException if an I/O operation was needed and failed.
     */
    public void testGridCoverage() throws IOException {
        final GridCoverage2D coverage = getRandomCoverage();
        assertNotNull(coverage);
        // Not much more test to do here, since most tests has been done
        // inside 'getRandomCoverage'. This method will be overridden by
        // 'InterpolatorTest', which will perform more tests.
        for (int i=GridCoverageExamples.getNumExamples(); --i>=0;) {
            assertNotNull(GridCoverageExamples.getExample(i));
        }
    }

    /**
     * Tests the serialization of a grid coverage.
     *
     * @throws IOException if an I/O operation was needed and failed.
     */
    public void testSerialization() throws IOException, ClassNotFoundException {
        final GridCoverage2D coverage = getRandomCoverage();
        assertNotNull(coverage);
        coverage.tileEncoding = null;
        /*
         * The previous line is not something that we should do.
         * But we want to test the default GridCoverage2D encoding.
         */
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(buffer);
        try {
            out.writeObject(coverage.view(ViewType.PACKED));
//          out.writeObject(coverage.view(ViewType.GEOPHYSICS));
        } catch (IllegalArgumentException e) {
            /*
             * TODO: this exception occurs when ImageLayout contains a SampleModel or a ColorModel
             *       unknow to javax.media.jai.remote.SerializerFactory getState(...) method. This
             *       happen if an operation we applied on the coverage in some subclass (especially
             *       OperationsTest). Ignore the exception for now, but we need to revisit this
             *       issue later.
             */
            if (getClass().equals(GridCoverageTest.class)) {
                e.printStackTrace();
            }
            out.close();
            return;
        }
        out.close();
        final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        GridCoverage2D read;
        read = (GridCoverage2D) in.readObject(); assertSame(read, read.view(ViewType.PACKED));
//      read = (GridCoverage2D) in.readObject(); assertSame(read, read.view(ViewType.GEOPHYSICS));
//      assertNotSame(read, read.geophysics(true));
        in.close();
    }

    /**
     * Applies an operation on the specified coverage, if wanted. The
     * default implementation returns {@code coverage} with no change.
     */
    protected GridCoverage2D transform(final GridCoverage2D coverage) {
        return coverage;
    }

    /**
     * Returns a grid coverage filled with random values. The coordinate
     * reference system default to {@link DefaultGeographicCRS#WGS84}.
     *
     * @return A random coverage.
     */
    protected GridCoverage2D getRandomCoverage() {
        return getRandomCoverage(DefaultGeographicCRS.WGS84);
    }

    /**
     * Returns a grid coverage filled with random values.
     *
     * @param crs The coverage coordinate reference system.
     * @return A random coverage.
     */
    protected GridCoverage2D getRandomCoverage(final CoordinateReferenceSystem crs) {
        final GridCoverage2D original = GridCoverageExamples.getRandomCoverage(crs);
        final GridCoverage2D coverage = transform(original);
        /*
         * Grid coverage construction finished. Now test it. Some tests will not be applicable
         * if a subclass overridden the 'transform' method are returned a transformed coverage.
         * We detect this case when 'coverage != original'.
         */
        assertSame(coverage.getRenderedImage(), coverage.getRenderableImage(0,1).createDefaultRendering());
        if (!coverage.getCoordinateReferenceSystem().equals(crs)) {
            assertEquals("Resampler2D", Classes.getShortClassName(coverage));
        }
        /*
         * Tests the creation of a "geophysics" view. This test make sure that the
         * 'geophysics' method do not creates more grid coverage than needed.
         */
        GridCoverage2D geophysics= coverage.view(ViewType.GEOPHYSICS);
        assertSame(coverage,       coverage.view(ViewType.PACKED));
        assertSame(coverage,     geophysics.view(ViewType.PACKED));
        assertSame(geophysics,   geophysics.view(ViewType.GEOPHYSICS));
        assertFalse( coverage.equals(geophysics));
        assertFalse( coverage.getSampleDimension(0).getSampleToGeophysics().isIdentity());
        assertTrue(geophysics.getSampleDimension(0).getSampleToGeophysics().isIdentity());
        return coverage;
    }

    /**
     * Checks the envelopes of this two {@link GridCoverage2D} for equality
     * using the smallest scale factor of their grid to world transformation as
     * the tolerance.
     *
     * @param a The first {@link GridCoverage2D}.
     * @param b The second {@link GridCoverage2D}.
     */
    protected static boolean checkEnvelopes(GridCoverage2D a, GridCoverage2D b) {
        // tolerance
        final double scaleA, scaleB;
        scaleA = XAffineTransform.getScale((AffineTransform) a.getGridGeometry().getGridToCRS2D());
        scaleB = XAffineTransform.getScale((AffineTransform) b.getGridGeometry().getGridToCRS2D());
        final double tolerance = Math.min(scaleA, scaleB) / 2.0;

        // actual check
        return ((GeneralEnvelope) a.getEnvelope()).equals(b.getEnvelope(), tolerance, false);
    }

    /**
     * Returns the "Sample to geophysics" transform as an affine transform.
     */
    protected static AffineTransform getAffineTransform(final GridCoverage2D coverage) {
        AffineTransform tr = (AffineTransform) coverage.getGridGeometry().getGridToCRS2D();
        tr = new AffineTransform(tr); // Change the type to the default Java2D implementation.
        return tr;
    }

    /**
     * Checks the envelopes of this two {@link GridCoverage2D} for equality
     * using the smallest scale factor of their grid to world transformation as
     * the tolerance.
     *
     * @param a The {@link GridCoverage2D} to rotate.
     * @param angle The angle to use.
     */
    protected static GridCoverage2D rotateCoverage(GridCoverage2D a, double angle) {
        // tolerance
        final AffineTransform atr = getAffineTransform(a);
        atr.preConcatenate(AffineTransform.getRotateInstance(angle));
        final MathTransform tr = ProjectiveTransform.create(atr);
        CoordinateReferenceSystem crs = a.getCoordinateReferenceSystem();
        crs = new DefaultDerivedCRS("F2", crs, tr, crs.getCoordinateSystem());
        return projectTo(a, crs, null, null, true);
    }

    /**
     * Projects the specified image to the specified CRS using the specified hints.
     * The result will be displayed in a window if {@link #SHOW} is set to {@code true}.
     *
     * @return The operation name which was applied on the image, or {@code null} if none.
     */
    protected static String projectTo(final GridCoverage2D            coverage,
                                      final CoordinateReferenceSystem targetCRS,
                                      final GridGeometry2D            geometry,
                                      final Hints                     hints,
                                      final boolean                   useGeophysics,
                                      final boolean                   show)
    {
        GridCoverage2D projected = projectTo(coverage, targetCRS, geometry, hints, useGeophysics);
        final RenderedImage image = projected.getRenderedImage();
        projected = projected.view(ViewType.PACKED);
        String operation = null;
        if (image instanceof RenderedOp) {
            operation = ((RenderedOp) image).getOperationName();
            AbstractProcessor.LOGGER.fine("Applied \"" + operation + "\" JAI operation.");
        }
        if (show) {
            Viewer.show(projected, operation);
        } else {
            // Force computation
            assertNotNull(projected.getRenderedImage().getData());
        }
        return operation;
    }

    /**
     * Projects the specified image to the specified CRS using the specified
     * hints.
     *
     * @return The operation name which was applied on the image, or {@code null} if none.
     */
    protected static GridCoverage2D projectTo(final GridCoverage2D coverage,
                                              final CoordinateReferenceSystem targetCRS,
                                              final GridGeometry2D geometry, final Hints hints,
                                              final boolean useGeophysics)
    {
        final AbstractProcessor processor = (hints != null) ?
                new DefaultProcessor(hints) : AbstractProcessor.getInstance();
        final String arg1,   arg2;
        final Object value1, value2;
        if (targetCRS != null) {
            arg1 = "CoordinateReferenceSystem";
            value1 = targetCRS;
            if (geometry != null) {
                arg2 = "GridGeometry";
                value2 = geometry;
            } else {
                arg2 = "InterpolationType";
                value2 = "bilinear";
            }
        } else {
            arg1 = "GridGeometry";
            value1 = geometry;
            arg2 = "InterpolationType";
            value2 = "bilinear";
        }
        GridCoverage2D projected = coverage.view(useGeophysics ? ViewType.GEOPHYSICS : ViewType.PACKED);
        final ParameterValueGroup param = processor.getOperation("Resample").getParameters();
        param.parameter("Source").setValue(projected);
        param.parameter(arg1).setValue(value1);
        param.parameter(arg2).setValue(value2);
        projected = (GridCoverage2D) processor.doOperation(param);
        final RenderedImage image = projected.getRenderedImage();
        if (image instanceof RenderedOp) {
            String operation = ((RenderedOp) image).getOperationName();
            AbstractProcessor.LOGGER.fine("Applied \"" + operation + "\" JAI operation.");
        }
        return projected;
    }

    /**
     * Compares two affine transforms up to the specified tolerance factor.
     */
    protected static void assertEquals(final AffineTransform expected, final AffineTransform actual, double eps) {
        assertEquals("scaleX",     expected.getScaleX(),     actual.getScaleX(),     eps);
        assertEquals("scaleY",     expected.getScaleY(),     actual.getScaleY(),     eps);
        assertEquals("shearX",     expected.getShearX(),     actual.getShearX(),     eps);
        assertEquals("shearY",     expected.getShearY(),     actual.getShearY(),     eps);
        assertEquals("translateX", expected.getTranslateX(), actual.getTranslateX(), eps);
        assertEquals("translateY", expected.getTranslateY(), actual.getTranslateY(), eps);
    }

    /**
     * Performs an Affine transformation on the provided {@link GridCoverage2D} using the
     * Resample operation.
     *
     * @param coverage the {@link GridCoverage2D} to apply the operation on.
     * @param show whether or not we should show this coverage in a viewer.
     */
    protected static void performAffine(final GridCoverage2D coverage,
                                        final Hints          hints,
                                        final boolean        useGeophysics,
                                        final String         testString1,
                                        final String         testString2,
                                        boolean show)
    {
        final AffineTransform atr = getAffineTransform(coverage);
        atr.preConcatenate(AffineTransform.getTranslateInstance(5, 5));
        final MathTransform tr = ProjectiveTransform.create(atr);
        CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem();
        crs = new DefaultDerivedCRS("F2", crs, tr, crs.getCoordinateSystem());
        /*
         * Note: In current Resample implementation, the affine transform effect tested
         *       on the first line below will not be visible with the simple viewer used
         *       here.  It would be visible however with more elaborated viewer like the
         *       one provided in the {@code org.geotools.renderer} package.
         */
        String operation = projectTo(coverage, crs, null,hints, useGeophysics,show);
        if (operation != null) {
            if (false) // TODO
                assertEquals(testString1, operation);
        }
        operation = projectTo(coverage, null, new GridGeometry2D(null, tr, null), hints, useGeophysics,show);
        if (operation != null) {
            if (false) // TODO
                assertEquals(testString2, operation);
        }
    }
}
