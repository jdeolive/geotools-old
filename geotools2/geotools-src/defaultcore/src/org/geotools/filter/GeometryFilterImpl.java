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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.feature.Feature;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Implements a geometry filter.
 * 
 * <p>
 * This filter implements a relationship - of some sort -  between two geometry
 * expressions. Note that this comparison does not attempt to restict its
 * expressions to be meaningful.  This means that it considers itself a valid
 * filter as long as it contains two <b>geometry</b> sub-expressions. It is
 * also slightly less  restrictive than the OGC Filter specification because
 * it does not require that one sub-expression be an geometry attribute and
 * the other be a geometry literal.
 * </p>
 * 
 * <p>
 * In other words, you may use this filter to compare two geometries in the
 * same feature, such as: attributeA inside attributeB?  You may also compare
 * two literal geometries, although this is fairly meaningless, since it could
 * be reduced (ie. it is always either true or false).  This approach is very
 * similar to that taken in the FilterCompare class.
 * </p>
 *
 * @author Rob Hranac, TOPP
 * @version $Id: GeometryFilterImpl.java,v 1.13 2003/08/06 18:11:41 desruisseaux Exp $
 *
 * @task REVISIT: make this class (and all filters) immutable, implement
 *       cloneable and return new filters when calling addLeftGeometry and
 *       addRightG Issues to think through: would be cleaner immutability to
 *       have constructor called with left and right Geometries, but this does
 *       not jive with SAX parsing, which is one of the biggest uses of
 *       filters.  But the alternative is not incredibly efficient either, as
 *       there will be two filters that  are just thrown away every time we
 *       make a full geometry filter.  These issues extend to most filters, as
 *       just about all of them are mutable when creating them.  Other issue
 *       is that lots of code will need to  be changed for immutability.
 *       (comments by cholmes) - MUTABLE FACTORIES!  Sax and immutability.
 */
