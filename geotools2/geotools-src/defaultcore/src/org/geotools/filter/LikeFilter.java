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

import org.apache.log4j.Category;
import org.geotools.data.*;
import org.geotools.feature.*;

/**
 * Defines a like filter, which checks to see if an attribute matches a REGEXP.
 *
 * @version $Id: LikeFilter.java,v 1.3 2002/07/12 20:18:14 robhranac Exp $
 * @author Rob Hranac, Vision for New York
 */
public class LikeFilter extends AbstractFilter {

    /** Standard logging instance */
    private static Category _log = Category.getInstance(LikeFilter.class.getName());

    /** The attribute value, which must be an attribute expression. */
    protected Expression attribute = null;

    /** The (limited) REGEXP pattern. */
    protected String pattern = null;

    /** The multi wildcard for the (limited) REGEXP pattern. */
    protected String wildcardSingle = null;

    /** The single wildcard for the (limited) REGEXP pattern. */
    protected String wildcardMulti = null;

    /** The escape sequence for the (limited) REGEXP pattern. */
    protected String escape = null;


    /**
     * Constructor which flags the operator as between.
     */
    public LikeFilter () {
        filterType = LIKE;
    }


    /**
     * Determines whether or not a given feature is 'inside' this filter.
     *
     * @param attribute The value of the attribute for comparison. 
     * @throws IllegalFilterException Filter is illegal.
     */
    public void setValue(Expression attribute)
        throws IllegalFilterException {
        
        if( (attribute.getType() != ExpressionDefault.ATTRIBUTE_STRING) ||
            permissiveConstruction ) {
            this.attribute = attribute;
        }
        else {
            throw new IllegalFilterException("Attempted to add something other than a string attribute expression to a like filter.");
        }
    }


    /**
     * Sets the matching pattern for this FilterLike.
     *
     * @param pattern The limited REGEXP pattern for this string. 
     */
    public void setPattern(Expression literal) {
        String pattern = literal.toString();
        pattern = pattern.replaceAll("\\?", ".?");
        pattern = pattern.replaceAll("\\*", ".*");
        this.pattern = pattern;
    }


    /**
     * Sets the matching pattern for this FilterLike.
     *
     * @param pattern The limited REGEXP pattern for this string. 
     */
    public void setPattern(String pattern, String wildcardMulti, String wildcardSingle, String escape) {

        // The following things happen for both wildcards:
        //  (1) If a user-defined wildcard exists, replace with Java wildcard
        //  (2) If a user-defined escape exists, Java wildcard + user-escape
        //  Then, test for matching pattern and return result.
        pattern = pattern.replaceAll(wildcardSingle, ".?");
        pattern = pattern.replaceAll(escape + wildcardSingle, "\\?");

        pattern = pattern.replaceAll(wildcardMulti, ".*");
        pattern = pattern.replaceAll(escape + wildcardMulti, "\\*");

        pattern = pattern.replaceAll(escape, "\\");

        this.pattern = pattern;
    }


    /**
     * Determines whether or not a given feature matches this pattern.
     *
     * @param feature Specified feature to examine.
     * @return Flag confirming whether or not this feature is inside the filter.
     * @throws MalformedFilterException Filter is not internally consistent.
     */
    public boolean contains(Feature feature) {

        // Checks to ensure that the attribute has been set
        if( attribute == null ) {
            return false;
        }

        else {

            // Note that this converts the attribute (whatever it is) to a string
            //  for comparison.  Unlike the math or geometry filters, which
            //  require specific types to function correctly, this filter
            //  using the mandatory string representation in Java
            // Of course, this does not guarantee a meaningful result, but it
            //  does guarantee a valid result.

            //_log.info("pattern: " + pattern);
            //_log.info("string: " + attribute.getValue(feature).toString());
            return attribute.getValue(feature).toString().matches(pattern);
        }
    }


    public String toString() {
        return "[ " + attribute.toString() + " is like " + pattern + " ]";        
    }
    
}
