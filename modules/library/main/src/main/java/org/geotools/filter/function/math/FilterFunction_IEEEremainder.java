package org.geotools.filter.function.math;

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

//this code is autogenerated - you shouldnt be modifying it!
import static org.geotools.filter.capability.FunctionNameImpl.parameter;

import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.opengis.filter.capability.FunctionName;

public class FilterFunction_IEEEremainder extends FunctionExpressionImpl {

    //public static FunctionName NAME = new FunctionNameImpl("IEEEremainder","dividend","divisor");
    public static FunctionName NAME = new FunctionNameImpl("IEEEremainder",
            parameter("remainder", Double.class),
            parameter("dividend",Number.class),
            parameter("divisor",Number.class));

    public FilterFunction_IEEEremainder() {
        super("IEEEremainder");
        functionName = NAME;
    }

    public int getArgCount() {
        return 2;
    }

    public Object evaluate(Object feature) {
        double arg0;
        double arg1;

        try { // attempt to get value and perform conversion
            arg0 = ((Number) getExpression(0).evaluate(feature)).doubleValue();
        } catch (Exception e) {
            // probably a type error
            throw new IllegalArgumentException(
                    "Filter Function problem for function IEEEremainder argument #0 - expected type double");
        }

        try { // attempt to get value and perform conversion
            arg1 = ((Number) getExpression(1).evaluate(feature)).doubleValue();
        } catch (Exception e) {
            // probably a type error
            throw new IllegalArgumentException(
                    "Filter Function problem for function IEEEremainder argument #1 - expected type double");
        }

        return new Double(Math.IEEEremainder(arg0, arg1));
    }
}
