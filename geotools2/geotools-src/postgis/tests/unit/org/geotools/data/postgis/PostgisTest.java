package org.geotools.data.postgis;

import junit.framework.*;
import com.vividsolutions.jts.geom.*;
import java.util.*;
import org.geotools.data.*;
import org.geotools.feature.*;
import org.geotools.filter.*;
import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.geotools.resources.Geotools;
import org.geotools.filter.FilterFactory;

public class PostgisTest extends TestCase {
    FilterFactory filterFac = FilterFactory.createFilterFactory();
    /**
     * The logger for the filter module.
     */
    private static final Logger LOGGER = Logger.getLogger
	("org.geotools.postgis");
    
    static {
    Geotools.init(Level.FINE);
    }

    private static String FEATURE_TABLE = "testset";

    DataSource postgis = null;
    
    FeatureCollection collection = FeatureCollections.newCollection();
    
    FeatureType schema;

    int srid = -1;

    PostgisConnectionFactory db;

    Connection connection;

    CompareFilter tFilter;

    int addId = 32;

    org.geotools.filter.GeometryFilter geomFilter;

    public PostgisTest(String testName){
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        LOGGER.info("starting suite...");
        TestSuite suite = new TestSuite(PostgisTest.class);
        LOGGER.info("made suite...");
        return suite;
    }
    
    public void setUp() {
        LOGGER.info("creating postgis connection...");
	db = new PostgisConnectionFactory("feathers.leeds.ac.uk","5432",
				   "postgis_test");
        LOGGER.info("created new db connection");
        db.setLogin("postgis_ro", "postgis_ro");
        LOGGER.info("set the login");

        LOGGER.info("created new datasource");

	try {
	    tFilter = 
		filterFac.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
	    Integer testInt = new Integer(5);
	    Expression testLiteral = 
		filterFac.createLiteralExpression(testInt);
	    tFilter.addLeftValue(testLiteral);
	    tFilter.addRightValue(testLiteral);
	} catch (IllegalFilterException e) {
	    fail("Illegal Filter Exception " + e);
	}
	try {
	    connection = db.getConnection();
	    postgis = new PostgisDataSource(connection, FEATURE_TABLE);
	    schema = ((PostgisDataSource)postgis).getSchema();
	} catch (Exception e) {
	    LOGGER.info("exception while making schema" + e.getMessage());
	}
    }
    
     public void tearDown() throws SQLException{
	connection.close();
    }

    public void testImport() {
        LOGGER.info("starting type enforcement tests...");
        try {
	postgis.getFeatures(collection,tFilter);
	LOGGER.info("there are " + collection.size() + " feats");
	assertEquals(6,collection.size());
	org.geotools.filter.GeometryFilter gf =
	    filterFac.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
	LiteralExpression right =
            filterFac.createBBoxExpression
	    (new Envelope(428500,430000,428500,440000));
	gf.addRightGeometry(right);
	gf.addLeftGeometry(filterFac.createAttributeExpression
			   (schema, "the_geom"));
	FeatureCollection geomCollection = postgis.getFeatures(gf);
	LOGGER.info("we have this number of features: " 
		    + collection.size());
	LOGGER.info("we have this number of filtered features: " 
		    + geomCollection.size());
	assertEquals(2, geomCollection.size());

        }
        catch(DataSourceException dse) {
            LOGGER.info("...threw data source exception" + dse);
            this.fail("...threw data source exception");
        }
        catch(IllegalFilterException fe) {
            LOGGER.info("...threw filter exception" + fe);
            this.fail("...threw filter exception");
        }
        
        LOGGER.info("...ending type enforcement tests");
	}

    public void testProperties() throws Exception{
	DefaultQuery query = new DefaultQuery();
	String[] attributes = { "gid", "name"};
	//new AttributeTypeDefault("gid", Integer.class), 
	//  new AttributeTypeDefault("name", String.class)};
	query.setPropertyNames(attributes);
	//FeatureType small = FeatureTypeFactory.create(attributes);
	postgis = new PostgisDataSource(connection, FEATURE_TABLE);
        collection = postgis.getFeatures(tFilter);
	Feature feature = (Feature)collection.iterator().next();
	LOGGER.fine("feature is " + feature + ", and feature type is " +
		     feature.getFeatureType());
		     }

