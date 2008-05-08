package org.geotools.feature.type;

import junit.framework.TestCase;

import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultFeatureType;

import com.vividsolutions.jts.geom.Point;

public class DefaultFeatureTypeBuilderTest extends TestCase {

	static final String URI = "gopher://localhost/test";
	
	DefaultFeatureTypeBuilder builder;
	
	protected void setUp() throws Exception {
		builder = new DefaultFeatureTypeBuilder();
	}
	
	public void testSanity() {
		builder.setName( "testName" );
		builder.setNamespaceURI( "testNamespaceURI" );
		builder.add( "point", Point.class );
		builder.add( "integer", Integer.class );
		
		DefaultFeatureType type = (DefaultFeatureType) builder.buildFeatureType();
		assertNotNull( type );
		
		assertEquals( 2, type.getAttributeCount() );
		
		AttributeType t = type.getAttributeType( "point" );
		assertNotNull( t );
		assertEquals( Point.class, t.getBinding() );
		
		t = type.getAttributeType( "integer" );
		assertNotNull( t );
		assertEquals( Integer.class, t.getBinding() );
		
		t = type.getDefaultGeometry();
		assertNotNull( t );
		assertEquals( Point.class, t.getBinding() );
	}
}
