/*
 * This code is released under the Apache license, availible at the root GML4j directory.
 */
package org.geotools.filter;

import com.vividsolutions.jts.geom.*;
import org.geotools.data.*;
import org.geotools.feature.*;
/**
 * Defines a like filter, which checks to see if an attribute matches a REGEXP.
 *
 * @author Rob Hranac, Vision for New York
 * @version 
 */
public class FactoryExpression {

    /** The attribute value, which must be an attribute expression. */
    private FactoryExpression expressionFactory = null;

    /** The (limited) REGEXP pattern. */
    private Expression currentExpression = null;

    /** The (limited) REGEXP pattern. */
    private String currentState;

    /** The (limited) REGEXP pattern. */
    private String declaredType;

    /** The (limited) REGEXP pattern. */
    private boolean readyFlag;

    /**
     * Constructor which flags the operator as between.
     */
    public FactoryExpression () {
    }


    /**
     * Constructor which flags the operator as between.
     */
    public void start(String declaredType) throws IllegalFilterException{

        this.declaredType = declaredType;
        
        // if the expression is math, then create a factory for its
        //  sub expressions, otherwise just instantiate the main expression
        if( ExpressionDefault.isMathExpression( convertType(declaredType) ) ) {
            expressionFactory = new FactoryExpression();
            currentExpression = new ExpressionMath(convertType(declaredType));
        }
        else if( ExpressionDefault.
                 isLiteralExpression( convertType(declaredType) ) ) {
            currentExpression = new ExpressionLiteral();
        }
        else if( ExpressionDefault.isAttributeExpression( convertType(declaredType) ) ) {
            currentExpression = new ExpressionAttribute();
        }
        currentState = setInitialState(currentExpression);

    }


    /**
     * Sets the matching pattern for this FilterLike.
     *
     * @param pattern The limited REGEXP pattern for this string. 
     */
    public void end(String message) throws IllegalFilterException{

        // first, check to see if there are internal (nested) expressions
        //  note that this is identical to checking if the currentExpression
        //  is a math expression
        // if this internal expression exists, send its factory an end message
        if( expressionFactory != null ) {
            expressionFactory.end(message);

            // if the factory is ready to be returned:
            //  (1) add its expression to the current expression, as determined
            //      by the current state
            //  (2) increment the current state
            //  (3) set the factory to null to indicate that it is now done
            // if in a bad state, throw exception
            if( expressionFactory.isReady() ) {
                if( currentState.equals("leftValue") ) {
                    ((ExpressionMath) currentExpression).
                        addLeftValue(expressionFactory.create());
                    currentState = "rightValue";
                    expressionFactory = null;
                }
                else if( currentState.equals("rightValue") ) {
                    ((ExpressionMath) currentExpression).
                        addRightValue(expressionFactory.create());
                    currentState = "complete";
                    expressionFactory = null;
                }
                else {
                    throw new IllegalFilterException
                        ("Attempted to add sub expression in a bad state: "
                         + currentState);
                }
            }
        }

        // if there are no nested expressions here,
        //  determine if this expression is ready and set flag appropriately
        else if( declaredType.equals(message) && currentState.equals("complete") ){
            readyFlag = true;
        }

        // otherwise, throw exception
        else {
            throw new IllegalFilterException
                ("Reached end of unready, non-nested expression: "
                 + currentState);
        }

    }

    /**
     * Checks to see if this expression is ready to be returned.
     *
     */
    public boolean isReady() {
        return readyFlag;
    }

    /**
     * Sets the matching pattern for this FilterLike.
     *
     * @param pattern The limited REGEXP pattern for this string. 
     */
    public void message(String message) throws IllegalFilterException{

        // TODO 2:
        // AT SOME POINT MUST MAKE THIS HANDLE A TYPED FEATURE
        // BY PASSING IT A FEATURE AND CHECKING ITS TYPE HERE

        // If an attribute path, set it.  Assumes undeclared type.
        if( currentExpression instanceof ExpressionAttribute ) {
            ((ExpressionAttribute) currentExpression).setAttributePath(message);
            currentState = "complete";
        }

        // This is a relatively loose assignment routine, which uses
        //  the fact that the three allowed literal types have a strict
        //  instatiation hierarchy (ie. double can be an int can be a 
        //  string, but not the other way around).
        // A better routine would consider the use of this expression
        //  (ie. will it be compared to a double or searched with a
        //  like filter?)
        else if( currentExpression instanceof ExpressionLiteral ) {
            try {
                Object tempLiteral = new Double(message);
                ((ExpressionLiteral) currentExpression).setLiteral(tempLiteral);
                currentState = "complete";
            }
            catch(NumberFormatException e1) {
                try {
                    Object tempLiteral = new Integer(message);
                    ((ExpressionLiteral) currentExpression).
                        setLiteral(tempLiteral);
                    currentState = "complete";
                }
                catch(NumberFormatException e2) {
                    Object tempLiteral = message;
                    ((ExpressionLiteral) currentExpression).
                        setLiteral(tempLiteral);
                    currentState = "complete";
                }                
            }
        }
    }

    /**
     * Get geometry.
     *
     * @param geometry The geometry from the filter.
     */
    public void geometry(Geometry geometry) throws IllegalFilterException{

        // Sets the geometry for the expression, as appropriate
        if( currentExpression.getType() == ExpressionDefault.LITERAL_GEOMETRY ) {
            ((ExpressionLiteral) currentExpression).setLiteral(geometry);
            currentState = "complete";
        }
    }

    /**
     * Sets the multi wildcard for this FilterLike.
     *
     */
    public Expression create() {
        Expression tempExpression = currentExpression;
        currentExpression = null;
        return tempExpression;
    }


    /**
     * Sets the multi wildcard for this FilterLike.
     *
     */
    private static String setInitialState(Expression currentExpression) 
        throws IllegalFilterException {

        if( currentExpression instanceof ExpressionMath ) {
            return "leftValue";
        }
        else if( ( currentExpression instanceof ExpressionAttribute ) ||
                 ( currentExpression instanceof ExpressionLiteral ) ) {
            return "";
        }
        else {
            throw new IllegalFilterException
                ("Created illegal expression: " + 
                 currentExpression.getClass().toString());
        }

    }

    /**
     * Checks to see if passed type is logic.
     *
     * @param filterType Type of filter for check.
     * @return Whether or not this is a logic filter type.
     */
    protected static short convertType(String expressionType) {

        // matches all filter types to the default logic type
        if( expressionType.equals("Add") ) {
            return ExpressionDefault.MATH_ADD;
        }
        else if( expressionType.equals("Sub") ) {
            return ExpressionDefault.MATH_SUBTRACT;
        }
        else if( expressionType.equals("Mul") ) {
            return ExpressionDefault.MATH_MULTIPLY;
        }
        else if( expressionType.equals("Div") ) {
            return ExpressionDefault.MATH_DIVIDE;
        }
        else if( expressionType.equals("PropertyName") ) {
            return ExpressionDefault.LITERAL_DOUBLE;
        }
        else if( expressionType.equals("Literal") ) {
            return ExpressionDefault.ATTRIBUTE_DOUBLE;
        }
        return ExpressionDefault.ATTRIBUTE_UNDECLARED;

    }

    
}