    public void testMaxFeatures(){ 
	try { 
 	    postgis = 
	    new PostgisDataSource(connection, FEATURE_TABLE);
	    schema = ((PostgisDataSource)postgis).getSchema();

	    //collection = FeatureCollections.newCollection(); 
	DefaultQuery query = new DefaultQuery();
	query.setMaxFeatures(4);
	collection = postgis.getFeatures(query);
	LOGGER.info("we have this number of filtered features: " 
		    + collection.size());
	assertEquals(4,collection.size());
	LikeFilter likeFilter = filterFac.createLikeFilter();
	likeFilter.setValue
	    (filterFac.createAttributeExpression(schema, "name"));    	
	likeFilter.setPattern
	    (filterFac.createLiteralExpression("*7*"),"*",".","!");
	
	DefaultQuery q2 = new DefaultQuery();
	q2.setMaxFeatures(3);
	collection = postgis.getFeatures(q2);
	LOGGER.info("there are " + collection.size() + " feats");
	assertEquals(3,collection.size());
	}
        catch(DataSourceException dse) {
            LOGGER.info("...threw data source exception" + dse);
            this.fail("...threw data source exception");
        }
        catch(IllegalFilterException fe) {
            LOGGER.info("...threw filter exception" + fe);
            this.fail("...threw filter exception");
        } 

 	}
    
    public void testAdd() throws Exception{
        postgis.setAutoCommit(false);
	String name = "test_add";
	addFeature(name);
	//clean up...basically a delete, but without using a remove features.
	try{
	    //Connection dbConnection = db.getConnection();
	    Statement statement = connection.createStatement();
	    ResultSet result = statement.executeQuery
		("SELECT * FROM " + FEATURE_TABLE + " WHERE gid = " + addId);
	    result.next();
	    assertEquals(result.getInt("gid"), addId);
	    //assertTrue(result.getDouble("area") == area.doubleValue());
	    assertTrue(result.getString("name").equals(name));
	     statement.executeUpdate("DELETE FROM " + FEATURE_TABLE + 
				     " WHERE gid = " + addId);

	    result.close();
	    statement.close();
	    //dbConnection.close();
	} catch(SQLException e){
	    LOGGER.info("we had some sql trouble " + e.getMessage());
	    fail();
	}
    
    }
 
