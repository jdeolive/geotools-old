/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.resources;

// Miscellaneous
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.io.Serializable;
import java.io.ObjectStreamException;


/**
 * Serializable, high-performance double-precision rectangle. Instead of using
 * <code>x</code>, <code>y</code>, <code>width</code> and <code>height</code>,
 * this class store rectangle's coordinates into the following fields:
 * {@link #xmin}, {@link #xmax}, {@link #ymin} et {@link #ymax}. Methods likes
 * <code>contains</code> and <code>intersects</code> are faster, which make this
 * class more appropriate for using intensively inside a loop. Furthermore, this
 * class work correctly with {@linkplain Double#POSITIVE_INFINITE infinites} and
 * {@linkplain Double#NaN NaN} values.
 *
 * @version $Id: XRectangle2D.java,v 1.6 2003/11/15 14:21:59 aaime Exp $
 * @author Martin Desruisseaux
 */
public class XRectangle2D extends Rectangle2D implements Serializable {
    /**
     * A small number for testing intersection between an arbitrary shape and a rectangle.
     */
    private static final double EPS = 1E-6;

    /**
     * An immutable instance of a {@link Rectangle2D} with bounds extending toward
     * infinities. The {@link #getMinX} and {@link #getMinY} methods return always
     * {@link java.lang.Double#NEGATIVE_INFINITY},  while the {@link #getMaxX} and
     * {@link #getMaxY} methods return always {@link java.lang.Double#POSITIVE_INFINITY}.
     * This rectangle can be used as argument in the {@link XRectangle2D} constructor for
     * initializing a new <code>XRectangle2D</code> to infinite bounds.
     */
    public static final Rectangle2D INFINITY = new Infinite();
    
    /**
     * The implementation of {@link XRectangle2D#INFINITY}.
     */
    private static final class Infinite extends Rectangle2D implements Serializable {
        private static final long serialVersionUID = 5281254268988984523L;
        public double getX()       {return java.lang.Double.NEGATIVE_INFINITY;}
        public double getY()       {return java.lang.Double.NEGATIVE_INFINITY;}
        public double getMinX()    {return java.lang.Double.NEGATIVE_INFINITY;}
        public double getMinY()    {return java.lang.Double.NEGATIVE_INFINITY;}
        public double getMaxX()    {return java.lang.Double.POSITIVE_INFINITY;}
        public double getMaxY()    {return java.lang.Double.POSITIVE_INFINITY;}
        public double getWidth()   {return java.lang.Double.POSITIVE_INFINITY;}
        public double getHeight()  {return java.lang.Double.POSITIVE_INFINITY;}
        public double getCenterX() {return java.lang.Double.NaN;}
        public double getCenterY() {return java.lang.Double.NaN;}

        public void        add       (Rectangle2D   rect)                     {            }
        public void        add       (Point2D      point)                     {            }
        public void        add       (double x, double y)                     {            }
        public int     outcode       (double x, double y)                     {return 0;   }
        public int     outcode       (Point2D      point)                     {return 0;   }
        public boolean contains      (Point2D      point)                     {return true;}
        public boolean contains      (Rectangle2D   rect)                     {return true;}
        public boolean contains      (double x, double y)                     {return true;}
        public boolean contains      (double x, double y, double w, double h) {return true;}
        public boolean intersects    (Rectangle2D   rect)                     {return true;}
        public boolean intersects    (double x, double y, double w, double h) {return true;}
        public boolean intersectsLine(double x, double y, double u, double v) {return true;}
        public boolean intersectsLine(Line2D        line)                     {return true;}

        public boolean     isEmpty           ()                 {return false;}
        public Rectangle2D getFrame          ()                 {return this;}
        public Rectangle2D getBounds2D       ()                 {return this;}
        public Rectangle2D createUnion       (Rectangle2D rect) {return this;}
        public Rectangle2D createIntersection(Rectangle2D rect) {return (Rectangle2D)rect.clone();}
        public void setRect(double x, double y, double w, double h) {
            throw new UnsupportedOperationException();
            // REVISIT: Throws UnmodifiableGeometryException instead?
            //          (defined in renderer module for now)
        }
        private Object readResolve() throws ObjectStreamException {
            return INFINITY;
        }
    };

    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1918221103635749436L;

    /** Minimal <var>x</var> coordinate. */ protected double xmin;
    /** Minimal <var>y</var> coordinate. */ protected double ymin;
    /** Maximal <var>x</var> coordinate. */ protected double xmax;
    /** Maximal <var>y</var> coordinate. */ protected double ymax;

