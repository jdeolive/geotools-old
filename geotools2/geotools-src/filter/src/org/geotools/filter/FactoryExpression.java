/*
 * This code is released under the Apache license, availible at the root GML4j directory.
 */
package org.geotools.filter;

import org.geotools.datasource.*;

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


    /**
     * Constructor which flags the operator as between.
     */
    public FactoryExpression (String type) {

        if( ExpressionDefault.isMath(type) ) {
            currentExpression =  new ExpressionMath(type);
        }
        else if( ExpressionDefault.isLiteral(type) ) {
            currentExpression =  new ExpressionLiteral(type);
        }
        else if( ExpressionDefault.isAttribute(type) ) {
            currentExpression =  new ExpressionAttribute();
        }
        else if( ExpressionDefault.is(type) ) {
            currentExpression =  new ExpressionAttribute();
        }

    }


    /**
     * Sets the matching pattern for this FilterLike.
     *
     * @param pattern The limited REGEXP pattern for this string. 
     */
    public void message(String message) {
    }

    /**
     * Sets the matching pattern for this FilterLike.
     *
     * @param pattern The limited REGEXP pattern for this string. 
     */
    public void message(String value, String message) {
    }

    /**
     * Sets the multi wildcard for this FilterLike.
     *
     * @param wildcardMulti The multi wildcard for this pattern. 
     */
    public void geometry(String wildcardMulti) {
    }

    /**
     * Sets the multi wildcard for this FilterLike.
     *
     */
    public Expression create() {
    }

    
}
