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

import org.geotools.feature.Feature;

/**
 * Defines an OpenGIS Filter object, with default behaviors for all methods.
 *
 * @version $Id: Filter.java,v 1.5 2002/07/22 21:01:34 jmacgill Exp $
 * @author Rob Hranac, Vision for New York
 */
public interface Filter {

    /**
     * Determines whether or not a given feature is 'contained by' this filter.
     *
     * <p>This is the core function of any filter.  'Contains' isn't a very
     * good term for this method because it implies some sort of spatial 
     * relationship between the feature and the filter that may or may 
     * not exist.  We name this method 'contains' only because the usage 
     * of 'contains' in this context is common and better terms are lacking.
     * However, users of this method should keep in mind the non-spatial nature
     * of this meaning of 'contains.'  For example, a feature may be 'contained
     * by' a filter if one of the feature's non-spatial property values is
     * equal to that of the filter's.</p>
     *
     * <p>Although some filters can be checked for validity when they are
     * constructed, it is impossible to impose this check on all expressions
     * because of a special feature of the <code>ExpressionAttribute</code>
     * class.  This class must hold the pointer (in XPath) to an attribute,
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
     * @return True if filter contains passed feature.
     */
    boolean contains(Feature feature);
    
    /**
     * Implements a logical AND with this filter and returns the merged filter.
     *
     * @param filter The filter to AND with this filter.
     * @return Combined filter.
     */
    Filter and(Filter filter);    

    /**
     * Implements a logical OR with this filter and returns the merged filter.
     *
     * @param filter The filter to OR with this filter.
     * @return Combined filter.
     */
    Filter or(Filter filter);    

    /**
     * Implements a logical NOT with this filter and returns the negated filter
     *
     * @return Combined filter.
     */
    Filter not();    

}
