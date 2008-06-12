/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.filter;

import org.geotools.filter.expression.Value;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.expression.Expression;

/**
 * Straight implementation of GeoAPI interface.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 */
public class IsBetweenImpl extends CompareFilterImpl implements BetweenFilter {

	private Expression expression;

	protected IsBetweenImpl(FilterFactory factory, Expression lower, Expression expression, Expression upper ){
		super( factory, lower, upper );
		this.expression = expression;
		
		//backwards compatability
		filterType = FilterType.BETWEEN;
	}
	
	public Expression getExpression() {
		return expression;
	}
	public void setExpression(Expression expression) {
		this.expression = expression;
	}
	
	//@Override
	public boolean evaluate(Object feature) {
		Value value = new Value( eval( expression, feature ) );
		if ( value.getValue() == null ) {
			return false;
		}
		
		//get the boundaries
		Value lower = new Value( eval( getExpression1(), feature ) );
		Value upper = new Value( eval( getExpression2(), feature ) );
		
		//first try to evaluate the bounds in terms of the middle
		Object o = value.getValue();
		Object l = lower.value( o.getClass() );
		Object u = upper.value( o.getClass() );
		if ( l == null || u == null ) {
			//that didn't work try converting all to same type as lower
			l = lower.getValue();
			o = value.value( l.getClass() );
			u = upper.value( l.getClass() );
			
			if ( o == null || u == null ) {
				//ok last try, try evaluating all in terms of upper
				u = upper.getValue();
				o = value.value( u.getClass() );
				l = lower.value( u.getClass() );
				
				if ( o == null || l == null ) {
					//no dice
					return false;
				}
			}
		}
		
		Comparable lc = comparable( l );
		Comparable uc = comparable( u );
		
		return lc.compareTo( o ) <= 0 && uc.compareTo( o ) >= 0;
	}

	public Object accept(FilterVisitor visitor, Object extraData) {
		return visitor.visit( this, extraData );
	}

	public Expression getLowerBoundary() {
		return getExpression1();
	}

	public void setLowerBoundary(Expression lowerBoundary) {
		setExpression1( lowerBoundary );
	}

	public Expression getUpperBoundary() {
		return getExpression2();
	}

	public void setUpperBoundary(Expression upperBoundary) {
		setExpression2( upperBoundary );
	}
	
	/**
	 * @deprecated use {@link #getExpression()}
	 */
	public final org.geotools.filter.Expression getMiddleValue() {
		return (org.geotools.filter.Expression) getExpression();
	}
	
	/**
	 * @deprecated use {@link #setExpression(Expression) }
	 */
	public void addMiddleValue(org.geotools.filter.Expression middleValue) {
		setExpression( middleValue );
	}
    
    public String toString() {
        return "[ " + expression + " BETWEEN " + expression1 + " AND " + expression2 + " ]";
    }
	
}
