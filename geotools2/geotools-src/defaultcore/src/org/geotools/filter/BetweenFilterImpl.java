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
 * Defines a 'between' filter (which is a specialized compare filter). A
 * between filter is just shorthand for a less-than-or-equal filter ANDed with
 * a greater-than-or-equal filter.  Arguably, this would be better handled
 * using those constructs, but the OGC filter specification creates its own
 * object for this, so we do as well.  An important note here is that a
 * between filter is actually a math filter, so its outer (left and right)
 * expressions must be math expressions.  This is enforced by the
 * FilterAbstract class, which considers a BETWEEN operator to be a math
 * filter.
 *
 * @author Rob Hranac, TOPP
 * @version $Id: BetweenFilterImpl.java,v 1.6 2003/07/23 16:11:18 cholmesny Exp $
 */
public class BetweenFilterImpl extends CompareFilterImpl
    implements BetweenFilter {
    /** The 'middle' value, which must be an attribute expression. */
    protected Expression middleValue = null;

    /**
     * Constructor which flags the operator as between.
     *
     * @throws IllegalFilterException DOCUMENT ME!
     */
    protected BetweenFilterImpl() throws IllegalFilterException {
        super(BETWEEN);
    }

    /**
     * Determines whether or not a given feature is 'inside' this filter.
     *
     * @param middleValue The value of this
     */
    public void addMiddleValue(Expression middleValue) {
        this.middleValue = middleValue;
    }

    /**
     * gets the middle value of the between.
     *
     * @return the expression in the middle.
     */
    public Expression getMiddleValue() {
        return middleValue;
    }

    /**
     * Determines whether or not a given feature is 'inside' this filter.
     *
     * @param feature Specified feature to examine.
     *
     * @return Flag confirming whether or not this feature is inside the
     *         filter.
     */
    public boolean contains(Feature feature) {
        if (middleValue == null) {
            return false;
        } else {
            double left = ((Number) leftValue.getValue(feature)).doubleValue();
            double right = ((Number) rightValue.getValue(feature)).
                doubleValue();
            double mid = ((Number) middleValue.getValue(feature)).doubleValue();

            return (left <= mid) && (right >= mid);
        }
    }

    /**
     * Returns a string representation of this filter.
     *
     * @return String representation of the between filter.
     */
    public String toString() {
        return "[ " + leftValue.toString() + " < " + middleValue.toString()
        + " < " + rightValue.toString() + " ]";
    }

    /**
     * Returns true if the passed in object is the same as this filter.  Checks
     * to make sure the filter types are the same as well as all three of the
     * values.
     *
     * @param oFilter the filter to test for eqaulity.
     *
     * @return True if the objects are equal.
     */
    public boolean equals(Object oFilter) {
        if (oFilter.getClass() == this.getClass()) {
            BetweenFilterImpl bFilter = (BetweenFilterImpl) oFilter;

            return ((bFilter.getFilterType() == this.filterType)
            && bFilter.getLeftValue().equals(this.leftValue)
            && bFilter.getMiddleValue().equals(this.middleValue)
            && bFilter.getRightValue().equals(this.rightValue));
        } else {
            return false;
        }
    }

    /**
     * Override of hashCode method.
     *
     * @return a code to hash this object by.
     */
    public int hashCode() {
        int result = 17;

        result = (37 * result)
            + ((leftValue == null) ? 0 : leftValue.hashCode());
        result = (37 * result)
            + ((middleValue == null) ? 0 : middleValue.hashCode());
        result = (37 * result)
            + ((rightValue == null) ? 0 : rightValue.hashCode());

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
