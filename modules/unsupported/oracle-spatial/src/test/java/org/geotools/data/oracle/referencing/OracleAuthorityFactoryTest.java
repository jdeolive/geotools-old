package org.geotools.data.oracle.referencing;

import java.sql.Connection;
import java.util.PropertyResourceBundle;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.geotools.data.jdbc.datasource.ManageableDataSource;
import org.geotools.data.oracle.OracleDataStoreFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class OracleAuthorityFactoryTest extends TestCase {
    
    private DataSource pool;
    private Connection conn;

    protected void setUp() throws Exception {
        super.setUp();

        PropertyResourceBundle resource;
        resource =
            new PropertyResourceBundle(this.getClass().getResourceAsStream("/org/geotools/data/oracle/fixture.properties"));

        String namespace = resource.getString("namespace");
        String host = resource.getString("host");
        int port = Integer.parseInt(resource.getString("port"));        
        String instance = resource.getString("instance");        
        String user = resource.getString("user");
        String password = resource.getString("password");

        if (namespace.equals("http://www.geotools.org/data/postgis")) {
            throw new IllegalStateException(
                "The fixture.properties file needs to be configured for your own database");
        }
         
        try {
            pool = OracleDataStoreFactory.getDefaultDataSource(host, user, password, port, instance, 10, 4, false);
            conn = pool.getConnection();
        }
        catch( Throwable t ){
                t.printStackTrace();
            System.out.println("Could not load test fixture, configure "+getClass().getResource("fixture.properties"));
            t.printStackTrace();
            return;
        }

    }
    
    protected void tearDown() throws Exception {
        if(conn != null)
            conn.close();
        if(pool instanceof ManageableDataSource)
            ((ManageableDataSource) pool).close();
    }
    
    public void testSRIDLookup() throws Exception {
        if(conn == null)
            return;
        
        OracleAuthorityFactory af = new OracleAuthorityFactory(pool);
        CoordinateReferenceSystem crs27700 = af.createCRS(27700);
        assertNotNull(crs27700);
        
        CoordinateReferenceSystem crs81989 = af.createCRS(81989);
        assertNotNull(crs81989);
    }
}
