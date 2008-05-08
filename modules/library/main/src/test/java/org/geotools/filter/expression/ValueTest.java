package org.geotools.filter.expression;

import junit.framework.TestCase;

public class ValueTest extends TestCase {

	Value value;
	
	protected void setUp() throws Exception {
		value = new Value( "1" );
	}
	
	public void testGetValue() {
		assertEquals( "1", value.getValue() );
	}
	
	public void testSetValue() {
		value.setValue( "2" );
		assertEquals( "2", value.getValue() );
	}
	
	public void testValue() {
		Object i = value.value( Integer.class );
		assertTrue( i instanceof Integer );
		assertEquals( new Integer(1), i );
	}
}
