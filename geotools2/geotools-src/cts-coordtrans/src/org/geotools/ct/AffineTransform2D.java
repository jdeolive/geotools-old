/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.ct;

// Geotools dependencies
import org.geotools.pt.Matrix;
import org.geotools.pt.CoordinatePoint;

// Resources
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.XAffineTransform;

// J2SE dependencies
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.io.Serializable;


/**
 * Transforms two-dimensional coordinate points using an {@link AffineTransform}.
 *
 * @version $Id: AffineTransform2D.java,v 1.2 2002/07/10 18:20:13 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class AffineTransform2D extends XAffineTransform implements MathTransform2D, LinearTransform {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5299837898367149069L;
    
    /**
     * The inverse transform. This field
     * will be computed only when needed.
     */
    private transient AffineTransform2D inverse;
    
    /**
     * Construct an affine transform.
     */
    protected AffineTransform2D(final AffineTransform transform) {
        super(transform);
    }
    
    /**
     * Throws an {@link UnsupportedOperationException} when a mutable method
     * is invoked, since <code>AffineTransform2D</code> must be immutable.
     */
    protected void checkPermission() {
        throw new UnsupportedOperationException(
                Resources.format(ResourceKeys.ERROR_UNMODIFIABLE_AFFINE_TRANSFORM));
    }
    
    /**
     * Gets the dimension of input points.
     */
    public int getDimSource() {
        return 2;
    }
    
    /**
     * Gets the dimension of output points.
     */
    public int getDimTarget() {
        return 2;
    }
    
    /**
     * Transforms the specified <code>ptSrc</code> and stores the result in <code>ptDst</code>.
     */
    public CoordinatePoint transform(final CoordinatePoint ptSrc, CoordinatePoint ptDst) {
        if (ptDst==null) {
            ptDst = new CoordinatePoint(2);
        }
        transform(ptSrc.ord, 0, ptDst.ord, 0, 1);
        return ptDst;
    }
    
    /**
     * Returns this transform as an affine transform matrix.
     */
    public Matrix getMatrix() {
        return new Matrix(this);
    }
    
    /**
     * Gets the derivative of this transform at a point.
     * For an affine transform, the derivative is the
     * same everywhere.
     */
    public Matrix derivative(final Point2D point) {
        final Matrix matrix = new Matrix(2);
        matrix.setElement(0,0, getScaleX());
        matrix.setElement(1,1, getScaleY());
        matrix.setElement(0,1, getShearX());
        matrix.setElement(1,0, getShearY());
        return matrix;
    }
    
    /**
     * Gets the derivative of this transform at a point.
     * For an affine transform, the derivative is the
     * same everywhere.
     */
    public Matrix derivative(final CoordinatePoint point) {
        return derivative((Point2D) null);
    }
    
    /**
     * Creates the inverse transform of this object.
     */
    public synchronized MathTransform inverse() throws NoninvertibleTransformException {
        if (inverse==null) try {
            if (!isIdentity()) {
                inverse = new AffineTransform2D(createInverse());
                inverse.inverse = this;
            } else {
                inverse = this;
            }
        }
        catch (java.awt.geom.NoninvertibleTransformException exception) {
            throw new NoninvertibleTransformException(exception.getLocalizedMessage(), exception);
        }
        return inverse;
    }
    
    /**
     * Returns the WKT for this affine transform.
     */
    public String toString() {
        return MatrixTransform.toString(new Matrix(this));
    }
}
