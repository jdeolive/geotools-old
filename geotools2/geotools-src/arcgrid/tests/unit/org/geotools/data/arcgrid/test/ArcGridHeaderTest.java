/*
 * ArcGridHeader.java
 * JUnit based test
 *
 * Created on 12 February 2002, 21:27
 */

package org.geotools.data.arcgrid.test;

import java.net.URL;

import org.geotools.data.arcgrid.ArcGridRaster;

/**
 * @author Christiaan ten Klooster
 */
public class ArcGridHeaderTest extends TestCaseSupport {

	public ArcGridHeaderTest(String testName) {
		super(testName);
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite(ArcGridHeaderTest.class));
	}

	public void testHeaderSource() throws Exception {
		
		URL url = getTestResource("ArcGrid.asc");
		ArcGridRaster header = new ArcGridRaster(url);
		assertEquals("ncols", header.getProperty(ArcGridRaster.NCOLS), new Integer(233));
		assertEquals("nrows", header.getProperty(ArcGridRaster.NROWS), new Integer(3));
		assertEquals(
			"xllcorner",
			header.getProperty(ArcGridRaster.XLLCORNER),
			new Double(122222.0));
		assertEquals(
			"yllcorner",
			header.getProperty(ArcGridRaster.YLLCORNER),
			new Double(45001.0));
		assertEquals("cellsize", header.getProperty(ArcGridRaster.CELLSIZE), new Double(250.0));
		assertEquals(
			"NODATA_value",
			header.getProperty(ArcGridRaster.NODATA_VALUE),
			new Double(1.70141E38));

	}

}
