package org.geotools.gml;

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
import java.net.URL;
import java.util.*;


import junit.framework.*;

/**
 *
 * @author ian
 */
public class GmlTest extends TestCase {
    
    static int NTests = 6;
    
    FeatureCollection table = null;
    
    public GmlTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(GmlSuite.class);
        return suite;
    }
    
 
    public void testGMLDataSource()throws Exception{
        try{
            String dataFolder = System.getProperty("dataFolder");
            URL url = new URL("file:///"+dataFolder+"/testGML7Features.gml");
            System.out.println("Testing ability to load "+url+" as Feature datasource");
            DataSource ds = new GMLDataSource(url);
            
            table = new FeatureCollectionDefault(ds);
            
            
            
            EnvelopeExtent r = new EnvelopeExtent();
            r.setBounds(new com.vividsolutions.jts.geom.Envelope(-100, 100, 0, 100.0));
            
            //table.requestExtent(r);
            //try {
                //fi = new SimpleIndex(table, "LONGITUDE");
                
                table.getFeatures(r);
                
            //}catch(DataSourceException exp) {
             //   System.out.println("Exception requesting Extent : "+exp.getClass().getName()+" : "+exp.getMessage());
             //   exp.printStackTrace();
            //}
            
            assertEquals(5,table.getFeatures().length);
            // TODO: add more tests here
            Feature[] features = table.getFeatures();
            System.out.println("Got "+ features.length + " features");
            for(int i=0;i<features.length;i++){
                
                System.out.println("Feature: "+features[i].toString());
            }
        }catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
            fail("Load failed because of exception "+e.toString());
        }
    }
    

    
}