        public void testRemove() {
	    LOGGER.info("starting type enforcement tests...");
	    try {
	    postgis.setAutoCommit(false);
            org.geotools.filter.GeometryFilter gf =
            filterFac.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
            LiteralExpression right =
		//new BBoxExpression(new Envelope(235,305,235,305));		
	       filterFac.createBBoxExpression
		(new Envelope(429500,430000,429000,440000));
            gf.addRightGeometry(right);
            gf.addLeftGeometry(filterFac.createAttributeExpression
			       (schema, "the_geom"));
	    doRemoveTest(gf, 2);

		LikeFilter likeFilter = filterFac.createLikeFilter();
		likeFilter.setValue
		    (filterFac.createAttributeExpression(schema, "name"));        
		likeFilter.setPattern
		    (filterFac.createLiteralExpression("*8*"),"*",".","!");
		doRemoveTest(likeFilter, 3);

	Filter andFilter = likeFilter.and(gf);
	doRemoveTest(andFilter, 1);
	 

	//TODO: Weird bug, I don't have time to figure out now.  It wiped
	//out half the database, something to do with differences between
	//how getFeatures does filtering and how deleteFeatures does filtering
	//may be even at the level of the filter code, am not sure yet.  But th
	//database that was wiped out needs to be restored to test this again, 
	//figure out what exactly is going on.  CH

	//	Filter orFilter = likeFilter.or(gf); TODO
	//doRemoveTest(orFilter, 5);

	//	Filter bigAnd = andFilter.and(orFilter);
	//doRemoveTest(bigAnd, 1);
   
	}
        catch(DataSourceException dse) {
            LOGGER.info("...threw data source exception " + dse);
            this.fail("...threw data source exception");
        }
        catch(IllegalFilterException fe) {
            LOGGER.info("...threw filter exception " + fe);
            this.fail("...threw filter exception");
        }
        //assertEquals(2,collection.size());
        LOGGER.info("...ending type enforcement tests");
 	    
	}
/*

    //this needs to be updated to work with the feathers.leeds.ac.uk database.  But this
    //should give an idea of how to test the modify features.  It works on my local db.  CH
    public void testModify() {
	try {
	    collection = FeatureCollections.newCollection();

	    org.geotools.filter.GeometryFilter gf =
	    new org.geotools.filter.GeometryFilter(AbstractFilter.GEOMETRY_BBOX);
	    ExpressionLiteral right =
		new BBoxExpression(new Envelope(235,305,235,305));
	    gf.addRightGeometry(right);
	    gf.addLeftGeometry(new ExpressionAttribute(schema, "geom"));
	    doModifyTest("name", "modified", gf);
	    
        LikeFilter likeFilter = new LikeFilter();
        likeFilter.setValue(new ExpressionAttribute(schema, "name"));        
	likeFilter.setPattern(new ExpressionLiteral("*not*"),"*",".","!");
	doModifyTest("name", "not me too!", likeFilter); 


	    Coordinate[] points = { new Coordinate(85, 85),
				new Coordinate(85, 95),
				new Coordinate(95, 95),
				new Coordinate(95, 85),
				 new Coordinate(85, 85) };
	LinearRing shell = new LinearRing(points, new PrecisionModel(), srid);
	Polygon testPoly = new Polygon(shell, new PrecisionModel(), srid);
	//CompareFilter compFilter = new CompareFilter(AbstractFilter.COMPARE_EQUALS);
	//compFilter.addLeftValue(new ExpressionAttribute(schema, "gid"));
	//compFilter.addRightValue(new ExpressionLiteral(new Integer(5)));
	//doModifyTest("geom", testPoly, compFilter);
		      

	} catch(IllegalFilterException fe) {
	    LOGGER.info("...threw filter exception" + fe.getMessage());
	    this.fail("...threw filter exception");	  
	}
	    
	
    }
*/   
      private void doRemoveTest(Filter filter, int expectedDel) 
	throws DataSourceException{
	//TODO: implement tests that don't use get and add.
	FeatureCollection allFeatures = postgis.getFeatures(tFilter); 
	     //tFilter is always true, selects all.
	int totNumFeatures = allFeatures.size();
	FeatureCollection delFeatures = postgis.getFeatures(filter); 
	       //so we can replace the features
	int numDelFeatures = delFeatures.size();
	postgis.removeFeatures(filter);
	FeatureCollection collection = postgis.getFeatures(tFilter); 
	int numRemainingFeatures = collection.size();
	LOGGER.fine(expectedDel + " total features = " + totNumFeatures + 
		    " and remaining feat " + numRemainingFeatures + 
		    " and num deleted (from filt) = " + numDelFeatures);
	//assertEquals(totNumFeatures - numRemainingFeatures, expectedDel); 
	//make sure proper number deleted.
	postgis.addFeatures(delFeatures); //put them back in.
	collection = postgis.getFeatures(tFilter); //get all again.
	//assertEquals(totNumFeatures, collection.size()); 
	//to be sure they aere all added back.
	//yes this tests add more than delete, but it's important 
	//to know the test put things back.
    }
    	   
    /*
    private void doModifyTest(String attributeName, Object newValue, 
			      Filter filter) {
	try {
	    collection = FeatureCollections.newCollection();
	    postgis.getFeatures(collection, filter);
	    Object unModified = 
		collection.getFeatures()[0].getAttribute(attributeName);
	    postgis.modifyFeatures(schema.getAttributeType(attributeName), 
				   newValue, filter);
	    collection = FeatureCollections.newCollection();
	    postgis.getFeatures(collection, filter);
	    Feature[] featureArr = collection.getFeatures();
	    postgis.modifyFeatures(schema.getAttributeType(attributeName), 
				   unModified, filter);
	    //yes, this sets all the values back the value of the 
	    //first one, but it's only a test.
	    for (int i = 0; i < featureArr.length; i++) {
		Object modified = featureArr[i].getAttribute(attributeName);
		assertTrue(newValue.equals(modified)); 
	    }
	} catch(DataSourceException dse) {
	    LOGGER.info("...threw data source exception "+ dse);
	    this.fail("...threw data source exception");
	}    catch(SchemaException se) {
	    LOGGER.info("...threw schema exception " + se);
	    this.fail("...threw schema exception");
	}   catch(IllegalFeatureException fe) {
	    LOGGER.info("...threw feature exception" + fe);
	    this.fail("...threw feature exception");
	} 

	}*/

