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
 * Holds a mathematical relationship between two expressions. Note that the sub
 * expressions must be math expressions.  In other words, they must be a math
 * literal, another math expression, or a feature attribute with a declared
 * math type.  You may create math expressions of arbitrary complexity by
 * nesting other math expressions as sub expressions in one or more math
 * expressions. This filter defines left and right values to clarify the sub
 * expression precedence for non-associative operations, such as subtraction
 * and division. For example, the left value is the numerator and the right is
 * the denominator in an ExpressionMath division operation.
 *
 * @author Rob Hranac, Vision for New York
 * @version $Id: MathExpression.java,v 1.2 2003/08/07 21:30:36 cholmesny Exp $
 */
public interface MathExpression extends Expression {
    /**
     * Returns the value for this expression.
     *
     * @param feature Feature to use when return sub expression values.
     *
     * @return Value of this expression.
     */
    Object getValue(Feature feature);

    /**
     * Adds the 'right' value to this expression.
     *
     * @param rightValue Expression to add to this expression.
     *
     * @throws IllegalFilterException Attempting to add non-math expression.
     */
    void addRightValue(Expression rightValue) throws IllegalFilterException;

    /**
     * Gets the type of this expression.
     *
     * @return Expression type.
     */
    short getType();

    /**
     * Gets the left expression.
     *
     * @return the expression on the left of the comparison.
     */
    Expression getLeftValue();

    /**
     * Gets the right expression.
     *
     * @return the expression on the right of the comparison.
     */
    Expression getRightValue();

    /**
     * Adds the 'left' value to this expression.
     *
     * @param leftValue Expression to add to this expression.
     *
     * @throws IllegalFilterException Attempting to add non-math expression.
     */
    void addLeftValue(Expression leftValue) throws IllegalFilterException;
}
