/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 */

package org.geotools.filter;

import org.geotools.data.*;
import org.geotools.feature.*;

/**
 * Holds a mathematical relationship between two expressions.
 *
 * Note that the sub expressions must be math expressions.  In other words,
 * they must be a math literal, another math expression, or a feature attribute
 * with a declared math type.  You may create math expressions of arbitrary
 * complexity by nesting other math expressions as sub expressions in one or
 * more math expressions.
 *
 * This filter defines left and right values to clarify the sub expression
 * precedence for non-associative operations, such as subtraction and division.
 * For example, the left value is the numerator and the right is the
 * denominator in an ExpressionMath division operation.
 *
 * @version $Id: ExpressionMath.java,v 1.3 2002/07/12 12:40:56 loxnard Exp $
 * @author Rob Hranac, Vision for New York
 */
public class ExpressionMath extends ExpressionDefault {

    /** Holds the 'left' value of this math expression. */
    protected Expression leftValue = null;

    /** Holds the 'right' value of this math expression. */
    protected Expression rightValue = null;


    /**
     * Constructor with expression type.
     *
     * @param expressionType The mathematical relationship between values.
     * @throws IllegalFilterException Attempting to declare illegal type.
     */
    public ExpressionMath (short expressionType)
        throws IllegalFilterException {

        // Check to see if this is a valid math type before adding.
        if (isMathExpression(expressionType)) {
            this.expressionType = expressionType;
        }
        else {
            throw new IllegalFilterException("Attempted to add non-math expression to math expression.");
        }
    }
    /**
     * Adds the 'left' value to this expression.
     *
     * @param leftValue Expression to add to this expression.
     * @throws IllegalFilterException Attempting to add non-math expression.
     */
    public void addLeftValue(Expression leftValue)
        throws IllegalFilterException {
        
        // Check to see if this is a valid math expression before adding.
        if (!isGeometryExpression(leftValue.getType()) && leftValue.getType() != LITERAL_STRING) {
            this.leftValue = leftValue;
        }
        else {
            throw new IllegalFilterException("Attempted to add Geometry or String expression to math expression.");
        }
    }

    /**
     * Adds the 'right' value to this expression.
     *
     * @param rightValue Expression to add to this expression.
     * @throws IllegalFilterException Attempting to add non-math expression.
     */
    public void addRightValue(Expression rightValue)
        throws IllegalFilterException {
        
        // Check to see if this is a valid math expression before adding.
        if (!isGeometryExpression(rightValue.getType()) && leftValue.getType() != LITERAL_STRING) {
            this.rightValue = rightValue;
        }
        else {
            throw new IllegalFilterException("Attempted to add Geometry or String sub expression to math expression.");
        }
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
     * @return Value of this expression.
     * @throws IllegalArgumentException Feature does not match declared schema.
     */
    public Object getValue(Feature feature)
        throws IllegalArgumentException {
        
        // Checks to make sure both sub expressions exist.
        if (leftValue == null || rightValue == null) {
            throw new IllegalArgumentException("Attempted read math expression with missing sub expressions.");
        }

        double leftValueDouble = ((Number) leftValue.getValue(feature)).doubleValue();
        double rightValueDouble = ((Number) rightValue.getValue(feature)).doubleValue();
        // Standard return values.
        if (expressionType == MATH_ADD) {
            return new Double(leftValueDouble + rightValueDouble);
        }
        else if (expressionType == MATH_SUBTRACT) {
            return new Double(leftValueDouble - rightValueDouble);
        }
        else if (expressionType == MATH_MULTIPLY) {
            return new Double(leftValueDouble * rightValueDouble);
        }
        else if (expressionType == MATH_DIVIDE) {
            return new Double(leftValueDouble / rightValueDouble);
        }

        // If the type has somehow been mis-set (can't happen externally)
        // then throw an exception.
        else {
            throw new IllegalArgumentException("Attempted read math expression with invalid type (ie. Add, Subtract, etc.).");
        }
    
    }
        
    
}
