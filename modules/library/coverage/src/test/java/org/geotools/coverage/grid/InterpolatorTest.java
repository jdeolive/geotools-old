/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2002, Institut de Recherche pour le Développement
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

import java.awt.geom.Point2D;
import java.awt.image.Raster;
import javax.media.jai.Interpolation;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.opengis.coverage.grid.GridRange;
import org.opengis.geometry.Envelope;


/**
 * Tests the {@link Interpolator2D} implementation. This method inherit all tests from
 * {@link GridCoverageTest}. Because we override {@link #transform}, tests will be performed
 * on {@link Interpolator2D} objects instead of default {@link GridCoverage2D}.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class InterpolatorTest extends GridCoverageTest {
    /**
     * Small value for comparaison of sample values. Since most grid coverage implementation in
     * Geotools 2 store geophysics values as {@code float} numbers, this {@code EPS} value must
     * be of the order of {@code float} relative precision, not {@code double}.
     */
    private static final double EPS = 1E-5;

    /**
     * The interpolators to use.
     */
    private Interpolation[] interpolations;

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
        return new TestSuite(InterpolatorTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public InterpolatorTest(final String name) {
        super(name);
        final int[] types = {
            Interpolation.INTERP_BICUBIC,
            Interpolation.INTERP_BILINEAR,
            Interpolation.INTERP_NEAREST
        };
        interpolations = new Interpolation[types.length];
        for (int i=0; i<interpolations.length; i++) {
            interpolations[i] = Interpolation.getInstance(types[i]);
        }
    }

    /**
     * Applies an operation on the specified coverage, if wanted.
     * The default implementation applies a set of interpolations
     * on <code>coverage</code>.
     */
    @Override
    protected GridCoverage2D transform(final GridCoverage2D coverage) {
        return Interpolator2D.create(coverage, interpolations);
    }

    /**
     * Tests the interpolations. Since <code>testGridCoverage()</code> tests value
     * at the center of pixels, all interpolations results should be identical to
     * a result without interpolation.
     */
    @Override
    public void testGridCoverage() {
        final GridCoverage2D coverage = getRandomCoverage();
        assertTrue(coverage instanceof Interpolator2D);
        assertTrue(coverage.geophysics(true)  instanceof Interpolator2D);
        assertTrue(coverage.geophysics(false) instanceof Interpolator2D);
    }

    /**
     * Tests bilinear intersection at pixel edges. It should be equals
     * to the average of the four pixels around.
     */
    public void testInterpolationAtEdges() {
        // Following constant is pixel size (in degrees).
        // This constant must be identical to the one defined in 'getRandomCoverage()'
        final double PIXEL_SIZE = 0.25;

        final GridCoverage2D coverage = Interpolator2D.create(getRandomCoverage().geophysics(true),
              new Interpolation[] {Interpolation.getInstance(Interpolation.INTERP_BILINEAR)});

        final int  band = 0; // Band to test.
        double[] buffer = null;
        final Raster          data = coverage.getRenderedImage().getData();
        final Envelope    envelope = coverage.getEnvelope();
        final GridRange      range = coverage.getGridGeometry().getGridRange();
        final double          left = envelope.getMinimum(0);
        final double         upper = envelope.getMaximum(1);
        final Point2D.Double point = new Point2D.Double(); // Will maps to pixel upper-left corner
        for (int j=range.getLength(1); --j>=1;) {
            for (int i=range.getLength(0); --i>=1;) {
                point.x  = left  + PIXEL_SIZE*i;
                point.y  = upper - PIXEL_SIZE*j;
                buffer   = coverage.evaluate(point, buffer);
                double t = buffer[band];

                // Computes the expected value:
                double r00 = data.getSampleDouble(i-0, j-0, band);
                double r01 = data.getSampleDouble(i-0, j-1, band);
                double r10 = data.getSampleDouble(i-1, j-0, band);
                double r11 = data.getSampleDouble(i-1, j-1, band);
                double r = (r00+r01+r10+r11)/4;
                if (Double.isNaN(r)) {
                    assertTrue("NaN", Double.isNaN(t));
                } else {
                    assertEquals(r, t, EPS);
                }
            }
        }
    }
}
