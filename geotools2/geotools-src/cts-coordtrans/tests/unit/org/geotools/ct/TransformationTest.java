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
package org.geotools.ct;

// J2SE dependencies
import java.util.Random;

// Geotools dependencies
import org.geotools.cs.CoordinateSystemFactory;
import org.geotools.ct.CoordinateTransformationFactory;

// JUnit dependencies
import junit.framework.TestCase;


/**
 * Base class for test classes.
 *
 * @version $Id: TransformationTest.java,v 1.1 2002/07/12 00:01:02 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class TransformationTest extends TestCase
{
    /**
     * The default coordinate systems factory.
     */
    protected CoordinateSystemFactory csFactory;

    /**
     * The default transformations factory.
     */
    protected CoordinateTransformationFactory trFactory;
    
    /**
     * Random numbers generator.
     */
    protected Random random;

    /**
     * Constructs a test case with the given name.
     */
    public TransformationTest(final String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests. This include
     * various factories.   This method is called once before
     * any test is executed.
     *
     * @throws Exception if the setup failed.
     */
    protected void setUp() throws Exception {
        super.setUp();
        random    = new Random();
        csFactory = CoordinateSystemFactory.getDefault();
        trFactory = CoordinateTransformationFactory.getDefault();
    }

    /**
     * Compare two arrays of points.
     *
     * @param name      The name of the comparaison to be performed.
     * @param expected  The expected array of points.
     * @param actial    The actual array of points.
     * @param delta     The maximal difference tolerated in comparaisons for each dimension.
     *                  This array length must be equal to coordinate dimension (usually 1, 2 or 3).
     */
    public static void assertPointsEqual(final String   name,
                                         final double[] expected,
                                         final double[] actual,
                                         final double[] delta)
    {
        final int dimension = delta.length;
        final int stop = Math.min(expected.length, actual.length)/dimension * dimension;
        assertEquals("Array length for expected points", stop, expected.length);
        assertEquals("Array length for actual points",   stop,   actual.length);

        final StringBuffer buffer = new StringBuffer(name);
        buffer.append(": point[");
        final int start = buffer.length();

        for (int i=0; i<stop; i++) {
            buffer.setLength(start);
            buffer.append(i/dimension);
            buffer.append(", dimension ");
            buffer.append(i % dimension);
            buffer.append(" of ");
            buffer.append(']');
            assertEquals(buffer.toString(), expected[i], actual[i], delta[i % dimension]);
        }
    }
}
