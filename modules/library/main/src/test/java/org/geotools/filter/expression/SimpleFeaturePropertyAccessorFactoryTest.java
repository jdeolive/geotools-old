package org.geotools.filter.expression;

import java.util.Map;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import junit.framework.TestCase;

public class SimpleFeaturePropertyAccessorFactoryTest extends TestCase {

	SimpleFeaturePropertyAccessorFactory factory;
	
	protected void setUp() throws Exception {
		factory = new SimpleFeaturePropertyAccessorFactory();
	}
	
	public void test() {
		
		//make sure features are supported
		assertNotNull( factory.createPropertyAccessor( SimpleFeature.class, "xpath", null, null ) );
		assertNotNull( factory.createPropertyAccessor( SimpleFeatureType.class, "xpath", null, null ) );
		assertNull( factory.createPropertyAccessor( Map.class , "xpath", null, null ) );
		
		//make sure only simple xpath
		assertNull( factory.createPropertyAccessor( SimpleFeature.class, "@xpath", null, null )  );
		assertNull( factory.createPropertyAccessor( SimpleFeatureType.class, "@xpath", null, null )  );
		
		assertNull( factory.createPropertyAccessor( SimpleFeature.class, "/xpath", null, null ) );
		assertNull( factory.createPropertyAccessor( SimpleFeatureType.class, "/xpath", null, null ) );
		
		assertNull( factory.createPropertyAccessor( SimpleFeature.class, "*[0]", null, null ) );
		assertNull( factory.createPropertyAccessor( SimpleFeatureType.class, "*[0]", null, null ) );
	}
	
	
	
}
