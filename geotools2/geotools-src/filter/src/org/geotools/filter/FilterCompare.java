/*
 * This code is released under the Apache license, availible at the root GML4j directory.
 */
package org.geotools.filter;

import org.geotools.datasource.*;

/**
 * Defines a comparison filter (can be a math comparison or generic equals).
 *
 * This fitler implements a comparison - of some sort - between two expressions.
 * The comparison may be a math comparison or a generic equals comparison.  If
 * is is a math comparison, only math expressions are allowed; if it is an
 * equals comparison, any expression types are allowed.
 *
 * Note that this comparison does not attempt to restict its expressions to be
 * meaningful.  This means that it considers itself a valid filter as long as
 * the expression comparison returns a valid result.  It does no checking to
 * see whether or not the expression comparison is meaningful with regard
 * to checking feature attributes.  In other words, this is a valid filter:
 * <b>5 < 2<b>, even though it will always return the same result and could
 * be simplified away.  It is up the the filter creater, therefore, to attempt
 * to simplify/make meaningful filter logic.
 * 
 * @author Rob Hranac, Vision for New York
 * @version 
 */
public class FilterCompare extends FilterDefault {

    /** Holds the 'left' value of this comparison filter. */
    protected Expression leftValue = null;

    /** Holds the 'right' value of this comparison filter. */
    protected Expression rightValue = null;


    /**
     * Constructor with filter type.
     *
     * @param filterType The type of comparison.
     * @throws IllegalFilterException Non-compare type.
     */
    public FilterCompare (short filterType)
        throws IllegalFilterException {
        
        if( isCompareFilter(filterType) ) {
            this.filterType = filterType;
        }
        else {
            throw new IllegalFilterException
                ("Attempted to create compare filter with non-compare type.");
        }
    }


    /**
     * Adds the 'left' value to this filter.
     *
     * @param leftValue Expression for 'left' value.
     * @throws IllegalFilterException Filter is not internally consistent.
     */
    public void addLeftValue(Expression leftValue)
        throws IllegalFilterException {
        
        // Checks if this is math filter or not and handles appropriately
        if( isMathFilter(filterType) ) {
            if( ExpressionDefault.isMathExpression(leftValue.getType())  ||
                permissiveConstruction ) {
                this.leftValue = leftValue;
            }
            else {
                throw new IllegalFilterException
                    ("Attempted to add non-math expression to math filter.");
            }
        }
        else {
            this.leftValue = leftValue;
        }

    }


    /**
     * Adds the 'right' value to this filter.
     *
     * @param rightValue Expression for 'right' value.
     * @throws IllegalFilterException Filter is not internally consistent.
     */
    public void addRightValue(Expression rightValue)
        throws IllegalFilterException {
        
        // Checks if this is math filter or not and handles appropriately
        if( isMathFilter(filterType) ) {
            if( ExpressionDefault.isMathExpression(leftValue.getType())  ||
                permissiveConstruction ) {
                this.rightValue = rightValue;
            }
            else {
                throw new IllegalFilterException("Attempted to add non-math expression to math filter.");
            }
        }
        else {
            this.rightValue = rightValue;
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
        
        // Checks for error condition
        if( leftValue == null | rightValue == null ) {
            throw new MalformedFilterException("Compare filter is missing required expression.");
        }

        // Non-math comparison
        if( filterType == COMPARE_EQUALS ) {
            return leftValue.getValue(feature).equals( rightValue.getValue(feature) );
        }

        // Math comparisons
        int mathResult = ((Double) leftValue.getValue(feature)).compareTo( (Double) rightValue.getValue(feature) );
        if( filterType == COMPARE_LESS_THAN ) {
            return (mathResult < 0);
        }
        if( filterType == COMPARE_GREATER_THAN ) {
            return (mathResult > 0);
        }
        if( filterType == COMPARE_LESS_THAN_EQUAL ) {
            return (mathResult <= 0);
        }
        if( filterType == COMPARE_GREATER_THAN_EQUAL ) {
            return (mathResult >= 0);
        }

        // Note that this is a pretty permissive logic
        //  if the type has somehow be mis-set (can't happen externally)
        //  then true is returned in all cases
        else {
            return true;
        }
    
    }
            
}
