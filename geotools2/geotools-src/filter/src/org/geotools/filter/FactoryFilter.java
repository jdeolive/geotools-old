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

import org.geotools.data.*;
import org.geotools.feature.*;

/**
 * Defines a like filter, which checks to see if an attribute matches a REGEXP.
 *
 * @version $Id: FactoryFilter.java,v 1.4 2002/06/05 13:23:20 loxnard Exp $
 * @author Rob Hranac, Vision for New York
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
     * Handles all incoming generic string 'messages,' including a message to
     * create the filter, based on the XML tag that represents the start of
     * the filter.
     *
     * @param message The string from the SAX filter.
     * @throws IllegalFilterException Filter is illegal.
     */
    public void start(short filterType)
        throws IllegalFilterException {

        if( FilterDefault.isGeometryFilter(filterType) ) {
            currentFilter = new FilterGeometry(filterType);            
        }
        else if( FilterDefault.isCompareFilter(filterType) ) {
            currentFilter = new FilterCompare(filterType);            
        }
        else if( filterType == FilterDefault.BETWEEN ) {
            currentFilter = new FilterBetween();            
        }
        else if( filterType == FilterDefault.NULL ) {
            currentFilter = new FilterNull();            
        }
        else if( filterType == FilterDefault.LIKE ) {
            currentFilter = new FilterLike();            
        }
        else {
            throw new IllegalFilterException
                ("Attempted to start a new filter with invalid type: "
                 + filterType);
        }
        currentState = setInitialState(currentFilter);
    
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
        if( currentFilter instanceof FilterCompare ) {
            if( currentState.equals("leftValue") ) {
                ((FilterCompare) currentFilter).addLeftValue(expression);
                currentState = "rightValue";
            }
            else if( currentState.equals("rightValue") ) {
                ((FilterCompare) currentFilter).addRightValue(expression);
                currentState = "complete";
            }
            else {
                throw new IllegalFilterException
                    ("Got expression for Compare Filter in illegal state: "
                     + currentState);
            }
        }
        else if( currentFilter instanceof FilterBetween ) {
            if( currentState.equals("attribute") ) {
                ((FilterBetween) currentFilter).addLeftValue(expression);
                currentState = "LowerBoundary";
            }
            if( currentState.equals("LowerBoundary") ) {
                ((FilterBetween) currentFilter).addLeftValue(expression);
                currentState = "UpperBoundary";
            }
            else if( currentState.equals("UpperBoundary") ) {
                ((FilterBetween) currentFilter).addRightValue(expression);
                currentState = "complete";
            }
            else {
                throw new IllegalFilterException
                    ("Got expression for Between Filter in illegal state: "
                     + currentState);
            }
        }
        else if( currentFilter instanceof FilterNull ) {
            if( currentState.equals("attribute") ) {
                ((FilterNull) currentFilter).nullCheckValue(expression);
                currentState = "complete";
            }
            else {
                throw new IllegalFilterException
                    ("Got expression for Between Filter in illegal state: "
                     + currentState);
            }
        }
        if( currentFilter instanceof FilterGeometry ) {
            if( currentState.equals("leftValue") ) {
                ((FilterGeometry) currentFilter).addLeftGeometry(expression);
                currentState = "rightValue";
            }
            else if( currentState.equals("rightValue") ) {
                ((FilterGeometry) currentFilter).addRightGeometry(expression);
                currentState = "complete";
            }
            else {
                throw new IllegalFilterException
                    ("Got expression for Geometry Filter in illegal state: "
                     + currentState);
            }
        }
        else if( currentFilter instanceof FilterLike ) {
            if( currentState.equals("attribute") ) {
                ((FilterLike) currentFilter).setValue(expression);
                currentState = "complete";
            }
            else {
                throw new IllegalFilterException
                    ("Got expression for Between Filter in illegal state: "
                     + currentState);
            }
        }
        else {
        }
    }


    /**
     * Sets the multi wildcard for this FilterLike.
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
     * Sets the multi wildcard for this FilterLike.
     *
     */
    private static String setInitialState(Filter currentFilter) 
        throws IllegalFilterException {

        if( ( currentFilter instanceof FilterCompare ) ||
            ( currentFilter instanceof FilterGeometry ) ) {
            return "leftValue";
        }
        else if( ( currentFilter instanceof FilterBetween ) ||
                 ( currentFilter instanceof FilterNull ) ||
                 ( currentFilter instanceof FilterLike ) ) {
            return "attribute";
        }
        else {
            throw new IllegalFilterException
                ("Created illegal filter: " + 
                 currentFilter.getClass().toString());
        }

    }

    /**
     * Sets the multi wildcard for this FilterLike.
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
