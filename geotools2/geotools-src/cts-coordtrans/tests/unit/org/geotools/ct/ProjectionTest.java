/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.ct;

// Geotools dependencies
import org.geotools.pt.CoordinatePoint;
import org.geotools.cs.FactoryException;
import org.geotools.ct.TransformException;
import org.geotools.ct.MathTransformFactory;
import org.geotools.ct.NoSuchClassificationException;
import org.geotools.ct.MathTransform;

// JAI dependencies
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListDescriptor;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.AssertionFailedError;


/**
 * Tests projection equations as well as the integration with {@link MathTransformFactory}. The
 * spherical tests here tests real spheres (tests in <code>&quot;Simple_TestScript&quot;</code>
 * are not exactly spherical).
 *
 * @version $Id: ProjectionTest.java,v 1.1 2003/05/14 10:15:41 desruisseaux Exp $
 * @author Rueben Schulz
 */
public class ProjectionTest extends TestCase {
    /**
     * Set to <code>true</code> for printing some informations to standard output
     * while performing tests.
     */
    private static final boolean VERBOSE = false;
    
    /** tolerance for test when units are degrees*/
    private final static double[] TOL_DEG = {1E-6,1E-6};
    
    /** tolerance for test when units are metres*/
    private final static double[] TOL_M = {1E-2,1E-2};
    
    /** factory to use to create projection transforms*/
    private MathTransformFactory mtFactory;
    
    /** parameters for a transform*/
    private ParameterList params;
    
    /** transform to test*/
    private MathTransform transform;

    /**
     * Construct a test with the given name.
     */
    public ProjectionTest(String name) {
        super(name);
    }
    
    /**
     * Uses reflection to dynamically create a test suite containing all 
     * the <code>testXXX()</code> methods - from the JUnit FAQ.
     */
    public static Test suite() {
        return new TestSuite(ProjectionTest.class);
    }
    
    /**
     * Runs the tests with the textual test runner.
     */
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
    
    /**
     * Set up common objects used by all tests.
     */
    protected void setUp() {
        mtFactory = MathTransformFactory.getDefault();
    }
    
    /**
     * Release common objects used by all tests.
     */
    protected void tearDown() {
        mtFactory = null;
    }
    
    /**
     * Check if two coordinate points are equals, in the limits of the specified
     * tolerance vector.
     *
     * @param expected  The expected coordinate point.
     * @param actual    The actual coordinate point.
     * @param tolerance The tolerance vector. If this vector length is smaller than the number
     *                  of dimension of <code>actual</code>, then the last tolerance value will
     *                  be reused for all extra dimensions.
     * @throws AssertionFailedError If the actual point is not equals to the expected point.
     */
    private static void assertEquals(final CoordinatePoint expected,
                                     final CoordinatePoint actual,
                                     final double[]        tolerance)
            throws AssertionFailedError
    {
        final int dimension = actual.getDimension();
        final int lastToleranceIndex = tolerance.length-1;
        assertEquals("The coordinate point doesn't have the expected dimension",
        expected.getDimension(), dimension);
        for (int i=0; i<dimension; i++) {
            assertEquals("Mismatch for ordinate "+i+" (zero-based):",
            expected.getOrdinate(i), actual.getOrdinate(i),
            tolerance[Math.min(i, lastToleranceIndex)]);
        }
    }
    
    /**
     * Helper method to test trasnform from a source to a target point.
     * Coordinate points are (x,y) or (long, lat)
     */
    private static void doTransform(CoordinatePoint source,
                                    CoordinatePoint target,
                                    MathTransform transform)
            throws TransformException 
    {
        CoordinatePoint calculated;
        calculated = transform.transform(source, null);
        assertEquals(target, calculated, TOL_M);
        
        //the inverse
        target = source;
        source = calculated;
        calculated = transform.inverse().transform(source, null);
        assertEquals(target, calculated, TOL_DEG);
    }
    
    /**
     * Print parameters for the specified projection.
     * Used to see the if parameters for a transform are correct.
     */
    private void printParameters(final String proj) throws NoSuchClassificationException {
        ParameterList parameters = mtFactory.getMathTransformProvider(proj).getParameterList();
        ParameterListDescriptor desc = parameters.getParameterListDescriptor();
        String[] descList = desc.getParamNames();
        System.out.print(proj);
        System.out.println(" Parameters:");
        for(int i=0; i<descList.length; ++i) {
            System.out.print("    ");
            System.out.println(descList[i]);
        }
    }
    
