/*
 * This code is released under the Apache license, availible at the root GML4j directory.
 */
package org.geotools.filter;

import org.geotools.data.*;
import org.geotools.feature.*;

/**
 * Defines a like filter, which checks to see if an attribute matches a REGEXP.
 *
 * @author Rob Hranac, Vision for New York
 * @version 
 */
public class FilterLike extends FilterDefault {

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
    public FilterLike () {
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
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Sets the multi wildcard for this FilterLike.
     *
     * @param wildcardMulti The multi wildcard for this pattern. 
     */
    public void setWildcardMulti(String wildcardMulti) {
        this.wildcardMulti = wildcardMulti;
    }

    /**
     * Sets the single wildcard for this FilterLike.
     *
     * @param wildcardSingle The single wildcard for this pattern. 
     */
    public void setWildcardSingle(String wildcardSingle) {
        this.wildcardSingle = wildcardSingle;
    }

    /**
     * Sets the escape string for this FilterLike.
     *
     * @param escape The escape string for this pattern. 
     */
    public void setEscape(String escape) {
        this.escape = escape;
    }

    /**
     * Determines whether or not a given feature matches this pattern.
     *
     * @param feature Specified feature to examine.
     * @return Flag confirming whether or not this feature is inside the filter.
     * @throws MalformedFilterException Filter is not internally consistent.
     */
    public boolean contains(Feature feature)
        throws MalformedFilterException {

        // Checks to insure that the attribute has been set
        if( attribute == null ) {
            throw new MalformedFilterException("Like filter missing expression.");
        }

        // The following things happen for both wildcards:
        //  (1) If a user-defined wildcard exists, replace with Java wildcard
        //  (2) If a user-defined escape exists, Java wildcard + user-escape
        //  Then, test for matching pattern and return result.
        else {
            if( wildcardSingle != null ) {
                pattern = pattern.replaceAll(wildcardSingle, "?");
                if( escape != null ) {
                    pattern = pattern.replaceAll(escape + "/?", wildcardSingle);
                }
            }
            if( wildcardMulti != null ) {
                pattern = pattern.replaceAll(wildcardMulti, "*");
                if( escape != null ) {
                    pattern = pattern.replaceAll(escape + "/*", wildcardMulti);
                }
            }

            // Note that this converts the atrribue (whatever it is) to a string
            //  for comparison.  Unlike the math or geometry filters, which
            //  require specific types to function correctly, this filter
            //  using the mandatory string representation in Java
            // Of course, this does not guarantee a meaningful result, but it
            //  does guarantee a valid result.
            return attribute.getValue(feature).toString().matches(pattern);
        }
    }
    
}
