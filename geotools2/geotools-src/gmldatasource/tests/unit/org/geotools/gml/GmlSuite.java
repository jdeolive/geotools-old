package org.geotools.gml;

/*
 * GmlSuite.java
 * JUnit based test
 *
 * Created on 04 March 2002, 16:09
 */
import org.geotools.datasource.*;
import org.geotools.datasource.extents.*;
import org.geotools.featuretable.*;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import java.net.URL;
import java.util.*;


import junit.framework.*;

/**
 *
 * @author ian
 */
public class GmlSuite extends TestCase {
    
    static int NTests = 6;
    
    FeatureTable table = null;
    
    public GmlSuite(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(GmlSuite.class);
        return suite;
    }
    
        
    public void testDataSource() throws Exception{
        System.out.println("testDataSource");
        try{
            String dataFolder = System.getProperty("dataFolder");
            URL url = new URL("file:///"+dataFolder+"/testGML7.gml");
            System.out.println("Testing ability to load "+url+" as datasource");
            GMLGeometryDataSource ds = new GMLGeometryDataSource(url.toString());
            
            table = new DefaultFeatureTable(ds);
            
            
            
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
            
            for(int i=0;i<features.length;i++){
                
                System.out.println("Feature: "+features[i].getGeometry().toString());
            }
        }catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
            fail("Load failed because of exception "+e.toString());
        }
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
}
