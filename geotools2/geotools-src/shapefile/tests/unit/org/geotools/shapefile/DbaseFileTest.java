/*
 * ShapefileTest.java
 * JUnit based test
 *
 * Created on 12 February 2002, 21:27
 */

package org.geotools.shapefile;

import junit.framework.*;
import java.net.*;
import cmp.LEDataStream.*;
import com.vividsolutions.jts.geom.*;
import java.io.*;
import java.util.ArrayList;
import org.geotools.shapefile.dbf.*;


/**
 *
 * @author James Macgill
 */
public class DbaseFileTest extends TestCase {
    
    public DbaseFileTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ShapefileTest.class);
        return suite;
    }
    
    public void testLoadingStatePop() {
        
        String dataFolder = System.getProperty("dataFolder");
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData";
        }
        try{
            URL url = new URL("file:////"+dataFolder+"/statepop");
            System.out.println("Testing ability to load "+url);
            DbaseFileReader dbf = new DbaseFileReader(url.getFile());
            assertEquals("Number of attributes found incorect",252,dbf.getNumFields());
            }
        catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
            fail("Load failed because of exception "+e.toString());
        }
    }
}
