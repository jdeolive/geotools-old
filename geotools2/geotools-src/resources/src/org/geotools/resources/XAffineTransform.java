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

// Geometry
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;


/**
 * Utility methods for affine transforms. This class provides a set
 * of public static methods working on any {@link AffineTransform}.
 * <br><br>
 * Class <code>XAffineTransform</code> overrides all mutable methods
 * of {@link AffineTransform} in order to check for permission before
 * changing the transform's state. If {@link #checkPermission} is
 * defined to always throw an exception, then <code>XAffineTransform</code>
 * is immutable.
 *
 * @version $Id: XAffineTransform.java,v 1.8 2003/06/25 15:16:19 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public abstract class XAffineTransform extends AffineTransform {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5215291166450556451L;

    /**
     * Tolerance value for floating point comparisons.
     */
    private static final double EPS = 1E-6;

    /**
     * Constructs a new <code>XAffineTransform</code> that is a
     * copy of the specified <code>AffineTransform</code> object.
     */
    protected XAffineTransform(final AffineTransform tr) {
        super(tr);
    }

    /**
     * Check if the caller is allowed to change this
     * <code>XAffineTransform</code>'s state.
     */
    protected abstract void checkPermission();

    /**
     * Check for permission before translating this transform.
     */
    public void translate(double tx, double ty) {
        checkPermission();
        super.translate(tx, ty);
    }

    /**
     * Check for permission before rotating this transform.
     */
    public void rotate(double theta) {
        checkPermission();
        super.rotate(theta);
    }

    /**
     * Check for permission before rotating this transform.
     */
    public void rotate(double theta, double x, double y) {
        checkPermission();
        super.rotate(theta, x, y);
    }

    /**
     * Check for permission before scaling this transform.
     */
    public void scale(double sx, double sy) {
        checkPermission();
        super.scale(sx, sy);
    }

    /**
     * Check for permission before shearing this transform.
     */
    public void shear(double shx, double shy) {
        checkPermission();
        super.shear(shx, shy);
    }

    /**
     * Check for permission before setting this transform.
     */
    public void setToIdentity() {
        checkPermission();
        super.setToIdentity();
    }

    /**
     * Check for permission before setting this transform.
     */
    public void setToTranslation(double tx, double ty) {
        checkPermission();
        super.setToTranslation(tx, ty);
    }

    /**
     * Check for permission before setting this transform.
     */
    public void setToRotation(double theta) {
        checkPermission();
        super.setToRotation(theta);
    }

    /**
     * Check for permission before setting this transform.
     */
    public void setToRotation(double theta, double x, double y) {
        checkPermission();
        super.setToRotation(theta, x, y);
    }

    /**
     * Check for permission before setting this transform.
     */
    public void setToScale(double sx, double sy) {
        checkPermission();
        super.setToScale(sx, sy);
    }

    /**
     * Check for permission before setting this transform.
     */
    public void setToShear(double shx, double shy) {
        checkPermission();
        super.setToShear(shx, shy);
    }

    /**
     * Check for permission before setting this transform.
     */
    public void setTransform(AffineTransform Tx) {
        checkPermission();
        super.setTransform(Tx);
    }

    /**
     * Check for permission before setting this transform.
     */
    public void setTransform(double m00, double m10,
                             double m01, double m11,
                             double m02, double m12) {
        checkPermission();
        super.setTransform(m00, m10, m01, m11, m02, m12);
    }

    /**
     * Check for permission before concatenating this transform.
     */
    public void concatenate(AffineTransform Tx) {
        checkPermission();
        super.concatenate(Tx);
    }

    /**
     * Check for permission before concatenating this transform.
     */
    public void preConcatenate(AffineTransform Tx) {
        checkPermission();
        super.preConcatenate(Tx);
    }

    /**
     * Returns a rectangle which entirely contains the direct transform of
     * <code>bounds</code>. This operation is equivalent to
     * <code>createTransformedShape(bounds).getBounds2D()</code>.
     *
     * @param transform Affine transform to use.
     * @param bounds    Rectangle to transform. This rectangle will not be
     *                  modified.
     * @param dest      Rectangle in which to place the result.  If null, a new
     *                  rectangle will be created.
     *
     * @return The direct transform of the <code>bounds</code> rectangle.
     */
    public static Rectangle2D transform(final AffineTransform transform,
                                        final Rectangle2D     bounds,
                                        final Rectangle2D     dest) {
        double xmin=Double.POSITIVE_INFINITY;
        double ymin=Double.POSITIVE_INFINITY;
        double xmax=Double.NEGATIVE_INFINITY;
        double ymax=Double.NEGATIVE_INFINITY;
        final Point2D.Double point=new Point2D.Double();
        for (int i=0; i<4; i++) {
            point.x = (i&1)==0 ? bounds.getMinX() : bounds.getMaxX();
            point.y = (i&2)==0 ? bounds.getMinY() : bounds.getMaxY();
            transform.transform(point, point);
            if (point.x<xmin) xmin=point.x;
            if (point.x>xmax) xmax=point.x;
            if (point.y<ymin) ymin=point.y;
            if (point.y>ymax) ymax=point.y;
        }
        if (dest!=null) {
            dest.setRect(xmin, ymin, xmax-xmin, ymax-ymin);
            return dest;
        }
        return new Rectangle2D.Double(xmin, ymin, xmax-xmin, ymax-ymin);
    }

    /**
     * Returns a rectangle which entirely contains the inverse transform of 
     * <code>bounds</code>. This operation is equivalent to
     * <code>createInverse().createTransformedShape(bounds).getBounds2D()</code>.
     *
     * @param transform Affine transform to use.
     * @param bounds    Rectangle to transform. This rectangle will not be
     *                  modified.
     * @param dest      Rectangle in which to place the result.  If null, a new
     *                  rectangle will be created.
     *
     * @return The inverse transform of the <code>bounds</code> rectangle.
     * @throws NoninvertibleTransformException if the affine transform can't be
     *         inverted.
     */
    public static Rectangle2D inverseTransform(final AffineTransform transform,
                                               final Rectangle2D     bounds,
                                               final Rectangle2D     dest)
        throws NoninvertibleTransformException
    {
        double xmin=Double.POSITIVE_INFINITY;
        double ymin=Double.POSITIVE_INFINITY;
        double xmax=Double.NEGATIVE_INFINITY;
        double ymax=Double.NEGATIVE_INFINITY;
        final Point2D.Double point=new Point2D.Double();
        for (int i=0; i<4; i++) {
            point.x = (i&1)==0 ? bounds.getMinX() : bounds.getMaxX();
            point.y = (i&2)==0 ? bounds.getMinY() : bounds.getMaxY();
            transform.inverseTransform(point, point);
            if (point.x<xmin) xmin=point.x;
            if (point.x>xmax) xmax=point.x;
            if (point.y<ymin) ymin=point.y;
            if (point.y>ymax) ymax=point.y;
        }
        if (dest!=null) {
            dest.setRect(xmin, ymin, xmax-xmin, ymax-ymin);
            return dest;
        }
        return new Rectangle2D.Double(xmin, ymin, xmax-xmin, ymax-ymin);
    }

    /**
     * Calculates the inverse affine transform of a point without bearing in
     * mind the translation.
     *
     * @param transform Affine transform to use.
     * @param source    Point to transform. This rectangle will not be modified.
     * @param dest      Point in which to place the result.  If null, a new
     *                  point will be created.
     *
     * @return The inverse transform of the <code>source</code> point.
     * @throws NoninvertibleTransformException if the affine transform can't be
     *         inverted.
     */
    public static Point2D inverseDeltaTransform(final AffineTransform transform,
                                                final Point2D         source,
                                                final Point2D         dest)
        throws NoninvertibleTransformException
    {
        final double m00 = transform.getScaleX();
        final double m11 = transform.getScaleY();
        final double m01 = transform.getShearX();
        final double m10 = transform.getShearY();
        final double det = m00*m11 - m01*m10;
        if (!(Math.abs(det) > Double.MIN_VALUE)) {
            return transform.createInverse().deltaTransform(source, dest);
        }
        final double x = source.getX();
        final double y = source.getY();
        if (dest!=null) {
            dest.setLocation((x*m11 - y*m01)/det,
                             (y*m00 - x*m10)/det);
            return dest;
        }
        return new Point2D.Double((x*m11 - y*m01)/det,
                                  (y*m00 - x*m10)/det);
    }

    /**
     * Returns the scale factor <var>x</var> by cancelling the effect of an
     * eventual rotation. This factor is calculated by
     * <IMG src="{@docRoot}/org/geotools/renderer/j2d/doc-files/scaleX0.png">.
     */
    public static double getScaleX0(final AffineTransform zoom) {
        return XMath.hypot(zoom.getScaleX(), zoom.getShearX());
    }

    /**
     * Returns the scale factor <var>y</var> by cancelling the effect of an
     * eventual rotation. This factor is calculated by
     * <IMG src="{@docRoot}/org/geotools/renderer/j2d/doc-files/scaleY0.png">.
     */
    public static double getScaleY0(final AffineTransform zoom) {
        return XMath.hypot(zoom.getScaleY(), zoom.getShearY());
    }

    /**
     * Returns a global scale factor for the specified affine transform.
     * This scale factor will combines {@link #getScaleX0} and {@link #getScaleY0}.
     * The way to compute such a "global" scale is somewhat arbitrary and may change
     * in a future version.
     */
    public static double getScale(final AffineTransform zoom) {
        return 0.5 * (getScaleX0(zoom) + getScaleY0(zoom));
    }

    /**
     * Returns an affine transform representing a zoom carried out around a
     * central point (<var>x</var>,<var>y</var>). The transforms will leave
     * the specified (<var>x</var>,<var>y</var>) coordinate unchanged.
     *
     * @param sx Scale along <var>x</var> axis.
     * @param sy Scale along <var>y</var> axis.
     * @param  x <var>x</var> coordinates of the central point.
     * @param  y <var>y</var> coordinates of the central point.
     * @return   Affine transform of a zoom which leaves the
     *          (<var>x</var>,<var>y</var>) coordinate unchanged.
     */
    public static AffineTransform getScaleInstance(final double sx, final double sy,
                                                   final double  x, final double  y) {
        return new AffineTransform(sx, 0, 0, sy, (1-sx)*x, (1-sy)*y);
    }

    /*
     * Checks whether the matrix coefficients are close to whole numbers.
     * If this is the case, these coefficients will be rounded up to the
     * nearest whole numbers. This rounding up is useful, for example, for
     * speeding up image displays.  Above all, it is efficient when we know that
     * a matrix has a chance of being close to the similarity matrix.
     */
    public static void round(final AffineTransform zoom) {
        double r;
        final double m00,m01,m10,m11;
        if (Math.abs((m00=Math.rint(r=zoom.getScaleX()))-r) <= EPS &&
            Math.abs((m01=Math.rint(r=zoom.getShearX()))-r) <= EPS &&
            Math.abs((m11=Math.rint(r=zoom.getScaleY()))-r) <= EPS &&
            Math.abs((m10=Math.rint(r=zoom.getShearY()))-r) <= EPS)
        {
            if ((m00!=0 || m01!=0) && (m10!=0 || m11!=0)) {
                double m02=Math.rint(r=zoom.getTranslateX()); if (!(Math.abs(m02-r)<=EPS)) m02=r;
                double m12=Math.rint(r=zoom.getTranslateY()); if (!(Math.abs(m12-r)<=EPS)) m12=r;
                zoom.setTransform(m00,m10,m01,m11,m02,m12);
            }
        }
    }
}
