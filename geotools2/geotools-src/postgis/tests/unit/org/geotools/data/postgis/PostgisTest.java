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
package org.geotools.data.postgis;

import com.vividsolutions.jts.geom.*;
import junit.framework.*;
import org.geotools.data.*;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.feature.*;
import org.geotools.filter.*;
import org.geotools.filter.FilterFactory;
import org.geotools.resources.Geotools;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Test for postgis.  Uses a publically available instance of postgis.
 *
 * @author Chris Holmes, TOPP
 */
public class PostgisTest extends TestCase {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.postgis");
    private static String FEATURE_TABLE = "testset";
    private FilterFactory filterFac = FilterFactory.createFilterFactory();
    private DataSource postgis = null;
    private FeatureCollection collection = FeatureCollections.newCollection();
    private FeatureType schema;
    private int srid = -1;
    private PostgisConnectionFactory db;
    private ConnectionPool connection;
    private CompareFilter tFilter;
    private int addId = 32;
    private org.geotools.filter.GeometryFilter geomFilter;

    public PostgisTest(String testName) {
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
        db = new PostgisConnectionFactory("feathers.leeds.ac.uk", "5432",
                "postgis_test");
        LOGGER.info("created new db connection");
        db.setLogin("postgis_ro", "postgis_ro");
        LOGGER.info("set the login");

        LOGGER.info("created new datasource");

        try {
            tFilter = filterFac.createCompareFilter(AbstractFilter.COMPARE_EQUALS);

            Integer testInt = new Integer(5);
            Expression testLiteral = filterFac.createLiteralExpression(testInt);
            tFilter.addLeftValue(testLiteral);
            tFilter.addRightValue(testLiteral);
        } catch (IllegalFilterException e) {
            fail("Illegal Filter Exception " + e);
        }

        try {
	    LOGGER.fine("getting connection pool");
            connection = db.getConnectionPool();
	    LOGGER.fine("about to create ds");
            postgis = new PostgisDataSource(connection, FEATURE_TABLE);
            LOGGER.fine("created de");
	    schema = ((PostgisDataSource) postgis).getSchema();
        } catch (Exception e) {
            LOGGER.info("exception while making schema" + e.getMessage());
        }
    }

    public void tearDown() throws SQLException {
        //connection.close();
    }

    public void testImport() {
        LOGGER.info("starting type enforcement tests...");

        try {
            postgis.getFeatures(collection, tFilter);
            LOGGER.info("there are " + collection.size() + " feats");
            assertEquals(6, collection.size());

            org.geotools.filter.GeometryFilter gf = filterFac
                .createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
            LiteralExpression right = filterFac.createBBoxExpression(new Envelope(
                        428500, 430000, 428500, 440000));
            gf.addRightGeometry(right);
            gf.addLeftGeometry(filterFac.createAttributeExpression(schema,
                    "the_geom"));

            FeatureCollection geomCollection = postgis.getFeatures(gf);
            LOGGER.info("we have this number of features: " + collection.size());
            LOGGER.info("we have this number of filtered features: "
                + geomCollection.size());
            assertEquals(2, geomCollection.size());
        } catch (DataSourceException dse) {
            LOGGER.info("...threw data source exception" + dse);
            this.fail("...threw data source exception");
        } catch (IllegalFilterException fe) {
            LOGGER.info("...threw filter exception" + fe);
            this.fail("...threw filter exception");
        }

        LOGGER.info("...ending type enforcement tests");
    }

    public void testProperties() throws Exception {
        DefaultQuery query = new DefaultQuery();
        String[] attributes = { "gid", "name" };

        //new AttributeTypeDefault("gid", Integer.class), 
        //  new AttributeTypeDefault("name", String.class)};
        query.setPropertyNames(attributes);

        //FeatureType small = FeatureTypeFactory.create(attributes);
        postgis = new PostgisDataSource(connection, FEATURE_TABLE);
        collection = postgis.getFeatures(query);

        Feature feature = (Feature) collection.iterator().next();
        LOGGER.fine("name feature is " + feature + ", and feature type is "
            + feature.getFeatureType());

        String[] fidsOnly = new String[0];
        query.setPropertyNames(fidsOnly);
        collection = postgis.getFeatures(query);
        feature = (Feature) collection.iterator().next();
        LOGGER.fine("fid feature is " + feature + ", and feature type is "
            + feature.getFeatureType());

        String[] badAtts = { "bull", "blorg" };
        query.setPropertyNames(badAtts);

        try {
            collection = postgis.getFeatures(query);
            fail("exception should be thrown for non matching propertyNames");
        } catch (DataSourceException dse) {
            assertTrue(dse != null);
        }
    }

