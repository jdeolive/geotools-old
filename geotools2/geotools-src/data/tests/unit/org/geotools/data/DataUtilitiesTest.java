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
package org.geotools.data;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.BetweenFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LikeFilter;
import org.geotools.filter.NullFilter;


/**
 * Tests cases for DataUtilities.
 *
 * @author Jody Garnett, Refractions Research
 */
public class DataUtilitiesTest extends DataTestCase {
    /**
     * Constructor for DataUtilitiesTest.
     *
     * @param arg0
     */
    public DataUtilitiesTest(String arg0) {
        super(arg0);
    }
    
    /*
     * Test for String[] attributeNames(FeatureType)
     */
    public void testAttributeNamesFeatureType() {
        String[] names;

        names = DataUtilities.attributeNames(roadType);
        assertEquals(3, names.length);
        assertEquals("id", names[0]);
        assertEquals("geom", names[1]);
        assertEquals("name", names[2]);

        names = DataUtilities.attributeNames(subRoadType);
        assertEquals(2, names.length);
        assertEquals("id", names[0]);
        assertEquals("geom", names[1]);
    }

    /*
     * Test for String[] attributeNames(Filter)
     */
    public void testAttributeNamesFilter() throws IllegalFilterException {
        FilterFactory factory = FilterFactory.createFilterFactory();
        String[] names;

        Filter filter = null;

        // check null
        names = DataUtilities.attributeNames(filter);
        assertNull(names);

        FidFilter fidFilter = factory.createFidFilter("fid");

        // check fidFilter         
        names = DataUtilities.attributeNames(fidFilter);
        assertEquals(0, names.length);

        AttributeExpression id = factory.createAttributeExpression(roadType, "id");
        AttributeExpression name = factory.createAttributeExpression(roadType,
                "name");
        AttributeExpression geom = factory.createAttributeExpression(roadType,
                "geom");

        NullFilter nullFilter = factory.createNullFilter();

        // check nullFilter
        nullFilter.nullCheckValue(id);
        names = DataUtilities.attributeNames(nullFilter);
        assertEquals(1, names.length);
        assertEquals("id", names[0]);

        CompareFilter equal = factory.createCompareFilter((short) 14);
        equal.addLeftValue(name);
        equal.addRightValue(id);
        names = DataUtilities.attributeNames(equal);
        assertEquals(2, names.length);
        assertEquals("name", names[0]);
        assertEquals("id", names[1]);

        FunctionExpression fnCall = factory.createFunctionExpression("Max");
        fnCall.setArgs(new Expression[] { id, name });

        LikeFilter fn = factory.createLikeFilter();
        fn.setValue(fnCall);
        names = DataUtilities.attributeNames(fn);
        assertEquals(2, names.length);
        assertEquals("name", names[0]);
        assertEquals("id", names[1]);

        BetweenFilter between = factory.createBetweenFilter();
        between.addLeftValue(id);
        between.addMiddleValue(name);
        between.addRightValue(geom);
        names = DataUtilities.attributeNames(between);
        assertEquals(3, names.length);
        assertEquals("geom", names[0]);
        assertEquals("name", names[1]);
        assertEquals("id", names[2]);
    }

    /*
     * Test for void traverse(Filter, FilterVisitor)
     */
    public void testTraverseFilterFilterVisitor() {
    }

    /*
     * Test for void traverse(Set, FilterVisitor)
     */
    public void testTraverseSetFilterVisitor() {
    }

    public void testTraverseDepth() {
    }

    public void testCompare() throws SchemaException {
        assertEquals(0, DataUtilities.compare(null, null));
        assertEquals(-1, DataUtilities.compare(roadType, null));
        assertEquals(-1, DataUtilities.compare(null, roadType));
        assertEquals(-1, DataUtilities.compare(riverType, roadType));
        assertEquals(-1, DataUtilities.compare(roadType, riverType));
        assertEquals(0, DataUtilities.compare(roadType, roadType));
        assertEquals(1, DataUtilities.compare(subRoadType, roadType));

        // different order
        FeatureType road2 = DataUtilities.createType("namespace.road",
                "geom:LineString,name:String,id:0");
        assertEquals(1, DataUtilities.compare(road2, roadType));

        // different namespace        
        FeatureType road3 = DataUtilities.createType("test.road",
                "id:0,geom:LineString,name:String");
        assertEquals(0, DataUtilities.compare(road3, roadType));
    }

