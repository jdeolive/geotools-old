/*
 * Created on 16/10/2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.geotools.data.oracle;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.ConnectionPoolManager;
import org.geotools.data.jdbc.JDBCDataStoreConfig;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.filter.AbstractFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LikeFilter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * @author geoghegs
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class OracleDataStoreTest extends TestCase {
    private ConnectionPool cPool;
    private FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private Properties properties;
    private GeometryFactory jtsFactory = new GeometryFactory();
    private String schemaName;
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        properties = new Properties();
        properties.load(new FileInputStream("test.properties"));
        schemaName = properties.getProperty("schema");
        OracleConnectionFactory fact = new OracleConnectionFactory(properties.getProperty("host"), 
                properties.getProperty("port"), properties.getProperty("instance"));
        fact.setLogin(properties.getProperty("user"), properties.getProperty("passwd"));
        cPool = fact.getConnectionPool();
        Connection conn = cPool.getConnection();
        System.out.println(conn.getTypeMap());
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        Connection conn = cPool.getConnection();
        Statement st = conn.createStatement();
        st.executeUpdate("DELETE FROM ORA_TEST_POINTS");
        st.executeUpdate("INSERT INTO ora_test_points VALUES ('point 1',10,1," +
                "MDSYS.SDO_GEOMETRY(2001,NULL,NULL," +
                "MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1)," +
                "MDSYS.SDO_ORDINATE_ARRAY(10,10)))");
        st.executeUpdate("INSERT INTO ora_test_points VALUES ('point 2',20,2," +
                        "MDSYS.SDO_GEOMETRY(2001,NULL,NULL," +
                        "MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1)," +
                        "MDSYS.SDO_ORDINATE_ARRAY(20,10)))");
        st.executeUpdate("INSERT INTO ora_test_points VALUES ('point 3',30,3," +
                        "MDSYS.SDO_GEOMETRY(2001,NULL,NULL," +
                        "MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1)," +
                        "MDSYS.SDO_ORDINATE_ARRAY(20,30)))");
        st.executeUpdate("INSERT INTO ora_test_points VALUES ('point 4',40,4," +
                        "MDSYS.SDO_GEOMETRY(2001,NULL,NULL," +
                        "MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1)," +
                        "MDSYS.SDO_ORDINATE_ARRAY(30,10)))");
        st.executeUpdate("INSERT INTO ora_test_points VALUES ('point 5',50,5," +
                        "MDSYS.SDO_GEOMETRY(2001,NULL,NULL," +
                        "MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1)," +
                        "MDSYS.SDO_ORDINATE_ARRAY(-20,10)))");
        ConnectionPoolManager manager = ConnectionPoolManager.getInstance();
        manager.closeAll();
    }

    public void testGetFeatureTypes() throws IOException {
        try {
            DataStore ds = new OracleDataStore(cPool, properties.getProperty("schema"), new HashMap());
            String[] fts = ds.getTypeNames();
            System.out.println(Arrays.asList(fts));
            assertEquals(3, fts.length);
        } catch (DataSourceException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void testGetSchema() throws Exception {
        try {
            DataStore ds = new OracleDataStore(cPool, properties.getProperty("schema"), new HashMap());
            FeatureType ft = ds.getSchema("ORA_TEST_POINTS");
            assertNotNull(ft);
            System.out.println(ft);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void testGetFeatureReader() throws Exception {
        try {
            DataStore ds = new OracleDataStore(cPool, properties.getProperty("schema"), new HashMap());
            FeatureType ft = ds.getSchema("ORA_TEST_POINTS");
            Query q = new DefaultQuery( "ORA_TEST_POINTS" );
            FeatureReader fr = ds.getFeatureReader( q, Transaction.AUTO_COMMIT);
            int count = 0;

            while (fr.hasNext()) {
                fr.next();
                count++;
            }

            assertEquals(5, count);

            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void testGetFeatureWriter() throws Exception {
        DataStore ds = new OracleDataStore(cPool, properties.getProperty("schema"), new HashMap());
        FeatureWriter writer = ds.getFeatureWriter("ORA_TEST_POINTS", Filter.NONE, Transaction.AUTO_COMMIT);
        assertNotNull(writer);

        Feature feature = writer.next();
        System.out.println(feature);
        feature.setAttribute(0, "Changed Feature");
        System.out.println(feature);
        writer.write();
        writer.close();

        Query q = new DefaultQuery( "ORA_TEST_POINTS" );
        FeatureReader reader = ds.getFeatureReader( q, Transaction.AUTO_COMMIT);
        Feature readF = reader.next();
        
        assertEquals("Changed Feature", feature.getAttribute(0));
        assertEquals(feature.getID(), readF.getID());
        assertEquals(feature.getAttribute(0), readF.getAttribute((0)));
        assertEquals(feature.getAttribute(1), readF.getAttribute((1)));
        // assertTrue(feature.getAttribute(2).equals(readF.getAttribute((2))));  JTS doesnt override equals.  POS
        
        reader.close();
    }
    
//    public void testMaxFeatures() throws Exception {        
//        DefaultQuery query = new DefaultQuery();
//        query.setTypeName("ORA_TEST_POINTS");
//        query.setMaxFeatures(3);
//        DataStore ds = new OracleDataStore(cPool, "SEAN");
//        FeatureSource fs = ds.getFeatureSource("ORA_TEST_POINTS");
//        FeatureResults fr = fs.getFeatures(query);
//        assertEquals(3, fr.getCount());
//    }

    public void testLikeGetFeatures() throws Exception {
        LikeFilter likeFilter = filterFactory.createLikeFilter();
        Expression pattern = filterFactory.createLiteralExpression("*");
        Expression attr = filterFactory.createAttributeExpression(null, "NAME");
        likeFilter.setPattern(pattern, "*", "?", "\\");
        likeFilter.setValue(attr);
        
        DataStore ds = new OracleDataStore(cPool, properties.getProperty("schema"), new HashMap());
        FeatureSource fs = ds.getFeatureSource("ORA_TEST_POINTS");
        FeatureResults fr = fs.getFeatures(likeFilter);
        assertEquals(5, fr.getCount());
        
        pattern = filterFactory.createLiteralExpression("*5");
        likeFilter.setPattern(pattern, "*", "?", "\\");
        fr = fs.getFeatures(likeFilter);
        assertEquals(1, fr.getCount());
    }
    
    public void testAttributeFilter() throws Exception {        
        DataStore ds = new OracleDataStore(cPool, properties.getProperty("schema"), new HashMap());
        CompareFilter attributeEquality = filterFactory.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
        Expression attribute = filterFactory.createAttributeExpression(ds.getSchema("ORA_TEST_POINTS"), "NAME");
        Expression literal = filterFactory.createLiteralExpression("point 1");
        attributeEquality.addLeftValue(attribute);
        attributeEquality.addRightValue(literal);
        
        FeatureSource fs = ds.getFeatureSource("ORA_TEST_POINTS");
        FeatureResults fr = fs.getFeatures(attributeEquality);
        assertEquals(1, fr.getCount());
        
        FeatureCollection fc = fr.collection();
        assertEquals(1, fc.size());
        Feature f = fc.features().next();
        assertEquals("point 1", f.getAttribute("NAME"));        
    }
    
    public void testBBoxFilter() throws Exception {
        DataStore ds = new OracleDataStore(cPool, properties.getProperty("schema"), new HashMap());        //
        GeometryFilter filter = filterFactory.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
        Expression right = filterFactory.createBBoxExpression(new Envelope(-180, 180, -90, 90));
        Expression left = filterFactory.createAttributeExpression(ds.getSchema("ORA_TEST_POINTS"), "SHAPE");
        filter.addLeftGeometry(left);
        filter.addRightGeometry(right);
        
        FeatureSource fs = ds.getFeatureSource("ORA_TEST_POINTS");
        FeatureResults fr = fs.getFeatures(filter);        
        assertEquals(5, fr.getCount());
        
        right = filterFactory.createBBoxExpression(new Envelope(15, 35, 0, 15));
        filter.addRightGeometry(right);
        fr = fs.getFeatures(filter);
        assertEquals(2, fr.getCount());
    }
    
    public void testPointGeometryConversion() throws Exception {
        DataStore ds = new OracleDataStore(cPool, properties.getProperty("schema"), new HashMap());
        CompareFilter filter = filterFactory.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
        Expression left = filterFactory.createAttributeExpression(ds.getSchema("ORA_TEST_POINTS"), "NAME");
        Expression right = filterFactory.createLiteralExpression("point 1");
        filter.addLeftValue(left);
        filter.addRightValue(right);
        
        
        FeatureSource fs = ds.getFeatureSource("ORA_TEST_POINTS");
        FeatureResults fr = fs.getFeatures(filter);        
        assertEquals(1, fr.getCount());
        
        Feature feature = (Feature) fr.collection().iterator().next();
        Geometry geom = feature.getDefaultGeometry();
        assertEquals(Point.class.getName(), geom.getClass().getName());
        Point point = (Point) geom;
        assertEquals(10.0, point.getX(), 0.001);
        assertEquals(10.0, point.getY(), 0.001);
    }
    
    public void testAddFeatures() throws Exception {
        Map fidGen = new HashMap();
        fidGen.put("ORA_TEST_POINTS", JDBCDataStoreConfig.FID_GEN_MANUAL_INC);
        JDBCDataStoreConfig config = JDBCDataStoreConfig.createWithSchemaNameAndFIDGenMap(schemaName, fidGen);
        DataStore ds = new OracleDataStore(cPool, config);
        
        String name = "add_name";
        BigDecimal intval = new BigDecimal(70);
        Point point = jtsFactory.createPoint(new Coordinate(-15.0, -25));
        Feature feature = ds.getSchema("ORA_TEST_POINTS").create(new Object[] { name, intval, point });
        FeatureReader reader = DataUtilities.reader(new Feature[] {feature});
        FeatureStore fs = (FeatureStore) ds.getFeatureSource("ORA_TEST_POINTS");
        fs.addFeatures(reader);

        // Select is directly from the DB
        Connection conn = cPool.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM ORA_TEST_POINTS WHERE NAME = 'add_name'");

        if (rs.next()) {
            assertEquals(rs.getString("NAME"), name);
            assertEquals(70, rs.getInt("INTVAL"));            
        } else {
            fail("Feature was not added correctly");
        }
    }
    
    public void testRemoveFeatures() throws Exception {
        DataStore ds = new OracleDataStore(cPool, properties.getProperty("schema"), new HashMap());
        FeatureStore fs = (FeatureStore) ds.getFeatureSource("ORA_TEST_POINTS");
        fs.removeFeatures(Filter.NONE);
        FeatureResults fr = fs.getFeatures();
        assertEquals(0, fr.getCount());
    }
    
    public void testPropertySelect() throws Exception {
        DataStore ds = new OracleDataStore(cPool, properties.getProperty("schema"), new HashMap());
        DefaultQuery q = new DefaultQuery("ORA_TEST_POINTS",Filter.NONE);
        q.setPropertyNames(new String[]{"NAME"});
        FeatureReader fr = ds.getFeatureReader(q, Transaction.AUTO_COMMIT);
        Feature f = fr.next();
        FeatureType ft = f.getFeatureType();
        assertEquals(1, ft.getAttributeCount());
        assertEquals("NAME", ft.getAttributeType(0).getName());        
    }
}
