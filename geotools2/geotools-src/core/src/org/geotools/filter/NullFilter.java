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
 * @author Chris Holmes, TOPP
 * @version $Id: NullFilter.java,v 1.3 2003/08/07 21:30:36 cholmesny Exp $
 */
public interface NullFilter extends Filter {
    /**
     * Determines whether or not a given feature is 'inside' this filter.
     *
     * @param nullCheck The attribute expression to null check.
     *
     * @throws IllegalFilterException If attempting to add a non-attribute
     *         expression.
     *
     * @task REVISIT: change arg to AttributeExpression?
     * @task REVISIT: change name to setNullCheckValue.
     */
    void nullCheckValue(Expression nullCheck) throws IllegalFilterException;

    /**
     * Returns the expression being checked for null.
     *
     * @return the Expression to null check.
     */
    Expression getNullCheckValue();

    /**
     * Determines whether or not a given feature is null for the nullCheck
     * attribute.
     *
     * @param feature Specified feature to examine.
     *
     * @return Flag confirming whether or not this feature is inside the
     *         filter.
     */
    boolean contains(Feature feature);
}