    /**
     * Construct a default rectangle. Initial coordinates are <code>(0,0,0,0)</code>.
     */
    public XRectangle2D() {
    }
    
    /**
     * Construct a rectangle with the specified location and dimension.
     * This constructor uses the same signature than {@link Rectangle2D} for consistency.
     */
    public XRectangle2D(final double x, final double y, final double width, final double height) {
        this.xmin = x;
        this.ymin = y;
        this.xmax = x+width;
        this.ymax = y+height;
    }
    
    /**
     * Construct a rectangle with the same coordinates than the supplied rectangle.
     *
     * @param rect The rectangle, or <code>null</code> in none (in which case this constructor
     *             is equivalents to the no-argument constructor). Use {@link #INFINITY} for
     *             initializing this <code>XRectangle2D</code> with infinite bounds.
     */
    public XRectangle2D(final Rectangle2D rect) {
        if (rect != null) {
            setRect(rect);
        }
    }

    /**
     * Create a rectangle using maximal <var>x</var> and <var>y</var> values
     * rather than width and height. This factory avoid the problem of NaN
     * values when extremums are infinite numbers.
     */
    public static XRectangle2D createFromExtremums(final double xmin, final double ymin,
                                                   final double xmax, final double ymax)
    {
        final XRectangle2D rect = new XRectangle2D();
        rect.xmin = xmin;
        rect.ymin = ymin;
        rect.xmax = xmax;
        rect.ymax = ymax;
        return rect;
    }
    
    /**
     * Determines whether the <code>RectangularShape</code> is empty.
     * When the <code>RectangularShape</code> is empty, it encloses no
     * area.
     *
     * @return <code>true</code> if the <code>RectangularShape</code> is empty;
     *      <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return !(xmin<xmax && ymin<ymax);
    }

    /**
     * Returns the X coordinate of the upper left corner of
     * the framing rectangle in <code>double</code> precision.
     *
     * @return the x coordinate of the upper left corner of the framing rectangle.
     */
    public double getX() {
        return xmin;
    }

    /**
     * Returns the Y coordinate of the upper left corner of
     * the framing rectangle in <code>double</code> precision.
     *
     * @return the y coordinate of the upper left corner of the framing rectangle.
     */
    public double getY() {
        return ymin;
    }
    
    /**
     * Returns the width of the framing rectangle in
     * <code>double</code> precision.
     * @return the width of the framing rectangle.
     */
    public double getWidth() {
        return xmax-xmin;
    }

    /**
     * Returns the height of the framing rectangle in <code>double</code> precision.
     *
     * @return the height of the framing rectangle.
     */
    public double getHeight() {
        return ymax-ymin;
    }

    /**
     * Returns the smallest X coordinate of the rectangle.
     */
    public double getMinX() {
        return xmin;
    }

    /**
     * Returns the smallest Y coordinate of the rectangle.
     */
    public double getMinY() {
        return ymin;
    }

    /**
     * Returns the largest X coordinate of the rectangle.
     */
    public double getMaxX() {
        return xmax;
    }

    /**
     * Returns the largest Y coordinate of the rectangle.
     */
    public double getMaxY() {
        return ymax;
    }

    /**
     * Returns the X coordinate of the center of the rectangle.
     */
    public double getCenterX() {
        return (xmin+xmax)*0.5;
    }

    /**
     * Returns the Y coordinate of the center of the rectangle.
     */
    public double getCenterY() {
        return (ymin+ymax)*0.5;
    }

    /**
     * Sets the location and size of this <code>Rectangle2D</code>
     * to the specified double values.
     *
     * @param x,&nbsp;y the coordinates to which to set the
     *        location of the upper left corner of this <code>Rectangle2D</code>
     * @param width the value to use to set the width of this <code>Rectangle2D</code>
     * @param height the value to use to set the height of this <code>Rectangle2D</code>
     */
    public void setRect(final double x, final double y, final double width, final double height) {
        this.xmin = x;
        this.ymin = y;
        this.xmax = x+width;
        this.ymax = y+height;
    }

    /**
     * Sets this <code>Rectangle2D</code> to be the same as the
     * specified <code>Rectangle2D</code>.
     *
     * @param r the specified <code>Rectangle2D</code>
     */
    public void setRect(final Rectangle2D r) {
        this.xmin = r.getMinX();
        this.ymin = r.getMinY();
        this.xmax = r.getMaxX();
        this.ymax = r.getMaxY();
    }

