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
/*
 * Point.java
 *
 * Created on 1 novembre 2003, 10.25
 */
package org.geotools.renderer.geom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.sfs.SFSPoint;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.CannotCreateTransformException;
import org.geotools.ct.CoordinateTransformation;
import org.geotools.ct.MathTransform;
import org.geotools.ct.TransformException;
import org.geotools.math.Statistics;
import org.geotools.pt.CoordinatePoint;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.Utilities;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;


/**
 * A wrapper around a single JTS Coordinate object to adapt it to the the
 * Geometry hierarchy.
 *
 * @author Andrea Aime
 * @version $Id: Point.java,v 1.1 2003/11/01 17:34:28 aaime Exp $
 */
public class Point extends Geometry {
    /**
     * Last coordinate transformation used for computing {@link
     * #coordinateTransform}. Used in order to avoid the costly call to {@link
     * CoordinateSystemFactory} methods when the same transform is requested
     * many consecutive time, which is a very common situation.
     */
    private static CoordinateTransformation lastCoordinateTransform = getIdentityTransform(DEFAULT_COORDINATE_SYSTEM);

    /** The wrapped JTS coordinate object. */
    private Coordinate coord;

    /**
     * The transformed coordinates if reprojection is needed, null if the
     * CoordinateTransform is an indentity one.
     */
    private float[] transformedPoint;

    /**
     * The coordinate transformation used to reproject points, if necessary.
     * Also holds references to the original and current coordinate system.
     */
    private CoordinateTransformation ct;

    /**
     * Creates a new instance of Point
     *
     * @param coord The point coordinates
     * @param ct The coordinate transformation to be applied to the coordinates
     *        (which are supposed to be unprojected)
     */
    public Point(Coordinate coord, CoordinateTransformation ct) {
        this.coord = coord;
        this.ct = ct;
    }

    /**
     * Creates a new instance of Point. An identity coordinate transformation
     * will be created trasparently.
     *
     * @param coord The point coordinates
     * @param cs The source coordinate system.
     */
    public Point(Coordinate coord, CoordinateSystem cs) {
        this.coord = coord;
        this.ct = getIdentityTransform(getCoordinateSystem2D(cs));
    }

    /**
     * Point compression is not supported as it makes no sense for points.
     *
     * @param level The compression level (or algorithm) to use. See the {@link
     *        CompressionLevel} javadoc for an explanation of available
     *        algorithms.
     *
     * @return A <em>estimation</em> of the compression rate. Will always be
     *         1.0 since no compression is operated
     */
    public float compress(CompressionLevel level) {
        return 1.0f;
    }

    /**
     * This method returns true if the shape is another <code>Point</code>
     * object with  the same coordinates, false otherwise. The point's
     * coordinates must be expressed  according to the current coordinate
     * system, that is {@link #getCoordinateSystem()}.
     */
    public boolean contains(java.awt.Shape shape) {
        if (shape instanceof Point) {
            Point p = (Point) shape;

            return (p.getX() == getX()) && (p.getY() == getY());
        } else {
            return false;
        }
    }

    /**
     * This method returns true if <code>p</code> has the same coordinates as
     * this object,  false otherwise. The point <code>p</code> coordinates
     * must be expressed  according to the current coordinate system, that is
     * {@link #getCoordinateSystem()}.
     *
     */
    public boolean contains(java.awt.geom.Point2D p) {
        return (p.getX() == getX()) && (p.getY() == getY());
    }

    /**
     * Returns the bounds of this points, that is, an immutable and empty
     * Rectangle2D centered on the current coordinates. Point reprojection
     * will change rectangle coordinates too.
     *
     */
    public java.awt.geom.Rectangle2D getBounds2D() {
        return new Point.PointBound();
    }

    /**
     * Returns a path iterator for this point.
     */
    public java.awt.geom.PathIterator getPathIterator(
        java.awt.geom.AffineTransform at) {
        return new Point.PointPathIterator(at);
    }

    /**
     * Returns the number of coordinates contained in this geometry, as such,
     * the result will always be 1
     */
    public int getPointCount() {
        return 1;
    }

    /**
     * Implemented for compatibility, but it makes no sense to compute
     * resolution for a single point
     */
    public Statistics getResolution() {
        return new Statistics();
    }

    /**
     * Returns true if the shape contains the point
     */
    public boolean intersects(java.awt.Shape shape) {
        return shape.contains(getX(), getY());
    }

