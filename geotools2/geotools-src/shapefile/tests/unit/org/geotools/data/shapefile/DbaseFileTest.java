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
import org.geotools.data.shapefile.dbf.*;


/**
 * @author Ian Schneider
 * @author James Macgill
 */
public class DbaseFileTest extends TestCaseSupport {
  
  static final String TEST_FILE = "statepop.dbf";
  
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
  
  public void testRowVsEntry() throws Exception {
    Object[] attrs = new Object[dbf.getHeader().getNumFields()];
    DbaseFileReader dbf2 = new DbaseFileReader(getTestResourceChannel(TEST_FILE));
    while (dbf.hasNext()) {
      dbf.readEntry(attrs);
      DbaseFileReader.Row r = dbf2.readRow();
      for (int i = 0, ii = attrs.length; i < ii; i++) {
        assertNotNull(attrs[i]);
        assertNotNull(r.read(i));
        assertEquals(attrs[i], r.read(i));
      }
    }
  }
  
  public void testHeader() throws Exception {
    DbaseFileHeader header = new DbaseFileHeader();
    header.addColumn("emptyString", 'C', 20, 0);
    header.addColumn("emptyInt", 'N', 20, 0);
    header.addColumn("emptyDouble", 'N',20,5);
    header.addColumn("emptyFloat", 'F', 20, 5);
    header.addColumn("emptyLogical", 'L', 1, 0);
    header.addColumn("emptyDate", 'D', 20, 0);
    int length = header.getRecordLength();
    header.removeColumn("emptyDate");
    assertTrue(length !=  header.getRecordLength());
    header.addColumn("emptyDate", 'D',20,0);
    assertTrue(length == header.getRecordLength());
    header.removeColumn("billy");
    assertTrue(length == header.getRecordLength());
  }

  
  public void testEmptyFields() throws Exception {
    DbaseFileHeader header = new DbaseFileHeader();
    header.addColumn("emptyString", 'C', 20, 0);
    header.addColumn("emptyInt", 'N', 20, 0);
    header.addColumn("emptyDouble", 'N',20,5);
    header.addColumn("emptyFloat", 'F', 20, 5);
    header.addColumn("emptyLogical", 'L', 1, 0);
    header.addColumn("emptyDate", 'D', 20, 0);
    header.setNumRecords(20);
    File f = new File(System.getProperty("java.io.tmpdir"),"scratchDBF.dbf");
    FileOutputStream fout = new FileOutputStream(f);
    DbaseFileWriter dbf = new DbaseFileWriter(header,fout.getChannel());
    for (int i = 0; i < header.getNumRecords(); i++) {
      dbf.write(new Object[6]);
    }
    dbf.close();
    FileInputStream in = new FileInputStream(f);
    DbaseFileReader r = new DbaseFileReader(in.getChannel());
    int cnt = 0;
    while (r.hasNext()) {
      cnt++;
      Object[] o = r.readEntry(); 
      assertTrue(o.length == r.getHeader().getNumFields());
    }
    assertEquals("Bad number of records",cnt,20);
    f.delete();
  }
  
}
