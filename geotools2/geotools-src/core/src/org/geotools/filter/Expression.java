/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.filter;

import org.geotools.feature.Feature;


/**
 * Defines an expression, the units that make up Filters.   This filter holds
 * one or more filters together and relates them logically in an internally
 * defined manner.
 *
 * @author Rob Hranac, Vision for New York
 * @version $Id: Expression.java,v 1.10 2004/04/04 16:01:21 aaime Exp $
 */
public interface Expression extends ExpressionType {
    /**
     * Gets the type of this expression.
     *
     * @return Expression type.
     */
    short getType();

    /**
     * Returns a value for this expression.  The feature argument is used if a
     * feature is needed to evaluate the expression, as in the case of an
     * AttributeExpression.
     *
     * @param feature Specified feature to use when returning value.   Some
     *        expressions, such as LiteralExpressions, may ignore this as it
     *        does not affect their return value.
     *
     * @return Value of the expression, evaluated with the feature object if
     *         necessary.
     */
    Object getValue(Feature feature);

    /**
     * Used by FilterVisitors to perform some action on this filter instance.
     * Typicaly used by Filter decoders, but may also be used by any thing
     * which needs infomration from filter structure. Implementations should
     * always call: visitor.visit(this); It is importatant that this is not
     * left to a parent class unless the parents API is identical.
     *
     * @param visitor The visitor which requires access to this filter, the
     *        method must call visitor.visit(this);
     */
    void accept(FilterVisitor visitor);
}
