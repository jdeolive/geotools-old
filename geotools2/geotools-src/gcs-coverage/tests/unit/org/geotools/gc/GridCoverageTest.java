/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
import java.awt.Color;
import java.awt.geom.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;

// JAI dependencies
import javax.media.jai.util.Range;

// Geotools dependencies
import org.geotools.pt.*;
import org.geotools.cs.*;
import org.geotools.ct.*;
import org.geotools.cv.*;
import org.geotools.gc.*;
import org.geotools.units.Unit;
import org.geotools.util.NumberRange;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link GridCoverage} implementation. This class can also be used
 * as a factory for sample {@link GridCoverage}, which may be used for tests
 * in other modules. The two following methods are for this purpose:
 *
 * <ul>
 *  <li>{@link #getNumExamples}</li>
 *  <li>{@link #getExample}</li>
 * </ul>
 *
 * @version $Id: GridCoverageTest.java,v 1.8 2003/05/13 10:59:53 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class GridCoverageTest extends TestCase {
    /**
     * Small value for comparaison of sample values. Since most grid coverage implementation in
     * Geotools 2 store geophysics values as <code>float</code> numbers, this <code>EPS</code>
     * value must be of the order of <code>float</code> relative precision, not <code>double</code>.
     */
    public static final double EPS = 1E-5;

    /**
     * Random number generator for this test.
     */
    private Random random;

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
    }

    /**
     * Apply an operation on the specified coverage, if wanted.
     * The default implementation returns <code>coverage</code>
     * with no change.
     */
    protected GridCoverage transform(final GridCoverage coverage) {
        return coverage;
    }

    /**
     * Returns a grid coverage filled with random values.
     */
    protected GridCoverage getRandomCoverage() {
        /*
         * Some constants used for the construction and test of the grid coverage.
         */
        final double      SCALE = 0.1; // Scale factor for pixel transcoding.
        final double     OFFSET = 5.0; // Offset factor for pixel transcoding.
        final double PIXEL_SIZE = .25; // Pixel size (in degrees). Used in transformations.
        final int   BEGIN_VALID = 3;   // The minimal valid index for quantative category.
        /*
         * Construct the grid coverage. We will assume that the grid coverage use
         * (longitude,latitude) coordinates, pixels of 0.25 degrees and a lower
         * left corner at 10°W 30°N.
         */
        final GridCoverage coverage;  // The grid coverage.
        final BufferedImage   image;  // The GridCoverage's data.
        final WritableRaster raster;  // The image's data as a raster.
        final Rectangle2D    bounds;  // The GridCoverage's envelope.
        final SampleDimension  band;  // The only image's band.

        band = new SampleDimension(new Category[] {
            new Category("No data",     null, 0),
            new Category("Land",        null, 1),
            new Category("Cloud",       null, 2),
            new Category("Temperature", null, BEGIN_VALID, 256, SCALE, OFFSET)
        }, Unit.get("°C"));
        image  = new BufferedImage(120, 80, BufferedImage.TYPE_BYTE_INDEXED);
        raster = image.getRaster();
        for (int i=raster.getWidth(); --i>=0;) {
            for (int j=raster.getHeight(); --j>=0;) {
                raster.setSample(i,j,0, random.nextInt(256));
            }
        }
        bounds = new Rectangle2D.Double(-10, 30, PIXEL_SIZE*image.getWidth(),
                                                 PIXEL_SIZE*image.getHeight());
        coverage = transform(new GridCoverage("Test", image, GeographicCoordinateSystem.WGS84,
                                              new Envelope(bounds), new SampleDimension[]{band},
                                              null, null));

        /* ----------------------------------------------------------------------------------------
         *
         * Grid coverage construction finished. Now, test it.
         */
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
        final int bandN = 0; // Band to test.
        double[] bufferCov = null;
        double[] bufferGeo = null;
        final double left  = bounds.getMinX() + (0.5*PIXEL_SIZE); // Includes translation to center
        final double upper = bounds.getMaxY() - (0.5*PIXEL_SIZE); // Includes translation to center
        final Point2D.Double point = new Point2D.Double(); // Will maps to pixel center.
        for (int j=raster.getHeight(); --j>=0;) {
            for (int i=raster.getWidth(); --i>=0;) {
                point.x = left  + PIXEL_SIZE*i;
                point.y = upper - PIXEL_SIZE*j;
                double r = raster.getSampleDouble(i,j,bandN);
                bufferCov =   coverage.evaluate(point, bufferCov);
                bufferGeo = geophysics.evaluate(point, bufferGeo);
                assertEquals(r, bufferCov[bandN], EPS);

                // Compare transcoded samples.
                if (r < BEGIN_VALID) {
                    assertTrue(Double.isNaN(bufferGeo[bandN]));
                } else {
                    assertEquals(OFFSET + SCALE*r, bufferGeo[bandN], EPS);
                }
            }
        }
        return coverage;
    }

    /**
     * Test the construction and access to a grid coverage.
     *
     * @throws IOException if an I/O operation was needed and failed.
     */
    public void testGridCoverage() throws IOException {
        final GridCoverage coverage = getRandomCoverage();
        assertNotNull(coverage);
        // Not much more test to do here, since most tests has been done
        // inside 'getRandomCoverage'.  This method will be overriden by
        // 'InterpolatorTest', which will perform more tests.
        for (int i=getNumExamples(); --i>=0;) {
            assertNotNull(getExample(i));
        }
    }



    ///////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                           ////////
    ////////    FACTORY METHODS FOR SAMPLE GridCoverage                                ////////
    ////////    Those methods do not use any of the above methods in this class.       ////////
    ////////    Factory methods are static and used by some tests in other modules.    ////////
    ////////                                                                           ////////
    ///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns a set of color for the specified code.
     */
    private static Color[] decode(final String color) {
        return new Color[] {Color.decode(color)};
    }

    /**
     * Returns the number of available image which may be used as example.
     */
    public static int getNumExamples() {
        return 1; // TODO: set to '2' if we commit the 'CHL01195.png' image (160 ko).
    }

    /**
     * Returns a {@link GridCoverage} which may be used as a "real world" example.
     *
     * @param  number The example number. Numbers are numeroted from
     *               0 to {@link #getNumExamples()} exclusive.
     * @return The "real world" grid coverage.
     * @throws IOException if an I/O operation was needed and failed.
     */
    public static GridCoverage getExample(final int number) throws IOException {
        final String           path;
        final String           unit;
        final Category[] categories;
        final CoordinateSystem   cs;
        final Rectangle2D    bounds;

        switch (number) {
            default: {
                throw new IllegalArgumentException(String.valueOf(number));
            }
            case 0: {
                unit = "°C";
                path = "test-data/QL95209.png";
                cs   = GeographicCoordinateSystem.WGS84;
                categories = new Category[] {
                    new Category("Coast line", decode("#000000"), new NumberRange(  0,   0), (MathTransform1D)null),
                    new Category("Cloud",      decode("#C3C3C3"), new NumberRange(  1,   9), (MathTransform1D)null),
                    new Category("Unused",     decode("#822382"), new NumberRange( 10,  29), (MathTransform1D)null),
                    new Category("Sea Surface Temperature", null, new NumberRange( 30, 219), 0.1, 10.0),
                    new Category("Unused",     decode("#A0505C"), new NumberRange(220, 239), (MathTransform1D)null),
                    new Category("Land",       decode("#D2C8A0"), new NumberRange(240, 254), (MathTransform1D)null),
                    new Category("No data",    decode("#FFFFFF"), new NumberRange(255, 255), (MathTransform1D)null),
                };
                // 41°S - 5°N ; 35°E - 80°E  (450 x 460 pixels)
                bounds = new Rectangle2D.Double(35, -41, 45, 46);
                break;
            }
            case 1: {
                unit = "mg/m³";
                path = "test-data/CHL01195.png";
                cs   = GeographicCoordinateSystem.WGS84;
                categories = new Category[] {
                    new Category("Land",       decode("#000000"), new NumberRange(255, 255), (MathTransform1D)null),
                    new Category("No data",    decode("#FFFFFF"), new NumberRange(  0,   0), (MathTransform1D)null),
                    new Category("Log chl-a",  null,              new NumberRange(  1, 254), 0.015, -1.985)
                };
                // 34°N - 45°N ; 07°W - 12°E  (1200 x 700 pixels)
                bounds = new Rectangle2D.Double(-7, 34, 19, 11);
                break;
            }
        }
        final SampleDimension[] bands = new SampleDimension[] {
            new SampleDimension(categories, (unit!=null) ? Unit.get(unit).rename(unit, null) : null)
        };
        final Envelope   envelope = new Envelope(bounds);
        final String     filename = new File(path).getName();
        final RenderedImage image = ImageIO.read(GridCoverageTest.class.getClassLoader().getResource(path));
        return new GridCoverage(filename, image, cs, envelope, bands, null, null);
    }
}
