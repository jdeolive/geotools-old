/*
 * FeatureTableModelTest.java
 * JUnit based test
 *
 * Created on March 18, 2002, 4:24 PM
 */

package org.geotools.gui.swing.tables;

import junit.framework.*;
import org.geotools.datasource.extents.*;
import org.geotools.data.*;
import org.geotools.feature.*;
import com.vividsolutions.jts.geom.*;



import java.io.*;
import java.net.*;

import javax.swing.*;

/**
 *
 * @author jamesm
 */
public class FeatureTableModelTest extends TestCase {
    /** Standard logging instance */
    //private static Logger _log = Logger.getLogger(FeatureTableModelTest.class);
    
    /** Feature on which to preform tests */
    private static Feature testFeatures[] = null;
    
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
        
        FeatureCollectionDefault table = new FeatureCollectionDefault();
        table.setDataSource(datasource);
        EnvelopeExtent r = new EnvelopeExtent();
        r.setBounds(new Envelope(-180, 180, -90, 90));
        table.getFeatures(r);
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
     * @throws IllegalFeatureException If problem setting up the feature.
     */
    protected void setUp()
    throws SchemaException, IllegalFeatureException {
        
        try{
        /** Schema on which to preform tests */
        FeatureType testSchema = null;
        
        // Create the schema attributes
        //_log.debug("creating flat feature...");
        AttributeType geometryAttribute =
        new AttributeTypeDefault("testGeometry", Geometry.class);
        //_log.debug("created geometry attribute");
        AttributeType booleanAttribute =
        new AttributeTypeDefault("testBoolean", Boolean.class);
        //_log.debug("created boolean attribute");
        AttributeType charAttribute =
        new AttributeTypeDefault("testCharacter", Character.class);
        AttributeType byteAttribute =
        new AttributeTypeDefault("testByte", Byte.class);
        AttributeType shortAttribute =
        new AttributeTypeDefault("testShort", Short.class);
        AttributeType intAttribute =
        new AttributeTypeDefault("testInteger", Integer.class);
        AttributeType longAttribute =
        new AttributeTypeDefault("testLong", Long.class);
        AttributeType floatAttribute =
        new AttributeTypeDefault("testFloat", Float.class);
        AttributeType doubleAttribute =
        new AttributeTypeDefault("testDouble", Double.class);
        AttributeType stringAttribute =
        new AttributeTypeDefault("testString", String.class);
        
        // Builds the schema
        testSchema = new FeatureTypeFlat(geometryAttribute);
        testSchema = testSchema.setAttributeType(booleanAttribute);
        testSchema = testSchema.setAttributeType(charAttribute);
        testSchema = testSchema.setAttributeType(byteAttribute);
        testSchema = testSchema.setAttributeType(shortAttribute);
        testSchema = testSchema.setAttributeType(intAttribute);
        testSchema = testSchema.setAttributeType(longAttribute);
        testSchema = testSchema.setAttributeType(floatAttribute);
        testSchema = testSchema.setAttributeType(doubleAttribute);
        testSchema = testSchema.setAttributeType(stringAttribute);
        
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
        FeatureFactory factory = new FeatureFactory(testSchema);
        testFeatures = new Feature[2];
        testFeatures[0] = factory.create(attributesA);
        testFeatures[1] = factory.create(attributesB);
        //_log.debug("...flat features created");
        }
        catch(TopologyException te){
    //        _log.error("Unable to construct test geometies",te);
            throw new IllegalFeatureException(te.toString());
        }
    }
    
    
    
}
