/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
/*
 * LiteralTest.java
 * JUnit based test
 *
 * Created on June 21, 2002, 12:24 PM
 */
package org.geotools.filter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import junit.framework.*;


/**
 * Tests the literal expressions.
 *
 * @author James Macgill
 */
public class LiteralTest extends TestCase {
    public LiteralTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(LiteralTest.class);

        return suite;
    }

    public void testValidConstruction() throws Exception {
        LiteralExpression a = new LiteralExpressionImpl(new Double(10));
        LiteralExpression b = new LiteralExpressionImpl("Label");
        LiteralExpression c = new LiteralExpressionImpl(new Integer(10));
        LiteralExpression d = new LiteralExpressionImpl(new GeometryCollection(
                    null, null, -1));
    }

    public void testInvalidConstruction1() throws Exception {
        try {
            //Byte was just a convinient object type to create
            LiteralExpression a = new LiteralExpressionImpl(new java.lang.Byte(
                        "1"));
        } catch (IllegalFilterException ife) {
            return;
        }

        fail("ExpressionLiterals can not contain non " +
            "Double, Integer, String or Geometry objects");
    }

    public void testInvalidConstruction2() throws Exception {
        try {
            LiteralExpression a = new LiteralExpressionImpl(new Double(10));
            LiteralExpression b = new LiteralExpressionImpl(a);
        } catch (IllegalFilterException ife) {
            return;
        }

        fail("ExpressionLiterals can not contain " + "other excepresions");
    }
}
