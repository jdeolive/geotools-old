/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2003, Institut de Recherche pour le Développement
 * (C) 1998, Pêches et Océans Canada
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */

package org.geotools.renderer.geom;

// J2SE dependencies

import java.awt.geom.Line2D;

import java.awt.geom.Point2D;

// Geotools dependencies

import org.geotools.units.Unit;
import org.geotools.pt.CoordinatePoint;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.TransformException;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.ct.CoordinateTransformationFactory;


/**
 * Coordinate associated with an intersection between two lines. This class is reseved for internal
 * use in order to determine how we should reclose geometric shapes of islands and continents.
 * The point memorised by this class will come from the intersection of two lines:
 * one of the edges of the map (generally one of the 4 sides of a rectangle, but it could be another
 * geometric shape) with a line passing through the two first or the two last points of the 
 * shore line.  We will call the first line (that of the map edge) "<code>line</code>".
 * This class will memorise the scalar product between a vector passing through the first point
 * of <code>line</code> and the intersection point with a vector passing through the first and last
 * points of <code>line</code>. This scalar product can be viewed as a sort of measure of the distance
 * between the start of <code>line</code> and the intersection point.
 *
 * @version $Id: IntersectionPoint.java,v 1.2 2003/02/19 20:21:14 jmacgill Exp $
 * @author Martin Desruisseaux
 */

final class IntersectionPoint extends Point2D.Double implements Comparable {

    /**
     * Number of the edge on which this point was found. This information is left to the
     * discretion of the programmer, who will put in the information he wishes.
     * This number could be useful to help us later refind the line on which the intersection
     * point was found.
     */

    int border;



    /**
     * Distance squared between the intersection point and the point that was closest to it.
     * This information is not used by this class, except in the method {@link #toString}.
     * It is useful for debugging purposes, but also for choosing which intersection point to
     * delete if there are too many.  We will delete the point which is found furthest from 
     * its border.
     */

    double minDistanceSq = java.lang.Double.NaN;



    /**
     * Scalar product between the line on which the intersection point was found and a vector
     * going from the start of this line to the intersection point.
     */

    double scalarProduct;



    /**
     * Segment of shoreline to which the line with which we calculated an
     * intersection point belonged.
     */

    Polygon path;



    /**
     * Indicates whether the intersection point was calculated from the two first or the two last
     * points of the shoreline. If <code>append</code> has the value <code>true</code>, this means
     * that the intersection was calculated from the two last points of the shoreline.  To
     * reclose the geometric shape of the island or continent would imply therefore that we add
     * points to the end of the shoreline ("append"), as opposed to adding points to the start of
     * the shoreline ("prepend").
     */

    boolean append;



    /**
     * This point's coordinate system. This information is only used by the method
     * {@link #toString}, so that a coordinate can be written in latitude and longitude.
     */

    CoordinateSystem coordinateSystem;



    /**
     * Constructs a point initialised at (0,0).
     */

    public IntersectionPoint() {

    }



    /**
     * Constructs a point initialised at the specified position.
     */

    public IntersectionPoint(final Point2D point) {

        super(point.getX(), point.getY());

    }



    /**
     * Memorises in this object the position of the specified point. The scalar product
     * of this point with the line <code>line</code> will also be calculated and placed
     * in the field {@link #scalarProduct}.
     *
     * @param point  Coordinates of the intersection.
     * @param line   Coordinates of the line on which the intersection <code>point</code> was
     *               found.
     * @param border Number of the line <code>line</code>. This information will be memorised in
     *               the field {@link #border} and is left to the discretion of the programmer.
     *               It is recommended to use a unique number for each line <code>line</code>,
     *               which grow in the same order as the lines <code>line</code> are swept.
     */

    final void setLocation(final Point2D point, final Line2D.Double line, final int border) {

        super.setLocation(point);

        final double dx = line.x2-line.x1;

        final double dy = line.y2-line.y1;

        scalarProduct = ((x-line.x1)*dx+(y-line.y1)*dy) / Math.sqrt(dx*dx + dy*dy);

        this.border = border;

    }



    /**
     * Compares this point with another. This comparison only involves
     * the position of these points on a particular segment. It will allow
     * the points to be classified in a clockwise fashion, or anticlockwise
     * depending on the way in which {@link PathIterator} is implemented.
     *
     * @param o Another intersection point with which to compare this one.
     * @return -1, 0 or +1 depending on whether this point, precedes, equals or
     *         follows point <code>o</code> in a particular direction (generally  
     *         clockwise).
     */

    public int compareTo(final IntersectionPoint pt) {

        if (border < pt.border) return -1;

        if (border > pt.border) return +1;

        if (scalarProduct < pt.scalarProduct) return -1;

        if (scalarProduct > pt.scalarProduct) return +1;

        return 0;

    }



    /**
     * Compares this point with another. This comparison only imvolves
     * the position of these points on a particular segment. It will allow
     * the classification of the points in a clockwise fashion, or anticlockwise
     * depending on the way in which {@link PathIterator} is implemented.
     *
     * @param o Another intersection point with which to compare this one.
     * @return -1, 0 or +1 depending on whether this point precedes, equals or follows
     *         point <code>o</code> in a particular direction (generally clockwise).
     */

    public int compareTo(Object o) {

        return compareTo((IntersectionPoint) o);

    }



    /**
     * Indicates whether this intersection point is identical to point <code>o</code>.
     * This method is defined to be coherent with {@link #compareTo}, but isn't used.
     *
     * @return <code>true</code> if this intersection point is the same as <code>o</code>.
     */

    public boolean equals(final Object o) {

        if (o instanceof IntersectionPoint) {

            return compareTo((IntersectionPoint) o) == 0;

        } else {

            return false;

        }

    }



    /**
     * Returns an almost unique code for this intersection point, based on
     * the scalar product and the line number. This code will be coherent
     * with the method {@link #equals}.
     *
     * @return An almost unique number for this intersection point.
     */

    public int hashCode() {

        final long bits = java.lang.Double.doubleToLongBits(scalarProduct);

        return border ^ (int)bits ^ (int)(bits >>> 32);

    }



    /**
     * Sends a character string representation of this intersection
     * point (for debugging purposes only).
     *
     * @return Character string representing this intersection point.
     */

    public String toString() {

        final CoordinateSystem WGS84 = GeographicCoordinateSystem.WGS84;

        final StringBuffer buffer = new StringBuffer("IntersectionPoint[");

        if (coordinateSystem != null) {

            try {

                CoordinatePoint coord = new CoordinatePoint(this);

                coord = CoordinateTransformationFactory.getDefault()

                                            .createFromCoordinateSystems(coordinateSystem, WGS84)

                                            .getMathTransform().transform(coord, coord);

                buffer.append(coord);

            } catch (TransformException exception) {

                buffer.append("error");

            }

        } else {

            buffer.append((float) x);

            buffer.append(' ');

            buffer.append((float) y);

        }

        buffer.append(']');

        if (!java.lang.Double.isNaN(minDistanceSq)) {

            buffer.append(" at ");

            buffer.append((float) Math.sqrt(minDistanceSq));

        }

        if (coordinateSystem != null) {

            buffer.append(' ');

            buffer.append(coordinateSystem.getUnits(0));

        }

        buffer.append(" from #");

        buffer.append(border);

        buffer.append(" (");

        buffer.append((float) scalarProduct);

        buffer.append(')');

        return buffer.toString();

    }

}

