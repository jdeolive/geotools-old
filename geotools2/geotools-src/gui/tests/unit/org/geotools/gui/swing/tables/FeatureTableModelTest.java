/*
 * FeatureTableModelTest.java
 * JUnit based test
 *
 * Created on March 18, 2002, 4:24 PM
 */

package org.geotools.gui.swing.tables;

import java.io.*;
import java.util.logging.Logger;
import java.net.*;
import javax.swing.*;
import junit.framework.*;
import org.geotools.data.*;
import org.geotools.feature.*;
import com.vividsolutions.jts.geom.*;


/**
 *
 * @author jamesm
 */
public class FeatureTableModelTest extends TestCase {
      /** Standard logging instance */
    protected static final Logger LOGGER = Logger.getLogger(
            "org.geotools.filter");
    protected static AttributeTypeFactory attFactory = AttributeTypeFactory.newInstance();
    
    /** Feature on which to preform tests */
    private static Feature testFeatures[] = null;
    
    private static FeatureType testSchema;

    public FeatureTableModelTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(FeatureTableModelTest.class);
        return suite;
    }
    
    public void testDisplay() throws Exception{
        MemoryDataSource datasource = new MemoryDataSource();
        datasource.addFeature(testFeatures[0]);
        datasource.addFeature(testFeatures[1]);
        
        FeatureCollection table = datasource.getFeatures();
        FeatureTableModel ftm = new FeatureTableModel();
        ftm.setFeatureCollection(table);
        
        JFrame frame = new JFrame();
        frame.setSize(400,400);
        JTable jtable = new JTable();
        jtable.setModel(ftm);
        JScrollPane scroll = new JScrollPane(jtable);
        frame.getContentPane().add(scroll,"Center");
        frame.setVisible(true);
        Thread.sleep(1000);
        frame.dispose();
    }
    
    /**
     * Sets up a schema and a test feature.
     * @throws SchemaException If there is a problem setting up the schema.
     * @throws IllegalAttributeException If problem setting up the feature.
     */
    protected void setUp()
    throws SchemaException, IllegalAttributeException {
	try {
         AttributeType geometryAttribute = attFactory.newAttributeType("testGeometry",
                Geometry.class);
        LOGGER.finest("created geometry attribute");

        AttributeType booleanAttribute = attFactory.newAttributeType("testBoolean",
                Boolean.class);
        LOGGER.finest("created boolean attribute");

        AttributeType charAttribute = attFactory.newAttributeType("testCharacter",
                Character.class);
        AttributeType byteAttribute = attFactory.newAttributeType("testByte",
                Byte.class);
        AttributeType shortAttribute = attFactory.newAttributeType("testShort",
                Short.class);
        AttributeType intAttribute = attFactory.newAttributeType("testInteger",
                Integer.class);
        AttributeType longAttribute = attFactory.newAttributeType("testLong",
                Long.class);
        AttributeType floatAttribute = attFactory.newAttributeType("testFloat",
                Float.class);
        AttributeType doubleAttribute = attFactory.newAttributeType("testDouble",
                Double.class);
        AttributeType stringAttribute = attFactory.newAttributeType("testString",
                String.class);

        AttributeType[] types = {
            geometryAttribute, booleanAttribute, charAttribute, byteAttribute,
            shortAttribute, intAttribute, longAttribute, floatAttribute,
            doubleAttribute, stringAttribute
        };

        // Builds the schema
        testSchema = FeatureTypeFactory.newFeatureType(types,"testSchema");
        
        // Creates coordinates for a linestring
        Coordinate[] lineCoords = new Coordinate[3];
        lineCoords[0] = new Coordinate(1,2);
        lineCoords[1] = new Coordinate(3,4);
        lineCoords[2] = new Coordinate(5,6);
        
        // Creates coordinates for a polygon
        Coordinate[] polyCoords = new Coordinate[5];
        polyCoords[0] = new Coordinate(1,1);
        polyCoords[1] = new Coordinate(2,4);
        polyCoords[2] = new Coordinate(4,4);
        polyCoords[3] = new Coordinate(8,2);
        polyCoords[4] = new Coordinate(1,1);
        
        GeometryFactory fac = new GeometryFactory();
        
        // Builds the test feature
        Object[] attributesA = new Object[10];
        attributesA[0] = fac.createLineString(lineCoords);
        attributesA[1] = new Boolean(true);
        attributesA[2] = new Character('t');
        attributesA[3] = new Byte("10");
        attributesA[4] = new Short("101");
        attributesA[5] = new Integer(1002);
        attributesA[6] = new Long(10003);
        attributesA[7] = new Float(10000.4);
        attributesA[8] = new Double(100000.5);
        attributesA[9] = "feature A";
        
        Object[] attributesB = new Object[10];
        LinearRing ring = fac.createLinearRing(polyCoords);
        attributesB[0] = fac.createPolygon(ring,null);
        attributesB[1] = new Boolean(false);
        attributesB[2] = new Character('t');
        attributesB[3] = new Byte("20");
        attributesB[4] = new Short("201");
        attributesB[5] = new Integer(2002);
        attributesB[6] = new Long(20003);
        attributesB[7] = new Float(20000.4);
        attributesB[8] = new Double(200000.5);
        attributesB[9] = "feature B";
        
        // Creates the feature itself
        testFeatures = new Feature[2];
        testFeatures[0] = testSchema.create(attributesA);
        testFeatures[1] = testSchema.create(attributesB);
        //_log.debug("...flat features created");
        }
        catch(TopologyException te){
    //        _log.error("Unable to construct test geometies",te);
            throw new IllegalAttributeException(te.toString());
        }
    }
    
    
    
}