    public void testIsMatch() {
    }

    public void testReType() throws Exception {
        Feature rd1 = roadFeatures[0];
        assertEquals(rd1, rd1);

        Feature rdDuplicate = roadType.duplicate(rd1);

        assertEquals(rd1, rdDuplicate);
        assertNotSame(rd1, rdDuplicate);

        Feature rd2 = DataUtilities.reType(roadType, rd1);

        assertEquals(rd1, rd2);
        assertNotSame(rd1, rd2);

        Feature rd3 = DataUtilities.reType(subRoadType, rd1);

        assertFalse(rd1.equals(rd3));
        assertEquals(2, rd3.getNumberOfAttributes());
        assertEquals(rd1.getID(), rd3.getID());
        assertEquals(rd1.getAttribute("id"), rd3.getAttribute("id"));
        assertEquals((Geometry) rd1.getAttribute("geom"),
            (Geometry) rd3.getAttribute("geom"));
        assertNotNull(rd3.getDefaultGeometry());

        Feature rv1 = riverFeatures[0];
        assertEquals(rv1, rv1);

        Feature rvDuplicate = riverType.duplicate(rv1);

        assertEquals(rv1, rvDuplicate);
        assertNotSame(rv1, rvDuplicate);

        Feature rv2 = DataUtilities.reType(riverType, rv1);

        assertEquals(rv1, rv2);
        assertNotSame(rv1, rv2);

        Feature rv3 = DataUtilities.reType(subRiverType, rv1);

        assertFalse(rv1.equals(rv3));
        assertEquals(2, rv3.getNumberOfAttributes());
        assertEquals(rv1.getID(), rv3.getID());
        assertEquals(rv1.getAttribute("name"), rv3.getAttribute("name"));
        assertEquals(rv1.getAttribute("flow"), rv3.getAttribute("flow"));
        assertNull(rv3.getDefaultGeometry());
    }

    /*
     * Test for Feature template(FeatureType)
     */
    public void testTemplateFeatureType() throws IllegalAttributeException {
        Feature feature = DataUtilities.template(roadType);
        assertNotNull(feature);
        assertEquals(roadType.getAttributeCount(), feature.getNumberOfAttributes());
    }

    /*
     * Test for Feature template(FeatureType, String)
     */
    public void testTemplateFeatureTypeString()
        throws IllegalAttributeException {
        Feature feature = DataUtilities.template(roadType, "Foo");
        assertNotNull(feature);
        assertEquals(roadType.getAttributeCount(), feature.getNumberOfAttributes());
        assertEquals("Foo", feature.getID());
        assertNull(feature.getAttribute("name"));
        assertNull(feature.getAttribute("id"));
        assertNull(feature.getAttribute("geom"));
    }

    public void testDefaultValues() throws IllegalAttributeException {
        Object[] values = DataUtilities.defaultValues(roadType);
        assertNotNull(values);
        assertEquals(values.length, roadType.getAttributeCount());
    }

    public void testDefaultValue() throws IllegalAttributeException {
        assertNull(DataUtilities.defaultValue(roadType.getAttributeType("name")));
        assertNull(DataUtilities.defaultValue(roadType.getAttributeType("id")));
        assertNull(DataUtilities.defaultValue(roadType.getAttributeType("geom")));
    }

    

    public void testCollection() {
        FeatureCollection collection = DataUtilities.collection( roadFeatures );
        assertEquals( roadFeatures.length,  collection.size() );                
    }

    public void testReaderFeatureArray() throws Exception {
        FeatureReader reader = DataUtilities.reader( roadFeatures );
        assertEquals( roadFeatures.length,  count( reader ) );
    }
    public void testReaderCollection() throws Exception {
        FeatureCollection collection = DataUtilities.collection( roadFeatures );
        assertEquals( roadFeatures.length,  collection.size() );
                
        FeatureReader reader = DataUtilities.reader( collection );
        assertEquals( roadFeatures.length,  count( reader ) );
    }    
    public void testCreateType() {
        //      TODO impelment test
    }

    public void testType() {
        //      TODO impelment test
    }

    public void testCreateAttribute() {
        //      TODO impelment test
    }

    
}
