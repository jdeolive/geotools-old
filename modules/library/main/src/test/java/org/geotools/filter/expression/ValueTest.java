/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
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
