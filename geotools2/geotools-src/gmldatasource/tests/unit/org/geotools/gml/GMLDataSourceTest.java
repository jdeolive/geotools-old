/*
 * GMLDataSourceTest.java
 * JUnit based test
 *
 * Created on 04 March 2002, 16:09
 */

package org.geotools.gml;

import junit.framework.*;
import org.geotools.datasource.*;
import java.net.*;
import java.io.*;
import com.vividsolutions.jts.geom.*;
import java.util.*;

/**
 *
 * @author ian
 */
public class GMLDataSourceTest extends TestCase {
    
    public GMLDataSourceTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(GMLDataSourceTest.class);
        
        return suite;
    }
    public void testRead() throws Exception{
        System.out.println("testRead");
        String dataFolder = System.getProperty("dataFolder");
        URL url = new URL("file:///"+dataFolder+"/testGML1.gml");
        System.out.println("Testing ability to read "+url);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        GMLReader gmlr = new GMLReader(in);
        
        GeometryCollection gc = gmlr.read();
        assertEquals(1,gc.getNumGeometries());
        url = new URL("file:///"+dataFolder+"/testGML2.gml");
        in=new BufferedReader(new InputStreamReader(url.openStream()));
        gmlr = new GMLReader(in);
        gc = gmlr.read();
        assertEquals(1,gc.getNumGeometries());
        
    }
    public void testDataSource() throws Exception{
        String dataFolder = System.getProperty("dataFolder");
        URL url = new URL("file:///"+dataFolder+"/testGML1.gml");
        System.out.println("Testing ability to load "+url+" as datasource");
        GMLDataSource ds = new GMLDataSource(url);
        
        ds.load((Extent)null);   
    }
}