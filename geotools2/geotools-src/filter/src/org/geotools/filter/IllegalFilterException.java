/*
 * This code is released under the Apache license, availible at the root GML4j directory.
 */
package org.geotools.filter;


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
public class IllegalFilterException extends Exception {

    /**
     * Constructor with filter type.
     *
     */
    public IllegalFilterException () {
        super();
    }

            
    /**
     * Constructor with filter type.
     *
     */
    public IllegalFilterException (String message) {
        super(message);
    }

            
}
