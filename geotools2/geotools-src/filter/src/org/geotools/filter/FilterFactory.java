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

import org.apache.log4j.Logger;
import org.geotools.data.*;
import org.geotools.feature.*;

/**
 * Defines a like filter, which checks to see if an attribute matches a REGEXP.
 *
 * @version $Id: FilterFactory.java,v 1.1 2002/07/16 19:36:48 robhranac Exp $
 * @author Rob Hranac, Vision for New York
 */
public class FilterFactory {

    /** Standard logging instance for this class. */
    private static Logger _log = Logger.getLogger(FilterFactory.class);

    /** The (limited) REGEXP pattern. */
    private Filter currentFilter = null;

    /** The (limited) REGEXP pattern. */
    private String currentState;

    /** The (limited) REGEXP pattern. */
    private short filterType;


    /**
     * Constructor which flags the operator as between.
     */
    public FilterFactory () {}


    /**
     * Handles all incoming generic string 'messages,' including a message to
     * create the filter, based on the XML tag that represents the start of
     * the filter.
     *
     * @param message The string from the SAX filter.
     * @throws IllegalFilterException Filter is illegal.
     */
    public void start(short filterType)
        throws IllegalFilterException {

        if( AbstractFilter.isGeometryFilter(filterType) ) {
            currentFilter = new GeometryFilter(filterType);            
        }
        else if( filterType == AbstractFilter.BETWEEN ) {
            currentFilter = new BetweenFilter();            
        }
        else if( AbstractFilter.isCompareFilter(filterType) ) {
            currentFilter = new CompareFilter(filterType);            
        }
        else if( filterType == AbstractFilter.NULL ) {
            currentFilter = new NullFilter();            
        }
        else if( filterType == AbstractFilter.LIKE ) {
            currentFilter = new LikeFilter();            
        }
        else {
            throw new IllegalFilterException
                ("Attempted to start a new filter with invalid type: "
                 + filterType);
        }
        currentState = setInitialState(filterType);
        this.filterType = filterType;
    }

    /**
     * Handles all incoming generic string 'messages,' including a message to
     * create the filter, based on the XML tag that represents the start of
     * the filter.
     *
     * @param message The string from the SAX filter. 
     * @throws IllegalFilterException Filter is illegal.
     */
    public void value(String message)
        throws IllegalFilterException {

    }

    /**
     * Determines whether or not a given feature is 'inside' this filter.
     *
     * @param message The value of the attribute for comparison.
     * @throws IllegalFilterException Filter is illegal.
     */
    public void expression(Expression expression) 
        throws IllegalFilterException {

        // Handle all filter compare states and expressions
        _log.debug("got expression: " + expression.toString());
        _log.debug("current state (start): " + currentState);
        if( filterType == AbstractFilter.BETWEEN ) {
            if( currentState.equals("attribute") ) {
                ((BetweenFilter) currentFilter).addMiddleValue(expression);
                currentState = "LowerBoundary";
            }
            else if( currentState.equals("LowerBoundary") ) {
                ((BetweenFilter) currentFilter).addLeftValue(expression);
                currentState = "UpperBoundary";
            }
            else if( currentState.equals("UpperBoundary") ) {
                ((BetweenFilter) currentFilter).addRightValue(expression);
                currentState = "complete";
            }
            else {
                throw new IllegalFilterException
                    ("Got expression for Between Filter in illegal state: "
                     + currentState);
            }
        }
        else if( AbstractFilter.isCompareFilter(filterType) ) {
            if( currentState.equals("leftValue") ) {
                ((CompareFilter) currentFilter).addLeftValue(expression);
                currentState = "rightValue";
            }
            else if( currentState.equals("rightValue") ) {
                ((CompareFilter) currentFilter).addRightValue(expression);
                currentState = "complete";
            }
            else {
                throw new IllegalFilterException
                    ("Got expression for Compare Filter in illegal state: "
                     + currentState);
            }
        }
        else if( filterType == AbstractFilter.NULL ) {
            if( currentState.equals("attribute") ) {
                ((NullFilter) currentFilter).nullCheckValue(expression);
                currentState = "complete";
            }
            else {
                throw new IllegalFilterException
                    ("Got expression for Null Filter in illegal state: "
                     + currentState);
            }
        }
        else if( AbstractFilter.isGeometryFilter(filterType) ) {
            if( currentState.equals("leftValue") ) {
                ((GeometryFilter) currentFilter).addLeftGeometry(expression);
                currentState = "rightValue";
            }
            else if( currentState.equals("rightValue") ) {
                ((GeometryFilter) currentFilter).addRightGeometry(expression);
                currentState = "complete";
            }
            else {
                throw new IllegalFilterException
                    ("Got expression for Geometry Filter in illegal state: "
                     + currentState);
            }
        }
        else if( filterType == AbstractFilter.LIKE ) {
            if( currentState.equals("attribute") ) {
                ((LikeFilter) currentFilter).setValue(expression);
                currentState = "pattern";
            }
            else if( currentState.equals("pattern") ) {
                ((LikeFilter) currentFilter).setPattern(expression);
                currentState = "complete";
            }
            else {
                throw new IllegalFilterException
                    ("Got expression for Like Filter in illegal state: "
                     + currentState);
            }
        }
        _log.debug("current state (end): " + currentState);
    }


    /**
     * Sets the multi wildcard for this LikeFilter.
     *
     */
    public Filter create()
        throws IllegalFilterException {

        if( isComplete() ) {
            return currentFilter;
        }
        else {
            throw new IllegalFilterException
                ("Got to the end state of an incomplete filter.");
        }


    }

    /**
     * Sets the multi wildcard for this LikeFilter.
     *
     */
    private static String setInitialState(short filterType) 
        throws IllegalFilterException {

        if( ( filterType == AbstractFilter.BETWEEN) ||
            ( filterType == AbstractFilter.NULL) ||
            ( filterType == AbstractFilter.LIKE) ) {
            return "attribute";
        }
        else if( ( AbstractFilter.isCompareFilter(filterType)) ||
                 ( AbstractFilter.isGeometryFilter(filterType))) {
            return "leftValue";
        }
        else {
            throw new IllegalFilterException
                ("Created illegal filter: " + filterType);
        }

    }

    /**
     * Sets the multi wildcard for this LikeFilter.
     *
     */
    private boolean isComplete() {
        if( currentState.equals("complete") ) {
            return true;
        }
        else {
            return false;
        }
    }

    
}
