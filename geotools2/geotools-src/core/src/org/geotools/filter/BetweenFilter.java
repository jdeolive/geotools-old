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
 * @version $Id: BetweenFilter.java,v 1.3 2003/08/07 19:55:21 cholmesny Exp $
 */
public interface BetweenFilter extends CompareFilter {

     /**
     * Determines whether or not a given feature is 'inside' this filter.
     *
     * @param feature Specified feature to examine.
     *
     * @return Whether or not this feature is inside the filter.
     */
    boolean contains(Feature feature);

    /**
     * Gets the middle value of the between.  Should generally be an 
     * AttributeExpression: 1 <= area <= 200 makes sense,
     * 1 <= 200 <= area should just use a less-than-or-equal filter.
     *
     * @return the expression in the middle.
     */
    Expression getMiddleValue();

     /**
     * Sets the values to be compared as between the left and right values.
     *
     * @param middleValue The expression to be compared.
     * @task REVISIT: rename to setMiddleValue?  You can't have more than 1.
     */
    void addMiddleValue(Expression middleValue);
}
