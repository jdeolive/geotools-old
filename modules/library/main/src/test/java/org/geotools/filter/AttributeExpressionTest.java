package org.geotools.filter;

import junit.framework.TestCase;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class AttributeExpressionTest extends TestCase {

	public void testFeature() {
		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		
		typeBuilder.setName( "test" );
		typeBuilder.setNamespaceURI( "http://www.geotools.org/test" );
		typeBuilder.add( "foo", Integer.class );
		typeBuilder.add( "bar", Double.class );
		
		SimpleFeatureType type = typeBuilder.buildFeatureType();
		
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
		builder.add( new Integer( 1 ) );
		builder.add( new Double( 2.0 ) );

		SimpleFeature feature = builder.buildFeature( "fid" );
		
		AttributeExpressionImpl ex = new AttributeExpressionImpl( "foo" );
		assertEquals( new Integer( 1 ), ex.evaluate( feature ) );
		
		ex = new AttributeExpressionImpl( "@id" );
		assertEquals( "fid", ex.evaluate( feature ) );
	}
	
	
}
