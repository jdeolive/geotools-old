/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.filter;

// J2SE dependencies
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import org.xml.sax.Attributes;

// Geotools dependencies
import org.geotools.data.*;
import org.geotools.feature.*;


/**
 * Defines a like filter, which checks to see if an attribute matches a REGEXP.
 *
 * @version $Id: FilterSAXParser.java,v 1.4 2003/06/02 23:29:20 cholmesny Exp $
 * @author Rob Hranac, Vision for New York
 */
public class FilterSAXParser {

    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.filter");
    private static final org.geotools.filter.FilterFactory filterFactory = org.geotools.filter.FilterFactory.createFilterFactory();
    /** The filter being currently constructed */
    private Filter currentFilter = null;

    /** The current completion state of the filter */
    private String currentState = "uninitialized";

    /** The short representation of this type of filter */
    private short filterType;

    /** the Attributes of the filter (only applicable to LIKE filters, I think) */
    private HashMap attributes = new HashMap();


    /**
     * Constructor which flags the operator as between.
     */
    public FilterSAXParser () {}


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
	LOGGER.finest("starting filter type " + filterType);
        if( filterType == AbstractFilter.FID && !currentState.equals("fid")) {
	    LOGGER.finer("creating the FID filter");
	    currentFilter = filterFactory.createFidFilter();            
	}
	else if( AbstractFilter.isGeometryDistanceFilter(filterType) ) {
	    currentFilter = filterFactory.createGeometryDistanceFilter(filterType);            
	    
        }
        else if( AbstractFilter.isGeometryFilter(filterType) ) {
            currentFilter = filterFactory.createGeometryFilter(filterType);            
        }
        else if( filterType == AbstractFilter.BETWEEN ) {
            currentFilter = filterFactory.createBetweenFilter();            
        }
        else if( filterType == AbstractFilter.NULL ) {
            currentFilter = filterFactory.createNullFilter();            
        }
        else if( filterType == AbstractFilter.LIKE ) {
            currentFilter = filterFactory.createLikeFilter();            
        }
        else if( AbstractFilter.isCompareFilter(filterType) ) {
            currentFilter = filterFactory.createCompareFilter(filterType);            
        }
        else {
            throw new IllegalFilterException
                ("Attempted to start a new filter with invalid type: "
                 + filterType);
        }
        currentState = setInitialState(filterType);
        this.filterType = filterType;

        attributes = new HashMap();
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
		if (AbstractFilter.isGeometryDistanceFilter(filterType)) {
		    currentState = "distance";
		} else {				 
		    currentState = "complete";
		}
		LOGGER.finer("expression called on geometry, curState = " + 
			     currentState);
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
                if(attributes.size()!=3){
                    throw new IllegalFilterException
                    ("Got wrong number of attributes (expecting 3): "
                     + attributes.size()+"\n"+attributes);
                }
                String wildcard = (String)attributes.get("wildCard");
                String singleChar = (String)attributes.get("singleChar");
                String escapeChar = (String)attributes.get("escape");
		LOGGER.fine("escape char is " + escapeChar);
		//old way, this should deprecate, but keep it for backwords
		//compatability.  Spec says escape.
		if (escapeChar == null) {
		    escapeChar = (String)attributes.get("escapeChar");
		}
		LOGGER.fine("if null get new : " + escapeChar);
                ((LikeFilter) currentFilter).setPattern(expression,
                                                        wildcard,
                                                        singleChar,
                                                        escapeChar);
                currentState = "complete";
            }
            else {
                throw new IllegalFilterException
                    ("Got expression for Like Filter in illegal state: "
                     + currentState);
            }
        }
        LOGGER.finer("current state (end): " + currentState);
    }


    /**
     * Sets the multi wildcard for this LikeFilter.
     *
     */
    public Filter create()
        throws IllegalFilterException {
       
        if( isComplete() ) {
	    LOGGER.finer("complete called, state = " + currentState);
	    currentState = "complete"; //added by cholmes fid bug.
            return currentFilter;
        }
        else {
            throw new IllegalFilterException
                ("Got to the end state of an incomplete filter, current" + 
		 " state is " + currentState);
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
        else if( ( filterType == AbstractFilter.FID) ) {
            return "fid";
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
     * This sets the distance for a GeometryDistanceFilter.  It currently
     * ignores the units, and attempts to convert the distance to a double.
     * 
     * @param distance the distance - should be a string of a double.
     * @param units a reference to a units dictionary.
     * @throws IllegelFilterException if the distance string can not be 
     * converted to a double.  
     * @task TODO: Implement units, probably with org.geotools.units package
     * and a special distance class in the filter package.  It would be
     * nice if the distance class could get any type of units, like
     * it would handle the conversion.
     */
    public void setDistance(String distance, String units) 
	throws IllegalFilterException {
	LOGGER.finer("set distance called, current state is " + currentState);
	if (currentState.equals("distance")) {
	    try {
		double distDouble = Double.parseDouble(distance);
		((GeometryDistanceFilter) currentFilter).setDistance(distDouble);
		currentState = "complete";
	    } catch (NumberFormatException nfe) {
		throw new IllegalFilterException("could not parse distance: " +
						 distance + " to a double");
	    }
	} else {
	    throw new IllegalFilterException
		("Got distance for Geometry Distance Filter in illegal state: "
		 + currentState + ", geometry and property should be set first");
	}
    }

    /**
     * Sets the filter attributes.  Called when attributes are encountered
     * by the filter filter.  Puts them in a hash map by thier name and 
     * value.
     * 
     * @param atts the attributes to set.
     */
    public void setAttributes(Attributes atts){
        LOGGER.finer("got attribute: " + atts.getLocalName(0) + 
                     ", " + atts.getValue(0));
        LOGGER.finer("current state: " + currentState);
        if( currentState.equals("fid")) {
            LOGGER.finer("is a fid");
            ((FidFilter) currentFilter).addFid( atts.getValue(0));
            LOGGER.finer("added fid");
        }
        else {
            for(int i = 0; i < atts.getLength(); i++) {
                this.attributes.put( atts.getLocalName(i),atts.getValue(i));
            }
        }
    }

    /**
     * Indicates that the filter is in a complete state (ready to be created.)
     *
     * @return <tt>true</tt> if the current state is either complete or fid
     */
    private boolean isComplete() {
        if( currentState.equals("complete") ||
            currentState.equals("fid") ) {
	
            return true;
        }
        else {
            return false;
        }
    }

    
}
