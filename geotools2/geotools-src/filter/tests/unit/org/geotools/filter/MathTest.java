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
 * MathTest.java
 * JUnit based test
 *
 * Created on 20 June 2002, 18:53
 */
package org.geotools.filter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import junit.framework.*;
import org.geotools.feature.*;


/**
 * TODO: this is just a copy of other tests, it is not yet populated with Math
 * tests!
 *
 * @author James
 */
public class MathTest extends TestCase {
    FeatureType schema = null;

    public MathTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(BetweenTest.class);

        return suite;
    }

    public Feature[] sampleFeatures() throws Exception {
        AttributeTypeFactory attFactory = AttributeTypeFactory.newInstance();

        AttributeType a1 = attFactory.newAttributeType("value", Integer.class);
        AttributeType a2 = attFactory.newAttributeType("geometry",
                Geometry.class);
        AttributeType a3 = attFactory.newAttributeType("name", String.class);
        schema = FeatureTypeFactory.newFeatureType(new AttributeType[] {
                    a1, a2, a3
                },"testSchema");

        Feature[] f = new Feature[3];
        f[0] = schema.create(new Object[] {
                    new Integer(12), new GeometryCollection(null, null, -1),
                    "first"
                });
        f[1] = schema.create(new Object[] {
                    new Integer(3), new GeometryCollection(null, null, -1),
                    "second"
                });
        f[2] = schema.create(new Object[] {
                    new Integer(15), new GeometryCollection(null, null, -1),
                    "third"
                });

        return f;
    }

    public void testTypeMissmatch() throws Exception {
        Feature[] f = sampleFeatures();

        //the following are intentionaly backwards
        AttributeExpressionImpl e1 = new AttributeExpressionImpl(schema, "value");
        AttributeExpressionImpl e2 = new AttributeExpressionImpl(schema, "name");
        boolean pass = false;
        Object value = null;
        value = e1.getValue(f[0]);

        if (value instanceof Integer) {
            pass = true;
        }

        assertTrue("String expresion returned an Integer", pass);
        pass = false;

        value = e2.getValue(f[0]);

        if (value instanceof String) {
            pass = true;
        }

        assertTrue("Integer expresion returned a String", pass);
    }

    public void testSetupAndExtraction() throws Exception {
        //this should move out to a more configurable system run from scripts
        //but we can start with a set of hard coded tests
        Feature[] f = sampleFeatures();

        AttributeExpressionImpl e1 = new AttributeExpressionImpl(schema, "value");
        AttributeExpressionImpl e2 = new AttributeExpressionImpl(schema, "name");

        assertEquals(12d, ((Integer) e1.getValue(f[0])).doubleValue(), 0);
        assertEquals(3d, ((Integer) e1.getValue(f[1])).doubleValue(), 0);
        assertEquals(15d, ((Integer) e1.getValue(f[2])).doubleValue(), 0);
        assertEquals("first", (String) e2.getValue(f[0]));
        assertEquals("second", (String) e2.getValue(f[1]));
    }
}
