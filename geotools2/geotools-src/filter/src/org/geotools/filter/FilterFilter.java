/*
 * This code is released under the Apache license, availible at the root GML4j directory.
 */
package org.geotools.filter;

import java.util.*;
import java.math.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;


/**
 * LEVEL1 saxGML4j GML filter: Sends basic alerts for GML types to GMLFilterGeometry.
 *
 * <p>This filter seperates and passes GML events to a GMLHandlerGeometry.  The main
 * simplification that it performs is to pass along coordinates as an abstracted method call,
 * regardless of their notation in the GML (Coord vs. Coordinates).  This call turns the coordinates
 * into doubles and makes sure that it distinguishes between 2 and 3 value coordinates.</p>
 * <p>The filter also handles some more subtle processing, including handling different delimeters
 * (decimal, coordinate, tuple) that may be used by more outlandish GML generators.<p> 
 * 
 * @author Rob Hranac, Vision for New York
 * @version alpha, 12/01/01
 */
public class FilterFilter extends XMLFilterImpl {

    /** Parent of the filter: must implement GMLHandlerGeometry. */
    private GMLHandlerGeometry parent;  
    
    /** Whether or not this parser should consider namespaces */
    private boolean namespaceAware = true;
    
    // Static Globals to handle some expected elements
    /** GML namespace string */
    private static final String GML_NAMESPACE = "http://www.opengis.net/gml";

    
    /**
     * Constructor with parent.
     *
     * @param parent Parent of the filter: must implement GMLHandlerGeometry.
     */
    public FitlerFilter (GMLHandlerGeometry parent) {
    }
    

    
}
