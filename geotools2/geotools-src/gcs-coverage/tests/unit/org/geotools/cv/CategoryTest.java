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
package org.geotools.cv;

// J2SE dependencies
import java.util.Random;
import java.util.Arrays;

// Geotools dependencies
import org.geotools.cv.*;
import org.geotools.ct.*;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link Category} implementation.
 *
 * @version $Id: CategoryTest.java,v 1.2 2002/07/23 17:57:25 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class CategoryTest extends TestCase {
    /**
     * Random number generator for this test.
     */
    private Random random;

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(CategoryTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public CategoryTest(final String name) {
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
     * Check if a {@link Comparable} is a number identical to the supplied integer value.
     */
    private static void assertEquals(String message, Comparable number, int expected) {
        assertTrue("Integer.class", number instanceof Integer);
        assertEquals(message, expected, ((Number)number).intValue());
    }

    /**
     * Check if a {@link Comparable} is a number identical to the supplied float value.
     */
    private static void assertEquals(String message, Comparable number, double expected, double EPS)
    {
        assertTrue("Double.class", number instanceof Double);
        final double actual = ((Number)number).doubleValue();
        if (Double.isNaN(expected)) {
            assertEquals(message, toHexString(expected), toHexString(actual));
        } else {
            assertEquals(message, expected, actual, EPS);
        }
    }

    /**
     * Returns the specified value as an hexadecimal string. Usefull
     * for comparing NaN values.
     */
    private static String toHexString(final double value) {
        return Integer.toHexString(Float.floatToRawIntBits((float)value));
    }

    /**
     * Make sure that qualitative category produce the expected result.
     */
    public void testQualitativeCategory() throws TransformException {
        for (int pass=0; pass<100; pass++) {
            final int      sample    = random.nextInt(64);
            final Category category1 = new Category("Auto", null, sample);
            final Category category2 = new Category(category1.getName(null),
                                                    category1.getColors(),
                                                    category1.getRange(),
                                                    category1.getSampleToGeophysics());

            assertEquals("<init>", category1, category2);
            assertEquals("lower",  category1.rescale(false).getRange().getMinValue(), sample);
            assertEquals("upper",  category1.rescale(false).getRange().getMaxValue(), sample);
            try {
                category1.rescale(true).getRange();
                fail(); // Should not happen
            } catch (IllegalStateException exception) {
                // This is the expected exception.
            }
            assertNull("rescale(false).transform", category1.rescale(false).getSampleToGeophysics());
            assertNull("rescale(true).transform",  category1.rescale(true ).getSampleToGeophysics());
            for (int i=0; i<200; i++) {
                final double x  = 100*random.nextDouble();
                final double y1 = category1.transform.transform(x);
                final double y2 = category2.transform.transform(x);
                assertTrue("toGeophysics(1)", Double.isNaN(y1));
                assertTrue("toGeophysics(2)", Double.isNaN(y2));
                assertEquals("NaN", Double.doubleToRawLongBits(y1), Double.doubleToRawLongBits(y2));
                assertEquals("toSample(1)", sample, category1.inverse.transform.transform(y1), 0);
                assertEquals("toSample(2)", sample, category2.inverse.transform.transform(y2), 0);
            }
        }
    }

    /**
     * Make sure that linear category produce the expected result.
     * This test check also if the default {@link MathTransform1D}
     * for a linear relation is right.
     */
    public void testLinearCategory() throws TransformException {
        final double EPS = 1E-9;
        for (int pass=0; pass<100; pass++) {
            final int     lower = random.nextInt(64);
            final int     upper = random.nextInt(128) + lower+1;
            final double  scale = 10*random.nextDouble() + 0.1; // Must be positive for this test.
            final double offset = 10*random.nextDouble() - 5.0;
            final Category category = new Category("Auto", null, lower, upper, scale, offset);

            assertEquals("lower",  category.rescale(false).getRange().getMinValue(), lower);
            assertEquals("upper",  category.rescale(false).getRange().getMaxValue(), upper);
            assertEquals("minimum", category.rescale(true).getRange().getMinValue(), lower*scale+offset, EPS);
            assertEquals("maximum", category.rescale(true).getRange().getMaxValue(), upper*scale+offset, EPS);

            for (int i=0; i<200; i++) {
                final double x = 100*random.nextDouble();
                final double y = x*scale + offset;
                assertEquals("toGeophysics", y,     category.transform.transform(x), EPS);
                assertEquals("toSample", x, category.inverse.transform.transform(y), EPS);
            }
        }
    }
}
