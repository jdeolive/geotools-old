/*
 * SVGSuite.java
 * JUnit based test
 *
 * Created on 08 June 2002, 00:04
 */

package org.geotools.svg;

import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.net.URL;

import junit.framework.*;

import org.geotools.map.*;
import org.geotools.styling.*;
import org.geotools.data.*;
import org.geotools.gml.*;
import org.geotools.feature.FeatureCollectionDefault;

import org.geotools.datasource.extents.EnvelopeExtent;

/**
 *
 * @author James
 */
public class SVGTest extends TestCase {
    
    public SVGTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SVGTest.class);
        return suite;
    }
    
   public void testGenerateSVG(){
       GenerateSVG gen = new GenerateSVG();
      try{
            String dataFolder = System.getProperty("dataFolder");
            URL url = new URL("file:///"+dataFolder+"/simple.gml");
            DataSource ds = new GMLDataSource(url);
            FeatureCollectionDefault fc = new FeatureCollectionDefault(ds);

            EnvelopeExtent r = new EnvelopeExtent();
            r.setBounds(new com.vividsolutions.jts.geom.Envelope(-100, 100, 0, 100.0));
            
            File f = new File(System.getProperty("dataFolder"),"simple.sld");
       
            Map map = new DefaultMap();
            
            SLDStyle style = new SLDStyle(f);
            map.addFeatureTable(fc,style);
       
            url = new URL("file:///"+dataFolder+"/simple.svg");
            FileOutputStream out = new FileOutputStream(url.getFile());
            gen.go(map,out);
            
            
        
        }catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
            fail("failed because of exception "+e.toString());
        }
       
       
   }
    
    
}
