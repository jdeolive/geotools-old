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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Represents the Filter capabilities that are supported by a SQLEncoder.  Each
 * SQLEncoder class should have one static FilterCapabilities, representing
 * the filter encoding operations that it can successfully perform.
 *
 * @author Chris Holmes, TOPP
 */
public class FilterCapabilities {
    /** The Filter types (shorts from AbstractFilter) supported. */
    private List supportTypes;

    /**
     * No argument Constructor.
     */
    public FilterCapabilities() {
        supportTypes = new ArrayList();
    }

    /**
     * Adds a new support type to capabilities.
     *
     * @param type The AbstractFilter type that is supported
     */
    public void addType(short type) {
        supportTypes.add(new Short(type));
    }

    /**
     * Determines if the filter type passed in is supported.
     *
     * @param type The AbstractFilter type to be tested
     *
     * @return true if supported, false otherwise.
     */
    public boolean supports(short type) {
        return supportTypes.contains(new Short(type));
    }

    /**
     * Determines if the filter passed in is supported.
     *
     * @param filter The Filter to be tested.
     *
     * @return true if supported, false otherwise.
     */
    public boolean supports(Filter filter) {
        short filterType = filter.getFilterType();

        return supports(filterType);
    }

    /**
     * Determines if the filter and all its sub filters are supported.  Is most
     * important for logic filters, as they are the only ones with subFilters.
     * Null filters should not be used here, if nothing should be filtered
     * than Filter.NONE can be used.  Embedded nulls can be a particular
     * source of problems, buried in logic filters.
     *
     * @param filter the filter to be tested.
     *
     * @return true if all sub filters are supported, false otherwise.
     *
     * @throws IllegalArgumentException If a null filter is passed in.  As this
     *         function is recursive a null in a logic filter will also cause
     *         an error.
     */
    public boolean fullySupports(Filter filter) {
        boolean supports = true;

        if (filter == null) {
            throw new IllegalArgumentException("Null filters can not be "
                + "unpacked, did you mean " + "Filter.NONE?");
        }

        short filterType = filter.getFilterType();

        if (AbstractFilter.isLogicFilter(filterType)) {
            Iterator filters = ((LogicFilter) filter).getFilterIterator();
            Filter testFilter = null;

            //short testFtype = 0;
            while (filters.hasNext()) {
                testFilter = (Filter) filters.next();

                if (!(this.fullySupports(testFilter))) {
                    supports = false;
                }
            }
        } else {
            supports = this.supports(filter);
        }

        return supports;
    }
}
