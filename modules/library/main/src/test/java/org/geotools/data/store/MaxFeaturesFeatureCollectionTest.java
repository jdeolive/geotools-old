package org.geotools.data.store;

import java.util.Iterator;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class MaxFeaturesFeatureCollectionTest extends
		FeatureCollectionWrapperTestSupport {

	MaxFeaturesFeatureCollection<SimpleFeatureType, SimpleFeature> max;
	
	protected void setUp() throws Exception {
		super.setUp();
		max = new MaxFeaturesFeatureCollection<SimpleFeatureType, SimpleFeature>( delegate, 2 );
	}
	
	public void testSize() throws Exception {
		assertEquals( 2, max.size() );
	}
	
	public void testIterator() throws Exception {
	
		Iterator i = max.iterator();
		for ( int x = 0; x < 2; x++ ) {
			assertTrue( i.hasNext() );
			i.next();
		}
		
		assertFalse( i.hasNext() );
	}
}