    //TODO: complete and test primary key as fid - need another table.
    //also one with no geometry.
    public void testMaxFeatures() {
        try {
            postgis = new PostgisDataSource(connection, FEATURE_TABLE);
            schema = ((PostgisDataSource) postgis).getSchema();

            //collection = FeatureCollections.newCollection(); 
            DefaultQuery query = new DefaultQuery();
            query.setMaxFeatures(4);
            collection = postgis.getFeatures(query);
            LOGGER.info("we have this number of filtered features: "
                + collection.size());
            assertEquals(4, collection.size());

            LikeFilter likeFilter = filterFac.createLikeFilter();
            likeFilter.setValue(filterFac.createAttributeExpression(schema,
                    "name"));
            likeFilter.setPattern(filterFac.createLiteralExpression("*7*"),
                "*", ".", "!");

            DefaultQuery q2 = new DefaultQuery();
            q2.setMaxFeatures(3);
            collection = postgis.getFeatures(q2);
            LOGGER.info("there are " + collection.size() + " feats");
            assertEquals(3, collection.size());
        } catch (DataSourceException dse) {
            LOGGER.info("...threw data source exception" + dse);
            this.fail("...threw data source exception");
        } catch (IllegalFilterException fe) {
            LOGGER.info("...threw filter exception" + fe);
            this.fail("...threw filter exception");
        }
    }

