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

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link Category} implementation.
 *
 * @version $Id: CategoryTest.java,v 1.1 2002/07/17 23:33:43 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class CategoryTest extends TestCase {
    /**
     * Random number generator for this test.
     */
    private final Random random = new Random();

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
     * Make sure that qualitative category produce the expected result.
     */
    public void testQualitativeCategory() {
        for (int pass=0; pass<100; pass++) {
            final int sample = random.nextInt(64);
            final Category category1 = Category.create("Auto", null, sample);
            final Category category2 = new Category("Category", category1.getColors(),
                                       category1.getRange(SampleInterpretation.INDEXED),
                                       category1.getSampleToGeophysics());

            assertTrue(!category1.equals(category2));
            assertTrue(category1 instanceof QualitativeCategory);
            try {
                category1.getRange(SampleInterpretation.GEOPHYSICS);
                fail(); // Should not happen
            } catch (IllegalStateException exception) {
                // This is the expected exception.
            }
            for (int i=0; i<200; i++) {
                final double x  = 100*random.nextDouble();
                final double y1 = category1.toGeophysicsValue(x);
                final double y2 = category2.toGeophysicsValue(x);
                assertTrue("toGeophysics(1)", Double.isNaN(y1));
                assertTrue("toGeophysics(2)", Double.isNaN(y2));
                assertEquals("NaN", Double.doubleToRawLongBits(y1), Double.doubleToRawLongBits(y2));
                assertEquals("toSample(1)", sample, category1.toSampleValue(y1), 0);
                assertEquals("toSample(2)", sample, category2.toSampleValue(y2), 0);
            }
        }
    }

    /**
     * Make sure that linear category produce the expected result.
     * This test check also if the default {@link MathTransform1D}
     * for a linear relation is right.
     */
    public void testLinearCategory() {
        final double EPS = 1E-9;
        for (int pass=0; pass<100; pass++) {
            final int     lower = random.nextInt(64);
            final int     upper = random.nextInt(128) + lower+1;
            final double  scale = 10*random.nextDouble() + 0.1; // Must be positive for this test.
            final double offset = 10*random.nextDouble() - 5.0;
            final Category category1 = Category.create("Auto", null, lower, upper, scale, offset);
            final Category category2 = new Category("Category", category1.getColors(),
                                       category1.getRange(SampleInterpretation.INDEXED),
                                       category1.getSampleToGeophysics());

            assertTrue (!category1.equals(category2));
            assertTrue ( category1 instanceof LinearCategory);
            assertEquals(category1.getRange(SampleInterpretation.GEOPHYSICS),
                         category2.getRange(SampleInterpretation.GEOPHYSICS));

            for (int i=0; i<200; i++) {
                final double x = 100*random.nextDouble();
                final double y = x*scale + offset;
                assertEquals("toGeophysics(1)", y, category1.toGeophysicsValue(x), EPS);
                assertEquals("toGeophysics(2)", y, category2.toGeophysicsValue(x), EPS);
                final double xClamp = Math.min(Math.max(x, lower), upper-1);
                assertEquals("toSample(1)", xClamp, category1.toSampleValue(y), EPS);
                assertEquals("toSample(2)", xClamp, category2.toSampleValue(y), EPS);
            }
        }
    }

    /**
     * Test the {@link CategoryComparator} object, more specifically its
     * {@link CategoryComparator#binarySearch} method.
     */
    public void testCategoryComparator() {
        for (int pass=0; pass<100; pass++) {
            final double[] array = new double[200];
            for (int i=0; i<array.length; i++) {
                array[i] = (random.nextInt(1000)-500)/10;
            }
            Arrays.sort(array);
            for (int i=0; i<300; i++) {
                final double searchFor = (random.nextInt(1500)-750)/10;
                assertEquals("binarySearch", Arrays.binarySearch(array, searchFor),
                                 CategoryComparator.binarySearch(array, searchFor));
            }
            /*
             * Previous test didn't tested NaN values (which is the main difference
             * between binarySearch method in Arrays and CategoryComparator). Now test it.
             */
            Category[] categories = new Category[array.length];
            for (int i=0; i<categories.length; i++) {
                categories[i] = Category.create(String.valueOf(i), null, random.nextInt(1000));
            }
            categories = CategoryComparator.BY_VALUES.sort(categories);
            assertTrue("isSorted", CategoryComparator.BY_VALUES.isSorted(categories));
            for (int i=0; i<categories.length; i++) {
                array[i] = categories[i].minValue;
            }
            for (int i=0; i<categories.length; i++) {
                final double expected = categories[i].minValue;
                final double actual   = categories[CategoryComparator.binarySearch(array, expected)].minValue;
                assertEquals("binarySearch", Double.doubleToRawLongBits(expected),
                                             Double.doubleToRawLongBits(actual));
            }
        }
    }
}
