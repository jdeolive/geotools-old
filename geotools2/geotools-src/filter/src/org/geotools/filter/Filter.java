/*
 * This code is released under the Apache license, availible at the root GML4j 
 * directory.
 */
package org.geotools.filter;

import org.geotools.datasource.*;

/**
 * Defines an OpenGIS Filter object, with default behaviors for all methods.
 *
 * @author Rob Hranac, Vision for New York
 * @version 4/18/02
 */
public interface Filter {

    /**
     * Determines whether or not a given feature is 'contained by' this filter.
     *
     * <p>This is the core function of any filter.  'Contains' isn't a very
     * good term for this method because it implies some sort of spatial 
     * relationship between the feature and the filter that may or may 
     * not exist.  We name this method 'contains' only because the useage 
     * of 'contains' in this context is common and better terms are lacking
     * However, users of this method should keep in mind the non-spatial nature
     * of this meaning of 'contains.'  For example, a feature may be 'contained
     * by' a filter if one of the feature's non-spatial property values is
     * equal to that of the filter's.</p>
     *
     * <p>Although some filters can be checked for validity when the are
     * constructed, it is impossible to impose this check on all expressions
     * because of a special feature of the <code>ExpressionAttribute</code>
     * class.  This class must hold the a pointer (in XPath) to an attribute,
     * but it is not passed the actual attribute (inside a feature) until
     * it calls the <code>isInside</code> class.</p>
     *
     * <p>To avoid a run-time Exception, this class is typed (ie. Double,
     * Integer, String) when it is created.  If the attribute found inside
     * the feature is found not to conform with its stated type, then a
     * <code>MalformedExpressionException</code> is thrown when <code>
     * contains</code> is called.  Since <code>ExpressionAttribute</code>
     * classes may be nested inside any filter, all filters must throw
     * this exception.  It is left to callers of this method to deal
     * with it gracefully.</p>
     * 
     * @param feature Specified feature to examine.
     */
    public boolean contains(Feature feature)
        throws MalformedFilterException;
    
    /**
     * Implements a logical AND with this filter and returns the merged filter.
     *
     * @param filter The filter to AND with this filter.
     * @return Combined filter.
     */
    public Filter and(Filter filter);    

    /**
     * Implements a logical OR with this filter and returns the merged filter.
     *
     * @param filter The filter to OR with this filter.
     * @return Combined filter.
     */
    public Filter or(Filter filter);    

    /**
     * Implements a logical NOT with this filter and returns the negated filter.
     *
     * @return Combined filter.
     */
    public Filter not();    

    
}
