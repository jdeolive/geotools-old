package org.geotools.data.gml;

import org.geotools.gml3.ApplicationSchemaConfiguration;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class GMLFeatureCollectionTest extends GMLDataStoreTestSupport {

	public void testSize() throws Exception {
		GMLFeatureCollection collection = new GMLFeatureCollection( 
			(GMLTypeEntry) dataStore.entry( "TestFeature" )
		);
		assertEquals( 3, collection.size() );
	}
	
	public void testManualBounds() throws Exception {
		GMLFeatureCollection collection = new GMLFeatureCollection( 
			(GMLTypeEntry) dataStore.entry( "TestFeature" )
		);
		Envelope bounds = collection.getBounds();
		assertNotNull( bounds );
		assertEquals( bounds, new Envelope( new Coordinate( 0, 0 ), new Coordinate( 2, 2 ) ) );
	}
	
	public void testOptimizedBounds() throws Exception {
		String location = getClass().getResource( "test-withBounds.xml" ).toString();
		String schemaLocation = getClass().getResource( "test.xsd" ).toString();
		
		GMLDataStore dataStore = 
			dataStore = new GMLDataStore( location, new ApplicationSchemaConfiguration( "http://www.geotools.org/test", schemaLocation ) );
		
		GMLFeatureCollection collection = 
			new GMLFeatureCollection( (GMLTypeEntry) dataStore.entry( "TestFeature" ) ); 
			
		Envelope bounds = collection.getBounds();
		assertNotNull( bounds );
		assertEquals( bounds, new Envelope( new Coordinate( 0, 0 ), new Coordinate( 3, 3 ) ) );
	}
	
	
}
