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

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.datasource.extents.EnvelopeExtent;



/**
 *
 * @author James
 */
public class SVGTest extends TestCase {
    String dataFolder;
    public SVGTest(java.lang.String testName) {
        super(testName);
        
        dataFolder = System.getProperty("dataFolder");
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData";
        }
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SVGTest.class);
        return suite;
    }
    
   public void testGenerateSVG(){
      String stylefile = "simple.sld"; 
      String gmlfile = "simple.gml";
      createSVG("simple.sld","simple.gml","simple.svg");
       
   }
   
   public void testNameFilterSVG(){
      createSVG("nameFilter.sld", "simple.gml", "nameFilter.svg");       
   }


   /**
    * @param stylefile
    * @param gmlfile
    */
    private void createSVG(final String stylefile, final String gmlfile, final String outfile) {
       try{
            GenerateSVG gen = new GenerateSVG();
            URL url = new URL("file:///"+dataFolder+"/"+gmlfile);
            DataSource ds = new GMLDataSource(url);
            FeatureCollectionDefault fc = new FeatureCollectionDefault(ds);

            
            
            //EnvelopeExtent r = new EnvelopeExtent(ds.getBbox());
            //r.setBounds(new com.vividsolutions.jts.geom.Envelope(-100, 100, 0, 100.0));

            //fc.getFeatures(r);
            
            File f = new File(dataFolder,stylefile);
       
            Map map = new DefaultMap();
            StyleFactory sFac = StyleFactory.createStyleFactory();
            SLDStyle reader = new SLDStyle(sFac,f);
            Style style = reader.readXML();
            map.addFeatureTable(fc,style);
            System.out.println("schema for first feature is " + fc.getFeatures(new EnvelopeExtent(new Envelope(0,30,0,30)))[1].getSchema());
            //fc.getFeatures(new Extent())[0].getSchema();
            url = new URL("file:///"+dataFolder+"/"+outfile);
            FileOutputStream out = new FileOutputStream(url.getFile());
            gen.go(map,new Envelope(0,30,0,30),out);
            
            
        
        }catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
            fail("failed because of exception "+e.toString());
        }
       
    }
    
    
}
