/*
 * This code is released under the Apache license, availible at the root GML4j directory.
 */
package org.geotools.filter;

import org.geotools.data.*;
import org.geotools.feature.*;

/**
 * Defines a 'between' filter (which is a specialized compare filter).
 *
 * A between filter is just shorthand for a less-than-or-equal filter
 * ANDed with a greater-than-or-equal filter.  Arguably, this would be better
 * handled using those constructs, but the OGC filter specification
 * creates its own object for this, so we do as well.
 * 
 * An important note here is that a between filter is actually a math filter,
 * so its outer (left and right) expressions must be math expressions.  This
 * is enforced by the FilterAbstract class, which considers a BETWEEN operator
 * to be a math filter.
 * 
 * @author Rob Hranac, Vision for New York
 * @version alpha, 12/01/01
 */
public class FilterBetween extends FilterCompare {

    /** The 'middle' value, which must be an attribute expression. */
    protected Expression middleValue = null;


    /**
     * Constructor which flags the operator as between.
     */
    public FilterBetween () throws IllegalFilterException{
        super(BETWEEN);
    }


    /**
     * Determines whether or not a given feature is 'inside' this filter.
     *
     * @param middleValue The value of this 
     * @throws IllegalFilterException Filter is illegal.
     */
    public void addMiddleValue(Expression middleValue)
        throws IllegalFilterException {
        
        if( ExpressionDefault.isAttributeExpression(middleValue.getType()) ) {
            this.middleValue = middleValue;
        }
        else {
            throw new IllegalFilterException
                ("Attempted to add non-attribute middle expression to between filter.");
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

        if( middleValue == null ) {
            throw new MalformedFilterException("Middle expression of between filter not set.");
        }
        else {
            int mathResultLeft = ((Double) leftValue.getValue(feature)).
                compareTo((Double) middleValue.getValue(feature));
            int mathResultRight = ((Double) rightValue.getValue(feature)).
                compareTo((Double) middleValue.getValue(feature));
            
            return (mathResultRight >= 0) && (mathResultLeft <= 0);
        }
    }
    
}
