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

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.*;
import org.geotools.feature.*;


/**
 * Defines geometry filters with a distance element.
 * 
 * <p>
 * These filters are defined in the filter spec by the DistanceBufferType,
 * which contains an additioinal field for a distance.  The two filters that
 * use the distance buffer type are Beyond and DWithin.
 * </p>
 * 
 * <p>
 * From the spec: The spatial operators DWithin and Beyond test whether the
 * value of a geometric property is within or beyond a specified distance of
 * the specified literal geometric value.  Distance values are expressed
 * using the Distance element.
 * </p>
 * 
 * <p>
 * For now this code does not take into account the units of distance,  we will
 * assume that the filter units are the same as the geometry being filtered,
 * and that they are cartesian.
 * </p>
 * 
 * <p></p>
 *
 * @author Chris Holmes, TOPP
 * @version $Id: CartesianDistanceFilter.java,v 1.1 2003/06/02 23:26:59 cholmesny Exp $
 *
 * @task REVISIT: add units for distance.
 */
public class CartesianDistanceFilter extends GeometryFilterImpl
    implements GeometryDistanceFilter {
    /** The distance value */
    protected double distance;

    /**
     * Constructor which flags the operator as between.
     *
     * @param filterType The type of filter to create - dwithin and beyond are
     * allowed.
     *
     * @throws IllegalFilterException If a filter other than dwithin or beyond
     * is attempted.
     */
    protected CartesianDistanceFilter(short filterType)
        throws IllegalFilterException {
        super(filterType);

        if (isGeometryDistanceFilter(filterType)) {
            this.filterType = filterType;
        } else {
            throw new IllegalFilterException("Attempted to create distance " +
                "geometry filter with nondistance" + " geometry type.");
        }
    }

    /**
     * Sets the distance allowed by this filter.
     *
     * @param distance the length beyond which this filter is valid or not.
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * Gets the distance allowed by this filter.
     *
     * @return distance the length beyond which this filter is valid or not.
     */
    public double getDistance() {
        return distance;
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
        Geometry right = null;

        if (rightGeometry != null) {
            right = (Geometry) rightGeometry.getValue(feature);
        } else {
            right = feature.getDefaultGeometry();
        }

        Geometry left = null;

        if (leftGeometry != null) {
            Object o = leftGeometry.getValue(feature);

            //LOGGER.finer("leftGeom = " + o.toString()); 
            left = (Geometry) o;
        } else {
            left = feature.getDefaultGeometry();
        }

        // Handles all normal geometry cases
        if (filterType == GEOMETRY_BEYOND) {
            return !left.isWithinDistance(right, distance);

            //return left.beyond(right);
        } else if (filterType == GEOMETRY_DWITHIN) {
            return left.isWithinDistance(right, distance);
        }
        // Note that this is a pretty permissive logic
        //  if the type has somehow been mis-set (can't happen externally)
        //  then true is returned in all cases
        else {
            return true;
        }
    }

    /**
     * Returns a string representation of this filter.
     *
     * @return String representation of the between filter.
     */
    public String toString() {
        String operator = null;

        // Handles all normal geometry cases
        if (filterType == GEOMETRY_BEYOND) {
            operator = " beyond ";
        } else if (filterType == GEOMETRY_DWITHIN) {
            operator = " dwithin ";
        }

        if ((leftGeometry == null) && (rightGeometry == null)) {
            return "[ " + "null" + operator + "null" + " ]";
        } else if (leftGeometry == null) {
            return "[ " + "null" + operator + rightGeometry.toString() + " ]";
        } else if (rightGeometry == null) {
            return "[ " + leftGeometry.toString() + operator + "null" + " ]";
        }

        return "[ " + leftGeometry.toString() + operator +
        rightGeometry.toString() + ", distance: " + distance + " ]";
    }

    /**
     * Returns true if the passed in object is the same as this filter.  Checks
     * to make sure the filter types are the same as well as all three of the
     * values.
     *
     * @param oFilter The filter to test equality against.
     *
     * @return True if the objects are equal.
     */
    public boolean equals(Object oFilter) {
        return super.equals(oFilter) && (distance == distance);
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
