/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2003, Institut de Recherche pour le Développement
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
package org.geotools.renderer.j2d;

// J2SE dependencies
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.PathIterator;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.Level;

// Geotools dependencies
import org.geotools.resources.XAffineTransform;


/**
 * Apply an arbitrary {@link AffineTransform} on a {@link Shape}. This class is used internally
 * by {@link RenderedMarks}. It is designed for reuse with many different affine transforms and
 * shapes. This class is <strong>not</strong> thread-safe.
 *
 * @version $Id: TransformedShape.java,v 1.1 2003/03/19 23:50:49 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class TransformedShape extends AffineTransform implements Shape {
    /**
     * The wrapped shape.
     */
    public Shape shape;

    /**
     * A temporary point.
     */
    private final Point2D.Double point = new Point2D.Double();

    /**
     * A temporary rectangle.
     */
    private final Rectangle2D.Double rectangle = new Rectangle2D.Double();

    /**
     * Construct a transformed shape initialized to the identity transform.
     */
    public TransformedShape() {
    }

    /**
     * Returns the 6 coefficients values.
     */
    public void getMatrix(final double[] matrix, int offset) {
        matrix[  offset] = getScaleX();     // m00
        matrix[++offset] = getShearY();     // m10
        matrix[++offset] = getShearX();     // m01
        matrix[++offset] = getScaleY();     // m11
        matrix[++offset] = getTranslateX(); // m02
        matrix[++offset] = getTranslateY(); // m12
    }

    /**
     * Set the transform from a flat matrix.
     *
     * @param matrix The flat matrix.
     * @param offset The index of the first element to use in <code>matrix</code>.
     */
    public void setTransform(final double[] matrix, int offset) {
        setTransform(matrix[  offset], matrix[++offset], matrix[++offset],
                     matrix[++offset], matrix[++offset], matrix[++offset]);
    }

    /**
     * Apply a uniform scale.
     */
    public void scale(final double s) {
        scale(s,s);
    }
    
    /**
     * Tests if the specified coordinates are inside the boundary of the <code>Shape</code>.
     */
    public boolean contains(double x, double y) {
        point.x = x;
        point.y = y;
        return contains(point);
    }
    
    /**
     * Tests if a specified {@link Point2D} is inside the boundary of the <code>Shape</code>.
     */
    public boolean contains(final Point2D p) {
        try {
            return shape.contains(inverseTransform(p, point));
        } catch (NoninvertibleTransformException exception) {
            exceptionOccured(exception, "contains");
            return false;
        }
    }
    
    /**
     * Tests if the interior of the <code>Shape</code> entirely contains
     * the specified rectangular area.
     */
    public boolean contains(double x, double y, double width, double height) {
        rectangle.x      = x;
        rectangle.y      = y;
        rectangle.width  = width;
        rectangle.height = height;
        return contains(rectangle);
    }
    
    /**
     * Tests if the interior of the <code>Shape</code> entirely contains the
     * specified <code>Rectangle2D</code>.  This method might conservatively
     * return <code>false</code>.
     */
    public boolean contains(final Rectangle2D r) {
        try {
            return shape.contains(XAffineTransform.inverseTransform(this, r, rectangle));
        } catch (NoninvertibleTransformException exception) {
            exceptionOccured(exception, "contains");
            return false;
        }
    }
    
    /**
     * Tests if the interior of the <code>Shape</code> intersects the interior of a
     * specified rectangular area.
     */
    public boolean intersects(double x, double y, double width, double height) {
        rectangle.x      = x;
        rectangle.y      = y;
        rectangle.width  = width;
        rectangle.height = height;
        return intersects(rectangle);
    }
    
    /**
     * Tests if the interior of the <code>Shape</code> intersects the interior of a specified
     * <code>Rectangle2D</code>. This method might conservatively return <code>true</code>.
     */
    public boolean intersects(final Rectangle2D r) {
        try {
            return shape.intersects(XAffineTransform.inverseTransform(this, r, rectangle));
        } catch (NoninvertibleTransformException exception) {
            exceptionOccured(exception, "intersects");
            return false;
        }
    }
    
    /**
     * Returns an integer {@link Rectangle} that completely encloses the <code>Shape</code>.
     */
    public Rectangle getBounds() {
        final Rectangle rect = shape.getBounds();
        return (Rectangle) XAffineTransform.transform(this, rect, rect);
    }
    
    /**
     * Returns a high precision and more accurate bounding box of
     * the <code>Shape</code> than the <code>getBounds</code> method.
     */
    public Rectangle2D getBounds2D() {
        final Rectangle2D rect = shape.getBounds2D();
        return XAffineTransform.transform(this, rect, rect);
    }
    
    /**
     * Returns an iterator object that iterates along the <code>Shape</code> boundary
     * and provides access to the geometry of the <code>Shape</code> outline.
     */
    public PathIterator getPathIterator(AffineTransform at) {
        if (!isIdentity()) {
            if (at==null || at.isIdentity()) {
                return shape.getPathIterator(this);
            }
            at = new AffineTransform(at);
            at.concatenate(this);
        }
        return shape.getPathIterator(at);
    }
    
    /**
     * Returns an iterator object that iterates along the <code>Shape</code> boundary and
     * provides access to a flattened view of the <code>Shape</code> outline geometry.
     */
    public PathIterator getPathIterator(AffineTransform at, final double flatness) {
        if (!isIdentity()) {
            if (at==null || at.isIdentity()) {
                return shape.getPathIterator(this, flatness);
            }
            at = new AffineTransform(at);
            at.concatenate(this);
        }
        return shape.getPathIterator(at, flatness);
    }

    /**
     * Invoked when an inverse transform was required but the transform is not invertible.
     * This error should not happen. However, even if it happen, it will not prevent the
     * application to work since <code>contains(...)</code> method may conservatively return
     * <code>false</code>. We will just log a warning message and continue.
     */
    private static void exceptionOccured(final NoninvertibleTransformException exception,
                                         final String method)
    {
        final LogRecord record = new LogRecord(Level.WARNING, exception.getLocalizedMessage());
        record.setSourceClassName("TransformedShape");
        record.setSourceMethodName(method);
        record.setThrown(exception);
        Logger.getLogger("org.geotools.renderer.j2d").log(record);
    }
}
