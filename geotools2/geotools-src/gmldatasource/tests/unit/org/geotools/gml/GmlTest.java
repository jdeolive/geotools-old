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
import org.geotools.data.gml.GMLDataSource;


import junit.framework.*;

/**
 *
 * @author ian
 */
public class GmlTest extends TestCase {
    
    static int NTests = 7;
    
    FeatureCollection table = null;
    
    public GmlTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(GmlTest.class);
        return suite;
    }
    
    
    public void testGMLDataSource()throws Exception{
        try{
            String dataFolder = System.getProperty("dataFolder");
            if(dataFolder==null){
                //then we are being run by maven
                dataFolder = System.getProperty("basedir");
                dataFolder+="/tests/unit/testData";
            }
            URL url = new URL("file:///"+dataFolder+"/testGML7Features.gml");
            System.out.println("Testing ability to load "+url+" as Feature datasource");
            DataSource ds = new GMLDataSource(url);
            
            table = ds.getFeatures(Query.ALL);
            
            
            
            
            
            //}catch(DataSourceException exp) {
            //   System.out.println("Exception requesting Extent : "+exp.getClass().getName()+" : "+exp.getMessage());
            //   exp.printStackTrace();
            //}
            
            assertEquals(7,table.size());
            // TODO: add more tests here
            Iterator i = table.iterator();
            System.out.println("Got "+ table.size() + " features");
            while(i.hasNext()){
                i.next();
                // System.out.println("Feature: "+i.next());
            }
        }catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            e.printStackTrace(new PrintWriter(out));
            fail("Load failed because of exception "+e.toString()+" "+out.toString());
        }
    }
    
    
    
}