    /**
     * Tests if the interior of this <code>Rectangle2D</code>
     * intersects the interior of a specified set of rectangular
     * coordinates.
     *
     * @param x,&nbsp;y the coordinates of the upper left corner
     *        of the specified set of rectangular coordinates
     * @param width the width of the specified set of rectangular coordinates
     * @param height the height of the specified set of rectangular coordinates
     * @return <code>true</code> if this <code>Rectangle2D</code>
     * intersects the interior of a specified set of rectangular
     * coordinates; <code>false</code> otherwise.
     */
    public boolean intersects(final double x,     final double y,
                              final double width, final double height)
    {
        if (!(xmin<xmax && ymin<ymax && width>0 && height>0)) {
            return false;
        } else {
            return (x<xmax && y<ymax && x+width>xmin && y+height>ymin);
        }
    }

    /**
     * Tests if the <strong>interior</strong> of this shape intersects the
     * <strong>interior</strong> of a specified rectangle. This methods overrides the default
     * {@link Rectangle2D} implementation in order to work correctly with
     * {@linkplain Double#POSITIVE_INFINITE infinites} and {@linkplain Double#NaN NaN} values.
     *
     * @param  rect the specified rectangle.
     * @return <code>true</code> if this shape and the specified rectangle intersect each other.
     *
     * @see #intersectInclusive(Rectangle2D, Rectangle2D)
     */
    public boolean intersects(final Rectangle2D rect) {
        if (!(xmin<xmax && ymin<ymax)) {
            return false;
        } else {
            final double xmin2 = rect.getMinX();
            final double xmax2 = rect.getMaxX(); if (!(xmax2 > xmin2)) return false;
            final double ymin2 = rect.getMinY();
            final double ymax2 = rect.getMaxY(); if (!(ymax2 > ymin2)) return false;
            return (xmin2<xmax && ymin2<ymax && xmax2>xmin && ymax2>ymin);
        }
    }

    /**
     * Tests if the interior and/or the edge of two rectangles intersect. This method
     * is similar to {@link #intersects(Rectangle2D)} except for the following points:
     * <ul>
     *   <li>This method doesn't test only if the <em>interiors</em> intersect.
     *       It tests for the edges as well.</li>
     *   <li>This method tests also rectangle with zero {@linkplain Rectangle2D#getWidth width} or
     *       {@linkplain Rectangle2D#getHeight height} (which are {@linkplain Shape#isEmpty empty}
     *       according {@link Shape} contract). However, rectangle with negative width or height
     *       are still considered as empty.</li>
     *   <li>This method work correctly with {@linkplain Double#POSITIVE_INFINITE infinites} and
     *       {@linkplain Double#NaN NaN} values.</li>
     * </ul>
     *
     * This method is said <cite>inclusive</cite> because it tests bounds as closed interval
     * rather then open interval (the default Java2D behavior). Usage of closed interval is
     * required if at least one rectangle may be the bounding box of a perfectly horizontal
     * or vertical line; such a bounding box has 0 width or height.
     *
     * @param  rect1 The first rectangle to test.
     * @param  rect2 The second rectangle to test.
     * @return <code>true</code> if the interior and/or the edge of the two specified rectangles
     *         intersects.
     */
    public static boolean intersectInclusive(final Rectangle2D rect1, final Rectangle2D rect2) {
        final double xmin1 = rect1.getMinX();
        final double xmax1 = rect1.getMaxX(); if (!(xmax1 >= xmin1)) return false;
        final double ymin1 = rect1.getMinY();
        final double ymax1 = rect1.getMaxY(); if (!(ymax1 >= ymin1)) return false;
        final double xmin2 = rect2.getMinX();
        final double xmax2 = rect2.getMaxX(); if (!(xmax2 >= xmin2)) return false;
        final double ymin2 = rect2.getMinY();
        final double ymax2 = rect2.getMaxY(); if (!(ymax2 >= ymin2)) return false;
	return (xmax2 >= xmin1 &&
		ymax2 >= ymin1 &&
		xmin2 <= xmax1 &&
		ymin2 <= ymax1);
    }

