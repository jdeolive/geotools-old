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
package org.geotools.feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;


/**
 * This is a support class which creates test features for use in testing.
 *
 * @author jamesm
 */
public class SampleFeatureFixtures {
    /**
     * Feature on which to preform tests
     */

    // private Feature testFeature = null;

    /**
     * Creates a new instance of SampleFeatureFixtures
     */
    public SampleFeatureFixtures() {
    }

    public static Feature createFeature() {
        try {
            FeatureType testType = createTestType();
            Object[] attributes = createAttributes();
            return testType.create(attributes);
        } catch (Exception e) {
            Error ae = new AssertionError(
                    "Sample Feature for tests has been misscoded");
            ae.initCause(e);
            throw ae;
        }
    }

    /**
     * creates and returns an array of sample attributes.
     *
     * @return
     */
    public static Object[] createAttributes() {
        Object[] attributes = new Object[10];
        attributes[0] = new Point(new Coordinate(1, 2), new PrecisionModel(), 1);
        attributes[1] = new Boolean(true);
        attributes[2] = new Character('t');
        attributes[3] = new Byte("10");
        attributes[4] = new Short("101");
        attributes[5] = new Integer(1002);
        attributes[6] = new Long(10003);
        attributes[7] = new Float(10000.4);
        attributes[8] = new Double(100000.5);
        attributes[9] = "test string data";

        return attributes;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     *
     * @throws SchemaException
     */
    public static FeatureType createTestType() throws SchemaException {
      FeatureTypeFactory typeFactory = FeatureTypeFactory.newInstance("test");
      typeFactory.addType(AttributeTypeFactory.newAttributeType("testGeometry",
                Point.class));

      typeFactory.addType(AttributeTypeFactory.newAttributeType("testBoolean",
                Boolean.class));

      typeFactory.addType(AttributeTypeFactory.newAttributeType("testCharacter",
                Character.class));
      typeFactory.addType(AttributeTypeFactory.newAttributeType("testByte",
                Byte.class));
      typeFactory.addType(AttributeTypeFactory.newAttributeType("testShort",
                Short.class));
      typeFactory.addType(AttributeTypeFactory.newAttributeType("testInteger",
                Integer.class));
      typeFactory.addType(AttributeTypeFactory.newAttributeType("testLong",
                Long.class));
      typeFactory.addType(AttributeTypeFactory.newAttributeType("testFloat",
                Float.class));
      typeFactory.addType(AttributeTypeFactory.newAttributeType("testDouble",
                Double.class));
      typeFactory.addType(AttributeTypeFactory.newAttributeType("testString",
                String.class));
      typeFactory.setDefaultGeometry((GeometryAttributeType) typeFactory.get(0));
      return typeFactory.getFeatureType();
    }
    
    
}
