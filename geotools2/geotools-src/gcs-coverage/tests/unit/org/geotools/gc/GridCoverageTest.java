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
package org.geotools.gc;

// J2SE dependencies
import java.util.Random;
import java.util.Arrays;
import java.awt.image.*;
import java.awt.geom.*;

// Geotools dependencies
import org.geotools.cv.*;
import org.geotools.gc.*;
import org.geotools.cs.*;
import org.geotools.pt.*;
import org.geotools.units.Unit;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link GridCoverage} implementation.
 *
 * @version $Id: GridCoverageTest.java,v 1.1 2002/07/27 12:43:05 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class GridCoverageTest extends TestCase {
    /**
     * Pixel size (in degrees). Used for transforming pixel coordinates to degrees.
     */
    private final static double PIXEL_SIZE = 0.25;

    /**
     * Scale factor for pixel transcoding.
     */
    private static final double SCALE = 0.1;

    /**
     * Offset factor for pixel transcoding.
     */
    private static final double OFFSET = 5;

    /**
     * Small value for comparaison. Must be in <code>float</code>
     * range (not <code>double</code>).
     */
    private static final double EPS = 1E-5;

    /**
     * Random number generator for this test.
     */
    private Random random;

    /**
     * The GridCoverage's sample dimension.
     */
    private SampleDimension band;

    /**
     * The GridCoverage's data.
     */
    private BufferedImage image;

    /**
     * The GridCoverage's envelope. We will assume that the grid coverage use
     * (longitude,latitude) coordinates, pixels of 0.25 degrees and a lower
     * left corner at 10°W 30°N.
     */
    private Rectangle2D bounds;

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
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
        random = new Random();
        band = new SampleDimension(new Category[] {
            new Category("No data",     null, 0),
            new Category("Land",        null, 1),
            new Category("Cloud",       null, 2),
            new Category("Temperature", null, 3, 256, SCALE, OFFSET)
        }, Unit.get("°C"));

        image = new BufferedImage(120, 80, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = image.getRaster();
        for (int i=raster.getWidth(); --i>=0;) {
            for (int j=raster.getHeight(); --j>=0;) {
                raster.setSample(i,j,0, random.nextInt(256));
            }
        }
        bounds = new Rectangle2D.Double(-10, 30, PIXEL_SIZE*image.getWidth(),
                                                 PIXEL_SIZE*image.getHeight());
    }

    /**
     * Test the construction and access to a grid coverage.
     */
    public void testGridCoverage() {
        GridCoverage coverage = new GridCoverage("Test", image, GeographicCoordinateSystem.WGS84,
                                                 new Envelope(bounds), new SampleDimension[]{band},
                                                 null, null);
        coverage = applyOperation(coverage);
        assertSame(image.getTile(0,0), coverage.getRenderedImage().getTile(0,0));

        // Test the creation of a "geophysics" view.
        GridCoverage geophysics= coverage.geophysics(true);
        assertSame(coverage,     coverage.geophysics(false));
        assertSame(coverage,   geophysics.geophysics(false));
        assertSame(geophysics, geophysics.geophysics(true ));
        assertTrue(!coverage.equals(geophysics));

        // Test sample dimensions.
        assertTrue( !coverage.getSampleDimensions()[0].getSampleToGeophysics().isIdentity());
        assertTrue(geophysics.getSampleDimensions()[0].getSampleToGeophysics().isIdentity());

        // Compare data.
        final int band = 0; // Band to test.
        double[] bufferCov = null;
        double[] bufferGeo = null;
        final Raster data  = image.getRaster();
        final double left  = bounds.getMinX() + (0.5*PIXEL_SIZE); // Pixel center
        final double upper = bounds.getMaxY() - (0.5*PIXEL_SIZE); // Pixel center
        final Point2D.Double point = new Point2D.Double();
        for (int j=data.getHeight(); --j>=0;) {
            for (int i=data.getWidth(); --i>=0;) {
                point.x = left  + PIXEL_SIZE*i;
                point.y = upper - PIXEL_SIZE*j;
                double r = data.getSampleDouble(i,j,band);
                bufferCov =   coverage.evaluate(point, bufferCov);
                bufferGeo = geophysics.evaluate(point, bufferGeo);
                assertEquals(r, bufferCov[band], EPS);

                // Compare transcoded samples.
                if (r < 3) {
                    assertTrue(Double.isNaN(bufferGeo[band]));
                } else {
                    assertEquals(OFFSET + SCALE*r, bufferGeo[band], EPS);
                }
            }
        }
    }

    /**
     * Apply a transformation on the specified grid coverage. This method will be
     * overrided by <code>InterpolatorTest</code> in <code>org.geotools.gp</code>.
     */
    protected GridCoverage applyOperation(final GridCoverage coverage) {
        return coverage;
    }
}
