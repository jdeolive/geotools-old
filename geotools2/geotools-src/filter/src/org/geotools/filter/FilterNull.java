/*
 * This code is released under the Apache license, availible at the root GML4j directory.
 */
package org.geotools.filter;

import org.geotools.datasource.*;

/**
 * Defines a null filter, which checks to see if an attribute is null.
 *
 * @author Rob Hranac, Vision for New York
 * @version 
 */
public class FilterNull extends FilterDefault {

    /** The null check value, which must be an attribute expression. */
    protected Expression nullCheck = null;


    /**
     * Constructor which flags the operator as between.
     */
    public FilterNull () {
        filterType = NULL;
    }


    /**
     * Determines whether or not a given feature is 'inside' this filter.
     *
     * @param nullCheckValue The value of this 
     * @throws IllegalFilterException Filter is illegal.
     */
    public void nullCheckValue(Expression nullCheck)
        throws IllegalFilterException {
        
        if( ExpressionDefault.isAttributeExpression( nullCheck.getType() ) ) {
            this.nullCheck = nullCheck;
        }
        else {
            throw new IllegalFilterException("Attempted to add non-attribute expression to a null filter.");
        }
    }

    /**
     * Determines whether or not a given feature is 'inside' this filter.
     *
     * @param feature Specified feature to examine.
     * @return Flag confirming whether or not this feature is inside the filter.
     * @throws IllegalFilterException Filter is not internally consistent.
     */
    public boolean contains(Feature feature)
        throws MalformedFilterException {

        if( nullCheck == null ) {
            throw new MalformedFilterException("Null filter expression missing.");
        }
        else {
            return (nullCheck.getValue(feature) == null);
        }
    }
    
}
