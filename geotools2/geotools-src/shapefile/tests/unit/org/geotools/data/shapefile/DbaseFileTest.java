/*
 * ShapefileTest.java
 * JUnit based test
 *
 * Created on 12 February 2002, 21:27
 */

package org.geotools.data.shapefile;

import junit.framework.*;
import java.net.*;
//import cmp.LEDataStream.*;
import com.vividsolutions.jts.geom.*;
import java.io.*;
import java.util.ArrayList;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;


/**
 * @author Ian Schneider
 * @author James Macgill
 */
public class DbaseFileTest extends TestCaseSupport {
  
  public static final String TEST_FILE = "statepop.dbf";
  
  private DbaseFileReader dbf = null;
  
  public DbaseFileTest(java.lang.String testName) {
    super(testName);
  }
  
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite(DbaseFileTest.class));
  }

  protected void setUp() throws Exception {
    dbf = new DbaseFileReader(getTestResourceChannel(TEST_FILE));
  }
  
  public void testNumberofColsLoaded(){
    assertEquals("Number of attributes found incorect",252,dbf.getHeader().getNumFields());
  }
  
  public void testNumberofRowsLoaded(){
    assertEquals("Number of rows",49,dbf.getHeader().getNumRecords());
  }
  
  public void testDataLoaded() throws Exception{
    Object[] attrs = new Object[dbf.getHeader().getNumFields()];
    dbf.readEntry(attrs);
    assertEquals("Value of Column 0 is wrong",attrs[0],new String("Illinois"));
    assertEquals("Value of Column 4 is wrong",((Double)attrs[4]).doubleValue(),143986.61,0.001);
  }
  
}
