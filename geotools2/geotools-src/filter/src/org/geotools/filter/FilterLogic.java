/*
 * This code is released under the Apache license, availible at the root GML4j directory.
 */
package org.geotools.filter;

import java.util.*;

import org.geotools.datasource.*;

/**
 * Defines a logic filter (the only filter type that contains other filters).
 *
 * This filter holds one or more filters together and relates them logically
 * with an internally defined type (AND, OR, NOT).
 *
 * @author Rob Hranac, Vision for New York
 * @version alpha, 12/01/01
 */
public class FilterLogic extends FilterDefault {


    /** Holds all sub filters of this filter. */
    protected Stack subFilters = new Stack();


    /**
     * Constructor with type (must be valid).
     *
     * @param type The final relation between all sub filters.
     */
    public FilterLogic (short fitlerType)
        throws IllegalFilterException {

        if( isLogicFilter(filterType) ) {
            this.filterType = filterType;
        }
        else {
            throw new IllegalFilterException("Attempted to create logic filter with non-logic type.");
        }
    }

    /**
     * Convenience constructor to create a NOT logic filter.
     *
     * @param filter The initial sub filter.
     * @param filterType The final relation between all sub filters.
     * @throws IllegalFilterException Does not conform to logic filter structure
     */
    public FilterLogic (Filter filter, short filterType)
        throws IllegalFilterException {

        if( isLogicFilter(filterType) ) {
            this.filterType = filterType;
        }
        else {
            throw new IllegalFilterException("Attempted to create logic filter with non-logic type.");
        }
        subFilters.push(filter);
    }

    /**
     * Convenience constructor to create an AND/OR logic filter.
     *
     * @param filter1 An initial sub filter.
     * @param filter2 An initial sub filter.
     * @param filterType The final relation between all sub filters.
     * @throws IllegalFilterException Does not conform to logic filter structure
     */
    public FilterLogic (Filter filter1, Filter filter2, short filterType)
        throws IllegalFilterException {

        if( isLogicFilter(filterType) ) {
            this.filterType = filterType;
        }
        else {
            throw new IllegalFilterException("Attempted to create logic filter with non-logic type.");
        }

        // Push the initial filter on the stack
        subFilters.push(filter1);

        // Add the second filter via internal method to check for illegal NOT
        this.addFilter(filter2);
    }


    /**
     * Adds a sub filter to this filter.
     *
     * @param filter Specified filter to add to the sub filter list.
     * @throws IllegalFilterException Does not conform to logic filter structure
     */
    public void addFilter(Filter filter) 
        throws IllegalFilterException {

        if( filterType != LOGIC_NOT ) {
            subFilters.push(filter);
        }
        else {
            throw new IllegalFilterException("Attempted to add an more than one filter to a NOT filter.");
        }
    }


    /**
     * Determines whether or not a given feature is 'inside' this filter.
     *
     * @param feature Specified feature to examine.
     * @return Flag confirming whether or not this feature is inside the filter.
     * @throws MalformedFilterException Filter is not correctly formed.
     */
    public boolean isInside(Feature feature)
        throws MalformedFilterException {
        
        boolean isInside = true;

        // Throw exception if there are no sub filters.
        if( subFilters.isEmpty() ) {
            throw new MalformedFilterException("Logic filter has no sub filters.");
        }

        // Handles all standard cases
        else if( filterType == LOGIC_OR ) {
            while( !subFilters.empty() ) {
                isInside = ((Filter) subFilters.pop()).isInside(feature) | isInside;
            }
        }
        else if( filterType == LOGIC_AND ) {
            while( !subFilters.empty() ) {
                isInside = ((Filter) subFilters.pop()).isInside(feature) && isInside;
            }
        }
        else if( filterType == LOGIC_NOT ) {
            isInside = !((Filter) subFilters.pop()).isInside(feature);
        }
        
        // Note that this is a pretty permissive logic
        //  if the type has somehow be mis-set (can't happen externally)
        //  then true is returned in all cases
        return isInside;
    }
        
    /**
     * Implements a logical OR with this filter and returns the merged filter.
     *
     * @param feature Parent of the filter: must implement GMLHandlerGeometry.
     * @return ORed filter.
     */
    public Filter or(Filter filter) {
        
        // Just makes sure that we are not creating unecessary new filters
        //  by popping onto stack if current filter is OR
        if( filterType == super.LOGIC_OR ) {
            subFilters.push(filter);
        }
        else {
            super.or(filter);
        }
    }
    
    /**
     * Implements a logical AND with this filter and returns the merged filter.
     *
     * @param filter Parent of the filter: must implement GMLHandlerGeometry.
     * @return ANDed filter.
     */
    public Filter and(Filter filter) {

        // Just makes sure that we are not creating unecessary new filters
        //  by popping onto stack if current filter is AND
        if( filterType == super.LOGIC_AND ) {
            subFilters.push(filter);
        }
        else {
            super.and(filter);
        }
    }

    /**
     * Implements a logical NOT with this filter and returns the merged filter.
     *
     * @return NOTed filter.
     */
    public Filter not() {

        // Just makes sure that we are not creating unecessary new filters
        //  by popping off sub filter if current filter is NOT
        if( filterType == super.LOGIC_NOT ) {
            return (Filter) subFilters.pop();
        }
        else {
            super.not();
        }
    }
    
    
}
