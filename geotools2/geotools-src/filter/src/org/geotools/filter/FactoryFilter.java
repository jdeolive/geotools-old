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
public class FactoryFilter {

    /** The (limited) REGEXP pattern. */
    private Filter currentFilter = null;

    /** The (limited) REGEXP pattern. */
    private String currentState;


    /**
     * Constructor which flags the operator as between.
     */
    public FactoryFilter () {}


    /**
     * Determines whether or not a given feature is 'inside' this filter.
     *
     * @param message The value of the attribute for comparison. 
     * @throws IllegalFilterException Filter is illegal.
     */
    public void message(String message) {

        if( currentFilter == null ) {
            currentFilter = new FactoryExpression(message);            
        }
        else {
            currentMessage = message;
        }
    }

    /**
     * Determines whether or not a given feature is 'inside' this filter.
     *
     * @param message The value of the attribute for comparison. 
     * @throws IllegalFilterException Filter is illegal.
     */
    public void expression(Expression expression) 
        throws IllegalFilterException {

        if( currentFilter instanceof FilterCompare ) {
            if( currentState.equals("leftValue") ) {
                currentFilter.addLeftValue(expression);
                currentState = "rightValue";
            }
            else if( currentState.equals("rightValue") ) {
                currentFilter.addRightValue(expression);
            }
        }
        else if( currentFilter instanceof FilterBetween ) {
            if( currentState.equals("lowerBoundary") ) {
                currentFilter.addLeftValue(expression);
                currentState = "upperBoundary";
            }
            else if( currentState.equals("upperBoundary") ) {
                currentFilter.addRightValue(expression);
            }
        }
        else {
        }
    }


    /**
     * Sets the multi wildcard for this FilterLike.
     *
     */
    public void create() {
    }

    
}