public class GeometryFilterImpl extends AbstractFilterImpl
    implements GeometryFilter {
    /** Class logger */
    private static final Logger LOGGER = 
         Logger.getLogger("org.geotools.filter");

    /** Holds the 'left' value of this comparison filter. */
    protected Expression leftGeometry = null;

    /** Holds the 'right' value of this comparison filter. */
    protected Expression rightGeometry = null;

    /**
     * Constructor with filter type.
     *
     * @param filterType The type of comparison.
     *
     * @throws IllegalFilterException Non-geometry type.
     */
    protected GeometryFilterImpl(short filterType)
        throws IllegalFilterException {
        if (isGeometryFilter(filterType)) {
            this.filterType = filterType;
        } else {
            throw new IllegalFilterException("Attempted to create geometry "
                + "filter with non-geometry type.");
        }
    }

    /**
     * Adds the 'left' value to this filter.
     *
     * @param leftGeometry Expression for 'left' value.
     *
     * @throws IllegalFilterException Filter is not internally consistent.
     *
     * @task REVISIT: make all filters immutable.
     */
    public void addLeftGeometry(Expression leftGeometry)
        throws IllegalFilterException {
        // Checks if this is geometry filter or not and handles appropriately
        if (DefaultExpression.isGeometryExpression(leftGeometry.getType())
                || permissiveConstruction) {
            this.leftGeometry = leftGeometry;
        } else {
            throw new IllegalFilterException("Attempted to add (left)"
                + " non-geometry expression" + " to geometry filter.");
        }
    }

    /**
     * Adds the 'right' value to this filter.
     *
     * @param rightGeometry Expression for 'right' value.
     *
     * @throws IllegalFilterException Filter is not internally consistent.
     *
     * @task REVISIT: make immutable.
     */
    public void addRightGeometry(Expression rightGeometry)
        throws IllegalFilterException {
        // Checks if this is math filter or not and handles appropriately
        if (DefaultExpression.isGeometryExpression(rightGeometry.getType())
                || permissiveConstruction) {
            this.rightGeometry = rightGeometry;
        } else {
            throw new IllegalFilterException("Attempted to add (right)"
                + " non-geometry" + "expression to geometry filter.");
        }
    }

    /**
     * Retrieves the expression on the left side of the comparison.
     *
     * @return the expression on the left.
     */
    public Expression getLeftGeometry() {
        return leftGeometry;
    }

    /**
     * Retrieves the expression on the right side of the comparison.
     *
     * @return the expression on the right.
     */
    public Expression getRightGeometry() {
        return rightGeometry;
    }

    /**
     * Determines whether or not a given feature is 'inside' this filter.
     *
     * @param feature Specified feature to examine.
     *
     * @return Flag confirming whether or not this feature is inside filter.
     */
    public boolean contains(Feature feature) {
        // Checks for error condition
        Geometry right = null;

        if (rightGeometry != null) {
            right = (Geometry) rightGeometry.getValue(feature);
        } else {
            right = feature.getDefaultGeometry();
        }

        Geometry left = null;

        if (leftGeometry != null) {
            Object obj = leftGeometry.getValue(feature);

            //LOGGER.finer("leftGeom = " + o.toString()); 
            left = (Geometry) obj;
        } else {
            left = feature.getDefaultGeometry();
        }

        // Handles all normal geometry cases
        if (filterType == GEOMETRY_EQUALS) {
            return left.equals(right);
        } else if (filterType == GEOMETRY_DISJOINT) {
            return left.disjoint(right);
        } else if (filterType == GEOMETRY_INTERSECTS) {
            return left.intersects(right);
        } else if (filterType == GEOMETRY_CROSSES) {
            return left.crosses(right);
        } else if (filterType == GEOMETRY_WITHIN) {
            return left.within(right);
        } else if (filterType == GEOMETRY_CONTAINS) {
            return left.contains(right);
        } else if (filterType == GEOMETRY_OVERLAPS) {
            return left.overlaps(right);
        } else if (filterType == GEOMETRY_BEYOND) {
            return left.within(right);
        } else if (filterType == GEOMETRY_BBOX) {
            Coordinate[] cr = right.getEnvelope().getCoordinates();
            Coordinate[] cl = left.getEnvelope().getCoordinates();

            if (left.getDimension() >= 1) {
                if ((cl[0].x >= cr[0].x) && (cl[2].x <= cr[2].x)
                        && (cl[0].y >= cr[0].y) && (cl[2].y <= cr[2].y)) {
                    // feature contained in the bbox
                    return true;
                } else if ((cl[0].x > cr[2].x) || (cl[2].x < cr[0].x)
                        || (cl[0].y > cr[2].y) || (cl[2].y < cr[0].y)) {
                    // feature outside the bbox
                    return false;
                } else {
                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.finer("Right: " + "[" + cr[0].x + "," + cr[0].y
                            + "]" + "-" + "[" + cr[3].x + "," + cr[3].y + "]");
                        LOGGER.finer("Left: " + "[" + cl[0].x + "," + cl[0].y
                            + "]" + "-" + "[" + cl[3].x + "," + cl[3].y + "]");
                    }

                    return left.intersects(right);
                }
            } else {
                return left.intersects(right);
            }

            // Note that this is a pretty permissive logic
            //  if the type has somehow been mis-set (can't happen externally)
            //  then true is returned in all cases
        } else {
            return true;
        }
    }

    /**
     * Return this filter as a string.
     *
     * @return String representation of this geometry filter.
     */
    public String toString() {
        String operator = null;

        // Handles all normal geometry cases
        if (filterType == GEOMETRY_EQUALS) {
            operator = " equals ";
        } else if (filterType == GEOMETRY_DISJOINT) {
            operator = " disjoint ";
        } else if (filterType == GEOMETRY_INTERSECTS) {
            operator = " intersects ";
        } else if (filterType == GEOMETRY_CROSSES) {
            operator = " crosses ";
        } else if (filterType == GEOMETRY_WITHIN) {
            operator = " within ";
        } else if (filterType == GEOMETRY_CONTAINS) {
            operator = " contains ";
        } else if (filterType == GEOMETRY_OVERLAPS) {
            operator = " overlaps ";
        } else if (filterType == GEOMETRY_BEYOND) {
            operator = " beyond ";
        } else if (filterType == GEOMETRY_BBOX) {
            operator = " bbox ";
        }

        if ((leftGeometry == null) && (rightGeometry == null)) {
            return "[ " + "null" + operator + "null" + " ]";
        } else if (leftGeometry == null) {
            return "[ " + "null" + operator + rightGeometry.toString() + " ]";
        } else if (rightGeometry == null) {
            return "[ " + leftGeometry.toString() + operator + "null" + " ]";
        }

        return "[ " + leftGeometry.toString() + operator
        + rightGeometry.toString() + " ]";
    }

    /**
     * Compares this filter to the specified object.  Returns true  if the
     * passed in object is the same as this filter.  Checks  to make sure the
     * filter types are the same as well as the left and right geometries.
     *
     * @param obj - the object to compare this GeometryFilter against.
     *
     * @return true if specified object is equal to this filter; else false
     */
    public boolean equals(Object obj) {
        if (obj instanceof GeometryFilterImpl) {
            GeometryFilterImpl geomFilter = (GeometryFilterImpl) obj;
            boolean isEqual = true;

            isEqual = geomFilter.getFilterType() == this.filterType;
            LOGGER.finest("filter type match:" + isEqual + "; in:"
                + geomFilter.getFilterType() + "; out:" + this.filterType);
            isEqual = (geomFilter.leftGeometry != null)
                ? (isEqual && geomFilter.leftGeometry.equals(this.leftGeometry))
                : (isEqual && (this.leftGeometry == null));
            LOGGER.finest("left geom match:" + isEqual + "; in:"
                + geomFilter.leftGeometry + "; out:" + this.leftGeometry);
            isEqual = (geomFilter.rightGeometry != null)
                ? (isEqual
                && geomFilter.rightGeometry.equals(this.rightGeometry))
                : (isEqual && (this.rightGeometry == null));
            LOGGER.finest("right geom match:" + isEqual + "; in:"
                + geomFilter.rightGeometry + "; out:" + this.rightGeometry);

            return isEqual;
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
        result = (37 * result) + filterType;
        result = (37 * result)
            + ((leftGeometry == null) ? 0 : leftGeometry.hashCode());
        result = (37 * result)
            + ((rightGeometry == null) ? 0 : rightGeometry.hashCode());

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
