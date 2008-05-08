package org.geotools.filter;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.expression.Expression;

public class IsNotEqualToImpltest extends TestCase {

	org.opengis.filter.FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory( null );
	
	public void testOperandsSameType() {
		Expression e1 = filterFactory.literal( 1 );
		Expression e2 = filterFactory.literal( 2 );
		
		PropertyIsNotEqualTo notEqual = filterFactory.notEqual( e1, e2, true );
		assertTrue( notEqual.evaluate( null ) );
	}
	
	public void testOperandsDifferentType() {
		Expression e1 = filterFactory.literal( 1 );
		Expression e2 = filterFactory.literal( "2"
				);
		
		PropertyIsNotEqualTo notEqual = filterFactory.notEqual( e1, e2, true );
		assertTrue( notEqual.evaluate( null ) );
	}
	
	public void testCaseSensitivity() {
		Expression e1 = filterFactory.literal( "foo" );
		Expression e2 = filterFactory.literal( "FoO" );
		
		PropertyIsNotEqualTo caseSensitive = filterFactory.notEqual( e1, e2, true );
		assertTrue( caseSensitive. evaluate( null ) );
		
		PropertyIsNotEqualTo caseInsensitive = filterFactory.notEqual( e1, e2, false );
		assertFalse( caseInsensitive. evaluate( null ) );
		
	}
}
