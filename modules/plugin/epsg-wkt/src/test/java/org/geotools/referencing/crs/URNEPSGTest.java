package org.geotools.referencing.crs;

import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import junit.framework.TestCase;

public class URNEPSGTest extends TestCase {

	public void test() throws Exception {
		CoordinateReferenceSystem crs1 = CRS.decode( "EPSG:4326" );
		CoordinateReferenceSystem crs2 = CRS.decode( "urn:x-ogc:def:crs:EPSG:6.11.2:4326" );
		
		assertEquals( crs1, crs2 );
	}
}
