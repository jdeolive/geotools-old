/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Management Committee (PMC)
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
 *    Foundation, Inc.. 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.cs;

// J2SE dependencies
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.IllegalPathStateException;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.cs.Ellipsoid;
import org.geotools.resources.XMath;


/**
 * Test the geodetic calculator.
 *
 * @version $Id: GeodeticCalculatorTest.java,v 1.1 2004/03/08 17:48:27 desruisseaux Exp $
 */
public class GeodeticCalculatorTest extends TestCase {
    /**
     * Run the test from the command line.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(GeodeticCalculatorTest.class);
    }

    /**
     * Construct a new test case.
     */
    public GeodeticCalculatorTest(final String name) {
        super(name);
    }

    /**
     * Test path on the 45th parallel. Data for this test come from the
     * <A HREF="http://www.univ-lemans.fr/~hainry/articles/loxonavi.html">Orthodromie et
     * loxodromie</A> page.
     */
    public void testParallel45() {
        // Column 1: Longitude difference in degrees.
        // Column 2: Orthodromic distance in kilometers
        // Column 3: Loxodromic  distance in kilometers
        final double[] DATA = {
              0.00,     0,      0,
             11.25,   883,    884,
             22.50,  1762,   1768,
             33.75,  2632,   2652,
             45.00,  3489,   3536,
             56.25,  4327,   4419,
             67.50,  5140,   5303,
             78.75,  5923,   6187,
             90.00,  6667,   7071,
            101.25,  7363,   7955,
            112.50,  8002,   8839,
            123.75,  8573,   9723,
            135.00,  9064,  10607,
            146.25,  9463,  11490,
            157.50,  9758,  12374,
            168.75,  9939,  13258,
            180.00, 10000,  14142
        };
        final double             R          = 20000/Math.PI;
        final Ellipsoid          ellipsoid  = Ellipsoid.createEllipsoid("Test",R,R,Unit.KILOMETRE);
        final GeodeticCalculator calculator = new GeodeticCalculator(ellipsoid);
        calculator.setAnchorPoint(0, 45);
        for (int i=0; i<DATA.length; i+=3) {
            calculator.setDestinationPoint(DATA[i], 45);
            final double orthodromic = calculator.getOrthodromicDistance();
//          final double loxodromic  = calculator. getLoxodromicDistance();
            assertEquals("Orthodromic distance", DATA[i+1], orthodromic, 0.75);
//          assertEquals( "Loxodromic distance", DATA[i+2], loxodromic,  0.75);
            /*
             * Test the orthodromic path. We compare its length with the expected length.
             */
            int    count=0;
            double length=0, lastX=Double.NaN, lastY=Double.NaN;
            final Shape        path     = calculator.getGeodeticCurve(1000);
            final PathIterator iterator = path.getPathIterator(null, 0.1);
            final double[]     buffer   = new double[6];
            while (!iterator.isDone()) {
                switch (iterator.currentSegment(buffer)) {
                    case PathIterator.SEG_LINETO: {
                        count++;
                        length += ellipsoid.orthodromicDistance(lastX, lastY, buffer[0], buffer[1]);
                        // Fall through
                    }
                    case PathIterator.SEG_MOVETO: {
                        lastX = buffer[0];
                        lastY = buffer[1];
                        break;
                    }
                    default: {
                        throw new IllegalPathStateException();
                    }
                }
                iterator.next();
            }
            assertEquals("Segment count", 1000, count); // Implementation check; will no longer be
                                                        // valid when the path will contains curves.
            assertEquals("Orthodromic path length", orthodromic, length, 1E-4);
        }
    }
}