    public void testAdd() throws Exception {
        //postgis.setAutoCommit(false);

        String name = "test_add";
        addFeature(name);

        //clean up...basically a delete, but without using a remove features.
        try {
            Connection dbConnection = connection.getConnection();
            Statement statement = dbConnection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM "
                    + FEATURE_TABLE + " WHERE gid = " + addId);
            result.next();
            assertEquals(result.getInt("gid"), addId);

            //assertTrue(result.getDouble("area") == area.doubleValue());
            assertTrue(result.getString("name").equals(name));
            statement.executeUpdate("DELETE FROM " + FEATURE_TABLE
                + " WHERE gid = " + addId);

            result.close();
            statement.close();

            dbConnection.close();
        } catch (SQLException e) {
            LOGGER.info("we had some sql trouble " + e.getMessage());
            fail();
        }
    }

    public void testRemove() throws Exception {
        LOGGER.info("starting type enforcement tests...");

        try {
            postgis.setAutoCommit(false);

            org.geotools.filter.GeometryFilter gf = filterFac
                .createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
            LiteralExpression right = 
                //new BBoxExpression(new Envelope(235,305,235,305));		
                filterFac.createBBoxExpression(new Envelope(429500, 430000,
                        429000, 440000));
            gf.addRightGeometry(right);
            gf.addLeftGeometry(filterFac.createAttributeExpression(schema,
                    "the_geom"));
            doRemoveTest(gf, 2);

            LikeFilter likeFilter = filterFac.createLikeFilter();
            likeFilter.setValue(filterFac.createAttributeExpression(schema,
                    "name"));
            likeFilter.setPattern(filterFac.createLiteralExpression("*7*"),
                "*", ".", "!");
            doRemoveTest(likeFilter, 3);

            Filter andFilter = likeFilter.and(gf);
            doRemoveTest(andFilter, 1);

            //TODO: Weird bug, I don't have time to figure out now.  It wiped
            //out half the database, something to do with differences between
            //how getFeatures does filtering and how deleteFeatures does 
            //filtering may be even at the level of the filter code, am not 
            //sure yet.  But the database that was wiped out needs to be 
            //restored to test this again to figure out what exactly is 
            //going on.  CH
            //	Filter orFilter = likeFilter.or(gf); TODO
            //doRemoveTest(orFilter, 5);
            //	Filter bigAnd = andFilter.and(orFilter);
            //doRemoveTest(bigAnd, 1);
            //postgis.rollback();
        } catch (DataSourceException dse) {
            LOGGER.info("...threw data source exception " + dse);
            this.fail("...threw data source exception");
        } catch (IllegalFilterException fe) {
            LOGGER.info("...threw filter exception " + fe);
            this.fail("...threw filter exception");
        }

        //assertEquals(2,collection.size());
        LOGGER.info("...ending type enforcement tests");
        postgis.rollback();
    }

    //this needs to be updated to work with the feathers.leeds.ac.uk database.  But this
    //should give an idea of how to test the modify features.  It works on my local db.  CH
    public void testModify() throws DataSourceException {
        try {
            postgis.setAutoCommit(true);

            org.geotools.filter.GeometryFilter gf = filterFac
                .createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
            LiteralExpression right = filterFac.createBBoxExpression(new Envelope(
                        428800, 430300, 428900, 440400));
            gf.addRightGeometry(right);
            gf.addLeftGeometry(filterFac.createAttributeExpression(schema,
                    "the_geom"));
            doModifyTest("name", "modified", gf);

            LikeFilter likeFilter = filterFac.createLikeFilter();
            likeFilter.setValue(filterFac.createAttributeExpression(schema,
                    "name"));
            likeFilter.setPattern(filterFac.createLiteralExpression("*4*"),
                "*", ".", "!");
            doModifyTest("gid", new Integer(23), likeFilter);

            Coordinate[] points = {
                new Coordinate(85, 85), new Coordinate(85, 95),
                new Coordinate(95, 95), new Coordinate(95, 85),
                new Coordinate(85, 85)
            };
            LinearRing shell = new LinearRing(points, new PrecisionModel(), srid);
            Polygon testPoly = new Polygon(shell, new PrecisionModel(), srid);

            //CompareFilter compFilter = new CompareFilter(AbstractFilter.COMPARE_EQUALS);
            //compFilter.addLeftValue(new ExpressionAttribute(schema, "gid"));
            //compFilter.addRightValue(new ExpressionLiteral(new Integer(5)));
            //doModifyTest("geom", testPoly, compFilter);
            postgis.rollback();
        } catch (IllegalFilterException fe) {
            LOGGER.info("...threw filter exception" + fe.getMessage());
            this.fail("...threw filter exception");
        }
    }

    private void doRemoveTest(Filter filter, int expectedDel)
        throws DataSourceException {
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
        LOGGER.fine(expectedDel + " total features = " + totNumFeatures
            + " and remaining feat " + numRemainingFeatures
            + " and num deleted (from filt) = " + numDelFeatures);

        //assertEquals(totNumFeatures - numRemainingFeatures, expectedDel); 
        //make sure proper number deleted.
        Set results = postgis.addFeatures(delFeatures); //put them back in.
        LOGGER.fine("postgis reported results: " + results);
	collection = postgis.getFeatures(tFilter); //get all again.
    }

    private void doModifyTest(String attributeName, Object newValue,
        Filter filter) {
        try {
            collection = postgis.getFeatures(filter);

            Object unModified = collection.features().next().getAttribute(attributeName);
            LOGGER.fine("unmodified att is " + unModified + ", att is "
                + schema.getAttributeType(attributeName));
            postgis.modifyFeatures(schema.getAttributeType(attributeName),
                newValue, filter);
            collection = FeatureCollections.newCollection();
            postgis.getFeatures(collection, filter);

            FeatureIterator features = collection.features();
            postgis.modifyFeatures(schema.getAttributeType(attributeName),
                unModified, filter);

            //yes, this sets all the values back the value of the 
            //first one, but it's only a test.
            while (features.hasNext()) {
                Object modified = features.next().getAttribute(attributeName);
                assertTrue(newValue.equals(modified));
            }
        } catch (DataSourceException dse) {
            LOGGER.info("...threw data source exception " + dse);
            this.fail("...threw data source exception");
        }
    }



    private void addFeature(String name) throws Exception {
        Coordinate[] points = {
            new Coordinate(45, 45), new Coordinate(45, 55),
            new Coordinate(55, 55), new Coordinate(55, 45),
            new Coordinate(45, 45)
        };
        PrecisionModel precModel = new PrecisionModel();
        LinearRing shell = new LinearRing(points, precModel, srid);
        Polygon[] testPolys = { new Polygon(shell, precModel, srid) };
        MultiPolygon the_geom = new MultiPolygon(testPolys, precModel, srid);
        Integer feaID = new Integer(addId);
        Double area = new Double(100.0);
        Double perimeter = new Double(40.0);
        Integer testb_ = new Integer(22);
        Integer testb_id = new Integer(4833);
        Integer code = new Integer(0);

        Object[] attributes = {
            feaID, area, perimeter, testb_, testb_id, name, code, code, the_geom
        };

        //FlatFeatureFactory factory = new FlatFeatureFactory(schema);
        Feature addFeature = schema.create(attributes, String.valueOf(feaID));
        FeatureCollection addCollection = FeatureCollections.newCollection();
        addCollection.add(addFeature);
        postgis.addFeatures(addCollection);
    }


    //TODO: have a tear down that deletes the committed feature in case 
    //the test fails or the client pulls the plug.
    public void testRollbacks() throws Exception {
        String rollbackName = "test rollback";
        //java.sql.Connection con = connection.getConnection();

        //for now client just handles connections commits
        //postgis = new PostgisDataSource(con, FEATURE_TABLE);
        postgis.setAutoCommit(false); //this should change to startMultiTransaction
        addFeature("test rollback");

        //create ds on different connection, to make sure transactions are
        //not committed until commit is called.
        //PostgisDataSource postgisCheck = new PostgisDataSource(connection,
	//      FEATURE_TABLE);
        postgis.getFeatures(collection, tFilter);
        LOGGER.fine("there are " + collection.size()
            + " features before commit, should be 6");
        //assertEquals(6, collection.size());

        postgis.commit();

        //con.commit();
        //db.getConnection().close();
        collection = FeatureCollections.newCollection();
        postgis.getFeatures(collection, tFilter);
        LOGGER.fine("there are " + collection.size() + 
		    " features after commit, should be 7");
        //assertEquals(7, collection.size());

        //db.startTransaction();
        addFeature("test2");
        addFeature("test3");
        collection = FeatureCollections.newCollection();
        postgis.getFeatures(collection, tFilter);
        LOGGER.fine("there are " + collection.size()
            + " features before rollback, should be 7");
        //assertEquals(7, collection.size());
        postgis.rollback(); //db.rollbackTransaction();
        collection = FeatureCollections.newCollection();
        postgis.getFeatures(collection, tFilter);
        LOGGER.fine("there are " + collection.size()
            + " features after rollback, should be 7");
        //assertEquals(7, collection.size());

        CompareFilter removeFilter = filterFac.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
        Expression testLiteral = filterFac.createLiteralExpression(rollbackName);
        removeFilter.addLeftValue(filterFac.createAttributeExpression(schema,
                "name"));
        removeFilter.addRightValue(testLiteral);
        postgis.removeFeatures(removeFilter);
        collection = FeatureCollections.newCollection();
        postgis.getFeatures(collection, tFilter);
        LOGGER.fine("there are " + collection.size()
            + " features before commit, should be 7");
        //assertEquals(7, collection.size());
        postgis.commit();
        collection = FeatureCollections.newCollection();
        postgis.getFeatures(collection, tFilter);
        LOGGER.fine("there are " + collection.size() + 
		    " features after commit, should be 6");
        //assertEquals(6, collection.size());

        //con.close();
	}

    public void testMetaData() {
        try {
            postgis.abortLoading();
        } catch (UnsupportedOperationException e) {
            LOGGER.fine("caught unsupported op " + e);
        } catch (NullPointerException e) {
            LOGGER.fine("caught null pointer " + e);
        }

        DataSourceMetaData md = postgis.getMetaData();
        LOGGER.fine("md add " + md.supportsAdd() + ", remove"
            + md.supportsRemove() + ", abort " + md.supportsAbort());
        assertTrue(md.supportsAdd());
        assertTrue(md.supportsRemove());
        assertTrue(md.supportsModify());
    }
}
