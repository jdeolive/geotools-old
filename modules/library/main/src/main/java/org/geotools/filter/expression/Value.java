/*
 *    GeoTools - The Open Source Java GIS Toolkit
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

import org.geotools.util.Converters;

/**
 * Placeholder value for the result of an expression.
 * <p>
 * This class holds onto the value of an expression in one form, and converts it on demand
 * into 
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class Value {

	/**
	 * The "raw" value.
	 */
	Object value;
	
	/**
	 * Creates the placeholder.
	 * 
	 * @param value The raw value of the placeholder.
	 */
	public Value( Object value ) {
		this.value = value;
	}
	
	/**
	 * @return The raw value.
	 */
	public Object getValue() {
		return value;
	}
	
	/**
	 * Sets the raw value.
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * Returns the "raw" value converted into an instance of <param>context</param>.
	 * 
	 * @param context The type of the object to be converted to.
	 * 
	 * @return The converted object ( an instance of <param>context</param>, or <code>null</code> ).
	 */
	public Object value( Class context ) {
            if( value == null || context.isAssignableFrom(value.getClass()) ) return value;
            
            return Converters.convert( value, context );
	}

	public String toString() {
	    return String.valueOf( value );
	}
}
