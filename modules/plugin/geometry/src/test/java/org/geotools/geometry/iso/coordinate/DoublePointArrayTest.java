package org.geotools.geometry.iso.coordinate;

import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import junit.framework.TestCase;

public class DoublePointArrayTest extends TestCase {
	
	public void testDoublePointArray() {
		
		CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
		double[] fullArray = {0.0, 1.0, 2.3, 3.1, 4.8, 5.6, 6.2, 7.0, 8.5, 9.9, 10.10};
		double[] smallArray = {4.8, 5.6, 6.2, 7.0, 8.5, 9.9};
		DoublePointArray dp_fromFullArray = new DoublePointArray(crs, fullArray, 4, 9);
		DoublePointArray dp_fromSmallArray = new DoublePointArray(crs, smallArray);
		
		assertNotNull(dp_fromFullArray);
		assertNotNull(dp_fromSmallArray);
		//System.out.println(dp_fromFullArray.equals(dp_fromSmallArray));
		assertEquals(dp_fromFullArray, dp_fromSmallArray);
		
	}

}
