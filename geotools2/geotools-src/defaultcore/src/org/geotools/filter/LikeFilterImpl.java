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


// Geotools dependencies
import org.geotools.feature.Feature;
import java.util.logging.Logger;

// J2SE dependencies
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Defines a like filter, which checks to see if an attribute matches a REGEXP.
 *
 * @author Rob Hranac, Vision for New York
 * @version $Id: LikeFilterImpl.java,v 1.7 2003/07/23 18:15:03 cholmesny Exp $
 */
public class LikeFilterImpl extends AbstractFilterImpl implements LikeFilter {
    /** The logger for the default core module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");

    /** The attribute value, which must be an attribute expression. */
    private Expression attribute = null;

    /** The (limited) REGEXP pattern. */
    private String pattern = null;

    /** The single wildcard for the REGEXP pattern. */
    private String wildcardSingle = ".?";

    /** The escaped version of the single wildcard for the REGEXP pattern. */
    private String escapedWildcardSingle = "\\.\\?";

    /** The multiple wildcard for the REGEXP pattern. */
    private String wildcardMulti = ".*";

    /** The escaped version of the multiple wildcard for the REGEXP pattern. */
    private String escapedWildcardMulti = "\\.\\*";

    /** The escape sequence for the REGEXP pattern. */
    private String escape = "\\";

    /** the pattern compiled into a java regex */
    private Pattern compPattern = null;

    /** The matcher to match patterns with. */
    private Matcher matcher = null;

    /**
     * Constructor which flags the operator as like.
     */
    protected LikeFilterImpl() {
        filterType = LIKE;
    }

    /**
     * Sets the expression to be evalutated as being like the pattern
     *
     * @param attribute The value of the attribute for comparison.
     *
     * @throws IllegalFilterException Filter is illegal.
     */
    public void setValue(Expression attribute) throws IllegalFilterException {
        if ((attribute.getType() != DefaultExpression.ATTRIBUTE_STRING)
                || permissiveConstruction) {
            this.attribute = attribute;
        } else {
            throw new IllegalFilterException(
                "Attempted to add something other than a string attribute "
                + "expression to a like filter.");
        }
    }

    /**
     * gets the Value (left hand side) of this filter
     *
     * @return the expression that is the value of the filter
     */
    public Expression getValue() {
        return attribute;
    }

    /**
     * Sets the match pattern for this FilterLike.
     *
     * @param p the expression which evaluates to the match pattern for this
     *        filter
     * @param wildcardMulti the string that represents a mulitple character
     *        (1->n) wildcard
     * @param wildcardSingle the string that represents a single character (1)
     *        wildcard
     * @param escape the string that represents an escape character
     */
    public void setPattern(Expression p, String wildcardMulti,
        String wildcardSingle, String escape) {
        String pattern = p.toString();
        setPattern(pattern, wildcardMulti, wildcardSingle, escape);
    }

    /**
     * Sets the match pattern for this FilterLike.
     *
     * @param pattern the string which contains the match pattern for this
     *        filter
     * @param wildcardMulti the string that represents a mulitple character
     *        (1->n) wildcard
     * @param wildcardSingle the string that represents a single character (1)
     *        wildcard
     * @param escape the string that represents an escape character
     */
    public void setPattern(String pattern, String wildcardMulti,
        String wildcardSingle, String escape) {
        // The following things happen for both wildcards:
        //  (1) If a user-defined wildcard exists, replace with Java wildcard
        //  (2) If a user-defined escape exists, Java wildcard + user-escape
        //  Then, test for matching pattern and return result.
        char esc = escape.charAt(0);
        LOGGER.finer("wildcard " + wildcardMulti + " single " + wildcardSingle);
        LOGGER.finer("escape " + escape + " esc " + esc + " esc == \\ "
            + (esc == '\\'));

        escapedWildcardMulti = fixSpecials(wildcardMulti);
        escapedWildcardSingle = fixSpecials(wildcardSingle);

        LOGGER.finer("after fixing: wildcard " + wildcardMulti + " single "
            + wildcardSingle + " escape " + escape);
        LOGGER.finer("start pattern = " + pattern);

        // escape any special chars which are not our wildcards
        StringBuffer tmp = new StringBuffer("");

        boolean escapedMode = false;

        for (int i = 0; i < pattern.length(); i++) {
            char chr = pattern.charAt(i);
            LOGGER.finer("tmp = " + tmp + " looking at " + chr);

            if (pattern.regionMatches(false, i, escape, 0, escape.length())) {
                // skip the escape string
                LOGGER.finer("escape ");
                escapedMode = true;

                i += escape.length();
                chr = pattern.charAt(i);
            }

            if (pattern.regionMatches(false, i, wildcardMulti, 0,
                        wildcardMulti.length())) { // replace with java wildcard
                LOGGER.finer("multi wildcard");

                if (escapedMode) {
                    LOGGER.finer("escaped ");
                    tmp.append(escapedWildcardMulti);
                } else {
                    tmp.append(this.wildcardMulti);
                }

                i += (wildcardMulti.length() - 1);
                escapedMode = false;

                continue;
            }

            if (pattern.regionMatches(false, i, wildcardSingle, 0,
                        wildcardSingle.length())) {
                // replace with java single wild card
                LOGGER.finer("single wildcard");

                if (escapedMode) {
                    LOGGER.finer("escaped ");
                    tmp.append(escapedWildcardSingle);
                } else {
                    tmp.append(this.wildcardSingle);
                }

                i += (wildcardSingle.length() - 1);
                escapedMode = false;

                continue;
            }

            if (isSpecial(chr)) {
                LOGGER.finer("special");
                tmp.append(this.escape + chr);
                escapedMode = false;

                continue;
            }

            tmp.append(chr);
            escapedMode = false;
        }

        this.pattern = tmp.toString();
        LOGGER.finer("final pattern " + this.pattern);
        compPattern = java.util.regex.Pattern.compile(this.pattern);
        matcher = compPattern.matcher("");
    }

