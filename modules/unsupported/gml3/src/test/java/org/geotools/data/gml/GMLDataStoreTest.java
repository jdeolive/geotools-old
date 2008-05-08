package org.geotools.data.gml;

import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeatureType;

public class GMLDataStoreTest extends GMLDataStoreTestSupport {

	
	public void testGetSchema() throws Exception {
		SimpleFeatureType featureType = dataStore.getSchema( "TestFeature" );
		assertNotNull( featureType );
	
		assertEquals( "http://www.geotools.org/test", featureType.getName().getNamespaceURI().toString() );
		assertTrue( featureType.getAttribute( "geom" ) != null );
		assertTrue( featureType.getAttribute( "count" ) != null );
	}
	
	public void testGetTypeNames() throws Exception {
		String[] typeNames = dataStore.getTypeNames();
		assertEquals( 1, typeNames.length );
		assertEquals( "TestFeature", typeNames[ 0 ] );
	}
	
	public void testGetFeatureSource() throws Exception {
		FeatureSource featureSource = dataStore.getFeatureSource( "TestFeature" );
		assertNotNull( featureSource );
	}
	
	public void testGetFeatures() throws Exception {
		FeatureSource featureSource = dataStore.getFeatureSource( "TestFeature" ); 
		FeatureCollection features = featureSource.getFeatures();
		assertEquals( 3, features.size() );
		
	}
	
	
}
