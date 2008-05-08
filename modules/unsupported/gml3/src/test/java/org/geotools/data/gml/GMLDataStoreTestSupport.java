package org.geotools.data.gml;

import org.geotools.gml3.ApplicationSchemaConfiguration;

import junit.framework.TestCase;

public class GMLDataStoreTestSupport extends TestCase {

	GMLDataStore dataStore;
	
	protected void setUp() throws Exception {
		String location = getClass().getResource( "test.xml" ).toString();
		String schemaLocation = getClass().getResource( "test.xsd" ).toString();
		
		dataStore = new GMLDataStore( location, new ApplicationSchemaConfiguration( "http://www.geotools.org/test", schemaLocation ) );
	}
	
}