    /**
     * Some tests for the Mercator Projection.
     */
    public void testMercator() throws FactoryException, TransformException {
        
        ///////////////////////////////////////
        // Mercator_1SP tests                //
        ///////////////////////////////////////
        if (VERBOSE) {
            printParameters("Mercator_1SP");
        }
        //epsg example (p 26)
        params = mtFactory.getMathTransformProvider("Mercator_1SP").getParameterList();
        params.setParameter("semi_major", 6377397.155);
        params.setParameter("semi_minor", 6356078.963);
        params.setParameter("central_meridian", 110.0);
        params.setParameter("scale_factor", 0.997);
        params.setParameter("false_easting", 3900000.0);
        params.setParameter("false_northing", 900000.0);
        transform = mtFactory.createParameterizedTransform("Mercator_1SP", params);
        if (VERBOSE) {
            System.out.println(transform);
        }
        doTransform(new CoordinatePoint(120.0, -3.0),
                    new CoordinatePoint(5009726.58, 569150.82), transform);
        
        //spherical test (Snyder p. 266)
        params.setParameter("semi_major", 1.0);
        params.setParameter("semi_minor", 1.0);
        params.setParameter("central_meridian", -180.0);
        params.setParameter("scale_factor", 1.0);
        params.setParameter("false_easting", 0.0);
        params.setParameter("false_northing", 0.0);
        transform = mtFactory.createParameterizedTransform("Mercator_1SP", params);
        if (VERBOSE) {
            System.out.println(transform);
        }
        doTransform(new CoordinatePoint(-75.0, 35.0),
                    new CoordinatePoint(1.8325957, 0.6528366), transform);
        
        //spherical test 2 (original units for target were feet)
        params.setParameter("semi_major", 6370997.0);
        params.setParameter("semi_minor", 6370997.0);
        params.setParameter("central_meridian", 0.0);
        params.setParameter("scale_factor", 1.0);
        params.setParameter("false_easting", 0.0);
        params.setParameter("false_northing", 0.0);
        transform = mtFactory.createParameterizedTransform("Mercator_1SP", params);
        if (VERBOSE) {
            System.out.println(transform);
        }    
        doTransform(new CoordinatePoint(-123.1, 49.2166666666),
                    new CoordinatePoint(-13688089.02443480, 6304639.84599441), transform);
        
        
        ///////////////////////////////////////
        // Mercator_2SP tests                //
        ///////////////////////////////////////
        if (VERBOSE) {
            printParameters("Mercator_2SP");
        }
        //epsg p25. Note FE and FN are wrong in guide 7. Correct in epsg database v6.3.
        params = mtFactory.getMathTransformProvider("Mercator_2SP").getParameterList();
        params.setParameter("semi_major", 6378245.0);
        params.setParameter("semi_minor", 6356863.019);
        params.setParameter("central_meridian", 51.0);
        params.setParameter("standard_parallel1", 42.0);
        params.setParameter("false_easting", 0.0);
        params.setParameter("false_northing", 0.0);
        transform = mtFactory.createParameterizedTransform("Mercator_2SP", params);
        if (VERBOSE) {
            System.out.println(transform);
        }    
        doTransform(new CoordinatePoint(53.0, 53.0),
                    new CoordinatePoint(165704.29, 5171848.07), transform);
        
        //a spherical case (me)
        params = mtFactory.getMathTransformProvider("Mercator_2SP").getParameterList();
        params.setParameter("semi_major", 6370997.0);
        params.setParameter("semi_minor", 6370997.0);
        params.setParameter("central_meridian", 180.0);
        params.setParameter("standard_parallel1", 60.0);
        params.setParameter("false_easting", -500000.0);
        params.setParameter("false_northing", -1000000.0);
        transform = mtFactory.createParameterizedTransform("Mercator_2SP", params);
        if (VERBOSE) {
            System.out.println(transform);
        }    
        doTransform(new CoordinatePoint(-123.1, 49.2166666666),
                    new CoordinatePoint(2663494.1734, 2152319.9230), transform);
        
    }
    