    /**
     * Tests if the interior of the <code>Shape</code> intersects the interior of a specified
     * rectangle. This method might conservatively return <code>true</code> when there is a high
     * probability that the rectangle and the shape intersect, but the calculations to accurately
     * determine this intersection are prohibitively expensive. This is similar to
     * {@link Shape#intersects(Rectangle2D)}, except that this method tests also rectangle with
     * zero {@linkplain Rectangle2D#getWidth width} or {@linkplain Rectangle2D#getHeight height}
     * (which are {@linkplain Shape#isEmpty empty} according {@link Shape} contract). However,
     * rectangle with negative width or height are still considered as empty.
     * <br><br>
     * This method is said <cite>inclusive</cite> because it try to mimic
     * {@link #intersectInclusive(Rectangle2D, Rectangle2D)} behavior, at
     * least for rectangle with zero width or height.
     *
     * @param shape The shape.
     * @param rect  The rectangle to test for inclusion.
     * @return <code>true</code> if the interior of the shape and  the interior of the specified
     *         rectangle intersect, or are both highly likely to intersect.
     */
    public static boolean intersectInclusive(final Shape shape, final Rectangle2D rect) {
        double x      = rect.getX();
        double y      = rect.getY();
        double width  = rect.getWidth();
        double height = rect.getHeight();
        if(width == 0 && height == 0) {
            width = EPS;
            height = EPS;
        } else if (width == 0) {
            width = height*EPS;
            x -= 0.5*width;
        } else if (height == 0) {
            height = width*EPS;
            y -= 0.5*height;
        }
        return shape.intersects(x, y, width, height);
    }

    /**
     * Tests if the interior of this <code>Rectangle2D</code> entirely
     * contains the specified set of rectangular coordinates.
     *
     * @param x,&nbsp;y the coordinates of the upper left corner
     *        of the specified set of rectangular coordinates
     * @param width the width of the specified set of rectangular coordinates
     * @param height the height of the specified set of rectangular coordinates
     * @return <code>true</code> if this <code>Rectangle2D</code>
     *         entirely contains specified set of rectangular
     *         coordinates; <code>false</code> otherwise.
     */
    public boolean contains(final double x,     final double y,
                            final double width, final double height)
    {
        if (!(xmin<xmax && ymin<ymax && width>0 && height>0)) {
            return false;
        } else {
            return (x>=xmin && y>=ymin && (x+width)<=xmax && (y+height)<=ymax);
        }
    }

    /**
     * Tests if the interior of this shape entirely contains the specified rectangle.
     * This methods overrides the default {@link Rectangle2D} implementation in order
     * to work correctly with {@linkplain Double#POSITIVE_INFINITE infinites} and
     * {@linkplain Double#NaN NaN} values.
     *
     * @param  rect the specified rectangle.
     * @return <code>true</code> if this shape entirely contains the specified rectangle.
     */
    public boolean contains(final Rectangle2D rect) {
        if (!(xmin<xmax && ymin<ymax)) {
            return false;
        } else {
            final double xmin2 = rect.getMinX();
            final double xmax2 = rect.getMaxX(); if (!(xmax2 > xmin2)) return false;
            final double ymin2 = rect.getMinY();
            final double ymax2 = rect.getMaxY(); if (!(ymax2 > ymin2)) return false;
            return (xmin2>=xmin && ymin2>=ymin && xmax2<=xmax && ymax2<=ymax);
        }
    }

    /**
     * Tests if a specified coordinate is inside the boundary of this <code>Rectangle2D</code>.
     *
     * @param x,&nbsp;y the coordinates to test.
     * @return <code>true</code> if the specified coordinates are
     *         inside the boundary of this <code>Rectangle2D</code>;
     *         <code>false</code> otherwise.
     */
    public boolean contains(final double x, final double y) {
        return (x>=xmin && y>=ymin && x<xmax && y<ymax);
    }

    /**
     * Determines where the specified coordinates lie with respect
     * to this <code>Rectangle2D</code>.
     * This method computes a binary OR of the appropriate mask values
     * indicating, for each side of this <code>Rectangle2D</code>,
     * whether or not the specified coordinates are on the same side
     * of the edge as the rest of this <code>Rectangle2D</code>.
     *
     * @param x,&nbsp;y the specified coordinates
     * @return the logical OR of all appropriate out codes.
     *
     * @see #OUT_LEFT
     * @see #OUT_TOP
     * @see #OUT_RIGHT
     * @see #OUT_BOTTOM
     */
    public int outcode(final double x, final double y) {
        int out=0;
        if (!(xmax > xmin)) out |= OUT_LEFT | OUT_RIGHT;
        else if (x < xmin)  out |= OUT_LEFT;
        else if (x > xmax)  out |= OUT_RIGHT;

        if (!(ymax > ymin)) out |= OUT_TOP | OUT_BOTTOM;
        else if (y < ymin)  out |= OUT_TOP;
        else if (y > ymax)  out |= OUT_BOTTOM;
        return out;
    }

