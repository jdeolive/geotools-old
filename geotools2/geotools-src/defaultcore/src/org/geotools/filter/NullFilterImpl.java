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
 * Defines a null filter, which checks to see if an attribute is null.
 *
 * @author Rob Hranac, Vision for New York
 * @version $Id: NullFilterImpl.java,v 1.5 2003/07/22 22:41:08 cholmesny Exp $
 */
public class NullFilterImpl extends AbstractFilterImpl implements NullFilter {
    /** The null check value, which must be an attribute expression. */
    private Expression nullCheck = null;

    /**
     * Constructor which flags the operator as between.
     */
    protected NullFilterImpl() {
        filterType = NULL;
    }

    /**
     * Determines whether or not a given feature is 'inside' this filter.
     *
     * @param nullCheck The value of this
     *
     * @throws IllegalFilterException Filter is illegal.
     */
    public void nullCheckValue(Expression nullCheck)
        throws IllegalFilterException {
        if (nullCheck instanceof AttributeExpressionImpl) {
            this.nullCheck = nullCheck;
        } else {
            throw new IllegalFilterException(
                "Attempted to add non-attribute expression to a null filter.");
        }
    }

    /**
     * Returns the expression being checked for null.
     *
     * @return the Expression to null check.
     */
    public Expression getNullCheckValue() {
        return nullCheck;
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
        if (nullCheck == null) {
            return false;
        } else {
            return (nullCheck.getValue(feature) == null);
        }
    }

    /**
     * Returns a string representation of this filter.
     *
     * @return String representation of the null filter.
     */
    public String toString() {
        return "[ " + nullCheck.toString() + " is null ]";
    }

    /**
     * Compares this filter to the specified object.  Returns true  if the
     * passed in object is the same as this filter.  Checks  to make sure the
     * filter types, and the NullCheckValue are the same.
     *
     * @param obj - the object to compare this LikeFilter against.
     *
     * @return true if specified object is equal to this filter; false
     *         otherwise.
     */
    public boolean equals(Object obj) {
        if (obj.getClass() == this.getClass()) {
            NullFilterImpl nullFilter = (NullFilterImpl) obj;

            return ((nullFilter.getFilterType() == this.filterType)
            && nullFilter.getNullCheckValue().equals(this.nullCheck));
        } else {
            return false;
        }
    }

    /**
     * Override of hashCode method.
     *
     * @return a hash code value for this geometry filter.
     */
    public int hashCode() {
        int result = 17;
        result = (37 * result) + nullCheck.hashCode();

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
