/*
 * ShapefileDataSourceTest.java
 * JUnit based test
 *
 * Created on March 4, 2002, 4:00 PM
 */                

package org.geotools.shapefile;

import org.geotools.data.*;
import org.geotools.feature.*;
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
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData";
        }
        try{
            URL url = new URL("file:///"+dataFolder+"/statepop.shp");
            System.out.println("Testing ability to load "+url);
            Shapefile shapefile = new Shapefile(url);
            ShapefileDataSource datasource = new ShapefileDataSource(shapefile);
            FeatureCollection table = new FeatureCollectionDefault();
            table.setDataSource(datasource);
            EnvelopeExtent r = new EnvelopeExtent();
            r.setBounds(new Envelope(-180, 180, -90, 90));
            Feature[] features = table.getFeatures(r);
            System.out.println("No features loaded = "+features.length);
            
        }
        catch(DataSourceException e){
            System.out.println(e);
            e.printStackTrace();
            fail("Load failed because of exception "+e.toString());
        }
        catch(IOException ioe){
            System.out.println(ioe);
            ioe.printStackTrace();
            fail("Load failed because of exception "+ioe.toString());
        }
        
    }
    
    public void testLoadWithDbf(){
        String dataFolder = System.getProperty("dataFolder");
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData";
        }
        try{
            URL url = new URL("file:///"+dataFolder+"/statepop.shp");
            System.out.println("Testing ability to load "+url);
            Shapefile shapefile = new Shapefile(url);
            url = new URL("file:///"+dataFolder+"/statepop");
            //DbaseFileReader dbf = new DbaseFileReader(url.getFile());
            ShapefileDataSource datasource = new ShapefileDataSource(shapefile);
            FeatureCollection table = new FeatureCollectionDefault();
            table.setDataSource(datasource);
            EnvelopeExtent r = new EnvelopeExtent();
            r.setBounds(new Envelope(-180, 180, -90, 90));
            Feature[] features = table.getFeatures(r);
            System.out.println("No features loaded = "+features.length);
            System.out.println(features[0].getSchema().toString());
        }
        catch(DataSourceException e){
            System.out.println(e);
            e.printStackTrace();
            fail("Load failed because of exception "+e.toString());
        }
        catch(IOException ioe){
            System.out.println(ioe);
            ioe.printStackTrace();
            fail("Load failed because of exception "+ioe.toString());
        }
        
    }


}
