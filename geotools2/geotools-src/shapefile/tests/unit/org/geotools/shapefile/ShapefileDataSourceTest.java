/*
 * ShapefileDataSourceTest.java
 * JUnit based test
 *
 * Created on March 4, 2002, 4:00 PM
 */                

package org.geotools.shapefile;

import org.geotools.datasource.*;
import org.geotools.datasource.extents.*;
import com.vividsolutions.jts.geom.*;
import java.io.*;
import java.net.*;

import junit.framework.*;

/**
 *
 * @author jamesm
 */                                
public class ShapefileDataSourceTest extends TestCase {
    
    public ShapefileDataSourceTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ShapefileDataSourceTest.class);
        return suite;
    }

    public void testLoad(){
        String dataFolder = System.getProperty("dataFolder");
        try{
            URL url = new URL("file:///"+dataFolder+"/statepop.shp");
            System.out.println("Testing ability to load "+url);
            Shapefile shapefile = new Shapefile(url);
            ShapefileDataSource datasource = new ShapefileDataSource(shapefile);
            FeatureTable table = new FeatureTable(datasource);
            
            EnvelopeExtent r = new EnvelopeExtent();
            r.setBounds(new Envelope(50, 360, 0, 180.0));
            table.requestExtent(r);
            
        }
        catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
            fail("Load failed because of exception "+e.toString());
        }
        
    }


}
