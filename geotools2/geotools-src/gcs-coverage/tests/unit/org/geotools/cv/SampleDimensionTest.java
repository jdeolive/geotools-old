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
 * Test the {@link SampleDimension} implementation. Since <code>SampleDimension</code>
 * rely on {@link CategoryList} for many of its work, many <code>SampleDimension</code>
 * tests are actually <code>CategoryList</code> tests.
 *
 * @version $Id: SampleDimensionTest.java,v 1.1 2002/07/17 23:33:43 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class SampleDimensionTest extends TestCase {
    /**
     * Random number generator for this test.
     */
    private final Random random = new Random();

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(SampleDimensionTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public SampleDimensionTest(final String name) {
        super(name);
    }

    /**
     * Test the creation of a dummy sample dimension.
     */
    public void testSampleDimension() {
        final String[] CATEGORY = {
            "No data",
            "Clouds",
            "Lands"
        };
        final int[] NO_DATA = {0, 1, 255};
        assertEquals("<init>", CATEGORY.length, NO_DATA.length);
        final Category[] categories = new Category[CATEGORY.length+1];
        for (int i=0; i<CATEGORY.length; i++) {
            categories[i] = Category.create(CATEGORY[i], null, NO_DATA[i]);
        }
        final double scale  = 0.1;
        final double offset = 5.0;
        categories[CATEGORY.length] = Category.create("SST", null, 10, 200, scale, offset);
        final SampleDimension test = new SampleDimension(categories, null);
        /*
         * Finished initialization. Test now...
         */
        final double[] nodataValues = test.getNoDataValue();
        assertEquals("nodataValues.length", CATEGORY.length, nodataValues.length);
        for (int i=0; i<CATEGORY.length; i++) {
            assertEquals("nodataValues["+i+']', NO_DATA[i], nodataValues[i], 0);
        }
        assertEquals("scale",   test.getScale(),     scale,  0);
        assertEquals("offset",  test.getOffset(),    offset, 0);
        assertEquals("minimum", test.getMinimumValue(),   0, 0);
        assertEquals("maximum", test.getMaximumValue(), 255, 0);
    }
}
