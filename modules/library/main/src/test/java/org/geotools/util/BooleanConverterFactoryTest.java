package org.geotools.util;

import junit.framework.TestCase;

public class BooleanConverterFactoryTest extends TestCase {

	BooleanConverterFactory factory;
	
	protected void setUp() throws Exception {
		factory = new BooleanConverterFactory();
	}
	
	public void testFromString() throws Exception {
		assertEquals( Boolean.TRUE, convert( "true" ) );
		assertEquals( Boolean.TRUE, convert( "1" ) );
		assertEquals( Boolean.FALSE, convert( "false" ) );
		assertEquals( Boolean.FALSE, convert( "0" ) );
		
	}
	
	public void testFromInteger() throws Exception {
		assertEquals( Boolean.TRUE, convert( new Integer( 1 ) ) );
		assertEquals( Boolean.FALSE, convert( new Integer( 0 ) ) );
	}
	
	Boolean convert( Object value ) throws Exception {
		return (Boolean) factory.createConverter( value.getClass(), Boolean.class, null ).convert( value, Boolean.class );
	}
}
