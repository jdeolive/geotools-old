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
package org.geotools.data.oracle;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import oracle.jdbc.OracleConnection;
import org.geotools.data.DataSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.Query;
import org.geotools.data.QueryImpl;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollectionDefault;
import org.geotools.feature.FlatFeatureFactory;
import org.geotools.filter.AbstractFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LikeFilter;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Set;


/**
 * Tests the OracleDataSource. The oracle datasource does not have a publically
 * available instance, so the sql script in oraclespatial/tests/unit/testData
 * named testData.sql needs to be run on your oracle install.  test.properties
 * in the same directory should be set to the correct properties for your
 * instance. Once you have set up your oracle database, ran the testData.sql
 * script  and set the test.properties, you should rename the file to
 * OracleTest  and uncomment the tests.
 *
 * @author geoghegs
 * @author $Author: cholmesny $
 * @version $Revision: 1.1 $ Last Modified: $Date: 2003/07/08 16:29:46 $
 */
public class OracleTest extends TestCase {
    private OracleConnection conn;
    private FilterFactory filterFactory;
    private GeometryFactory jtsFactory;
    private Properties properties;
    private DataSource ds;

    public OracleTest(String testName) {
        super(testName);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(OracleTest.class);
    }

    /**
     * Does nothing, to keep maven happy when all tests are commented out.
     */
    public void testDummy() {
    }

