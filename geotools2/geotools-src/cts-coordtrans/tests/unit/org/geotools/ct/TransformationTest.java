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
package org.geotools.ct;

// J2SE dependencies
import java.util.Random;

// Geotools dependencies
import org.geotools.cs.*;
import org.geotools.ct.*;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Base class for transformation test classes.
 *
 * @version $Id: TransformationTest.java,v 1.9 2004/03/08 11:30:56 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class TransformationTest extends TestCase {
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
     * Uses reflection to dynamically create a test suite containing all 
     * the <code>testXXX()</code> methods - from the JUnit FAQ.
     */
    public static Test suite() {
        return new TestSuite(TransformationTest.class);
    }
    
    /**
     * Runs the tests with the textual test runner.
     */
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
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
        random    = new Random(-3531834320875149028L);
        csFactory = CoordinateSystemFactory.getDefault();
        trFactory = CoordinateTransformationFactory.getDefault();
    }

    /**
     * Quick self test, in part to give this test suite a test
     * and also to test the internal method.
     */
    public void testAssertPointsEqual(){
        String name = "self test";
        double a[] = {10,10};
        double b[] = {10.1,10.1};
        double delta[] = {0.2,0.2};
        assertPointsEqual(name,a,b,delta);
    }

    /**
     * Make sure that <code>createFromCoordinateSystems(sourceCS, targetCS)</code>
     * returns an identity transform when <code>sourceCS</code> and <code>targetCS</code>
     * are identical, and tests the promiscuous CS.
     */
    public void testPromiscuousTransform() throws TransformException {
        assertTrue(trFactory.createFromCoordinateSystems(GeographicCoordinateSystem.WGS84,
                   GeographicCoordinateSystem.WGS84).getMathTransform().isIdentity());
        assertTrue(trFactory.createFromCoordinateSystems(LocalCoordinateSystem.CARTESIAN,
                   LocalCoordinateSystem.CARTESIAN).getMathTransform().isIdentity());
        assertTrue(trFactory.createFromCoordinateSystems(LocalCoordinateSystem.PROMISCUOUS,
                   LocalCoordinateSystem.PROMISCUOUS).getMathTransform().isIdentity());
        assertTrue(trFactory.createFromCoordinateSystems(LocalCoordinateSystem.PROMISCUOUS,
                   LocalCoordinateSystem.CARTESIAN).getMathTransform().isIdentity());
        assertTrue(trFactory.createFromCoordinateSystems(LocalCoordinateSystem.CARTESIAN,
                   LocalCoordinateSystem.PROMISCUOUS).getMathTransform().isIdentity());
        assertTrue(trFactory.createFromCoordinateSystems(GeographicCoordinateSystem.WGS84,
                   LocalCoordinateSystem.PROMISCUOUS).getMathTransform().isIdentity());
        assertTrue(trFactory.createFromCoordinateSystems(LocalCoordinateSystem.PROMISCUOUS,
                   GeographicCoordinateSystem.WGS84).getMathTransform().isIdentity());
        try {
            trFactory.createFromCoordinateSystems(LocalCoordinateSystem.CARTESIAN,
                                                  GeographicCoordinateSystem.WGS84);
            fail();
        } catch (CannotCreateTransformException exception) {
            // This is the expected exception.
        }
        try {
            trFactory.createFromCoordinateSystems(GeographicCoordinateSystem.WGS84,
                                                  LocalCoordinateSystem.CARTESIAN);
            fail();
        } catch (CannotCreateTransformException exception) {
            // This is the expected exception.
        }
    }
    
    /**
     * Convenience method for checking if a boolean value is false.
     */
    public static void assertFalse(final boolean value) {
        assertTrue(!value);
    }

    /**
     * Returns <code>true</code> if the specified number is real
     * (neither NaN or infinite).
     */
    public static boolean isReal(final double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    /**
     * Verify that the specified transform implements {@link MathTransform1D}
     * or {@link MathTransform2D} as needed.
     *
     * @param transform The transform to test.
     */
    public static void assertInterfaced(final MathTransform transform) {
        if ((transform instanceof LinearTransform) && !((LinearTransform) transform).getMatrix().isAffine())
        {
            // Special case: Non-affine transforms not yet declared as a 1D or 2D transform.
            return;
        }
        int dim = transform.getDimSource();
        if (transform.getDimTarget() != dim) {
            dim = 0;
        }
        assertTrue("MathTransform1D", (dim==1) == (transform instanceof MathTransform1D));
        assertTrue("MathTransform2D", (dim==2) == (transform instanceof MathTransform2D));
    }

    /**
     * Compare two arrays of points.
     *
     * @param name      The name of the comparaison to be performed.
     * @param expected  The expected array of points.
     * @param actual    The actual array of points.
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
            buffer.append(dimension);
            buffer.append(']');
            if (isReal(expected[i])) {
                // The "two steps" method in ConcatenatedTransformTest sometime produces
                // random NaN numbers. This "two steps" is used only for comparaison purpose;
                // the "real" (tested) method work better.
                assertEquals(buffer.toString(), expected[i], actual[i], delta[i % dimension]);
            }
        }
    }
}