    /**
     * Some tests for the Lambert Conic Conformal Projection.
     */
    public void testLambert() throws FactoryException, TransformException {
        
        ///////////////////////////////////////
        // Lambert_Conformal_Conic_1SP tests //
        ///////////////////////////////////////
        if (VERBOSE) {
            printParameters("Lambert_Conformal_Conic_1SP");
        }
        //EPGS p. 18
        params = mtFactory.getMathTransformProvider("Lambert_Conformal_Conic_1SP").getParameterList();
        params.setParameter("semi_major", 6378206.4);
        params.setParameter("semi_minor", 6356583.8);
        params.setParameter("central_meridian", -77.0);
        params.setParameter("latitude_of_origin", 18.0);
        params.setParameter("scale_factor", 1.0);
        params.setParameter("false_easting", 250000.0);
        params.setParameter("false_northing", 150000.0);
        transform = mtFactory.createParameterizedTransform("Lambert_Conformal_Conic_1SP", params);
        if (VERBOSE) {
            System.out.println(transform);
        }    
        doTransform(new CoordinatePoint(-76.943683333, 17.932166666),
                    new CoordinatePoint(255966.58, 142493.51), transform);
        
        //Spherical (me)
        params.setParameter("semi_major", 6370997.0);
        params.setParameter("semi_minor", 6370997.0);
        params.setParameter("central_meridian", 111.0);
        params.setParameter("latitude_of_origin", -55.0);
        params.setParameter("scale_factor", 1.0);
        params.setParameter("false_easting", 500000.0);
        params.setParameter("false_northing", 1000000.0);
        transform = mtFactory.createParameterizedTransform("Lambert_Conic_Conformal_1SP", params);
        if (VERBOSE) {
            System.out.println(transform.toString());
        }    
        doTransform(new CoordinatePoint(151.283333333, -33.916666666),
                    new CoordinatePoint(4232963.1816, 2287639.9866), transform);
        
        
        ///////////////////////////////////////
        // Lambert_Conformal_Conic_2SP tests //
        ///////////////////////////////////////
        if (VERBOSE) {
            printParameters("Lambert_Conformal_Conic_2SP");
        }        
        //EPSG p. 17
        params = mtFactory.getMathTransformProvider("Lambert_Conformal_Conic_2SP").getParameterList();
        params.setParameter("semi_major", 6378206.4);
        params.setParameter("semi_minor", 6356583.8);
        params.setParameter("central_meridian", -99.0);
        params.setParameter("latitude_of_origin", 27.833333333);
        params.setParameter("standard_parallel1", 28.383333333);
        params.setParameter("standard_parallel2", 30.283333333);
        params.setParameter("false_easting", 609601.218);        //metres
        params.setParameter("false_northing", 0.0);
        transform = mtFactory.createParameterizedTransform("Lambert_Conformal_Conic_2SP", params);
        if (VERBOSE) {
            System.out.println(transform);
        }    
        doTransform(new CoordinatePoint(-96.0, 28.5),
                    new CoordinatePoint(903277.7965, 77650.94219), transform);
        
        //Spherical (me)
        params.setParameter("semi_major", 6370997.0);
        params.setParameter("semi_minor", 6370997.0);
        params.setParameter("central_meridian", -120.0);
        params.setParameter("latitude_of_origin", 0.0);
        params.setParameter("standard_parallel1", 2.0);
        params.setParameter("standard_parallel2", 60.0);
        params.setParameter("false_easting", 0.0);
        params.setParameter("false_northing", 0.0);
        transform = mtFactory.createParameterizedTransform("Lambert_Conic_Conformal_2SP", params);
        if (VERBOSE) {
            System.out.println(transform);
        }    
        doTransform(new CoordinatePoint(139.733333333, 35.6833333333),
                    new CoordinatePoint(-6789805.6471, 7107623.6859), transform);
        
        //1SP where SP != lat of origin (me)
        params.setParameter("semi_major", 6378137.0);
        params.setParameter("semi_minor", 6356752.31424518);
        params.setParameter("central_meridian", 0.0);
        params.setParameter("latitude_of_origin", -50.0);
        params.setParameter("standard_parallel1", -40.0);
        params.setParameter("standard_parallel2", -40.0);
        params.setParameter("false_easting", 100000.0);
        params.setParameter("false_northing", 0.0);
        transform = mtFactory.createParameterizedTransform("Lambert_Conformal_Conic_2SP", params);
        if (VERBOSE) {
            System.out.println(transform);
        }    
        doTransform(new CoordinatePoint(18.45, -33.9166666666),
                    new CoordinatePoint(1803288.3324, 1616657.7846), transform);
        
        
        ///////////////////////////////////////////////
        // Lambert_Conformal_Conic_2SP_Belgium test  //
        ///////////////////////////////////////////////
        if (VERBOSE) {
            printParameters("Lambert_Conic_Conformal_2SP_Belgium");
        }    
        //epsg p. 19
        params = mtFactory.getMathTransformProvider("Lambert_Conic_Conformal_2SP_Belgium").getParameterList();
        params.setParameter("semi_major", 6378388.0);
        params.setParameter("semi_minor", 6356911.946);
        params.setParameter("central_meridian", 4.356939722);
        params.setParameter("latitude_of_origin", 90.0);
        params.setParameter("standard_parallel1", 49.833333333);
        params.setParameter("standard_parallel2", 51.166666666);
        params.setParameter("false_easting", 150000.01);
        params.setParameter("false_northing", 5400088.44);
        transform = mtFactory.createParameterizedTransform("Lambert_Conic_Conformal_2SP_Belgium", params);
        if (VERBOSE) {
            System.out.println(transform);
        }    
        doTransform(new CoordinatePoint(5.807370277, 50.6795725),
                    new CoordinatePoint(251763.20, 153034.13), transform);
    }
}
