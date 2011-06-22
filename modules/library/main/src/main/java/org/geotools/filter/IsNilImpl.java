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

import org.geotools.util.Converters;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.PropertyIsNil;
import org.opengis.filter.expression.Expression;

public class IsNilImpl extends CompareFilterImpl implements PropertyIsNil {

    Object nilReason;

    public IsNilImpl(FilterFactory factory, Expression e1, Object nilReason) {
        super(factory, e1, null);
        this.nilReason = nilReason;
    }

    public boolean evaluate(Object object) {
        Expression expr = getExpression();
        Object value = eval(expr, object);

        if (nilReason == null) {
            return value == null;
        }

        return nilReason.equals(Converters.convert(value, nilReason.getClass()));
    }

    public Expression getExpression() {
        return getExpression1();
    }

    public Object getNilReason() {
        return nilReason;
    }

    @Override
    public Object accept(FilterVisitor visitor, Object extraData) {
        return visitor.visit(this, extraData);
    }

}
