/*
 * ShapefileTest.java
 * JUnit based test
 *
 * Created on 12 February 2002, 21:27
 */

package org.geotools.shapefile;

import junit.framework.*;
import java.net.*;
//import cmp.LEDataStream.*;
import com.vividsolutions.jts.geom.*;
import java.io.*;
import java.util.ArrayList;
import org.geotools.shapefile.dbf.DbaseFileHeader;
import org.geotools.shapefile.dbf.DbaseFileReader;


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
        TestSuite suite = new TestSuite(DbaseFileTest.class);
        return suite;
    }
    static DbaseFileReader dbf;
    static DbaseFileHeader header;
    static boolean setup = false;
    public void setup() {
        if(setup) return;
        setup=true;
        String dataFolder = System.getProperty("dataFolder");
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData";
        }
        try{
            File url = new File(dataFolder,"statepop");
            System.out.println("Testing ability to load "+url);
            dbf = new DbaseFileReader(url.toString());
            
            }
        catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
            fail("Load failed because of exception "+e.toString());
        }
        if(dbf == null) {
            fail("DbfReader is null");
        }
    }
    
    public void testNumberofColsLoaded(){
        setup();
        assertEquals("Number of attributes found incorect",252,dbf.getNumFields()); 
    }
    
    public void testNumberofRowsLoaded(){
        setup();
        assertEquals("Number of rows",49,dbf.getHeader().getNumRecords());
    }
    public void testDataLoaded() throws Exception{
        setup();
        Object[] attrs = new Object[dbf.getNumFields()];
        dbf.read(attrs, 0);
        assertEquals("Value of Column 0 is wrong",((String)attrs[0]),"Illinois");
        assertEquals("Value of Column 4 is wrong",((Double)attrs[4]).doubleValue(),143986.61,0.001);
    }
    
}
