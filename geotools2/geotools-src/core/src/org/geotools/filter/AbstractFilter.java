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

// Geotools dependencies
import java.util.logging.Logger;


/**
 * Implements Filter interface, with constants and default behaviors for
 * methods.
 *
 * @author Rob Hranac, Vision for New York
 * @version $Id: AbstractFilter.java,v 1.11 2004/03/15 20:15:53 aaime Exp $
 */
public abstract class AbstractFilter implements Filter {
    /** The logger for the default core module. */
    protected static final Logger LOGGER = Logger.getLogger("org.geotools.core");


    /** Defines filter type (all valid types defined below). */
    protected short filterType;

    /** Sets the permissiveness of the filter construction handling. */
    protected boolean permissiveConstruction = true;

    /**
     * Implements a 'contained by' check for a given feature, defaulting to
     * true.
     *
     * @param feature Specified feature to examine.
     *
     * @return Result of 'contains' test.
     */
    public abstract boolean contains(Feature feature);

    /* todo: replace public abstract void accept(FilterVisitor visitor); */
    /* ************************************************************************
     * Following static methods check for certain aggregate types, based on
     * (above) declared types.  Note that these aggregate types do not
     * necessarily map directly to the sub-classes of FilterDefault.  In most,
     * but not all, cases, a single class implements an aggregate type.
     * However, there are aggregate types that are implemented by multiple
     * classes (ie. the Math type is implemented by two seperate classes).
     * ***********************************************************************/

    /**
     * Checks to see if passed type is logic.
     *
     * @param filterType Type of filter for check.
     *
     * @return Whether or not this is a logic filter type.
     */
    protected static boolean isLogicFilter(short filterType) {
        LOGGER.entering("AbstractFilter", "isLogicFilter", new Short(filterType));

        return ((filterType == LOGIC_OR) || (filterType == LOGIC_AND)
        || (filterType == LOGIC_NOT));
    }

    /**
     * Checks to see if passed type is math.
     *
     * @param filterType Type of filter for check.
     *
     * @return Whether or not this is a math filter type.
     */
    protected static boolean isMathFilter(short filterType) {
        return ((filterType == COMPARE_LESS_THAN)
        || (filterType == COMPARE_GREATER_THAN)
        || (filterType == COMPARE_LESS_THAN_EQUAL)
        || (filterType == COMPARE_GREATER_THAN_EQUAL));
    }

    /**
     * Checks to see if passed type is compare.
     *
     * @param filterType Type of filter for check.
     *
     * @return Whether or not this is a compare filter type.
     */
    protected static boolean isCompareFilter(short filterType) {
        return ((isMathFilter(filterType)) || (filterType == COMPARE_EQUALS)
        || (filterType == BETWEEN) || (filterType == COMPARE_NOT_EQUALS));
    }

    /**
     * Checks to see if passed type is geometry.
     *
     * @param filterType Type of filter for check.
     *
     * @return Whether or not this is a geometry filter type.
     */
    protected static boolean isGeometryFilter(short filterType) {
        return ((filterType == GEOMETRY_BBOX)
        || (filterType == GEOMETRY_EQUALS) || (filterType == GEOMETRY_DISJOINT)
        || (filterType == GEOMETRY_TOUCHES)
        || (filterType == GEOMETRY_INTERSECTS)
        || (filterType == GEOMETRY_CROSSES) || (filterType == GEOMETRY_WITHIN)
        || (filterType == GEOMETRY_CONTAINS)
        || (filterType == GEOMETRY_OVERLAPS)
        || (filterType == GEOMETRY_DWITHIN) || (filterType == GEOMETRY_BEYOND));
    }

    /**
     * Checks to see if passed type is geometry distance type.
     *
     * @param filterType Type of filter for check.
     *
     * @return Whether or not this is a geometry filter type.
     */
    protected static boolean isGeometryDistanceFilter(short filterType) {
        return ((filterType == GEOMETRY_DWITHIN)
        || (filterType == GEOMETRY_BEYOND));
    }

    /**
     * Checks to see if passed type is logic.
     *
     * @param filterType Type of filter for check.
     *
     * @return Whether or not this is a logic filter type.
     */
    protected static boolean isSimpleFilter(short filterType) {
        return (isCompareFilter(filterType) || isGeometryFilter(filterType)
        || (filterType == NULL) || (filterType == FID) || (filterType == LIKE));
    }

    /**
     * Retrieves the type of filter.
     *
     * @return a short representation of the filter type.
     */
    public short getFilterType() {
        return filterType;
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
    public abstract void accept(FilterVisitor visitor);
}
