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
import java.awt.geom.*;

// Geotools dependencies
import org.geotools.gc.*;
import org.geotools.cs.*;
import org.geotools.ct.*;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link GridLocalization} implementation.
 *
 * @version $Id: LocalizationGridTest.java,v 1.2 2002/08/05 17:53:40 desruisseaux Exp $
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
public class LocalizationGridTest extends TestCase {
    /**
     * Random number generator for this test.
     */
    private Random random;

    /**
     * The grid of localization to test.
     */
    protected LocalizationGrid grid;

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(LocalizationGridTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public LocalizationGridTest(final String name) {
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
     * Test some mathematical identities used if {@link LocalizationGrid#fitPlane}.
     */
    public void testGridCoverage() {
        int sum_x  = 0;
        int sum_y  = 0;
        int sum_xx = 0;
        int sum_yy = 0;
        int sum_xy = 0;

        final int width  = random.nextInt(100)+5;
        final int height = random.nextInt(100)+5;
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                sum_x  += x;
                sum_y  += y;
                sum_xx += x*x;
                sum_yy += y*y;
                sum_xy += x*y;
            }
        }
        final int n = width*height;
        assertEquals("sum_x" , (n * (width -1))/2,           sum_x );
        assertEquals("sum_y" , (n * (height-1))/2,           sum_y );
        assertEquals("sum_xy", (n * (width-1)*(height-1))/4, sum_xy);
    }
}