    /**
     * Returns a new <code>Rectangle2D</code> object representing the
     * intersection of this <code>Rectangle2D</code> with the specified
     * <code>Rectangle2D</code>.
     *
     * @param  rect the <code>Rectangle2D</code> to be intersected with this <code>Rectangle2D</code>
     * @return the largest <code>Rectangle2D</code> contained in both the specified
     *         <code>Rectangle2D</code> and in this <code>Rectangle2D</code>.
     */
    public Rectangle2D createIntersection(final Rectangle2D rect) {
        final XRectangle2D r=new XRectangle2D();
        r.xmin = Math.max(xmin, rect.getMinX());
        r.ymin = Math.max(ymin, rect.getMinY());
        r.xmax = Math.min(xmax, rect.getMaxX());
        r.ymax = Math.min(ymax, rect.getMaxY());
        return r;
    }
    
    /**
     * Returns a new <code>Rectangle2D</code> object representing the
     * union of this <code>Rectangle2D</code> with the specified
     * <code>Rectangle2D</code>.
     *
     * @param rect the <code>Rectangle2D</code> to be combined with
     *             this <code>Rectangle2D</code>
     * @return the smallest <code>Rectangle2D</code> containing both
     *         the specified <code>Rectangle2D</code> and this
     *         <code>Rectangle2D</code>.
     */
    public Rectangle2D createUnion(final Rectangle2D rect) {
        final XRectangle2D r=new XRectangle2D();
        r.xmin = Math.min(xmin, rect.getMinX());
        r.ymin = Math.min(ymin, rect.getMinY());
        r.xmax = Math.max(xmax, rect.getMaxX());
        r.ymax = Math.max(ymax, rect.getMaxY());
        return r;
    }

    /**
     * Adds a point, specified by the double precision arguments
     * <code>x</code> and <code>y</code>, to this <code>Rectangle2D</code>.
     * The resulting <code>Rectangle2D</code> is the smallest <code>Rectangle2D</code>
     * that contains both the original <code>Rectangle2D</code> and the specified point.
     * <p>
     * After adding a point, a call to <code>contains</code> with the
     * added point as an argument does not necessarily return
     * <code>true</code>. The <code>contains</code> method does not
     * return <code>true</code> for points on the right or bottom
     * edges of a rectangle. Therefore, if the added point falls on
     * the left or bottom edge of the enlarged rectangle,
     * <code>contains</code> returns <code>false</code> for that point.
     *
     * @param newx,&nbsp;newy the coordinates of the new point
     */
    public void add(final double x, final double y) {
        if (x<xmin) xmin=x;
        if (x>xmax) xmax=x;
        if (y<ymin) ymin=y;
        if (y>ymax) ymax=y;
    }

    /**
     * Adds a <code>Rectangle2D</code> object to this <code>Rectangle2D</code>.
     * The resulting <code>Rectangle2D</code> is the union of the two
     * <code>Rectangle2D</code> objects.
     *
     * @param rect the <code>Rectangle2D</code> to add to this <code>Rectangle2D</code>.
     */
    public void add(final Rectangle2D rect) {
        double t;
        if ((t=rect.getMinX()) < xmin) xmin=t;
        if ((t=rect.getMaxX()) > xmax) xmax=t;
        if ((t=rect.getMinY()) < ymin) ymin=t;
        if ((t=rect.getMaxY()) > ymax) ymax=t;
    }

    /**
     * Returns the <code>String</code> representation of this <code>Rectangle2D</code>.
     *
     * @return a <code>String</code> representing this <code>Rectangle2D</code>.
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer(Utilities.getShortClassName(this));
        final NumberFormat format = NumberFormat.getNumberInstance();
        final FieldPosition dummy = new FieldPosition(0);
        buffer.append("[xmin="); format.format(xmin, buffer, dummy);
        buffer.append(" xmax="); format.format(xmax, buffer, dummy);
        buffer.append(" ymin="); format.format(ymin, buffer, dummy);
        buffer.append(" ymax="); format.format(ymax, buffer, dummy);
        buffer.append(']');
        return buffer.toString();
    }
}
