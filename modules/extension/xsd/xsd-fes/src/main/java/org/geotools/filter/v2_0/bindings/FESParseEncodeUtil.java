/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2011, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.filter.v2_0.bindings;

import org.geotools.xml.Node;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;

/**
 * Utility class for FES bindings.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class FESParseEncodeUtil {

    /**
     * Parses the two operands for a binary temporal filter.
     */
    static Expression[] temporal(Node node, FilterFactory factory) {
        PropertyName name = (PropertyName) node.getChildValue(PropertyName.class);
        Object other = null;
        for (Object o : node.getChildValues(Object.class)) {
            if (o == name) {
                continue;
            }
            
            other = o;
            break;
        }
        
        if (other == null) {
           throw new IllegalArgumentException("Temporal filter did not specify two operands");
        }
        
        Expression expr = null;
        if (other instanceof Expression) {
            expr = (Expression) other;
        }
        else {
            expr = factory.literal(other);
        }
        
        return new Expression[]{name, expr};
    }
}
