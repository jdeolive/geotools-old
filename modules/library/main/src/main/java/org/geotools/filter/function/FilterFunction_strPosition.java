/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.filter.function;

import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.opengis.filter.capability.FunctionName;

/**
 * Filter function implementing the Symbology Encoding "StringPosition" function.
 * <p>
 * This function takes three arguments:
 * <ol>
 *   <li>The look up string</li>
 *   <li>The source string, ie the string to lookup from</li>
 *   <li>The search direction, frontToBack or backToFront
 * </ol>
 * </p>
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class FilterFunction_strPosition extends FunctionExpressionImpl {

    public static FunctionName NAME = new FunctionNameImpl("strPosition","string","lookup","forward/backToFront");

    public FilterFunction_strPosition() {
        super("strPosition");
        functionName = NAME;
    }

    @Override
    public int getArgCount() {
        return 3;
    }
    
    @Override
    public Object evaluate(Object object) {
        String lookup = getExpression(0).evaluate(object, String.class);
        String string = getExpression(1).evaluate(object, String.class);
        String dir = getExpression(2).evaluate(object, String.class);

        if ("backToFront".equalsIgnoreCase(dir)) {
            return new StringBuffer(string).lastIndexOf(lookup) + 1;
        }
        else {
            return string.indexOf(lookup) + 1;
        }
    }
}
