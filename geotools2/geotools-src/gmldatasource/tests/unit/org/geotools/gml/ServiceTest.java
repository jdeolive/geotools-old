package org.geotools.gml;

/*
 * GmlSuite.java
 * JUnit based test
 *
 * Created on 04 March 2002, 16:09
 */
import org.geotools.data.*;
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
            if(fac instanceof GMLDataSourceFactory){
                found=true;
                break;
            }
        }
        assertTrue("GMLDataSourceFactory not registered", found);
    }
    
    public void testGMLDataSource()throws Exception{
        HashMap params = new HashMap();
        String dataFolder = System.getProperty("dataFolder");
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData";
        }
        params.put("url", "file:///"+dataFolder+"/testGML7Features.gml");
        DataSource ds = DataSourceFinder.getDataSource(params);
        assertNotNull(ds);
    }
    
    
    
}
