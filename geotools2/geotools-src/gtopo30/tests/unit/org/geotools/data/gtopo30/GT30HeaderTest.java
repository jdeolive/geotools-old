/*
 * ShapefileTest.java
 * JUnit based test
 *
 * Created on 12 February 2002, 21:27
 */

package org.geotools.data.gtopo30;

import java.net.URL;
import junit.framework.*;


/**
 * @author Ian Schneider
 * @author James Macgill
 */
public class GT30HeaderTest extends TestCaseSupport {
  
    public GT30HeaderTest(String testName) {
    super(testName);
  }
    
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite(GT30HeaderTest.class));
  }
  
  public void testHeaderDEM() throws Exception {
      URL headerURL = getTestResource("DEM.HDR");
      GT30Header header = new GT30Header(headerURL);
      assertEquals("Byteorder", header.getProperty(GT30Header.BYTEORDER), "M");
      assertEquals("Layout", header.getProperty(GT30Header.LAYOUT), "BIL");
      assertEquals("NRows", header.getProperty(GT30Header.NROWS), new Integer(6000));
      assertEquals("BCols", header.getProperty(GT30Header.NCOLS), new Integer(4800));
      assertEquals("NBands", header.getProperty(GT30Header.NBANDS), new Integer(1));
      assertEquals("NBits", header.getProperty(GT30Header.NBITS), new Integer(16));
      assertEquals("BandRowBytes", header.getProperty(GT30Header.BANDROWBYTES), new Integer(9600));
      assertEquals("TotalRowBytes", header.getProperty(GT30Header.TOTALROWBYTES), new Integer(9600));
      assertEquals("BandGapBytes", header.getProperty(GT30Header.BANDGAPBYTES), new Integer(0));
      assertEquals("NoData", header.getProperty(GT30Header.NODATA), new Integer(-9999));
      assertEquals("ULXMap", header.getProperty(GT30Header.ULXMAP), new Double(-19.99583333333333));
      assertEquals("ULYMap", header.getProperty(GT30Header.ULYMAP), new Double(89.99583333333334));
      assertEquals("XDim", header.getProperty(GT30Header.XDIM), new Double(0.00833333333333));
      assertEquals("YDim", header.getProperty(GT30Header.YDIM), new Double(0.00833333333333));
  }
  
  public void testHeaderDEMExtraInfo() throws Exception {
      URL headerURL = getTestResource("DEM_MORE.HDR");
      GT30Header header = new GT30Header(headerURL);
      assertEquals("Byteorder", header.getProperty(GT30Header.BYTEORDER), "M");
      assertEquals("Layout", header.getProperty(GT30Header.LAYOUT), "BIL");
      assertEquals("NRows", header.getProperty(GT30Header.NROWS), new Integer(6000));
      assertEquals("BCols", header.getProperty(GT30Header.NCOLS), new Integer(4800));
      assertEquals("NBands", header.getProperty(GT30Header.NBANDS), new Integer(1));
      assertEquals("NBits", header.getProperty(GT30Header.NBITS), new Integer(16));
      assertEquals("BandRowBytes", header.getProperty(GT30Header.BANDROWBYTES), new Integer(9600));
      assertEquals("TotalRowBytes", header.getProperty(GT30Header.TOTALROWBYTES), new Integer(9600));
      assertEquals("BandGapBytes", header.getProperty(GT30Header.BANDGAPBYTES), new Integer(0));
      assertEquals("NoData", header.getProperty(GT30Header.NODATA), new Integer(-9999));
      assertEquals("ULXMap", header.getProperty(GT30Header.ULXMAP), new Double(-19.99583333333333));
      assertEquals("ULYMap", header.getProperty(GT30Header.ULYMAP), new Double(89.99583333333334));
      assertEquals("XDim", header.getProperty(GT30Header.XDIM), new Double(0.00833333333333));
      assertEquals("YDim", header.getProperty(GT30Header.YDIM), new Double(0.00833333333333));
  }
  
  public void testHeaderSource() throws Exception  {
      URL headerURL = getTestResource("SRC.SCH");
      GT30Header header = new GT30Header(headerURL);
      assertEquals("Byteorder", header.getProperty(GT30Header.BYTEORDER), "M");
      assertEquals("Layout", header.getProperty(GT30Header.LAYOUT), "BIL");
      assertEquals("NRows", header.getProperty(GT30Header.NROWS), new Integer(6000));
      assertEquals("BCols", header.getProperty(GT30Header.NCOLS), new Integer(4800));
      assertEquals("NBands", header.getProperty(GT30Header.NBANDS), new Integer(1));
      assertEquals("NBits", header.getProperty(GT30Header.NBITS), new Integer(8));
      assertEquals("BandRowBytes", header.getProperty(GT30Header.BANDROWBYTES), new Integer(4800));
      assertEquals("TotalRowBytes", header.getProperty(GT30Header.TOTALROWBYTES), new Integer(4800));
      assertEquals("BandGapBytes", header.getProperty(GT30Header.BANDGAPBYTES), new Integer(0));
      assertEquals("NoData", header.getProperty(GT30Header.NODATA), new Integer(-9999));
      assertEquals("ULXMap", header.getProperty(GT30Header.ULXMAP), new Double(-19.99583333333333));
      assertEquals("ULYMap", header.getProperty(GT30Header.ULYMAP), new Double(89.99583333333334));
      assertEquals("XDim", header.getProperty(GT30Header.XDIM), new Double(0.00833333333333));
      assertEquals("YDim", header.getProperty(GT30Header.YDIM), new Double(0.00833333333333));
  }

}
