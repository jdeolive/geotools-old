package org.geotools.data.mysql;

import junit.framework.*;
import org.apache.log4j.Category;
import org.apache.log4j.BasicConfigurator;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.geom.*;
import java.util.*;
import org.geotools.data.*;
import org.geotools.feature.*;
import org.geotools.datasource.extents.*;
import java.sql.*;
import org.geotools.filter.*;
//import org.geotools.data.mysql;

public class MysqlTestSuite extends TestCase {

    /** Standard logging instance */
    private static Category _log = Category.getInstance(MysqlTestSuite.class.getName());

    private static String FEATURE_TABLE = "STREET_LAMP";

    private static String GEOM_TABLE = "STREET_LAMP_LOC";

    private static int NUM_TEST_BULBS = 4;

    /** Well Known Text writer (from JTS). */
    private static WKTWriter geometryWriter = new WKTWriter();

    private AttributeType[] lampAttr= { new AttributeTypeDefault("NUM_BULBS", Integer.class),
					new AttributeTypeDefault("LOCATION", Geometry.class)
					    };

    private MysqlConnection db;

    private DataSource mysql = null;

    private FeatureCollection collection = new FeatureCollectionDefault();

    private CompareFilter tFilter;
    
    public MysqlTestSuite(String testName){
        super(testName);
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        BasicConfigurator.configure();
        _log.info("starting suite...");
        TestSuite suite = new TestSuite(MysqlTestSuite.class);
	suite.addTest(new TestSuite(MysqlConTestSuite.class));
	suite.addTest(new TestSuite(MysqlGeomColTestSuite.class));
        _log.info("made suite...");
        return suite;
    }
    
    public void setUp() {
	_log.info("creating MysqlConnection connection...");
	db = new MysqlConnection ("localhost","3306","test_Feature"); 
	_log.info("created new db connection");
	mysql = new MysqlDataSource(db, FEATURE_TABLE);
	_log.info("created new datasource");
	lampAttr[0].setPosition(0);
	lampAttr[0].setPosition(1);
	//create a filter that is always true, just to pass into getFeatures
	try {
	    tFilter = new CompareFilter(AbstractFilter.COMPARE_EQUALS);
	    Integer testInt = new Integer(5);
	    Expression testLiteral = new ExpressionLiteral(testInt);
	    tFilter.addLeftValue(testLiteral);
	    tFilter.addRightValue(testLiteral);
	} catch (IllegalFilterException e) {
	    fail("Illegal Filter Exception " + e);
	}
    }
    	
     public void testGet() {
        _log.info("starting type enforcement tests...");
        try {
	       	    mysql.getFeatures(collection, tFilter);
	     assertEquals(4, collection.getFeatures().length);
	} catch(DataSourceException e) {
            _log.info("...threw data source exception: " + e.getMessage());    
	    fail();
        }
        _log.info("...ending type enforcement tests");
	}
    
    public void testAdd() {
	int feaID = 8;
	int numBulbs = 4;
	int geomID;
	AttributeType[] lampAttr= { new AttributeTypeDefault("NUM_BULBS", Integer.class),
				    new AttributeTypeDefault("LOCATION", Geometry.class)
					};
	Feature[] features = new Feature[1];
	Geometry geom = new Point(new Coordinate(6, 10), new PrecisionModel(), 1);
	Object[] attributes = { new Integer(numBulbs), geom};
	try{
	    FeatureFactory factory = new FeatureFactory(new FeatureTypeFlat(lampAttr));
	    features[0] = factory.create(attributes, String.valueOf(feaID));
	    collection = new FeatureCollectionDefault();
	    collection.addFeatures(features);
	    mysql.addFeatures(collection);
	} catch(DataSourceException e){
	    _log.info("threw data source exception");
	    fail();
	} catch(SchemaException e){
	    fail();
	    _log.info("trouble creating feature type");
	} catch(IllegalFeatureException e){
	    fail("illegal feature " + e);
	}

	//clean up...basically a delete, but without using a delete.
	try{
	    Connection dbConnection = db.getConnection();
	    Statement statement = dbConnection.createStatement();
	    ResultSet result = statement.executeQuery("SELECT * FROM " + FEATURE_TABLE + " WHERE ID = " + feaID);
	    result.next();
	    assertEquals(result.getInt(1), feaID);
	    assertEquals(result.getInt(2), numBulbs);
	    geomID = result.getInt(3);
	    statement.executeUpdate("DELETE FROM " + FEATURE_TABLE + " WHERE ID = " + feaID);
	    result = statement.executeQuery("SELECT * FROM " + GEOM_TABLE + " WHERE GID = " + geomID);
	    result.next();
	    assertTrue(result.getString(6).equals(geometryWriter.write(geom)));
	    statement.executeUpdate("DELETE FROM " + GEOM_TABLE + " WHERE GID = " + geomID);
	    result.close();
	    statement.close();
	    dbConnection.close();
	} catch(SQLException e){
	   
	    _log.info("we had some sql trouble " + e.getMessage());
	    fail();
	}
    
    }

    public void testRemove() {
	try {
	    FeatureCollection delFeatures = mysql.getFeatures(tFilter);
	    FeatureType schema = delFeatures.getFeatures()[0].getSchema();
	    mysql.removeFeatures(tFilter);
	    collection = mysql.getFeatures(tFilter);
	    assertEquals(0, collection.getFeatures().length);
	    mysql.addFeatures(delFeatures);
	    collection = mysql.getFeatures(tFilter);
	    assertEquals(4, collection.getFeatures().length);
	} catch (DataSourceException e){
	    fail("Data source exception " + e);
	}
    }
    
    public void testModify() {
	Integer tBulbs = new Integer(NUM_TEST_BULBS);
	Integer restBulbs = new Integer(NUM_TEST_BULBS - 2);
	Geometry geom = new Point(new Coordinate(6, 10), new PrecisionModel(), 1);
	try {
	    mysql.modifyFeatures(lampAttr[0], tBulbs, tFilter);
	    //mysql.modifyFeatures(lampAttr[1], geom, tFilter);
	    //do a geom test when we figure out how to get the filters to work
	    collection = mysql.getFeatures(tFilter);
	    assertEquals(tBulbs, 
			 (Integer) collection.getFeatures()[0].getAttribute("NUM_BULBS"));
	    mysql.modifyFeatures(lampAttr[0], restBulbs, tFilter);
	    collection = mysql.getFeatures(tFilter);
	    assertEquals(restBulbs, 
	    	 (Integer) collection.getFeatures()[0].getAttribute("NUM_BULBS"));
	} catch (DataSourceException e) {
	    fail("Data source Exception " + e);
	} catch (IllegalFeatureException e) {
	    fail("Illegal feature ex: " + e);
	}
    }
    
}

