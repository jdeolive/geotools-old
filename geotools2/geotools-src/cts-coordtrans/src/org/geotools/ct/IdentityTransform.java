/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2002, Institut de Recherche pour le Développement
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

// Miscellaneous
import java.io.Serializable;
import org.geotools.pt.Matrix;
import org.geotools.pt.CoordinatePoint;


/**
 * The identity transform. The data are only copied without any transformation.
 * This class is used for identity transform of dimension greater than 2.
 * For 2D identity transform, {@link java.awt.geom.AffineTransform} is already
 * optimized. For 1D transform, {@link IdentityTransform1D} is the class to use.
 *
 * @version $Id: IdentityTransform.java,v 1.2 2002/07/24 18:01:17 desruisseaux Exp $
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 */
final class IdentityTransform extends AbstractMathTransform implements LinearTransform, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5339040282922138164L;
    
    /**
     * The input and output dimension.
     */
    private final int dimension;
    
    /**
     * Construct a transform.
     */
    protected IdentityTransform(final int dimension) {
        this.dimension = dimension;
    }
    
    /**
     * Tests whether this transform does not move any points.
     */
    public boolean isIdentity() {
        return true;
    }
    
    /**
     * Gets the dimension of input points.
     */
    public int getDimSource() {
        return dimension;
    }
    
    /**
     * Gets the dimension of output points.
     */
    public int getDimTarget() {
        return dimension;
    }
    
    /**
     * Returns a copy of the identity matrix.
     */
    public Matrix getMatrix() {
        return new Matrix(dimension+1);
    }
    
    /**
     * Gets the derivative of this transform at a point.
     * For an identity transform, the derivative is the
     * same everywhere.
     */
    public Matrix derivative(final CoordinatePoint point) {
        return new Matrix(dimension);
    }
    
    /**
     * Transforms an array of floating point coordinates by this transform.
     */
    public void transform(final float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
    {
        System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts*dimension);
    }
    
    /**
     * Transforms an array of floating point coordinates by this transform.
     */
    public void transform(final double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
    {
        System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts*dimension);
    }
    
    /**
     * Returns the inverse transform of this object, which
     * is this transform itself
     */
    public MathTransform inverse() {
        return this;
    }
    
    /**
     * Returns a hash value for this transform.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        return 78215634 + dimension;
    }
    
    /**
     * Compares the specified object with
     * this math transform for equality.
     */
    public boolean equals(final Object object) {
        if (object==this) {
            // Slight optimization
            return true;
        }
        if (super.equals(object)) {
            final IdentityTransform that = (IdentityTransform) object;
            return this.dimension == that.dimension;
        }
        return false;
    }
    
    /**
     * Returns the WKT for this math transform.
     */
    public String toString() {
        return MatrixTransform.toString(getMatrix());
    }
}
