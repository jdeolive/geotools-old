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
 * Implements a default expression, with helpful variables and static methods.
 *
 * @version $Id: ExpressionDefault.java,v 1.6 2002/06/20 22:42:00 jmacgill Exp $
 * @author Rob Hranac, Vision for New York
 */
public class ExpressionDefault implements Expression {


    /** Defines the type of this expression. */
    protected short expressionType;

    /** Defines the type of this expression. */
    protected boolean permissiveConstruction;

    /* This is a listing of all possible expression types, grouped by 
       expressions that are implemented by a single expression class
       (ie. all math types are implemented by ExpressionMath). */

    /* Types implemented by ExpressionLiteral */
    /** Defines a literal expression with a declared double type. */  
    public static final short LITERAL_DOUBLE = 101;
    /** Defines a literal expression with a declared integer type. */  
    public static final short LITERAL_INTEGER = 102;
    /** Defines a literal expression with a declared string type. */  
    public static final short LITERAL_STRING = 103;
    /** Defines a literal expression with a declared string type. */  
    public static final short LITERAL_GEOMETRY = 104;

    /* Types implemented by ExpressionMath. */
    /** Defines a math expression for adding. */  
    public static final short MATH_ADD = 105;
    /** Defines a math expression for subtracting. */  
    public static final short MATH_SUBTRACT = 106;
    /** Defines a math expression for multiplying. */  
    public static final short MATH_MULTIPLY = 107;
    /** Defines a math expression for dividing. */  
    public static final short MATH_DIVIDE = 108;
    
    /* Types implemented by ExpressionAttribute. */
    /** Defines an attribute expression with a declared double type. */  
    public static final short ATTRIBUTE_DOUBLE = 109;
    /** Defines an attribute expression with a declared integer type. */  
    public static final short ATTRIBUTE_INTEGER = 110;
    /** Defines an attribute expression with a declared string type. */  
    public static final short ATTRIBUTE_STRING = 111;
    /** Defines an attribute expression with a declared string type. */  
    public static final short ATTRIBUTE_GEOMETRY = 112;
    /** Defines an attribute expression with a declared string type. */  
    public static final short ATTRIBUTE_UNDECLARED = 100;

    /**
     * Gets the type of this expression.
     */
    public short getType() {
        return expressionType;
    }

    /**
     * Returns a value for this expression.
     *
     * @param feature Specified feature to use when returning value.
     */
    public Object getValue(Feature feature)
        throws MalformedFilterException {
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
     * @return Whether or not this is an attribute expression type.
     */
    protected static boolean isAttributeExpression(short expressionType) {
        if( (expressionType == ATTRIBUTE_DOUBLE) ||
            (expressionType == ATTRIBUTE_INTEGER) ||
            (expressionType == ATTRIBUTE_STRING) ) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Checks to see if passed type is math. Modified by Ian to consider
     * Integers and Doubles to be acceptable in MathExpressions.
     *
     * @param expressionType Type of expression for check.
     * @return Whether or not this is a math expression type.
     */
    protected static boolean isMathExpression(short expressionType) {
        if( (expressionType == MATH_ADD) ||
            (expressionType == MATH_SUBTRACT) ||
            (expressionType == MATH_MULTIPLY) ||
            (expressionType == MATH_DIVIDE)  ||
            (expressionType == LITERAL_DOUBLE) ||
            (expressionType == LITERAL_INTEGER) ) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Checks to see if passed type is geometry.
     *
     * @param expressionType Type of expression for check.
     * @return Whether or not this is a geometry expression type.
     */
    protected static boolean isLiteralExpression(short expressionType) {
        if( (expressionType == LITERAL_GEOMETRY) ||
            (expressionType == LITERAL_DOUBLE) ||
            (expressionType == LITERAL_INTEGER) ||
            (expressionType == LITERAL_STRING) ) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Checks to see if passed type is geometry.
     *
     * @param expressionType Type of expression for check.
     * @return Whether or not this is a geometry expression type.
     */
    protected static boolean isGeometryExpression(short expressionType) {
        if( (expressionType == ATTRIBUTE_GEOMETRY) ||
            (expressionType == LITERAL_GEOMETRY) ) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Checks to see if passed type is geometry.
     *
     * @param expressionType Type of expression for check.
     * @return Whether or not this is a geometry expression type.
     */
    protected static boolean isExpression(short expressionType) {
        if( isMathExpression(expressionType) ||
            isAttributeExpression(expressionType) ||
            isLiteralExpression(expressionType) ) {
            return true;
        }
        else {
            return false;
        }
    }

    
}