    private void addFeature(String name) throws Exception{
	Coordinate[] points = { new Coordinate(45, 45),
				new Coordinate(45, 55),
				new Coordinate(55, 55),
				new Coordinate(55, 45),
				 new Coordinate(45, 45) };
	PrecisionModel precModel = new PrecisionModel();
	LinearRing shell = new LinearRing(points, precModel, srid);
	Polygon[] testPolys = {new Polygon(shell, precModel, srid)};
	MultiPolygon the_geom = new MultiPolygon(testPolys, precModel, srid);
	Integer feaID = new Integer(addId);
	Double area = new Double(100.0);
	Double perimeter = new Double(40.0);
	Integer testb_ = new Integer(22);
	Integer testb_id = new Integer(4833);
	Integer code = new Integer(0);

	Object[] attributes = { feaID, area, perimeter, testb_, 
				testb_id, name, code, code, the_geom };
	
	//FlatFeatureFactory factory = new FlatFeatureFactory(schema);
	 Feature addFeature = schema.create(attributes, String.valueOf(feaID));
	 FeatureCollection addCollection = FeatureCollections.newCollection();
	 addCollection.add(addFeature);
	 postgis.addFeatures(addCollection);
    }

    //TODO: use postgis methods instead of the connections themselves.
    public void testRollbacks() throws Exception {
	String rollbackName = "test rollback";
	java.sql.Connection con = db.getConnection();
	con.setAutoCommit(false); //this should change to startMultiTransaction
	//for now client just handles connections commits
	postgis = new PostgisDataSource(con, FEATURE_TABLE);
	addFeature("test rollback");
	//create ds on different connection, to make sure transactions are
	//not committed until commit is called.
	PostgisDataSource postgisCheck =  
	    new PostgisDataSource(connection, FEATURE_TABLE); 
	postgisCheck.getFeatures(collection,tFilter);
	LOGGER.fine("there are " + collection.size() + 
		    " features before commit");
	assertEquals(6,collection.size());

	//db.commitTransaction();
	con.commit();
	//db.getConnection().close();
	collection = FeatureCollections.newCollection();
	postgisCheck.getFeatures(collection,tFilter);
	LOGGER.fine("there are " + collection.size() + 
		    " features after commit");
	assertEquals(7,collection.size());
	//db.startTransaction();
	
	addFeature("test2");
	addFeature("test3");
	collection = FeatureCollections.newCollection();
	postgisCheck.getFeatures(collection,tFilter);
	LOGGER.fine("there are " + collection.size() + 
		    " features before rollback");
	assertEquals(7,collection.size());
	con.rollback();//db.rollbackTransaction();
	collection = FeatureCollections.newCollection();
	postgisCheck.getFeatures(collection,tFilter);
	LOGGER.fine("there are " + collection.size() + 
		    " features after rollback");
	assertEquals(7,collection.size());
	CompareFilter removeFilter = 
	    filterFac.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
	Expression testLiteral = 
	    filterFac.createLiteralExpression(rollbackName);
	removeFilter.addLeftValue(filterFac.createAttributeExpression
				  (schema, "name"));
	removeFilter.addRightValue(testLiteral);
	postgis.removeFeatures(removeFilter);
	collection = FeatureCollections.newCollection();
	postgisCheck.getFeatures(collection,tFilter);
	LOGGER.fine("there are " + collection.size() + 
		    " features before commit");
	assertEquals(7,collection.size());
	con.commit();
	collection = FeatureCollections.newCollection();
	postgisCheck.getFeatures(collection,tFilter);
	LOGGER.fine("there are " + collection.size() + 
		    " features after commit");
	assertEquals(6,collection.size());
	con.close();
	
	}
    
    public void testMetaData(){
	try {
	    postgis.abortLoading();
	} catch (UnsupportedOperationException e) {
	    LOGGER.fine("caught unsupported op " + e);
	} catch (NullPointerException e) {
	    LOGGER.fine("caught null pointer " + e);
	}
	DataSourceMetaData md = postgis.getMetaData();
	LOGGER.fine("md add " + md.supportsAdd() + ", remove" + 
		    md.supportsRemove() + ", abort " + md.supportsAbort());
	assertTrue(md.supportsAdd());
	assertTrue(md.supportsRemove());
	assertTrue(md.supportsModify());
	}
    
}
