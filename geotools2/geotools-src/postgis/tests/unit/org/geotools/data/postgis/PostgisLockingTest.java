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
 * Test for postgis.
 * 
 * Does not a publically available instance of postgis.
 *
 * @author Jody Garnett, TOPP
 */
public class PostgisLockingTest extends TestCase {
    
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.postgislocking");

    // Test Configuration
    //
    private static String USERNAME = "jgarnett";
    private static String PASSWORD = "jody2003";
    private static String HOST = "hydra";
    private static String DATABASE = "jgarnett";
    private static String PORT = "5432";                 
    private static String TABLE = "lockingtest";    
    private static String[] ATTRIBUTES = { "id", "name" };    
    
    private FilterFactory filterFac = FilterFactory.createFilterFactory();
    
    private PostgisLockingDataSource postgis = null;
    private FeatureCollection collection = FeatureCollections.newCollection();
    private FeatureType schema;
    private int srid = -1;
    private PostgisConnectionFactory db;
    private ConnectionPool connection;
    private CompareFilter tFilter;
    private int addId = 32;
    private org.geotools.filter.GeometryFilter geomFilter;

    public PostgisLockingTest(String testName) {
        super(testName);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        LOGGER.info("starting suite...");

        TestSuite suite = new TestSuite(PostgisLockingTest.class);
        LOGGER.info("made suite...");

        return suite;
    }

    public void setUp() {
        LOGGER.info("creating postgis connection...");
        
        db = new PostgisConnectionFactory( HOST, PORT, DATABASE );
                       
        LOGGER.info("created new db connection");
        db.setLogin( USERNAME, PASSWORD );
        
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
            LOGGER.setLevel( Level.FINE );
    	    LOGGER.fine("getting connection pool");
            connection = db.getConnectionPool();
            
            dropTable();
            createTable();  
            
    	    LOGGER.fine("about to create ds");
            postgis = new PostgisLockingDataSource(connection, TABLE);
            
            LOGGER.fine("created de");
    	    schema = ((PostgisDataSource) postgis).getSchema();
        } catch (Exception e) {
            LOGGER.info("exception while setting up "+TABLE+":" + e.getMessage());
        }
        finally {
            LOGGER.setLevel( Level.INFO );
        }       
    }
    protected void createTable() throws SQLException {
        if( tableExists() ) dropTable();
        
        LOGGER.fine("about to create table");
        sql( "CREATE TABLE "+TABLE+" (id INT4,name VARCHAR(32))" );
        sql( "SELECT AddGeometryColumn('"+DATABASE+"','"+TABLE+"','geom',1,'POINT',2)" );
                                    
        LOGGER.fine("populate table with test data");                                                
        sql( "INSERT INTO "+TABLE+" (id, name, geom) VALUES "+
             "(1, 'Olympia', GeometryFromText('POINT(-122.90 46.97)',1))"
        );
        sql( "INSERT INTO "+TABLE+" (id, name, geom) VALUES "+
             "(2, 'Renton', GeometryFromText('POINT(-122.22 47.50)',1))"
        );
    }
    protected boolean tableExists() throws SQLException {
        Connection con = connection.getConnection();
        DatabaseMetaData meta = con.getMetaData();
        ResultSet set = meta.getTables( con.getCatalog(), null, TABLE, null );
        return set.next();
    }
    protected void dropTable() throws SQLException {
        LOGGER.info("about to drop table");
        sql( "SELECT DropGeometryColumn('"+DATABASE+"','"+TABLE+"','geom')" );
        sql( "DROP TABLE "+TABLE );           
    }
    public boolean contains( ResultSet set, int column, String value )
        throws SQLException
    {
        while( set.next() ){
            if( value.equals( set.getString( column ) ) ){
                return true;
            }
        }                    
        return false;
    }        
    public void dump( String msg, ResultSet set ) throws SQLException{
        ResultSetMetaData meta = set.getMetaData();
        int count = 0;
        while( set.next() ){
            StringBuffer buf = new StringBuffer( msg );
            buf.append( ":[" );
            for( int i=1; i<=meta.getColumnCount(); i++){
                buf.append( set.getString( i ) );
                buf.append( "," );                
            }
            buf.append( "]" );
            LOGGER.info( buf.toString() );
            count++;            
        }
        LOGGER.info( msg +":#"+count );
    }
    
    protected void sql( String sql ) throws SQLException{
        Connection con = connection.getConnection();
        Statement st = con.createStatement();
        System.out.println( "sql:"+ sql );
        st.execute( sql );
    }

    public void tearDown() throws SQLException {
        if( tableExists() ) dropTable();
    }
   
    public void testProperties() throws Exception {
        DefaultQuery query = new DefaultQuery();
        query.setPropertyNames( ATTRIBUTES );

        postgis = new PostgisLockingDataSource(connection, TABLE);
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
}
