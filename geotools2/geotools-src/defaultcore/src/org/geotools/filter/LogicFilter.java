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

import java.util.*;

import org.apache.log4j.Category;
import org.geotools.data.*;
import org.geotools.feature.*;

/**
 * Defines a logic filter (the only filter type that contains other filters).
 *
 * This filter holds one or more filters together and relates them logically
 * with an internally defined type (AND, OR, NOT).
 *
 * @version $Id: LogicFilter.java,v 1.2 2002/07/11 14:06:26 ianturton Exp $
 * @author Rob Hranac, TOPP
 */
public class LogicFilter extends AbstractFilter {


    private static Category _log = Category.getInstance(LogicFilter.class.getName());

    /** Holds all sub filters of this filter. */
    protected List subFilters = new ArrayList();


    /**
     * Constructor with type (must be valid).
     *
     * @param type The final relation between all sub filters.
     */
    public LogicFilter (short filterType)
        throws IllegalFilterException {
        //_log.debug("filtertype "+filterType);
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
    public LogicFilter (Filter filter, short filterType)
        throws IllegalFilterException {

        if( isLogicFilter(filterType) ) {
            this.filterType = filterType;
        }
        else {
            throw new IllegalFilterException("Attempted to create logic filter with non-logic type.");
        }
        subFilters.add(filter);
    }

    /**
     * Convenience constructor to create an AND/OR logic filter.
     *
     * @param filter1 An initial sub filter.
     * @param filter2 An initial sub filter.
     * @param filterType The final relation between all sub filters.
     * @throws IllegalFilterException Does not conform to logic filter structure
     */
    public LogicFilter (Filter filter1, Filter filter2, short filterType)
        throws IllegalFilterException {

        if( isLogicFilter(filterType) ) {
            this.filterType = filterType;
        }
        else {
            throw new IllegalFilterException("Attempted to create logic filter with non-logic type.");
        }

        // Push the initial filter on the stack
        subFilters.add(filter1);

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
            subFilters.add(filter);
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
     */
    public boolean contains(Feature feature) {
        Iterator iterator = subFilters.iterator();
        boolean contains = false;

        // Return true if there are no sub filters; fail fast logic.
        if( !iterator.hasNext() ) {
            return false;
        }

        // Handles all standard cases
        else if( filterType == LOGIC_OR ) {
            contains = false;
            while( iterator.hasNext() ) {
                contains = ((Filter) iterator.next()).contains(feature) || contains;
            }
        }
        else if( filterType == LOGIC_AND ) {
            contains = true;
            while( iterator.hasNext() ) {
                contains = ((Filter) iterator.next()).contains(feature) && contains;
            }
        }
        else if( filterType == LOGIC_NOT ) {
            contains = !((Filter) subFilters.get(0)).contains(feature);
        }

        return contains;
    }
        

    /**
     * Implements a logical OR with this filter and returns the merged filter.
     *
     * @param feature Parent of the filter: must implement GMLHandlerGeometry.
     * @return ORed filter.
     */
    public Filter or(Filter filter) {
        
        // Just makes sure that we are not creating unnecessary new filters
        //  by popping onto stack if current filter is OR
        //HACK: not sure what should be returned by this method
        //HACK: assuming it is the result of each method
        if( filterType == super.LOGIC_OR ) {
            subFilters.add(filter);
            return this;
        }
        else {
            return super.or(filter);
        }
    }
    
    /**
     * Implements a logical AND with this filter and returns the merged filter.
     *
     * @param filter Parent of the filter: must implement GMLHandlerGeometry.
     * @return ANDed filter.
     */
    public Filter and(Filter filter) {

        // Just makes sure that we are not creating unnecessary new filters
        //  by popping onto stack if current filter is AND
        //HACK: not sure what should be returned by this method
        //HACK: assuming it is the result of each method
        if( filterType == super.LOGIC_AND ) {
            subFilters.add(filter);
            return this;
        }
        else {
            return super.and(filter);
        }
    }

    /**
     * Implements a logical NOT with this filter and returns the merged filter.
     *
     * @return NOTed filter.
     */
    public Filter not() {

        // Just makes sure that we are not creating unnecessary new filters
        //  by popping off sub filter if current filter is NOT
        //HACK: not sure what should be returned by this method
        //HACK: assuming it is the result of each method
        if( filterType == super.LOGIC_NOT ) {
            return (Filter) subFilters.get(0);
        }
        else {
            return super.not();
        }
    }
    

    /**
     * Returns a string representation of this filter.
     *
     * @return String representation of the logic filter.
     */
    public String toString() {
        String returnString = "";
        String operator = "";
        Iterator iterator = subFilters.iterator();

        if( filterType == LOGIC_OR ) {
            operator = " OR ";
        }
        else if( filterType == LOGIC_AND ) {
            operator = " AND ";
        }
        else if( filterType == LOGIC_NOT ) {
            return "NOT " + ((Filter) iterator.next()).toString();
        }

        while( iterator.hasNext() ) {
            returnString = returnString + ((Filter) iterator.next()).toString();
            if( iterator.hasNext() ) {
                returnString = returnString + operator;                
            }
        }

        return returnString;
    }
    
}
