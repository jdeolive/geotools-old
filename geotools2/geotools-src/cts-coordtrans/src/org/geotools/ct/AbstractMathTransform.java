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

// J2SE and vecmath dependencies
import java.lang.ref.WeakReference;
import java.lang.ref.Reference;
import java.io.Serializable;
import java.util.Locale;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.AffineTransform;
import java.awt.geom.IllegalPathStateException;
import javax.vecmath.SingularMatrixException;

// OpenGIS dependencies
import org.opengis.pt.PT_Matrix;
import org.opengis.pt.PT_CoordinatePoint;
import org.opengis.ct.CT_MathTransform;
import org.opengis.ct.CT_DomainFlags;

// Geotools dependencies
import org.geotools.pt.Matrix;
import org.geotools.pt.CoordinatePoint;
import org.geotools.pt.MismatchedDimensionException;

// Resources
import org.geotools.resources.Geometry;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Provides a default implementations for most methods required by the
 * {@link MathTransform} interface. <code>AbstractMathTransform</code>
 * provides a convenient base class from which other transform classes
 * can be easily derived. In addition, <code>AbstractMathTransform</code>
 * implements methods required by the {@link MathTransform2D} interface,
 * but <strong>does not</strong> implements <code>MathTransform2D</code>.
 * Subclasses must declare <code>implements&nbsp;MathTransform2D</code>
 * themself if they know to maps two-dimensional coordinate systems.
 *
 * @version $Id: AbstractMathTransform.java,v 1.9 2003/01/20 23:16:16 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public abstract class AbstractMathTransform implements MathTransform {
    /**
     * OpenGIS object returned by {@link #cachedOpenGIS}.
     * It may be a hard or a weak reference.
     */
    private transient Object proxy;

    /**
     * Construct a math transform which is an adapter for
     * the specified {@link CT_MathTransform} interface.
     *
     * @param opengis The {@link CT_MathTransform} interface. We declare
     *        a generic {@link Object} in order to avoid too early class
     *        loading of OpenGIS's interfaces.
     */
    AbstractMathTransform(final Object opengis) {
        proxy = opengis;
    }

    /**
     * Construct a math transform.
     */
    public AbstractMathTransform() {
    }
    
    /**
     * Returns a human readable name, if available. If no name is available in
     * the specified locale,   then this method returns a name in an arbitrary
     * locale. If no name is available in any locale, then this method returns
     * <code>null</code>. The default implementation always returns <code>null</code>.
     *
     * @param  locale The desired locale, or <code>null</code> for a default locale.
     * @return The transform name localized in the specified locale if possible, or
     *         <code>null</code> if no name is available in any locale.
     */
    protected String getName(final Locale locale) {
        return null;
    }
    
    /**
     * Tests whether this transform does not move any points.
     * The default implementation always returns <code>false</code>.
     */
    public boolean isIdentity() {
        return false;
    }
    
    /**
     * Transforms the specified <code>ptSrc</code> and stores the result in <code>ptDst</code>.
     * The default implementation invokes {@link #transform(double[],int,double[],int,int)}
     * using a temporary array of doubles.
     *
     * @param ptSrc the specified coordinate point to be transformed.
     * @param ptDst the specified coordinate point that stores the
     *              result of transforming <code>ptSrc</code>, or
     *              <code>null</code>.
     * @return the coordinate point after transforming <code>ptSrc</code>
     *         and stroring the result in <code>ptDst</code>.
     * @throws MismatchedDimensionException if this transform
     *         doesn't map two-dimensional coordinate systems.
     * @throws TransformException if the point can't be transformed.
     *
     * @see MathTransform2D#transform(Point2D,Point2D)
     */
    public Point2D transform(final Point2D ptSrc, final Point2D ptDst)
            throws TransformException
    {
        if (getDimSource()!=2 || getDimTarget()!=2) {
            throw new MismatchedDimensionException();
        }
        final double[] ord = new double[] {ptSrc.getX(), ptSrc.getY()};
        this.transform(ord, 0, ord, 0, 1);
        if (ptDst!=null) {
            ptDst.setLocation(ord[0], ord[1]);
            return ptDst;
        } else {
            return new Point2D.Double(ord[0], ord[1]);
        }
    }
    
    /**
     * Transforms the specified <code>ptSrc</code> and stores the result
     * in <code>ptDst</code>. The default implementation invokes
     * {@link #transform(double[],int,double[],int,int)}.
     */
    public CoordinatePoint transform(final CoordinatePoint ptSrc, CoordinatePoint ptDst)
            throws TransformException
    {
        final int  dimPoint = ptSrc.getDimension();
        final int dimSource = getDimSource();
        final int dimTarget = getDimTarget();
        if (dimPoint != dimSource) {
            throw new MismatchedDimensionException(dimPoint, dimSource);
        }
        if (ptDst==null) {
            ptDst = new CoordinatePoint(dimTarget);
        } else if (ptDst.getDimension() != dimTarget) {
            throw new MismatchedDimensionException(ptDst.getDimension(), dimTarget);
        }
        transform(ptSrc.ord, 0, ptDst.ord, 0, 1);
        return ptDst;
    }
    
    /**
     * Transforms a list of coordinate point ordinal values. The default implementation
     * invokes {@link #transform(double[],int,double[],int,int)} using a temporary array
     * of doubles.
     */
    public void transform(final float[] srcPts, final int srcOff,
                          final float[] dstPts, final int dstOff, final int numPts)
        throws TransformException
    {
        final int dimSource = getDimSource();
        final int dimTarget = getDimTarget();
        final double[] tmpPts = new double[numPts*Math.max(dimSource, dimTarget)];
        for (int i=numPts*dimSource; --i>=0;) {
            tmpPts[i] = srcPts[srcOff+i];
        }
        transform(tmpPts, 0, tmpPts, 0, numPts);
        for (int i=numPts*dimTarget; --i>=0;) {
            dstPts[dstOff+i] = (float)tmpPts[i];
        }
    }
    
    /**
     * Transform the specified shape. The default implementation compute
     * quadratic curves using three points for each shape's segments.
     *
     * @param  shape Shape to transform.
     * @return Transformed shape, or <code>shape</code> if
     *         this transform is the identity transform.
     * @throws IllegalStateException if this transform doesn't map 2D coordinate systems.
     * @throws TransformException if a transform failed.
     *
     * @see MathTransform2D#createTransformedShape(Shape)
     */
    public Shape createTransformedShape(final Shape shape) throws TransformException {
        return isIdentity() ? shape : createTransformedShape(shape, null, null, Geometry.PARALLEL);
    }
    
    /**
     * Transforme une forme géométrique. Cette méthode copie toujours les coordonnées
     * transformées dans un nouvel objet. La plupart du temps, elle produira un objet
     * {@link GeneralPath}. Elle peut aussi retourner des objets {@link Line2D} ou
     * {@link QuadCurve2D} si une telle simplification est possible.
     *
     * @param  shape  Forme géométrique à transformer.
     * @param  preTr  Transformation affine à appliquer <em>avant</em> de transformer la forme
     *                <code>shape</code>, ou <code>null</code> pour ne pas en appliquer.
     *                Cet argument sera surtout utile lors des transformations inverses.
     * @param  postTr Transformation affine à appliquer <em>après</em> avoir transformée la
     *                forme <code>shape</code>, ou <code>null</code> pour ne pas en appliquer.
     *                Cet argument sera surtout utile lors des transformations directes.
     * @param quadDir Direction des courbes quadratiques ({@link Geometry#HORIZONTAL}
     *                ou {@link Geometry#PARALLEL}).
     *
     * @return La forme géométrique transformée.
     * @throws MismatchedDimensionException if this transform
     *         doesn't map two-dimensional coordinate systems.
     * @throws TransformException Si une transformation a échoué.
     */
    final Shape createTransformedShape(final Shape shape,
                                       final AffineTransform preTr,
                                       final AffineTransform postTr,
                                       final int quadDir)
        throws TransformException
    {
        if (getDimSource()!=2 || getDimTarget()!=2) {
            throw new MismatchedDimensionException();
        }
        final PathIterator    it = shape.getPathIterator(preTr);
        final GeneralPath   path = new GeneralPath(it.getWindingRule());
        final Point2D.Float ctrl = new Point2D.Float();
        final double[]    buffer = new double[6];
        
        double ax=0, ay=0;  // Coordonnées du dernier point avant la projection.
        double px=0, py=0;  // Coordonnées du dernier point après la projection.
        int indexCtrlPt=0;  // Index du point de contrôle dans 'buffer'.
        int indexLastPt=0;  // Index du dernier point dans 'buffer'.
        for (; !it.isDone(); it.next()) {
            switch (it.currentSegment(buffer)) {
                default: {
                    throw new IllegalPathStateException();
                }
                case PathIterator.SEG_CLOSE: {
                    /*
                     * Ferme la forme géométrique, puis continue la boucle. On utilise une
                     * instruction 'continue' plutôt que 'break' car il ne faut pas exécuter
                     * le code qui suit ce 'switch'.
                     */
                    path.closePath();
                    continue;
                }
                case PathIterator.SEG_MOVETO: {
                    /*
                     * Mémorise les coordonnées spécifiées (avant et après les avoir
                     * projetées), puis continue la boucle. On utilise une instruction
                     * 'continue' plutôt que 'break' car il ne faut pas exécuter le
                     * code qui suit ce 'switch'.
                     */
                    ax = buffer[0];
                    ay = buffer[1];
                    transform(buffer, 0, buffer, 0, 1);
                    path.moveTo((float) (px=buffer[0]),
                    (float) (py=buffer[1]));
                    continue;
                }
                case PathIterator.SEG_LINETO: {
                    /*
                     * Place dans 'buffer[2,3]' les coordonnées
                     * d'un point qui se trouve sur la droite:
                     *
                     *  x = 0.5*(x1+x2)
                     *  y = 0.5*(y1+y2)
                     *
                     * Ce point sera traité après le 'switch', d'où
                     * l'utilisation d'un 'break' plutôt que 'continue'.
                     */
                    indexLastPt = 0;
                    indexCtrlPt = 2;
                    buffer[2] = 0.5*(ax + (ax=buffer[0]));
                    buffer[3] = 0.5*(ay + (ay=buffer[1]));
                    break;
                }
                case PathIterator.SEG_QUADTO: {
                    /*
                     * Place dans 'buffer[0,1]' les coordonnées
                     * d'un point qui se trouve sur la courbe:
                     *
                     *  x = 0.5*(ctrlx + 0.5*(x1+x2))
                     *  y = 0.5*(ctrly + 0.5*(y1+y2))
                     *
                     * Ce point sera traité après le 'switch', d'où
                     * l'utilisation d'un 'break' plutôt que 'continue'.
                     */
                    indexLastPt = 2;
                    indexCtrlPt = 0;
                    buffer[0] = 0.5*(buffer[0] + 0.5*(ax + (ax=buffer[2])));
                    buffer[1] = 0.5*(buffer[1] + 0.5*(ay + (ay=buffer[3])));
                    break;
                }
                case PathIterator.SEG_CUBICTO: {
                    /*
                     * Place dans 'buffer[0,1]' les coordonnées
                     * d'un point qui se trouve sur la courbe:
                     *
                     *  x = 0.25*(1.5*(ctrlx1+ctrlx2) + 0.5*(x1+x2));
                     *  y = 0.25*(1.5*(ctrly1+ctrly2) + 0.5*(y1+y2));
                     *
                     * Ce point sera traité après le 'switch', d'où
                     * l'utilisation d'un 'break' plutôt que 'continue'.
                     *
                     * NOTE: Le point calculé est bien sur la courbe, mais n'est pas
                     *       nécessairement représentatif. Cet algorithme remplace les
                     *       deux points de contrôles par un seul, ce qui se traduit par
                     *       une perte de souplesse qui peut donner de mauvais résultats
                     *       si la courbe cubique était bien tordue. Projeter une courbe
                     *       cubique ne me semble pas être un problème simple, mais ce
                     *       cas devrait être assez rare. Il se produira le plus souvent
                     *       si on essaye de projeter un cercle ou une ellipse, auxquels
                     *       cas l'algorithme actuel donnera quand même des résultats
                     *       tolérables.
                     */
                    indexLastPt = 4;
                    indexCtrlPt = 0;
                    buffer[0] = 0.25*(1.5*(buffer[0]+buffer[2]) + 0.5*(ax + (ax=buffer[4])));
                    buffer[1] = 0.25*(1.5*(buffer[1]+buffer[3]) + 0.5*(ay + (ay=buffer[5])));
                    break;
                }
            }
            /*
             * Applique la transformation sur les points qui se
             * trouve dans le buffer, puis ajoute ces points à
             * la forme géométrique projetée comme une courbe
             * quadratique.
             */
            transform(buffer, 0, buffer, 0, 2);
            if (Geometry.parabolicControlPoint(px, py,
            buffer[indexCtrlPt], buffer[indexCtrlPt+1],
            buffer[indexLastPt], buffer[indexLastPt+1],
            quadDir, ctrl)!=null) {
                path.quadTo(ctrl.x, ctrl.y, (float) (px=buffer[indexLastPt+0]),
                                            (float) (py=buffer[indexLastPt+1]));
            } else {
                path.lineTo((float) (px=buffer[indexLastPt+0]),
                            (float) (py=buffer[indexLastPt+1]));
            }
        }
        /*
         * La projection de la forme géométrique est terminée. Applique
         * une transformation affine si c'était demandée, puis retourne
         * une version si possible simplifiée de la forme géométrique.
         */
        if (postTr!=null) {
            path.transform(postTr);
        }
        return Geometry.toPrimitive(path);
    }
    
    /**
     * Gets the derivative of this transform at a point. The default implementation always
     * throw an exception. Subclasses that implement the {@link MathTransform2D} interface
     * should override this method. Other subclasses should override
     * {@link #derivative(CoordinatePoint)} instead.
     *
     * @param  point The coordinate point where to evaluate the derivative.
     * @return The derivative at the specified point as a 2&times;2 matrix.
     * @throws MismatchedDimensionException if the input dimension is not 2.
     * @throws TransformException if the derivative can't be evaluated at the specified point.
     *
     * @see MathTransform2D#derivative(Point2D)
     */
    public Matrix derivative(final Point2D point) throws TransformException {
        final int dimSource = getDimSource();
        if (dimSource != 2) {
            throw new MismatchedDimensionException(2, dimSource);
        }
        throw new TransformException(Resources.format(ResourceKeys.ERROR_CANT_COMPUTE_DERIVATIVE));
    }
    
    /**
     * Gets the derivative of this transform at a point. The default implementation
     * ensure that <code>point</code> has a valid dimension. Next, it try to delegate
     * the work to an other method:
     *
     * <ul>
     *   <li>If the input dimension is 2, then this method delegates the work to
     *       {@link #derivative(Point2D)}.</li>
     *   <li>If this object is an instance of {@link MathTransform1D}, then this
     *       method delegates the work to {@link MathTransform1D#derivative(double)
     *       derivative(double)}.</li>
     * </ul>
     *
     * Otherwise, a {@link TransformException} is thrown.
     *
     * @param  point The coordinate point where to evaluate the derivative.
     * @return The derivative at the specified point (never <code>null</code>).
     * @throws NullPointerException if the derivative dependents on coordinate
     *         and <code>point</code> is <code>null</code>.
     * @throws MismatchedDimensionException if <code>point</code> doesn't have
     *         the expected dimension.
     * @throws TransformException if the derivative can't be evaluated at the
     *         specified point.
     */
    public Matrix derivative(final CoordinatePoint point) throws TransformException {
        final int dimSource = getDimSource();
        if (point != null) {
            final int dimPoint = point.getDimension();
            if (dimPoint != dimSource) {
                throw new MismatchedDimensionException(dimPoint, dimSource);
            }
            if (dimSource == 2) {
                return derivative(point.toPoint2D());
            }
        } else if (dimSource == 2) {
            return derivative((Point2D)null);
        }
        if (this instanceof MathTransform1D) {
            return new Matrix(1, 1, new double[] {
                ((MathTransform1D) this).derivative(point.ord[0])
            });
        }
        throw new TransformException(Resources.format(ResourceKeys.ERROR_CANT_COMPUTE_DERIVATIVE));
    }
    
    /**
     * Creates the inverse transform of this object.
     * The default implementation returns <code>this</code> if this transform is an identity
     * transform, and throws a {@link NoninvertibleTransformException} otherwise. Subclasses
     * should override this method.
     */
    public MathTransform inverse() throws NoninvertibleTransformException {
        if (isIdentity()) {
            return this;
        }
        throw new NoninvertibleTransformException(
                Resources.format(ResourceKeys.ERROR_NONINVERTIBLE_TRANSFORM));
    }

    /**
     * Concatenates in an optimized way a {@link MathTransform} <code>other</code> to this
     * <code>MathTransform</code>. A new math transform is created to perform the combined
     * transformation. The <code>applyOtherFirst</code> value determine the transformation
     * order as bellow:
     *
     * <ul>
     *   <li>If <code>applyOtherFirst</code> is <code>true</code>, then transforming a point
     *       <var>p</var> by the combined transform is equivalent to first transforming
     *       <var>p</var> by <code>other</code> and then transforming the result by the
     *       original transform <code>this</code>.</li>
     *   <li>If <code>applyOtherFirst</code> is <code>false</code>, then transforming a point
     *       <var>p</var> by the combined transform is equivalent to first transforming
     *       <var>p</var> by the original transform <code>this</code> and then transforming
     *       the result by <code>other</code>.</li>
     * </ul>
     *
     * If no special optimization is available for the combined transform, then this method
     * returns <code>null</code>.  In the later case, the concatenation will be prepared by
     * {@link MathTransformFactory} using a generic {@link ConcatenatedTransform}.
     *
     * The default implementation always returns <code>null</code>. This method is ought to be
     * overrided by subclasses capable of concatenating some combinaison of transforms in a
     * special way. Examples are {@link ExponentialTransform1D} and {@link LogarithmicTransform1D}.
     *
     * @param  other The math transform to apply.
     * @param  applyOtherFirst <code>true</code> if the transformation order is <code>other</code>
     *         followed by <code>this</code>, or <code>false</code> if the transformation order is
     *         <code>this</code> followed by <code>other</code>.
     * @return The combined math transform, or <code>null</code> if no optimized combined
     *         transform is available.
     */
    MathTransform concatenate(final MathTransform other, final boolean applyOtherFirst) {
        return null;
    }
    
    /**
     * Returns a hash value for this transform.
     */
    public int hashCode() {
        return getDimSource() + 37*getDimTarget();
    }
    
    /**
     * Compares the specified object with this math transform for equality.
     * The default implementation checks if <code>object</code> is an instance
     * of the same class than <code>this</code>. Subclasses should override
     * this method in order to compare internal fields.
     */
    public boolean equals(final Object object) {
        // Do not check 'object==this' here, since this
        // optimization is usually done in subclasses.
        return (object!=null && getClass().equals(object.getClass()));
    }
    
    /**
     * Returns a string représentation of this transform.
     * Subclasses should override this method in order to
     * returns Well Know Text (WKT) instead.
     */
    public String toString() {
        final StringBuffer buffer=new StringBuffer(Utilities.getShortClassName(this));
        buffer.append('[');
        final String name = getName(null);
        if (name != null) {
            buffer.append('"');
            buffer.append(name);
            buffer.append("\": ");
        }
        buffer.append(getDimSource());
        buffer.append("D \u2192 "); // Arrow -->
        buffer.append(getDimTarget());
        buffer.append("D]");
        return buffer.toString();
    }
    
    /**
     * Returns a string buffer initialized with "PARAM_MT"
     * and a classification name. This is a convenience
     * method for WKT formatting.
     */
    static StringBuffer paramMT(final String classification) {
        final StringBuffer buffer=new StringBuffer("PARAM_MT[\"");
        buffer.append(classification);
        buffer.append('"');
        return buffer;
    }
    
    /**
     * Add the <code>", PARAMETER["<name>", <value>]"</code> string
     * to the specified string buffer. This is a convenience method
     * for constructing WKT for "PARAM_MT".
     */
    static void addParameter(final StringBuffer buffer, final String key, final double value) {
        buffer.append(", PARAMETER[\"");
        buffer.append(key);
        buffer.append("\",");
        buffer.append(value);
        buffer.append(']');
    }
    
    /**
     * Add the <code>", PARAMETER["<name>", <value>]"</code> string
     * to the specified string buffer. This is a convenience method
     * for constructing WKT for "PARAM_MT".
     */
    static void addParameter(final StringBuffer buffer, final String key, final int value) {
        buffer.append(", PARAMETER[\"");
        buffer.append(key);
        buffer.append("\",");
        buffer.append(value);
        buffer.append(']');
    }
    
    /**
     * Returns an OpenGIS interface for this math transform.
     * The returned object is suitable for RMI use. This method
     * look in the cache. If no interface was previously cached,
     * then this method create a new adapter  and cache the result.
     *
     * @param  adapters The originating {@link Adapters}.
     * @return A {@link CT_MathTransform} object. The returned type
     *         is a generic {@link Object} in order to avoid too early
     *         class loading of OpenGIS interface.
     */
    final Object cachedOpenGIS(final Object adapters) {
        if (proxy!=null) {
            if (proxy instanceof Reference) {
                final Object ref = ((Reference) proxy).get();
                if (ref!=null) {
                    return ref;
                }
            } else {
                return proxy;
            }
        }
        final Object opengis = new MathTransformExport(adapters, this);
        proxy = new WeakReference(opengis);
        return opengis;
    }
    
    /**
     * Invert the specified matrix in place. If the matrix can't be inverted
     * because of a {@link SingularMatrixException}, then the exception is
     * wrapped into a {@link NoninvertibleTransformException}.
     */
    private static Matrix invert(final Matrix matrix) throws NoninvertibleTransformException {
        try {
            matrix.invert();
            return matrix;
        } catch (SingularMatrixException exception) {
            NoninvertibleTransformException e = new NoninvertibleTransformException(Resources.format(ResourceKeys.ERROR_NONINVERTIBLE_TRANSFORM));
            e.initCause(exception);
            throw e;
        }
    }
    
    /**
     * Default implementation for inverse math transform. This inner class is the inverse
     * of the enclosing {@link MathTransform}. It is serializable only if the enclosing
     * math transform is also serializable.
     *
     * @version $Id: AbstractMathTransform.java,v 1.9 2003/01/20 23:16:16 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    protected abstract class Inverse extends AbstractMathTransform implements Serializable {
        /**
         * Serial number for interoperability with different versions. This serial number is
         * especilly important for inner classes, since the default <code>serialVersionUID</code>
         * computation will not produce consistent results across implementations of different
         * Java compiler. This is because different compilers may generate different names for
         * synthetic members used in the implementation of inner classes. See:
         *
         * http://developer.java.sun.com/developer/bugParade/bugs/4211550.html
         */
        private static final long serialVersionUID = -864892964444937416L;

        /**
         * Construct an inverse math transform.
         */
        public Inverse() {
        }
        
        /**
         * Gets the dimension of input points. The default
         * implementation returns the dimension of output
         * points of the enclosing math transform.
         */
        public int getDimSource() {
            return AbstractMathTransform.this.getDimTarget();
        }
        
        /**
         * Gets the dimension of output points. The default
         * implementation returns the dimension of input
         * points of the enclosing math transform.
         */
        public int getDimTarget() {
            return AbstractMathTransform.this.getDimSource();
        }
        
        /**
         * Gets the derivative of this transform at a point. The default
         * implementation compute the inverse of the matrix returned by
         * the enclosing math transform.
         */
        public Matrix derivative(final Point2D point) throws TransformException {
            return invert(AbstractMathTransform.this.derivative(this.transform(point, null)));
        }
        
        /**
         * Gets the derivative of this transform at a point. The default
         * implementation compute the inverse of the matrix returned by
         * the enclosing math transform.
         */
        public Matrix derivative(final CoordinatePoint point) throws TransformException {
            return invert(AbstractMathTransform.this.derivative(this.transform(point, null)));
        }
        
        /**
         * Returns the inverse of this math transform, which is the enclosing math transform.
         * This method is declared final because some implementation assume that the inverse
         * of <code>this</code> is always <code>AbstractMathTransform.this</code>.
         */
        public final MathTransform inverse() {
            return AbstractMathTransform.this;
        }
        
        /**
         * Tests whether this transform does not move any points.
         * The default implementation delegate this tests to the
         * enclosing math transform.
         */
        public boolean isIdentity() {
            return AbstractMathTransform.this.isIdentity();
        }
        
        /**
         * Returns a hash code value for this math transform.
         */
        public int hashCode() {
            return ~AbstractMathTransform.this.hashCode();
        }
        
        /**
         * Compares the specified object with this inverse math
         * transform for equality. The default implementation tests
         * if <code>object</code> in an instance of the same class
         * than <code>this</code>, and then test their enclosing
         * math transforms.
         */
        public boolean equals(final Object object) {
            if (object==this) {
                // Slight optimization
                return true;
            }
            if (object instanceof Inverse) {
                final Inverse that = (Inverse) object;
                return Utilities.equals(this.inverse(), that.inverse());
            } else {
                return false;
            }
        }
        
        /**
         * Returns the Well Know Text (WKT)
         * for this inverse math transform.
         */
        public String toString() {
            return "INVERSE_MT["+AbstractMathTransform.this+']';
        }
    }
}
