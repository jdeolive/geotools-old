/*
 * MapInfoDataSourceTest.java
 * JUnit based test
 *
 * Created on 29 July 2002, 12:13
 */

package org.geotools.mapinfo;

import junit.framework.*;
import org.geotools.data.DataSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.Extent;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.Feature;
import org.geotools.filter.Filter;
import org.geotools.feature.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Vector;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.StringTokenizer;
import com.vividsolutions.jts.geom.*;
import java.net.URL;

/**
 *
 * @author iant
 */
public class MapInfoDataSourceTest extends TestCase {
    MapInfoDataSource dsMapInfo;
    boolean setup = false;
    public MapInfoDataSourceTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(MapInfoDataSourceTest.class);
        
        return suite;
    }
   
    String dataFolder;
    public void testGetFeatures(){
        String dataFolder = System.getProperty("dataFolder");
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData";
        }
        try{
            URL url = new URL("file:////"+dataFolder+"/statepop.mif");
            System.out.println("Testing ability to load "+url);
            MapInfoDataSource datasource = new MapInfoDataSource(url);
            FeatureCollection table = datasource.getFeatures(null);
            Feature[] features = table.getFeatures();
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
  /*
    /** Test of readMifMid method, of class org.geotools.mapinfo.MapInfoDataSource.
    public void testReadMifMid() {
        System.out.println("testReadMifMid");
   
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
   
    /** Test of importFeatures method, of class org.geotools.mapinfo.MapInfoDataSource.
    public void testImportFeatures() {
        System.out.println("testImportFeatures");
   
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
   
    /** Test of exportFeatures method, of class org.geotools.mapinfo.MapInfoDataSource.
    public void testExportFeatures() {
        System.out.println("testExportFeatures");
   
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
   
    /** Test of stopLoading method, of class org.geotools.mapinfo.MapInfoDataSource.
    public void testStopLoading() {
        System.out.println("testStopLoading");
   
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
   
    /** Test of getExtent method, of class org.geotools.mapinfo.MapInfoDataSource.
    public void testGetExtent() {
        System.out.println("testGetExtent");
   
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
   
 
   
    /** Test of addFeatures method, of class org.geotools.mapinfo.MapInfoDataSource.
    public void testAddFeatures() {
        System.out.println("testAddFeatures");
   
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
   
    /** Test of abortLoading method, of class org.geotools.mapinfo.MapInfoDataSource.
    public void testAbortLoading() {
        System.out.println("testAbortLoading");
   
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
   
    /** Test of getBbox method, of class org.geotools.mapinfo.MapInfoDataSource.
    public void testGetBbox() {
        System.out.println("testGetBbox");
   
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
   
    /** Test of modifyFeatures method, of class org.geotools.mapinfo.MapInfoDataSource.
    public void testModifyFeatures() {
        System.out.println("testModifyFeatures");
   
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
   
    /** Test of removeFeatures method, of class org.geotools.mapinfo.MapInfoDataSource.
    public void testRemoveFeatures() {
        System.out.println("testRemoveFeatures");
   
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
   
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
   
   */
    
}
