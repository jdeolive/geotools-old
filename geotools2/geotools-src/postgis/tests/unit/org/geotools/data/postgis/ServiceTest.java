package org.geotools.data.postgis;

/*
 * GmlSuite.java
 * JUnit based test
 *
 * Created on 04 March 2002, 16:09
 */
import org.geotools.data.*;
import org.geotools.datasource.extents.*;
import org.geotools.feature.*;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;
import java.util.HashMap;
import java.util.Iterator;


import junit.framework.*;
import org.geotools.data.DataSourceFinder;
import org.geotools.data.DataSource;

/**
 *
 * @author ian
 */
public class ServiceTest extends TestCase {
    
    
    
    public ServiceTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ServiceTest.class);
        return suite;
    }
    
    public void testIsAvailable() {
        Iterator list = DataSourceFinder.getAvailableDataSources();
        boolean found = false;
        while(list.hasNext()){
            DataSourceFactorySpi fac = (DataSourceFactorySpi)list.next();
            if(fac instanceof PostgisDataSourceFactory){
                found=true;
                break;
            }
        }
        assertTrue("PostgisDataSourceFactory not registered", found);
    }
    
    public void testPostgisDataSource()throws Exception{
        HashMap params = new HashMap();
        
        params.put("dbtype", "postgis");
        params.put("host","feathers.leeds.ac.uk");
        params.put("port", "5432");
        params.put("database","postgis_test");
        params.put("user","postgis_ro");
        params.put("passwd","postgis_ro");
        params.put("table","testset");
        DataSource ds = DataSourceFinder.getDataSource(params);
        assertNotNull(ds);
    }
    
    
    
}
