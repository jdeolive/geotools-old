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
 * Implements a default expression, with helpful variables and static methods.
 *
 * @author Rob Hranac, Vision for New York
 * @version $Id: DefaultExpression.java,v 1.7 2004/04/04 16:01:21 aaime Exp $
 */
public abstract class DefaultExpression implements Expression {
    
    /** Defines the type of this expression. */
    protected short expressionType;

    /** Defines the type of this expression. */
    protected boolean permissiveConstruction;

    /**
     * Gets the type of this expression.
     *
     * @return The short representation of the expression type.
     */
    public short getType() {
        return expressionType;
    }

    /**
     * Returns a value for this expression.  If the expression is an attribute
     * expression then the attribute of the feature should be returned.  If a
     * literal then the feature is ignored, the literal is returned as it has
     * no relation to the feature.
     *
     * @param feature Specified feature to use when returning value.
     *
     * @return The value of this expression based on the feature.
     *
     * @task REVISIT: make abstract?
     */
    public Object getValue(Feature feature) {
        return new Object();
    }

    /* ***********************************************************************
     * Following static methods check for certain aggregate types, based on
     * (above) declared types.  Note that these aggregate types do not
     * necessarily map directly to the sub-classes of FilterDefault.  In most,
     * but not all, cases, a single class implements an aggregate type.
     * However, there are aggregate types that are implemented by multiple
     * classes (ie. the Math type is implemented by two separate classes).
     ************************************************************************/

    /**
     * Checks to see if passed type is attribute.
     *
     * @param expressionType Type of expression for check.
     *
     * @return Whether or not this is an attribute expression type.
     */
    protected static boolean isAttributeExpression(short expressionType) {
        return ((expressionType == ATTRIBUTE_DOUBLE)
        || (expressionType == ATTRIBUTE_INTEGER)
        || (expressionType == ATTRIBUTE_STRING));
    }

    /**
     * Checks to see if passed type is math.
     *
     * @param expressionType Type of expression for check.
     *
     * @return Whether or not this is a math expression type.
     */
    protected static boolean isMathExpression(short expressionType) {
        return ((expressionType == MATH_ADD)
        || (expressionType == MATH_SUBTRACT)
        || (expressionType == MATH_MULTIPLY) || (expressionType == MATH_DIVIDE));
    }

    /**
     * Checks to see if passed type is geometry.
     *
     * @param expressionType Type of expression for check.
     *
     * @return Whether or not this is a geometry expression type.
     */
    protected static boolean isLiteralExpression(short expressionType) {
        return ((expressionType == LITERAL_GEOMETRY)
        || (expressionType == LITERAL_DOUBLE)
        || (expressionType == LITERAL_INTEGER)
        || (expressionType == LITERAL_STRING));
    }

    /**
     * Checks to see if passed type is geometry.
     *
     * @param expressionType Type of expression for check.
     *
     * @return Whether or not this is a geometry expression type.
     */
    protected static boolean isGeometryExpression(short expressionType) {
        return ((expressionType == ATTRIBUTE_GEOMETRY)
        || (expressionType == LITERAL_GEOMETRY));
    }

    /**
     * Checks to see if passed type is geometry.
     *
     * @param expressionType Type of expression for check.
     *
     * @return Whether or not this is a geometry expression type.
     */
    protected static boolean isExpression(short expressionType) {
        return (isMathExpression(expressionType)
        || isAttributeExpression(expressionType)
        || isLiteralExpression(expressionType));
    }
}