    /**
     * Emtpy method, provided for compatibility with base class.
     */
    public void setResolution(double resolution)
        throws org.geotools.ct.TransformException, 
            UnmodifiableGeometryException {
        // nothing to do...
    }

    /**
     * Returns the x coordinate of the point, projected to the new coordinate
     * system if  the user has specified an non identity CoordinateTransform
     */
    public float getX() {
        if (ct.getMathTransform().isIdentity()) {
            return (float) coord.x;
        } else {
            return transformedPoint[0];
        }
    }

    /**
     * Returns the y coordinate of the point, projected to the new coordinate
     * system if  the user has specified an non identity CoordinateTransform
     */
    public float getY() {
        if (ct.getMathTransform().isIdentity()) {
            return (float) coord.y;
        } else {
            return transformedPoint[1];
        }
    }

    /**
     * Always returns false, the point will never be frozen
     *
     * @see Geometry#isFrozen()
     */
    public boolean isFrozen() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ////////////                                                                       ////////////
    ////////////          C O O R D I N A T E   S Y S T E M S   S E T T I N G          ////////////
    ////////////                                                                       ////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Same as {@link CTSUtilities#getCoordinateSystem2D}, but wraps the {@link
     * TransformException} into an {@link IllegalArgumentException}. Used for
     * constructors only. Other methods still use the method throwing a
     * transform exception.
     *
     * @param cs The coordinate system
     *
     * @return The correspondent CoordinateSystem2D
     *
     * @throws IllegalArgumentException if a transformation exception occurs
     */
    private static CoordinateSystem getCoordinateSystem2D(
        final CoordinateSystem cs) throws IllegalArgumentException {
        try {
            return CTSUtilities.getCoordinateSystem2D(cs);
        } catch (TransformException exception) {
            throw new IllegalArgumentException(exception.getLocalizedMessage());
        }
    }

    /**
     * @return the native coordinate system of {@link #data}'s points, or
     *         <code>null</code> if unknown.
     */
    private CoordinateSystem getInternalCS() {
        // copy 'coordinateTransform' reference in order to avoid synchronization
        final CoordinateTransformation coordinateTransform = this.ct;

        return (ct != null) ? ct.getSourceCS() : null;
    }

    /**
     * Returns the polyline's coordinate system, or <code>null</code> if
     * unknown.
     */
    public CoordinateSystem getCoordinateSystem() {
        // copy 'coordinateTransform' reference in order to avoid synchronization
        final CoordinateTransformation ct = this.ct;

        return (ct != null) ? ct.getTargetCS() : null;
    }

    /**
     * Returns the transform from coordinate system used by {@link #data} to
     * the specified coordinate system. If at least one of the coordinate
     * systems is unknown, this method returns <code>null</code>.
     *
     * @throws CannotCreateTransformException If the transform cannot be
     *         created.
     */
    final CoordinateTransformation getTransformationFromInternalCS(
        final CoordinateSystem cs) throws CannotCreateTransformException {
        // copy 'coordinateTransform' reference in order to avoid synchronization
        CoordinateTransformation ct = this.ct;

        if ((cs != null) && (ct != null)) {
            if (cs.equals(ct.getTargetCS(), false)) {
                return ct;
            }

            final CoordinateSystem internalCS = ct.getSourceCS();
            ct = lastCoordinateTransform;

            if (cs.equals(ct.getTargetCS(), false)) {
                if (equivalents(ct.getSourceCS(), internalCS)) {
                    return ct;
                }
            }

            ct = getCoordinateTransformation(internalCS, cs);
            lastCoordinateTransform = ct;

            return ct;
        }

        return null;
    }

    /**
     * Sets the polyline's coordinate system. Calling this method is equivalent
     * to reprojecting all polyline's points from the old coordinate system to
     * the new one.
     *
     * @param coordinateSystem The new coordinate system. A <code>null</code>
     *        value resets the coordinate system given at construction time.
     *
     * @throws TransformException If a transformation failed. In case of
     *         failure, the state of this object will stay unchanged (as if
     *         this method has never been invoked).
     * @throws UnmodifiableGeometryException if modifying this geometry would
     *         corrupt a container. To avoid this exception, {@linkPlain
     *         #clone clone} this geometry before to modify it.
     */
    public synchronized void setCoordinateSystem(
        CoordinateSystem coordinateSystem)
        throws TransformException, UnmodifiableGeometryException {
        // Do not use 'Polyline.getCoordinateSystem2D', since
        // we want a 'TransformException' in case of failure.
        coordinateSystem = CTSUtilities.getCoordinateSystem2D(coordinateSystem);

        if (coordinateSystem == null) {
            coordinateSystem = getInternalCS();

            // May still null. Its ok.
        }

        if (Utilities.equals(coordinateSystem, getCoordinateSystem())) {
            return;
        }

        CoordinateTransformation transformCandidate = getTransformationFromInternalCS(coordinateSystem);

        if (transformCandidate == null) {
            transformCandidate = getIdentityTransform(coordinateSystem);
        }

        /*
         * Store the new coordinate transform
         * only after projection has succeeded.
         */
        this.ct = transformCandidate;

        if (ct.getMathTransform().isIdentity()) {
            transformedPoint = null;
        } else {
            float[] src = new float[] { (float) coord.x, (float) coord.y };
            transformedPoint = new float[2];
            ct.getMathTransform().transform(src, 0, transformedPoint, 0, 2);
        }

        assert Utilities.equals(coordinateSystem, getCoordinateSystem());
    }

    /**
     * Indicates whether the specified transform is the identity transform. A
     * null transform (<code>null</code>) is considered to be an identity
     * transform.
     */
    private static boolean isIdentity(
        final CoordinateTransformation coordinateTransform) {
        return (coordinateTransform == null)
        || coordinateTransform.getMathTransform().isIdentity();
    }

    /**
     * Inner class that provides the bounds of a point without requiring to
     * duplicate the memory required to store coordinates. 
     */
    private class PointBound extends Rectangle2D {
        /**
         * @see Rectangle2D#createIntersection
         */
        public Rectangle2D createIntersection(Rectangle2D r) {
            if (r.contains(Point.this.getX(), Point.this.getY())) {
                return this;
            } else {
                return new Rectangle2D.Float(0, 0, 0, 0);
            }
        }

        public Rectangle2D createUnion(Rectangle2D r) {
            float x = Point.this.getX();
            float y = Point.this.getY();
            double x1 = Math.min(x, r.getMinX());
            double y1 = Math.min(y, r.getMinY());
            double x2 = Math.max(x, r.getMaxX());
            double y2 = Math.max(y, r.getMaxY());
            Rectangle2D dest = new Rectangle2D.Float();
            dest.setFrameFromDiagonal(x1, y1, x2, y2);

            return dest;
        }

        public double getHeight() {
            return 0.0;
        }

        public double getWidth() {
            return 0.0;
        }

        public double getX() {
            return Point.this.getX();
        }

        public double getY() {
            return Point.this.getY();
        }

        public int outcode(double x, double y) {
            int result = 0;

            if (x < Point.this.getX()) {
                result |= Rectangle2D.OUT_LEFT;
            } else if (x > Point.this.getX()) {
                result |= Rectangle2D.OUT_RIGHT;
            }

            if (y < Point.this.getY()) {
                result |= Rectangle2D.OUT_BOTTOM;
            } else if (y > Point.this.getY()) {
                result |= Rectangle2D.OUT_TOP;
            }

            return result;
        }

        public void setRect(double x, double y, double w, double h) {
            throw new UnsupportedOperationException("Unmodifiable rectangle");
        }

        public boolean isEmpty() {
            return true;
        }
    }

    /**
     * Simple path iterator that wraps the Point and uses directly its coordinates
     * to avoid memory duplication
     */
    private class PointPathIterator implements PathIterator {
        private AffineTransform at;

        public PointPathIterator(AffineTransform at) {
            this.at = at;
        }

        public int currentSegment(double[] coords) {
            coords[0] = getX();
            coords[1] = getY();
            at.transform(coords, 0, coords, 0, 1);

            return PathIterator.SEG_LINETO;
        }

        public int currentSegment(float[] coords) {
            coords[0] = getX();
            coords[1] = getY();
            at.transform(coords, 0, coords, 0, 1);

            return PathIterator.SEG_LINETO;
        }

        public int getWindingRule() {
            return PathIterator.WIND_NON_ZERO;
        }

        public boolean isDone() {
            return true;
        }

        public void next() {
            // nothing to do
        }
    }
}
