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
public class FactoryFilterCompare extends FactoryFilterAbstract {

    /** The attribute value, which must be an attribute expression. */
    private FactoryFilter filterFactory = null;

    /** The attribute value, which must be an attribute expression. */
    private FactoryExpression expressionFactory = null;

    /** The (limited) REGEXP pattern. */
    private Filter currentFilter = null;


    /**
     * Constructor which flags the operator as between.
     */
    public FactoryFilter (String type) {
        filterType = LIKE;
    }


    /**
     * Determines whether or not a given feature is 'inside' this filter.
     *
     * @param attribute The value of the attribute for comparison. 
     * @throws IllegalFilterException Filter is illegal.
     */
    public void value(String message, String value) {

        if( isFilter(type) ) {
            filterFactory = new FactoryFilter(message);
        }
        else if( isExpression(type) ) {
            filterFactory = new FactoryExpression(message);
        }
        else {
        }
    }

    /**
     * Sets the multi wildcard for this FilterLike.
     *
     * @param wildcardMulti The multi wildcard for this pattern. 
     */
    public void geometry(String wildcardMulti) {
    }

    
}
