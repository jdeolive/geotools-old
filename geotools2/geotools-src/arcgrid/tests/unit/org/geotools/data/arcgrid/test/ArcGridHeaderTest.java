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
        header.parseHeader();
		assertEquals("ncols", header.getNCols(), 233);
		assertEquals("nrows", header.getNRows(), 3);
		assertEquals("xllcorner",header.getXlCorner(), 122222.0, 0);
		assertEquals("yllcorner",header.getYlCorner(), 45001.0, 0);
		assertEquals("cellsize", header.getCellSize(), 250.0, 0);
		assertEquals("NODATA_value",header.getNoData(), 1.70141E38,0);

	}

}