    /*public void setUp() {
       try {
           properties = new Properties();
           properties.load(new FileInputStream("test.properties"));
           OracleDataSourceFactory dsFact = new OracleDataSourceFactory();
           OracleConnectionFactory conFact = new OracleConnectionFactory(properties.getProperty(
                       "host"), properties.getProperty("port"), properties.getProperty("instance"));
           conn = conFact.getOracleConnection(properties.getProperty("user"),
                   properties.getProperty("passwd"));
           ds = dsFact.createDataSource(properties);
           filterFactory = FilterFactory.createFilterFactory();
           jtsFactory = new GeometryFactory();
       } catch (Exception e) {
           fail("Error creating connection: " + e.getMessage());
           System.exit(1);
       }
       }
       public void tearDown() throws SQLException {
           conn.close();
           }*/
    /*
       public void testGetFeatures() {
           try {
               //OracleDataSource ds = new OracleDataSource(conn,"ORA_TEST_POINTS");
               FeatureCollection collection = new FeatureCollectionDefault();
               System.out.println("About to get Features");
               ds.getFeatures(collection, Query.ALL);
               System.out.println("Got Features");
               assertEquals(5, collection.getFeatures().length);
           } catch (DataSourceException e) {
               e.printStackTrace();
               fail("Threw Data source exception " + e.toString());
           }
       }
       public void testMaxFeatures() {
           try {
               QueryImpl query = new QueryImpl();
               query.setMaxFeatures(3);
               FeatureCollection collection = new FeatureCollectionDefault();
               ds.getFeatures(collection, query);
               assertEquals(3, collection.getFeatures().length);
           } catch (DataSourceException e) {
               fail("Threw Data source exception " + e.toString());
           }
       }
       public void testLikeGetFeatures() {
           try {
               //OracleDataSource ds = new OracleDataSource(conn,"ORA_TEST_POINTS");
               LikeFilter likeFilter = filterFactory.createLikeFilter();
               Expression pattern = filterFactory.createLiteralExpression("*");
               Expression attr = filterFactory.createAttributeExpression(null, "NAME");
               likeFilter.setPattern(pattern, "*", "?", "\\");
               likeFilter.setValue(attr);
               FeatureCollection collection = new FeatureCollectionDefault();
               ds.getFeatures(collection, likeFilter);
               assertEquals(5, collection.getFeatures().length);
               pattern = filterFactory.createLiteralExpression("*5");
               likeFilter.setPattern(pattern, "*", "?", "\\");
               collection = new FeatureCollectionDefault();
               ds.getFeatures(collection, likeFilter);
               assertEquals(1, collection.getFeatures().length);
           } catch (Exception e) {
               //fail("Threw Data source exception " + e.getMessage());
               StringWriter sw = new StringWriter();
               PrintWriter pw = new PrintWriter(sw);
               e.printStackTrace(pw);
               fail(sw.toString());
           }
       }
       public void testAttributeFilter() {
           try {
               //OracleDataSource ds = new OracleDataSource(conn,"ORA_TEST_POINTS");
               CompareFilter attributeEquality = filterFactory.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
               Expression attribute = filterFactory.createAttributeExpression(ds.getSchema(), "NAME");
               Expression literal = filterFactory.createLiteralExpression("point 1");
               attributeEquality.addLeftValue(attribute);
               attributeEquality.addRightValue(literal);
               FeatureCollection collection = new FeatureCollectionDefault();
               ds.getFeatures(collection, attributeEquality);
               assertEquals(1, collection.getFeatures().length);
           } catch (Exception e) {
               fail(e.toString());
           }
       }
       public void testBBoxFilter() {
           try {
               //OracleDataSource ds = new OracleDataSource(conn,"ORA_TEST_POINTS");
               GeometryFilter filter = filterFactory.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
               Expression right = filterFactory.createBBoxExpression(new Envelope(-180, 180, -90, 90));
               Expression left = filterFactory.createAttributeExpression(ds.getSchema(), "SHAPE");
               filter.addLeftGeometry(left);
               filter.addRightGeometry(right);
               FeatureCollection collection = new FeatureCollectionDefault();
               ds.getFeatures(collection, filter);
               assertEquals(5, collection.getFeatures().length);
               right = filterFactory.createBBoxExpression(new Envelope(15, 35, 0, 15));
               filter.addRightGeometry(right);
               collection.clear();
               ds.getFeatures(collection, filter);
               assertEquals(2, collection.getFeatures().length);
           } catch (Exception e) {
               fail(e.toString());
           }
       }
       public void testPointGeometryConversion() {
           try {
               //OracleDataSource ds = new OracleDataSource(conn,"ORA_TEST_POINTS");
               CompareFilter filter = filterFactory.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
               Expression left = filterFactory.createAttributeExpression(ds.getSchema(), "NAME");
               Expression right = filterFactory.createLiteralExpression("point 1");
               filter.addLeftValue(left);
               filter.addRightValue(right);
               FeatureCollection collection = new FeatureCollectionDefault();
               ds.getFeatures(collection, filter);
               assertEquals(1, collection.getFeatures().length);
               Feature feature = collection.getFeatures()[0];
               Geometry geom = feature.getDefaultGeometry();
               assertEquals(Point.class.getName(), geom.getClass().getName());
               Point point = (Point) geom;
               assertEquals(10.0, point.getX(), 0.001);
               assertEquals(10.0, point.getY(), 0.001);
           } catch (Exception e) {
               fail(e.toString());
           }
       }
       public void testGetBBox() {
           try {
               //OracleDataSource ds = new OracleDataSource(conn,"ORA_TEST_POINTS");
               Envelope expected = new Envelope(-180, 180, -90, 90);
               Envelope actual = ds.getBbox();
               assertEquals(expected, actual);
           } catch (Exception e) {
               fail(e.toString());
           }
       }
       public void testAddFeatures() {
           try {
               //OracleDataSource ds = new OracleDataSource(conn,"ORA_TEST_POINTS");
               String name = "add_name";
               BigDecimal intval = new BigDecimal(60);
               Point point = jtsFactory.createPoint(new Coordinate(-15.0, -25));
               FlatFeatureFactory fFactory = new FlatFeatureFactory(ds.getSchema());
               Feature feature = fFactory.create(new Object[] { name, intval, point });
               FeatureCollection fc = new FeatureCollectionDefault();
               fc.add(feature);
               Set fids = ds.addFeatures(fc);
               // check the fid
               assertEquals("ORA_TEST_POINTS.6", (fids.toArray())[0]);
               // Select is directly from the DB
               Statement statement = conn.createStatement();
               ResultSet rs = statement.executeQuery("SELECT * FROM ORA_TEST_POINTS WHERE ID = 6");
               if (rs.next()) {
                   assertEquals(rs.getString("NAME"), name);
                   // remove the feature
                   statement.executeUpdate("DELETE FROM ORA_TEST_POINTS WHERE ID = 6");
               } else {
                   fail("Feature was not added correctly");
               }
           } catch (Exception e) {
               fail(e.toString());
           }
       }
       public void testRemoveFeatures() {
           try {
               //OracleDataSource ds = new OracleDataSource(conn,"ORA_TEST_POINTS");
               FeatureCollection initial = ds.getFeatures(Query.ALL);
               GeometryFilter filter = filterFactory.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
               Expression right = filterFactory.createBBoxExpression(new Envelope(15, 35, 0, 15));
               Expression left = filterFactory.createAttributeExpression(ds.getSchema(), "SHAPE");
               filter.addLeftGeometry(left);
               filter.addRightGeometry(right);
               FeatureCollection toRemove = ds.getFeatures(filter);
               assertEquals(2, toRemove.getFeatures().length);
               assertEquals(5, initial.getFeatures().length);
               ds.removeFeatures(filter);
               FeatureCollection postRemove = ds.getFeatures(Query.ALL);
               assertEquals(initial.getFeatures().length - toRemove.getFeatures().length,
                   postRemove.getFeatures().length);
               // put them back in
               Statement statement = conn.createStatement();
               statement.executeUpdate("INSERT INTO ORA_TEST_POINTS VALUES ('point 2',20,2," +
                   "MDSYS.SDO_GEOMETRY(2001,NULL,NULL," + "MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1)," +
                   "MDSYS.SDO_ORDINATE_ARRAY(20,10) ) )");
               statement.executeUpdate("INSERT INTO ORA_TEST_POINTS VALUES ('point 4',40,4," +
                   "MDSYS.SDO_GEOMETRY(2001,NULL,NULL," + "MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1)," +
                   "MDSYS.SDO_ORDINATE_ARRAY(30,10) ) )");
               // Now do a filter that is unsupported by the encoder
               LikeFilter likeFilter = filterFactory.createLikeFilter();
               likeFilter.setValue(filterFactory.createAttributeExpression(ds.getSchema(), "NAME"));
               likeFilter.setPattern(filterFactory.createLiteralExpression("*5"), "*", ".", "!");
               toRemove = ds.getFeatures(likeFilter);
               assertEquals(1, toRemove.getFeatures().length);
               ds.removeFeatures(likeFilter);
               postRemove = ds.getFeatures(Query.ALL);
               assertEquals(initial.getFeatures().length - toRemove.getFeatures().length,
                   postRemove.getFeatures().length);
               statement.execute("INSERT INTO ora_test_points VALUES ('point 5',50,5," +
                   "MDSYS.SDO_GEOMETRY(2001,NULL,NULL," + "MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1)," +
                   "MDSYS.SDO_ORDINATE_ARRAY(-20,10)))");
               //Filter andFilter = likeFilter.and(gf);
           } catch (DataSourceException e) {
               fail(e.getMessage());
           } catch (IllegalFilterException e) {
               fail(e.getMessage());
           } catch (SQLException e) {
               fail(e.getMessage());
           }
       }
       public void testModifyFeatures() {
           try {
               //OracleDataSource ds = new OracleDataSource(conn,"ORA_TEST_POINTS");
               GeometryFilter gf = filterFactory.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
               Expression right = filterFactory.createBBoxExpression(new Envelope(15, 35, 0, 15));
               Expression left = filterFactory.createAttributeExpression(ds.getSchema(), "SHAPE");
               gf.addLeftGeometry(left);
               gf.addRightGeometry(right);
               ds.modifyFeatures(ds.getSchema().getAttributeType("NAME"), "modified", gf);
               Feature[] modFeatures = ds.getFeatures(gf).getFeatures();
               for (int i = 0; i < modFeatures.length; i++) {
                   assertEquals("modified", modFeatures[i].getAttribute("NAME"));
               }
               // reset them
               Statement statement = conn.createStatement();
               statement.executeUpdate("UPDATE ORA_TEST_POINTS SET NAME = 'point 2' WHERE ID = 2");
               statement.executeUpdate("UPDATE ORA_TEST_POINTS SET NAME = 'point 4' WHERE ID = 4");
           } catch (Exception e) {
               fail(e.getMessage());
           }
           }*/
}