    /**
     * Accessor method to retrieve the pattern.
     *
     * @return the pattern being matched.
     */
    public String getPattern() {
        return this.pattern;
    }

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
    public boolean contains(Feature feature) {
        // Checks to ensure that the attribute has been set
        if (attribute == null) {
            return false;
        } else {
            // Note that this converts the attribute to a string
            //  for comparison.  Unlike the math or geometry filters, which
            //  require specific types to function correctly, this filter
            //  using the mandatory string representation in Java
            // Of course, this does not guarantee a meaningful result, but it
            //  does guarantee a valid result.
            //LOGGER.finest("pattern: " + pattern);
            //LOGGER.finest("string: " + attribute.getValue(feature));
            //return attribute.getValue(feature).toString().matches(pattern);
            Object value = attribute.getValue(feature);

            if (null == value) {
                return false;
            }

            matcher.reset(attribute.getValue(feature).toString());

            return matcher.matches();
        }
    }

    /**
     * Return this filter as a string.
     *
     * @return String representation of this like filter.
     */
    public String toString() {
        return "[ " + attribute.toString() + " is like " + pattern + " ]";
    }

    /**
     * Getter for property escape.
     *
     * @return Value of property escape.
     */
    public java.lang.String getEscape() {
        return escape;
    }

    /**
     * Getter for property wildcardMulti.
     *
     * @return Value of property wildcardMulti.
     */
    public java.lang.String getWildcardMulti() {
        return wildcardMulti;
    }

    /**
     * Getter for property wildcardSingle.
     *
     * @return Value of property wildcardSingle.
     */
    public java.lang.String getWildcardSingle() {
        return wildcardSingle;
    }

    /**
     * convienience method to determine if a character is special to the regex
     * system.
     *
     * @param chr the character to test
     *
     * @return is the character a special character.
     */
    private boolean isSpecial(final char chr) {
        return ((chr == '.') || (chr == '?') || (chr == '*') || (chr == '^')
        || (chr == '$') || (chr == '+') || (chr == '[') || (chr == ']')
        || (chr == '(') || (chr == ')') || (chr == '|') || (chr == '\\')
        || (chr == '&'));
    }

    /**
     * convienience method to escape any character that is special to the regex
     * system.
     *
     * @param inString the string to fix
     *
     * @return the fixed string
     */
    private String fixSpecials(final String inString) {
        StringBuffer tmp = new StringBuffer("");

        for (int i = 0; i < inString.length(); i++) {
            char chr = inString.charAt(i);

            if (isSpecial(chr)) {
                tmp.append(this.escape + chr);
            } else {
                tmp.append(chr);
            }
        }

        return tmp.toString();
    }

    /**
     * Compares this filter to the specified object.  Returns true  if the
     * passed in object is the same as this filter.  Checks  to make sure the
     * filter types, the value, and the pattern are the same. &
     *
     * @param obj - the object to compare this LikeFilter against.
     *
     * @return true if specified object is equal to this filter; false
     *         otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof LikeFilterImpl) {
            LikeFilterImpl lFilter = (LikeFilterImpl) obj;

            //REVISIT: check for nulls.
            return ((lFilter.getFilterType() == this.filterType)
            && lFilter.getValue().equals(this.attribute)
            && lFilter.getPattern().equals(this.pattern));
        } else {
            return false;
        }
    }

    /**
     * Override of hashCode method.
     *
     * @return the hash code for this like filter implementation.
     */
    public int hashCode() {
        int result = 17;
        result = (37 * result)
            + ((attribute == null) ? 0 : attribute.hashCode());
        result = (37 * result) + ((pattern == null) ? 0 : pattern.hashCode());

        return result;
    }

    /**
     * Used by FilterVisitors to perform some action on this filter instance.
     * Typicaly used by Filter decoders, but may also be used by any thing
     * which needs infomration from filter structure. Implementations should
     * always call: visitor.visit(this); It is importatant that this is not
     * left to a parent class unless the parents API is identical.
     *
     * @param visitor The visitor which requires access to this filter, the
     *        method must call visitor.visit(this);
     */
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }
}
