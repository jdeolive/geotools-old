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
 * @version $Id: LikeFilter.java,v 1.8 2002/07/22 20:22:03 jmacgill Exp $
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
    
    public void setPattern(Expression p, String wildcardMulti, String wildcardSingle, String escape) {
        String pattern = p.toString();
        setPattern(pattern,wildcardMulti,wildcardSingle,escape);
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
        
        char esc = escape.charAt(0);
        _log.debug("wildcard "+wildcardMulti+" single "+wildcardSingle);
        _log.debug("escape "+escape+" esc "+esc+" esc == \\ "+(esc == '\\'));
        if(esc == '.' || esc == '?' || esc == '*' || esc == '^' || esc == '$' ||
            esc == '+' || esc == '[' || esc == ']' || esc == '(' ||
            esc == ')' || esc == '|' ) {
                escape = "\\\\"+escape;
                _log.debug("escape "+escape);
        }else if (esc == '\\'){
            escape = "\\"+escape;
        }
        char wcs = wildcardSingle.charAt(0);
        
        if(wcs == '.' || wcs == '?' || wcs == '*' || wcs == '^' || wcs == '$' ||
            wcs == '\\' || wcs == '+' || wcs == '[' || wcs == ']' || wcs == '(' ||
            wcs == ')' || wcs == '|') {
                wildcardSingle = "\\"+wildcardSingle;
                _log.debug("wildcardSingle "+wildcardSingle);
        }
        char wcm = wildcardMulti.charAt(0);
        
        if(wcm == '.' || wcm == '?' || wcm == '*' || wcm == '^' || wcm == '$' ||
            wcm == '\\' || wcm == '+' || wcm == '[' || wcm == ']' || wcm == '(' ||
            wcm == ')' || wcm == '|') {
                wildcardMulti = "\\"+wildcardMulti;
                _log.debug("wildcardMulti "+wildcardMulti);
        }
        _log.debug("start pattern = "+pattern);
        pattern = pattern.replaceAll("([^"+escape+"])"+wildcardSingle, "$1.?");
        _log.debug("post single pattern = "+pattern);
        pattern = pattern.replaceAll("([^"+escape+"])"+escape + wildcardSingle, "$1"+wildcardSingle);
        _log.debug("post esc single pattern = "+pattern);

        pattern = pattern.replaceAll("([^"+escape+"])"+wildcardMulti, "$1.*");
        _log.debug("post multi pattern = "+pattern);
        pattern = pattern.replaceAll("([^"+escape+"])"+escape + wildcardMulti, "$1"+wildcardMulti);
        _log.debug("post esc multi pattern = "+pattern);

        //pattern = pattern.replaceAll(escape, "\\\\");
        pattern = pattern.replaceAll(escape+escape,escape);
        _log.debug("post esc pattern = "+pattern);

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
    
    /** Used by FilterVisitors to perform some action on this filter instance.
     * Typicaly used by Filter decoders, but may also be used by any thing which needs
     * infomration from filter structure.
     *
     * Implementations should always call: visitor.visit(this);
     *
     * It is importatant that this is not left to a parent class unless the parents
     * API is identical.
     *
     * @param visitor The visitor which requires access to this filter,
     *                the method must call visitor.visit(this);
     *
     */
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }
    
}
