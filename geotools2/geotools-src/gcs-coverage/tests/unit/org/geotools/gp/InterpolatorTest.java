/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2002, Institut de Recherche pour le Développement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.gp;

// J2SE dependencies
import java.awt.geom.*;
import java.awt.image.*;

// JAI dependencies
import javax.media.jai.*;

// Geotools dependencies
import org.geotools.gp.*;
import org.geotools.gc.*;
import org.geotools.cv.*;
import org.geotools.cs.*;
import org.geotools.pt.*;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link Interpolator} implementation. This method inherit all
 * {@link GridCoverage} test. Because we override {@link #setUp}, tests
 * will be performed on {@link Interpolator} objects instead of default
 * {@link GridCoverage}.
 *
 * @version $Id: InterpolatorTest.java,v 1.1 2002/07/27 22:10:30 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class InterpolatorTest extends GridCoverageTest {
    /**
     * The interpolators to use.
     */
    private Interpolation[] interpolations;

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
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
        coverage = Interpolator.create(coverage, interpolations);
    }

    /**
     * Test the interpolations. Since <code>testGridCoverage()</code> tests value
     * at the center of pixels, all interpolations results should be identical to
     * a result without interpolation.
     */
    public void testGridCoverage() {
        assertTrue(coverage instanceof Interpolator);
        assertTrue(coverage.geophysics(true)  instanceof Interpolator);
        assertTrue(coverage.geophysics(false) instanceof Interpolator);
        super.testGridCoverage();
    }

    /**
     * Test bilinear intersection at pixel edges. It should be equals
     * to the average of the four pixels around.
     */
    public void testInterpolationAtEdges() {
        final GridCoverage coverage = Interpolator.create(this.coverage.geophysics(true),
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

                // Compute the expected value:
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
