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
 * @version $Id: MathExpressionImpl.java,v 1.7 2003/07/23 18:13:32 cholmesny Exp $
 */
public class MathExpressionImpl extends DefaultExpression
    implements MathExpression {
    /** Holds the 'left' value of this math expression. */
    private Expression leftValue = null;

    /** Holds the 'right' value of this math expression. */
    private Expression rightValue = null;

    /**
     * No argument constructor.
     */
    protected MathExpressionImpl() {
    }

    /**
     * Constructor with expression type.
     *
     * @param expType The mathematical relationship between values.
     *
     * @throws IllegalFilterException Attempting to declare illegal type.
     */
    protected MathExpressionImpl(short expType) throws IllegalFilterException {
        // Check to see if this is a valid math type before adding.
        if (isMathExpression(expType)) {
            this.expressionType = expType;
        } else {
            throw new IllegalFilterException(
                "Attempted to add non-math expression to math expression.");
        }
    }

    /**
     * Adds the 'left' value to this expression.
     *
     * @param leftValue Expression to add to this expression.
     *
     * @throws IllegalFilterException Attempting to add non-math expression.
     */
    public void addLeftValue(Expression leftValue)
        throws IllegalFilterException {
        // Check to see if this is a valid math expression before adding.
        if (!isGeometryExpression(leftValue.getType())
                && (leftValue.getType() != LITERAL_STRING)) {
            this.leftValue = leftValue;
        } else {
            throw new IllegalFilterException(
                "Attempted to add Geometry or String expression to "
                + "math expression.");
        }
    }

    /**
     * Adds the 'right' value to this expression.
     *
     * @param rightValue Expression to add to this expression.
     *
     * @throws IllegalFilterException Attempting to add non-math expression.
     */
    public void addRightValue(Expression rightValue)
        throws IllegalFilterException {
        // Check to see if this is a valid math expression before adding.
        if (!isGeometryExpression(rightValue.getType())
                && (rightValue.getType() != LITERAL_STRING)) {
            this.rightValue = rightValue;
        } else {
            throw new IllegalFilterException(
                "Attempted to add Geometry or String sub expression to "
                + "math expression.");
        }
    }

    /**
     * Gets the left expression.
     *
     * @return the expression on the left of the comparison.
     */
    public Expression getLeftValue() {
        return leftValue;
    }

    /**
     * Gets the right expression.
     *
     * @return the expression on the right of the comparison.
     */
    public Expression getRightValue() {
        return rightValue;
    }

    /**
     * Gets the type of this expression.
     *
     * @return Expression type.
     */
    public short getType() {
        return expressionType;
    }

    /**
     * Returns the value for this expression.
     *
     * @param feature Feature to use when return sub expression values.
     *
     * @return Value of this expression.
     *
     * @throws IllegalArgumentException Feature does not match declared schema.
     */
    public Object getValue(Feature feature) throws IllegalArgumentException {
        // Checks to make sure both sub expressions exist.
        if ((leftValue == null) || (rightValue == null)) {
            throw new IllegalArgumentException(
                "Attempted read math expression with missing sub expressions.");
        }

        double leftDouble = ((Number) leftValue.getValue(feature))
            .doubleValue();
        double rightDouble = ((Number) rightValue.getValue(feature))
            .doubleValue();

        // Standard return values.
        if (expressionType == MATH_ADD) {
            return new Double(leftDouble + rightDouble);
        } else if (expressionType == MATH_SUBTRACT) {
            return new Double(leftDouble - rightDouble);
        } else if (expressionType == MATH_MULTIPLY) {
            return new Double(leftDouble * rightDouble);
        } else if (expressionType == MATH_DIVIDE) {
            return new Double(leftDouble / rightDouble);
        } else {
            // If the type has somehow been mis-set (can't happen externally)
            // then throw an exception.
            throw new IllegalArgumentException(
                "Attempted read math expression with invalid type "
                + "(ie. Add, Subtract, etc.).");
        }
    }

    /**
     * Returns a string representation of this expression
     *
     * @return String representation of the math expression.
     */
    public String toString() {
        String operation;

        switch (expressionType) {
        case MATH_ADD:
            operation = " + ";

            break;

        case MATH_SUBTRACT:
            operation = " - ";

            break;

        case MATH_MULTIPLY:
            operation = " * ";

            break;

        case MATH_DIVIDE:
            operation = " / ";

            break;

        default:
            operation = " ? "; //should never happen.

            break;
        }

        return "(" + leftValue.toString() + operation + rightValue.toString()
        + ")";
    }

    /**
     * Compares this expression to the specified object.  Returns true  if the
     * passed in object is the same as this expression.  Checks  to make sure
     * the expression types as well as the left and right values are equal.
     *
     * @param obj - the object to compare this ExpressionLiteral against.
     *
     * @return true if specified object is equal to this expression; false
     *         otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof MathExpressionImpl) {
            MathExpression expMath = (MathExpression) obj;

            return ((expMath.getType() == this.expressionType)
            && expMath.getLeftValue().equals(this.leftValue)
            && expMath.getRightValue().equals(this.rightValue));
        } else {
            return false;
        }
    }

    /**
     * Override of hashCode method.
     *
     * @return a hash code value for this math expression.
     */
    public int hashCode() {
        int result = 23;
        result = (37 * result) + expressionType;
        result = (37 * result) + leftValue.hashCode();
        result = (37 * result) + rightValue.hashCode();

        return result;
    }

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
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }
}
