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
    public void setUp() {
        if(setup) return;
        setup=true;
        dsMapInfo = new MapInfoDataSource();
    }
    String dataFolder;
    public void testLoad() throws Exception{

        // Load file
        dataFolder = System.getProperty("dataFolder");
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData";
        }

        String miffile = dataFolder + "/statepop.mif";
        Vector objects = dsMapInfo.readMifMid(miffile);
        System.out.println("Read "+objects.size()+" features");
        assertEquals("Wrong number of features read ",49,objects.size());
        assertEquals("First feature name is wrong","Illinois",((Feature)objects.get(0)).getAttribute("STATE_NAME"));
        
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
   
    /** Test of getFeatures method, of class org.geotools.mapinfo.MapInfoDataSource.
    public void testGetFeatures() {
        System.out.println("testGetFeatures");
   
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
