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

import org.geotools.feature.Feature;


/**
 * Defines a like filter, which checks to see if an attribute matches a REGEXP.
 *
 * @author Rob Hranac, Vision for New York
 * @version $Id: LikeFilter.java,v 1.5 2003/08/07 21:30:36 cholmesny Exp $
 */
public interface LikeFilter extends Filter {
    /**
     * Sets the match pattern for this FilterLike.
     *
     * @param pattern The string which contains the match pattern for this
     *        filter.
     * @param wildcardMulti The string that represents a mulitple character
     *        (1->n) wildcard.
     * @param wildcardSingle The string that represents a single character (1)
     *        wildcard.
     * @param escape The string that represents an escape character.
     */
    void setPattern(String pattern, String wildcardMulti,
        String wildcardSingle, String escape);

    /**
     * Getter for property wildcardMulti.
     *
     * @return Value of property wildcardMulti.
     */
    String getWildcardMulti();

    /**
     * Accessor for property escape.
     *
     * @return Value of property escape.
     */
    String getEscape();

    /**
     * Sets the match pattern for this FilterLike.
     *
     * @param p The expression which evaluates to the match pattern for this
     *        filter.
     * @param wildcardMulti The string that represents a mulitple character
     *        (1->n) wildcard.
     * @param wildcardSingle the string that represents a single character (1)
     *        wildcard.
     * @param escape The string that represents an escape character.
     */
    void setPattern(Expression p, String wildcardMulti, String wildcardSingle,
        String escape);

    /**
     * Accessor method to retrieve the pattern.
     *
     * @return the pattern being matched.
     */
    String getPattern();

    /**
     * Sets the expression to be evalutated as being like the pattern.
     *
     * @param attribute The value of the attribute for comparison.
     *
     * @throws IllegalFilterException Filter is illegal, adding something other
     *         than a string attribute.
     */
    void setValue(Expression attribute) throws IllegalFilterException;

    /**
     * Gets the Value (left hand side) of this filter.
     *
     * @return The expression that is the value of the filter.
     */
    Expression getValue();

    /**
     * Accessor for property wildcardSingle.
     *
     * @return Value of property wildcardSingle.
     */
    String getWildcardSingle();

    /**
     * Determines whether or not a given feature matches this pattern.
     *
     * @param feature Specified feature to examine.
     *
     * @return Flag confirming whether or not this feature is inside the
     *         filter.
     *
     * @task REVISIT: could the pattern be null such that a null = null?
     */
    boolean contains(Feature feature);
}
