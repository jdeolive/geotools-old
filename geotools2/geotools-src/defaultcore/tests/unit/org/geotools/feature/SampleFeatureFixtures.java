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
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeDefault;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFlat;


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

            return new FeatureFlat((FeatureTypeFlat) testType, attributes);
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
    private static Object[] createAttributes() {
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
    private static FeatureType createTestType() throws SchemaException {
        AttributeType geometryAttribute = new AttributeTypeDefault("testGeometry",
                Point.class);

        AttributeType booleanAttribute = new AttributeTypeDefault("testBoolean",
                Boolean.class);

        AttributeType charAttribute = new AttributeTypeDefault("testCharacter",
                Character.class);
        AttributeType byteAttribute = new AttributeTypeDefault("testByte",
                Byte.class);
        AttributeType shortAttribute = new AttributeTypeDefault("testShort",
                Short.class);
        AttributeType intAttribute = new AttributeTypeDefault("testInteger",
                Integer.class);
        AttributeType longAttribute = new AttributeTypeDefault("testLong",
                Long.class);
        AttributeType floatAttribute = new AttributeTypeDefault("testFloat",
                Float.class);
        AttributeType doubleAttribute = new AttributeTypeDefault("testDouble",
                Double.class);
        AttributeType stringAttribute = new AttributeTypeDefault("testString",
                String.class);

        FeatureType testType = new FeatureTypeFlat(geometryAttribute);
        testType = testType.setAttributeType(booleanAttribute);
        testType = testType.setAttributeType(charAttribute);
        testType = testType.setAttributeType(byteAttribute);
        testType = testType.setAttributeType(shortAttribute);
        testType = testType.setAttributeType(intAttribute);
        testType = testType.setAttributeType(longAttribute);
        testType = testType.setAttributeType(floatAttribute);
        testType = testType.setAttributeType(doubleAttribute);
        testType = testType.setAttributeType(stringAttribute);

        return testType;
    }
}
