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
import org.apache.log4j.*;
/**
 *
 * @author iant
 */
public class TestLoad extends TestCase {
    // change loging level if problems occur in this test
     Logger _log = Logger.getLogger("MifMid");
    MapInfoDataSource dsMapInfo;
    boolean setup = false;
    String dataFolder;
    public TestLoad(java.lang.String testName) {
        super(testName);
        
        dataFolder = System.getProperty("dataFolder");
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData/";
        }
        
        
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(TestLoad.class);
        
        return suite;
    }
    public void setUp() {
        if(setup) return;
        setup=true;
        dsMapInfo = new MapInfoDataSource();
    }
    
    public void testLoad() throws Exception{

        // Load file
        
        _log.getLoggerRepository().setThreshold(Level.INFO);
        
        String miffile = dataFolder + "/statepop.mif";
        
        Vector objects = dsMapInfo.readMifMid(miffile);
        System.out.println("Read "+objects.size()+" features");
        assertEquals("Wrong number of features read ",49,objects.size());
        assertEquals("First feature name is wrong","Illinois",((Feature)objects.get(0)).getAttribute("STATE_NAME"));
        
    }
    
    
  
    
}
