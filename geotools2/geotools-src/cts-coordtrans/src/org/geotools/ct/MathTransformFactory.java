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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.ct;

// J2SE dependencies
import java.util.Locale;
import java.text.ParseException;
import java.awt.geom.AffineTransform;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

// JAI dependencies
import javax.media.jai.ParameterList;
import javax.media.jai.IntegerSequence;

// OpenGIS dependencies
import org.opengis.pt.PT_Matrix;
import org.opengis.ct.CT_Parameter;
import org.opengis.ct.CT_MathTransform;
import org.opengis.ct.CT_MathTransformFactory;

// Geotools dependencies
import org.geotools.pt.Matrix;
import org.geotools.cs.Projection;
import org.geotools.cs.FactoryException;
import org.geotools.ct.proj.Provider;

// Resources
import org.geotools.units.Unit;
import org.geotools.util.WeakHashSet;
import org.geotools.resources.XArray;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.DescriptorNaming;
import org.geotools.resources.JAIUtilities;


/**
 * Creates math transforms. <code>MathTransformFactory</code> is a low level
 * factory that is used to create {@link MathTransform} objects.   Many high
 * level GIS applications will never need to use a <code>MathTransformFactory</code>
 * directly; they can use a {@link CoordinateTransformationFactory} instead.
 * However, the <code>MathTransformFactory</code> class is specified here,
 * since it can be used directly by applications that wish to transform other
 * types of coordinates (e.g. color coordinates, or image pixel coordinates).
 * <br><br>
 * A math transform is an object that actually does the work of applying
 * formulae to coordinate values.    The math transform does not know or
 * care how the coordinates relate to positions in the real world.  This
 * lack of semantics makes implementing <code>MathTransformFactory</code>
 * significantly easier than it would be otherwise.
 *
 * For example <code>MathTransformFactory</code> can create affine math
 * transforms. The affine transform applies a matrix to the coordinates
 * without knowing how what it is doing relates to the real world. So if
 * the matrix scales <var>Z</var> values by a factor of 1000, then it could
 * be converting meters into millimeters, or it could be converting kilometers
 * into meters.
 * <br><br>
 * Because math transforms have low semantic value (but high mathematical
 * value), programmers who do not have much knowledge of how GIS applications
 * use coordinate systems, or how those coordinate systems relate to the real
 * world can implement <code>MathTransformFactory</code>.
 *
 * The low semantic content of math transforms also means that they will be
 * useful in applications that have nothing to do with GIS coordinates.  For
 * example, a math transform could be used to map color coordinates between
 * different color spaces, such as converting (red, green, blue) colors into
 * (hue, light, saturation) colors.
 * <br><br>
 * Since a math transform does not know what its source and target coordinate
 * systems mean, it is not necessary or desirable for a math transform object
 * to keep information on its source and target coordinate systems.
 *
 * @version $Id: MathTransformFactory.java,v 1.28 2003/08/04 17:11:17 desruisseaux Exp $
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.ct.CT_MathTransformFactory
 *
 * @task REVISIT: Consider creating a set of package-privated methods
 *                in <code>AbstractMathTransform</code>  (for example
 *                <code>concatenante(MathTransformFactory, ...)</code>) and moves some code in
 *                <code>AbstractMathTransform</code>'s sub-classes. It would simplify
 *                <code>MathTransformFactory</code> but would introduces dependencies
 *                to the factory right inside the abstract math transform.
 */
public class MathTransformFactory {
    /**
     * The default math transform factory. This factory
     * will be constructed only when first needed.
     */
    private static MathTransformFactory DEFAULT;

    /**
     * The object to use for parsing <cite>Well-Known Text</cite> (WKT) strings.
     * Will be created only when first needed.
     */
    private transient WKTParser parser;
    
    /**
     * A pool of math transform. This pool is used in order to
     * returns instance of existing math transforms when possible.
     */
    static final WeakHashSet pool = new WeakHashSet();

    /**
     * Identity transforms for dimensions ranging from to 0 to 7. Used only in order to
     * make the call to {@link #createIdentityTransform} faster and generate less garbages.
     * Elements in this array will be created only when first requested.
     */
    private static final MathTransform[] identities = new MathTransform[8];
    
    /**
     * List of registered math transforms.
     */
    private final MathTransformProvider[] providers;

    /**
     * OpenGIS object returned by {@link #toOpenGIS}.
     * It may be a hard or a weak reference.
     */
    private transient Object proxy;
    
    /**
     * Construct a factory using the specified providers.
     */
    public MathTransformFactory(final MathTransformProvider[] providers) {
        this.providers = (MathTransformProvider[]) providers.clone();
    }
    
    /**
     * Returns the default math transform factory.
     */
    public static synchronized MathTransformFactory getDefault() {
        if (DEFAULT == null) {
            MathTransformProvider[] transforms = new MathTransformProvider[] {
                new              MatrixTransform.Provider(),      // Affine (default to 4x4)
                new          GeocentricTransform.Provider(false), // Ellipsoid_To_Geocentric
                new          GeocentricTransform.Provider(true),  // Geocentric_To_Ellipsoid
                new  AbridgedMolodenskiTransform.Provider(),      // Abridged_Molodenski
                new       ExponentialTransform1D.Provider(false), // Exponential
                new       ExponentialTransform1D.Provider(true)   // Logarithmic
            };
            final int offset = transforms.length;
            final MathTransformProvider[] projections = Provider.getDefaults();
            transforms = (MathTransformProvider[])XArray.resize(transforms, offset+projections.length);
            System.arraycopy(projections, 0, transforms, offset, projections.length);
            DEFAULT = new MathTransformFactory(transforms);
        }
        return DEFAULT;
    }
    
    /**
     * Creates an identity transform of the specified dimension.
     *
     * @param  dimension The source and target dimension.
     * @return The identity transform.
     */
    public MathTransform createIdentityTransform(final int dimension) {
        // No need to synchronize.
        MathTransform transform;
        if (dimension < identities.length) {
            transform = identities[dimension];
            if (transform != null) {
                return transform;
            }
        }
        // Affine transform has one more row/column than dimension.
        transform = createAffineTransform(new Matrix(dimension+1));
        if (dimension < identities.length) {
            identities[dimension] = transform;
        }
        return transform;
    }
    
    /**
     * Creates an affine transform from a matrix.
     *
     * @param matrix The matrix used to define the affine transform.
     * @return The affine transform.
     */
    public MathTransform2D createAffineTransform(final AffineTransform matrix) {
        if (matrix.isIdentity()) {
            return MathTransform2D.IDENTITY;
        }
        return (MathTransform2D) pool.canonicalize(new AffineTransform2D(matrix));
    }
    
    /**
     * Creates an affine transform from a matrix.
     *
     * @param  matrix The matrix used to define the affine transform.
     * @return The affine transform.
     *
     * @see org.opengis.ct.CT_MathTransformFactory#createAffineTransform
     */
    public MathTransform createAffineTransform(final Matrix matrix) {
        /*
         * If the user is requesting a 2D transform, delegate to the
         * highly optimized java.awt.geom.AffineTransform class.
         */
        final int numRow = matrix.getNumRow();
        if (matrix.isAffine()) {
            // Affine transform are square.
            switch (numRow) {
                case 2: return (MathTransform) pool.canonicalize(
                            LinearTransform1D.create(matrix.getElement(0,0),   // scale
                                                     matrix.getElement(0,1))); // offset
                case 3: return createAffineTransform(matrix.toAffineTransform2D());
            }
        }
        /*
         * The 1D and 2D cases have their own optimized identity transform, which is why
         * the test for identity must come after the 'isAffine()' test. If the transform
         * is not an identity, fallback to the general case (slower).  May not be a real
         * affine transform, but accept it anyway...
         */
        return (MathTransform) pool.canonicalize(matrix.isIdentity() ?
                                (MathTransform) new IdentityTransform(numRow-1) :
                                (MathTransform) new MatrixTransform(matrix));
    }
    
    /**
     * Returns the underlying matrix for the specified transform,
     * or <code>null</code> if the matrix is unavailable.
     */
    private static Matrix getMatrix(final MathTransform transform) {
        if (transform instanceof LinearTransform) {
            return ((LinearTransform) transform).getMatrix();
        }
        if (transform instanceof AffineTransform) {
            return new Matrix((AffineTransform) transform);
        }
        return null;
    }
    
    /**
     * Tests if one math transform is the inverse of the other. This implementation
     * can't detect every case. It just detect the case when <code>tr2</code> is an
     * instance of {@link AbstractMathTransform.Inverse}.
     */
    private static boolean areInverse(final MathTransform tr1, final MathTransform tr2) {
        if (tr2 instanceof AbstractMathTransform.Inverse) {
            return tr1.equals(((AbstractMathTransform.Inverse) tr2).inverse());
            // TODO: we could make this test more general (just compare with tr2.inverse(),
            //       no matter if it is an instance of AbstractMathTransform.Inverse or not,
            //       and catch the exception if one is thrown). Would it be too expensive to
            //       create inconditionnaly the inverse transform?
        }
        return false;
    }
    
    /**
     * Creates a transform by concatenating two existing transforms.
     * A concatenated transform acts in the same way as applying two
     * transforms, one after the other. The dimension of the output
     * space of the first transform must match the dimension of the
     * input space in the second transform. If you wish to concatenate
     * more than two transforms, then you can repeatedly use this method.
     *
     * @param  tr1 The first transform to apply to points.
     * @param  tr2 The second transform to apply to points.
     * @return The concatenated transform.
     *
     * @see org.opengis.ct.CT_MathTransformFactory#createConcatenatedTransform
     *
     * @task TODO: We could add one more optimisation: if one transform is a matrix and the
     *             other transform is a PassThroughTransform, and if the matrix as 0 elements
     *             for all rows matching the PassThrough sub-transform, then we can get ride
     *             of the whole PassThroughTransform object.
     */
    public MathTransform createConcatenatedTransform(MathTransform tr1, MathTransform tr2) {
        if (tr1.isIdentity()) return tr2;
        if (tr2.isIdentity()) return tr1;
        /*
         * If both transforms use matrix, then we can create
         * a single transform using the concatenated matrix.
         */
        final Matrix matrix1 = getMatrix(tr1);
        if (matrix1 != null) {
            final Matrix matrix2 = getMatrix(tr2);
            if (matrix2 != null) {
                // Compute "matrix = matrix2 * matrix1". Reuse an existing matrix object
                // if possible, which is always the case when both matrix are square.
                final int numRow = matrix2.getNumRow();
                final int numCol = matrix1.getNumCol();
                final Matrix matrix;
                if (numCol == matrix2.getNumCol()) {
                    matrix = matrix2;
                    matrix2.mul(matrix1);
                } else {
                    matrix = new Matrix(numRow, numCol);
                    matrix.mul(matrix2, matrix1);
                }
                // May not be really affine, but work anyway...
                // This call will detect and optimize the special
                // case where an 'AffineTransform' can be used.
                return createAffineTransform(matrix);
            }
        }
        /*
         * If one transform is the inverse of the
         * other, returns the identity transform.
         */
        if (areInverse(tr1, tr2) || areInverse(tr2, tr1)) {
            assert tr1.getDimSource()==tr2.getDimTarget();
            assert tr1.getDimTarget()==tr2.getDimSource();
            return createIdentityTransform(tr1.getDimSource());
        }
        /*
         * If one or both math transform are instance of {@link ConcatenatedTransform},
         * then maybe it is possible to efficiently concatenate <code>tr1</code> or
         * <code>tr2</code> with one of step transforms. Try that...
         */
        if (tr1 instanceof ConcatenatedTransform) {
            final ConcatenatedTransform ctr = (ConcatenatedTransform) tr1;
            tr1 = ctr.transform1;
            tr2 = createConcatenatedTransform(ctr.transform2, tr2);
        }
        if (tr2 instanceof ConcatenatedTransform) {
            final ConcatenatedTransform ctr = (ConcatenatedTransform) tr2;
            tr1 = createConcatenatedTransform(tr1, ctr.transform1);
            tr2 = ctr.transform2;
        }
        /*
         * Before to create a general {@link ConcatenatedTransform} object, give a
         * chance to {@link AbstractMathTransform} to returns an optimized object.
         */
        if (tr1 instanceof AbstractMathTransform) {
            final MathTransform optimized = ((AbstractMathTransform) tr1).concatenate(tr2, false);
            if (optimized != null) {
                return (MathTransform) pool.canonicalize(optimized);
            }
        }
        if (tr2 instanceof AbstractMathTransform) {
            final MathTransform optimized = ((AbstractMathTransform) tr2).concatenate(tr1, true);
            if (optimized != null) {
                return (MathTransform) pool.canonicalize(optimized);
            }
        }
        return (MathTransform) pool.canonicalize(ConcatenatedTransform.create(this, tr1, tr2));
    }

    /**
     * Creates a transform which passes through a subset of ordinates to another transform.
     * This allows transforms to operate on a subset of ordinates. For example, if you have
     * (<var>latitidue</var>,<var>longitude</var>,<var>height</var>) coordinates, then you
     * may wish to convert the height values from feet to meters without affecting the
     * latitude and longitude values.
     *
     * @param  firstAffectedOrdinate Index of the first affected ordinate.
     * @param  subTransform The sub transform.
     * @param  numTrailingOrdinates Number of trailing ordinates to pass through.
     *         Affected ordinates will range from <code>firstAffectedOrdinate</code>
     *         inclusive to <code>dimTarget-numTrailingOrdinates</code> exclusive.
     * @return A pass through transform with the following dimensions:<br>
     *         <pre>
     * Source: firstAffectedOrdinate + subTransform.getDimSource() + numTrailingOrdinates
     * Target: firstAffectedOrdinate + subTransform.getDimTarget() + numTrailingOrdinates</pre>
     *
     * @see org.opengis.ct.CT_MathTransformFactory#createPassThroughTransform
     * @see #createSubTransform
     */
    public MathTransform createPassThroughTransform(final int firstAffectedOrdinate,
                                                    final MathTransform subTransform,
                                                    final int numTrailingOrdinates)
    {
        if (firstAffectedOrdinate < 0) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                    "firstAffectedOrdinate", new Integer(firstAffectedOrdinate)));
        }
        if (numTrailingOrdinates < 0) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                    "numTrailingOrdinates", new Integer(numTrailingOrdinates)));
        }
        if (firstAffectedOrdinate==0 && numTrailingOrdinates==0) {
            return subTransform;
        }
        /*
         * Optimize the "Identity transform" case.
         */
        if (subTransform.isIdentity()) {
            final int dimension = subTransform.getDimSource();
            if (dimension == subTransform.getDimTarget()) {
                // The AffineTransform is easier to concatenate with other transforms.
                return createIdentityTransform(firstAffectedOrdinate + dimension + numTrailingOrdinates);
            }
        }
        /*
         * Special case for transformation backed by a matrix. Is is possible to use a
         * new matrix for such transform, instead of wrapping the sub-transform into a
         * PassThroughTransform object. It is faster and easier to concatenate.
         */
        if (subTransform instanceof LinearTransform) {
            Matrix matrix = ((LinearTransform)subTransform).getMatrix();
            matrix = PassThroughTransform.expand(matrix, firstAffectedOrdinate, numTrailingOrdinates, 1);
            return createAffineTransform(matrix);
        }
        /*
         * Construct the general PassThroughTransform object. An optimisation
         * for the "Pass through case" is done right in the  constructor.
         */
        return (MathTransform) pool.canonicalize(new PassThroughTransform(
                firstAffectedOrdinate, subTransform, numTrailingOrdinates));
    }
    
    /**
     * Creates a transform working on a subset of an other transform's inputs/outputs.
     *
     * @param  lower Index of the first ordinate to keep.
     * @param  upper Index after the last ordinate to keep. Must be greater than <code>lower</code>.
     * @param  transform The transform. Its dimension must not be lower than <code>upper</code>.
     * @return The same transform than <code>transform</code>, but keeping only ordinates
     *         from index <code>lower</code> inclusive to <code>upper</code> exclusive.
     *
     * @deprecated This method never specified clearly if it was working on input or output
     *             dimensions, and actually mixed both of them. This method is Now replaced
     *             by {@link #createSubTransform} and {@link #createFilterTransform}.
     */
    public MathTransform createSubMathTransform(final int lower, final int upper,
                                                final MathTransform transform)
    {
        if (transform instanceof PassThroughTransform) try {
            // It was the wrong behaviour in the old implementation.
            // Keep it until we delete this deprecated method.
            return createSubTransform(transform, JAIUtilities.createSequence(lower, upper-1), null);
        } catch (FactoryException exception) {
            throw new RuntimeException(exception.getLocalizedMessage(), exception);
        }
        return createFilterTransform(transform, JAIUtilities.createSequence(lower, upper-1));
    }

    /**
     * Reduces the number of input dimensions for the specified transform. The remaining output
     * dimensions will be selected automatically according the specified input dimensions.  For
     * example if the supplied <code>transform</code> has (<var>x</var>, <var>y</var>, <var>z</var>)
     * inputs and (<var>longitude</var>, <var>latitude</var>, <var>height</var>) outputs,  then
     * <code>createSubTransform(transform, new {@linkplain IntegerSequence IntegerSequence}(0,1),
     * null)</code> will returns a transform with (<var>x</var>, <var>y</var>) inputs and (probably)
     * (<var>longitude</var>, <var>latitude</var>) outputs. This method can be used in order to
     * separate a {@linkplain #createConcatenatedTransform concatenation} of
     * {@linkplain #createPassThroughTransform pass through} transforms.
     *
     * @param  transform The transform to reduces.
     * @param  inputDimensions The input dimension to keep. This sequence can contains any integers
     *         in the range 0 inclusive to <code>transform.{@linkplain MathTransform#getDimSource
     *         getDimSource()}</code> exclusive.
     * @param  outputDimensions An optional sequence in which to store output dimensions. If
     *         non-null, then this sequence will be filled with output dimensions selected by
     *         this method. The integers will be in the range 0 inclusive to
     *         <code>transform.{@linkplain MathTransform#getDimTarget getDimTarget()}</code>
     *         exclusive.
     * @return A transform expecting only the specified input dimensions. The following invariant
     *         should hold:
     *         <blockquote><pre>
     *         subTransform.{@linkplain MathTransform#getDimSource getDimSource()} ==  inputDimensions.{@linkplain IntegerSequence#getNumElements getNumElements()};
     *         subTransform.{@linkplain MathTransform#getDimTarget getDimTarget()} == outputDimensions.{@linkplain IntegerSequence#getNumElements getNumElements()};
     *         </pre></blockquote>
     *
     * @throws FactoryException if the transform is not separable.
     */
    public MathTransform createSubTransform(final MathTransform    transform,
                                            final IntegerSequence  inputDimensions,
                                            final IntegerSequence outputDimensions)
            throws FactoryException
    {
        final int dimSource = transform.getDimSource();
        final int dimTarget = transform.getDimTarget();
        final int dimInput  = inputDimensions.getNumElements();
        final int lower     = JAIUtilities.getMinimum(inputDimensions);
        final int upper     = JAIUtilities.getMaximum(inputDimensions)+1;
        if (lower<0 || lower>=upper) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                    "minimum(inputDimensions)", new Integer(lower)));
        }
        if (upper > dimSource) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                    "maximum(inputDimensions)", new Integer(upper-1)));
        }
        /*
         * Check for easiest cases: same transform, identity transform or concatenated transforms.
         */
        if (dimInput == dimSource) {
            assert lower==0 && upper==dimSource;
            JAIUtilities.fill(outputDimensions, 0, dimTarget);
            return transform;
        }
        if (transform.isIdentity()) {
            JAIUtilities.add(outputDimensions, inputDimensions, 0);
            return createIdentityTransform(dimInput);
        }
        if (transform instanceof ConcatenatedTransform) {
            final ConcatenatedTransform ctr = (ConcatenatedTransform) transform;
            final IntegerSequence trans = new IntegerSequence();
            final MathTransform step1 = createSubTransform(ctr.transform1, inputDimensions, trans);
            final MathTransform step2 = createSubTransform(ctr.transform2, trans, outputDimensions);
            return createConcatenatedTransform(step1, step2);
        }
        /*
         * Special case for the pass through transform:  if at least one input dimension
         * belong to the passthrough's sub-transform, then delegates part of the work to
         * <code>subTransform(passThrough.transform, ...)</code>
         */
        if (transform instanceof PassThroughTransform) {
            final PassThroughTransform passThrough = (PassThroughTransform) transform;
            final int dimPass  = passThrough.transform.getDimSource();
            final int dimDiff  = passThrough.transform.getDimTarget() - dimPass;
            final int subLower = passThrough.firstAffectedOrdinate;
            final int subUpper = subLower + dimPass;
            final IntegerSequence subInputs = new IntegerSequence();
            for (inputDimensions.startEnumeration(); inputDimensions.hasMoreElements();) {
                int n = inputDimensions.nextElement();
                if (n>=subLower && n<subUpper) {
                    subInputs.insert(n - subLower);
                } else if (outputDimensions != null) {
                    if (n >= subUpper) {
                        n += dimDiff;
                    }
                    outputDimensions.insert(n);
                }
            }
            if (subInputs.getNumElements() == 0) {
                // No input dimension belong to the sub-transform.
                return createIdentityTransform(dimInput);
            }
            final IntegerSequence subOutputs;
            final MathTransform subTransform;
            subOutputs = (outputDimensions!=null) ? new IntegerSequence() : null;
            subTransform = createSubTransform(passThrough.transform, subInputs, subOutputs);
            JAIUtilities.add(outputDimensions, subOutputs, subLower);
            if (JAIUtilities.containsAll(inputDimensions, lower, subLower) &&
                JAIUtilities.containsAll(inputDimensions, subUpper, upper))
            {
                final int  firstAffectedOrdinate = Math.max(0, subLower-lower);
                final int   numTrailingOrdinates = Math.max(0, upper-subUpper);
                return createPassThroughTransform(firstAffectedOrdinate,
                                                  subTransform, numTrailingOrdinates);
            }
            // TODO: handle more general case here...
        }
        /*
         * If the transformation is specified by a matrix, select all output dimensions which
         * do not depends on any of the discarted input dimensions.
         */
        if (transform instanceof LinearTransform) {
            int nRows = 0;
            boolean hasLastRow = false;
            double[][] rows = ((LinearTransform)transform).getMatrix().getElements();
            assert rows.length-1 == dimTarget;
reduce:     for (int j=0; j<rows.length; j++) {
                final double[] row = rows[j];
                assert row.length-1 == dimSource : row.length;
                int nCols = 0;
                // Stop at row.length-1 because we don't
                // want to check the translation term.
                for (int i=0; i<dimSource; i++) {
                    if (JAIUtilities.contains(inputDimensions, i)) {
                        row[nCols++] = row[i];
                    } else if (row[i] != 0) {
                        // Output dimension 'j' depends on one of discarted input dimension 'i'.
                        continue reduce;
                    }
                }
                if (j == dimTarget) {
                    hasLastRow = true;
                } else if (outputDimensions != null) {
                    outputDimensions.insert(j);
                }
                assert nCols == dimInput : nCols;
                row [nCols++] = row[dimSource]; // Copy the translation term.
                rows[nRows++] = XArray.resize(row, nCols);
            }
            rows = (double[][]) XArray.resize(rows, nRows);
            if (hasLastRow) {
                return createAffineTransform(new Matrix(rows));
            }
            // In an affine transform,  the last row is not supposed to have dependency
            // to any input dimension. But in this particuler case, our matrix has such
            // dependencies. REVISIT: is there anything we could do about that?
        }
        throw new FactoryException(Resources.format(ResourceKeys.ERROR_INSEPARABLE_TRANSFORM));
    }
    
    /**
     * Creates a transform which retains only a subset of an other transform's outputs. The number
     * and nature of inputs stay unchanged. For example if the supplied <code>transform</code> has
     * (<var>longitude</var>, <var>latitude</var>, <var>height</var>) outputs, then a sub-transform
     * may be used to keep only the (<var>longitude</var>, <var>latitude</var>) part. In most cases,
     * the created sub-transform is non-invertible since it loose informations.
     * <br><br>
     * This transform may be see as a non-square matrix transform with less rows
     * than columns, concatenated with <code>transform</code>. However, invoking
     * <code>createFilterTransfom(...)</code> allows the optimization of some common cases.
     *
     * @param  transform The transform to reduces.
     * @param  outputDimensions The output dimension to keep.
     *         This sequence can contains any integers in the range 0 inclusive to
     *         <code>transform.{@linkplain MathTransform#getDimTarget getDimTarget()}</code>
     *         exclusive.
     * @return The <code>transform</code> keeping only the specified output dimensions.
     */
    public MathTransform createFilterTransform(MathTransform transform,
                                               final IntegerSequence outputDimensions)
    {
        final int dimSource = transform.getDimSource();
        final int dimTarget = transform.getDimTarget();
        final int dimOutput = outputDimensions.getNumElements();
        final int lower     = JAIUtilities.getMinimum(outputDimensions);
        final int upper     = JAIUtilities.getMaximum(outputDimensions)+1;
        if (lower<0 || lower>=upper) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                    "minimum(outputDimensions)", new Integer(lower)));
        }
        if (upper > dimTarget) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                    "maximum(outputDimensions)", new Integer(upper)));
        }
        if (dimOutput == dimTarget) {
            assert lower==0 && upper==dimTarget;
            return transform;
        }
        /*
         * If the transform is an instance of "pass through" transform but no dimension from its
         * subtransform is requested, then ignore the subtransform (i.e. treat the whole transform
         * as identity, except for the number of output dimension which may be different from the
         * number of input dimension).
         */
        int dimPass = 0;
        int dimDiff = 0;
        int dimStep = dimTarget;
        if (transform instanceof PassThroughTransform) {
            final PassThroughTransform passThrough = (PassThroughTransform) transform;
            final int subLower = passThrough.firstAffectedOrdinate;
            final int subUpper = subLower + passThrough.transform.getDimTarget();
            if (!JAIUtilities.containsAny(outputDimensions, subLower, subUpper)) {
                transform = null;
                dimStep = dimSource;
                dimPass = subLower;
                dimDiff = (subLower + passThrough.transform.getDimSource()) - subUpper;
            }
        }
        /*
         * Create the matrix to be used as a filter,        [x']     [1  0  0  0] [x]
         * and concatenate it to the transform. The         [z']  =  [0  0  1  0] [y]
         * matrix will contains only a 1 for the output     [1 ]     [0  0  0  1] [z]
         * dimension to keep, as in the following example:                        [1]
         */
        final Matrix matrix = new Matrix(dimOutput+1, dimStep+1);
        matrix.setZero();
        int j=0;
        for (outputDimensions.startEnumeration(); outputDimensions.hasMoreElements(); j++) {
            int i = outputDimensions.nextElement();
            if (i >= dimPass) {
                i += dimDiff;
            }
            matrix.setElement(j, i, 1);
        }
        // Affine transform has one more row/column than dimension.
        matrix.setElement(dimOutput, dimStep, 1);
        MathTransform filtered = createAffineTransform(matrix);
        if (transform != null) {
            filtered = createConcatenatedTransform(transform, filtered);
        }
        return filtered;
    }

    /**
     * Creates a transform from a classification name and parameters.
     * The client must ensure that all the linear parameters are expressed
     * in meters, and all the angular parameters are expressed in degrees.
     * Also, they must supply "semi_major" and "semi_minor" parameters
     * for cartographic projection transforms.
     *
     * @param  classification The classification name of the transform
     *         (e.g. "Transverse_Mercator"). Leading and trailing spaces
     *         are ignored, and comparaison is case-insensitive.
     * @param  parameters The parameter values in standard units.
     * @return The parameterized transform.
     * @throws NoSuchClassificationException if there is no transform for the specified
     *         classification.
     * @throws MissingParameterException if a parameter was required but not found.
     * @throws FactoryException if the math transform creation failed from some other reason.
     *
     * @see org.opengis.ct.CT_MathTransformFactory#createParameterizedTransform
     * @see #getAvailableTransforms
     */
    public MathTransform createParameterizedTransform(String classification,
                                                      final ParameterList parameters)
            throws MissingParameterException, FactoryException
    {
        final MathTransform transform;
        classification = classification.trim();
        if (classification.equalsIgnoreCase("Affine")) {
            return createAffineTransform(MatrixParameters.getMatrix(parameters));
        }
        transform = getMathTransformProvider(classification).create(parameters);
        return (MathTransform) pool.canonicalize(transform);
    }
    
    /**
     * Creates a transform from a projection. The client must ensure that all the linear
     * parameters are expressed in meters, and all the angular parameters are expressed
     * in degrees. Also, they must supply "semi_major" and "semi_minor" parameters for
     * cartographic projection transforms.
     *
     * @param  projection The projection.
     * @return The parameterized transform.
     * @throws NoSuchClassificationException if there is no transform for the specified projection.
     * @throws MissingParameterException if a parameter was required but not found.
     * @throws FactoryException if the math transform creation failed from some other reason.
     */
    public MathTransform createParameterizedTransform(final Projection projection)
            throws MissingParameterException, FactoryException
    {
        final MathTransform transform;
        transform = getMathTransformProvider(projection.getClassName()).create(projection);
        return (MathTransform) pool.canonicalize(transform);
    }
    
    /**
     * Returns the classification names of every available transforms.
     * The returned array may have a zero length, but will never be null.
     *
     * @see #createParameterizedTransform
     */
    public String[] getAvailableTransforms() {
        final String[] names = new String[providers.length];
        for (int i=0; i<names.length; i++) {
            names[i] = providers[i].getClassName();
        }
        return names;
    }
    
    /**
     * Returns the provider for the specified classification. This provider
     * may be used to query parameter list for a classification name (e.g.
     * <code>getMathTransformProvider("Transverse_Mercator").getParameterList()</code>),
     * or the transform name in a given locale (e.g.
     * <code>getMathTransformProvider("Transverse_Mercator").getName({@link Locale#FRENCH})</code>)
     *
     * @param  classification The classification name of the transform
     *         (e.g. "Transverse_Mercator"). It should be one of the name
     *         returned by {@link #getAvailableTransforms}. Leading and
     *         trailing spaces are ignored. Comparisons are case-insensitive.
     * @return The provider for a math transform.
     * @throws NoSuchClassificationException if there is no provider registered
     *         with the specified classification name.
     */
    public MathTransformProvider getMathTransformProvider(String classification)
            throws NoSuchClassificationException
    {
        classification = classification.trim();
        for (int i=0; i<providers.length; i++) {
            if (classification.equalsIgnoreCase(providers[i].getClassName().trim())) {
                return providers[i];
            }
        }
        throw new NoSuchClassificationException(null, classification);
    }
    
    /**
     * Create a provider for affine transforms of the specified
     * dimension. Created affine transforms will have a size of
     * <code>numRow&nbsp;&times;&nbsp;numCol</code>.
     * <br><br>
     * <table align="center" border='1' cellpadding='3' bgcolor="F4F8FF">
     *   <tr bgcolor="#B9DCFF"><th>Parameter</th> <th>Description</th></tr>
     *   <tr><td><code>num_row</code></td> <td>Number of rows in matrix</td></tr>
     *   <tr><td><code>num_col</code></td> <td>Number of columns in matrix</td></tr>
     *   <tr><td><code>elt_&lt;r&gt;_&lt;c&gt;</code></td> <td>Element of matrix</td></tr>
     * </table>
     * <br>
     * For the element parameters, <code>&lt;r&gt;</code> and <code>&lt;c&gt;</code>
     * should be substituted by printed decimal numbers. The values of <var>r</var>
     * should be from 0 to <code>(num_row-1)</code>, and the values of <var>c</var>
     * should be from 0 to <code>(num_col-1)</code>. Any undefined matrix elements
     * are assumed to be zero for <code>(r!=c)</code>, and one for <code>(r==c)</code>.
     * This corresponds to the identity transformation when the number of rows and columns
     * are the same. The number of columns corresponds to one more than the dimension of
     * the source coordinates and the number of rows corresponds to one more than the
     * dimension of target coordinates. The extra dimension in the matrix is used to
     * let the affine map do a translation.
     *
     * @param  numRow The number of matrix's rows.
     * @param  numCol The number of matrix's columns.
     * @return The provider for an affine transform.
     * @throws IllegalArgumentException if <code>numRow</code>
     *         or <code>numCol</code> is not a positive number.
     * @throws FactoryException if the provider can't be created from some other reason.
     *
     * @deprecated Use {@link #getMathTransformProvider} instead. The generic method now
     *             use an "extensible" {@link ParameterList}, which may growth or shrink
     *             according the change in matrix size.
     */
    public MathTransformProvider getAffineTransformProvider(final int numRow, final int numCol)
            throws IllegalArgumentException, FactoryException
    {
        return new MatrixTransform.Provider();
    }

    /**
     * Returns the unit for the specified parameter.
     * This method returns one of the following:
     * <ul>
     *   <li>If the specified parameter is a linear measure, then this method returns
     *       {@link Unit#METRE}. Other linear units are not authorized.</li>
     *   <li>If the specified parameter is an angular measure, then this method returns
     *       {@link Unit#DEGREE}. Other angular units are not authorized.</li>
     *   <li>Otherwise, this method may returns {@link Unit#DIMENSIONLESS} or
     *       <code>null</code>.</li>
     * </ul>
     *
     * @param  parameter The parameter name (e.g. <code>"false_easting"</code>
     *                   or <code>"central_meridian"</code>).
     * @return The parameter unit, or <code>null</code>.
     */
    public Unit getParameterUnit(final String parameter) {
        return DescriptorNaming.getParameterUnit(parameter);
    }

    /**
     * Creates a math transform object from a <cite>Well-Known Text</cite> (WKT) string.
     * WKT are part of <cite>Coordinate Transformation Services Specification</cite>.
     *
     * @param  text The <cite>Well-Known Text</cite>.
     * @return The math transform (never <code>null</code>).
     * @throws FactoryException if the Well-Known Text can't be parsed,
     *         or if the math transform creation failed from some other reason.
     */
    public MathTransform createFromWKT(final String text) throws FactoryException {
        if (parser == null) {
            // Not a big deal if we are not synchronized. If this method is invoked in
            // same time by two different threads, we may have two WKTParser objects
            // for a short time. It doesn't hurt...
            parser = new WKTParser(Locale.US, this);
        }
        try {
            return parser.parseMathTransform(text);
        } catch (ParseException exception) {
            final Throwable cause = exception.getCause();
            if (cause instanceof FactoryException) {
                throw (FactoryException) cause;
            }
            throw new FactoryException(exception.getLocalizedMessage(), exception);
        }
    }

    /**
     * Returns an OpenGIS interface for this info. This method first looks in the cache.
     * If no interface was previously cached, then this method creates a new adapter and
     * caches the result.
     *
     * Note: The returned type is a generic {@link Object} in order
     *       to avoid premature class loading of OpenGIS interface.
     *
     * @param  adapters The originating {@link Adapters}.
     * @return The OpenGIS interface for this info.
     * @throws RemoteException if the object can't be exported.
     */
    final synchronized Object toOpenGIS(final Object adapters) throws RemoteException {
        if (proxy != null) {
            if (proxy instanceof Reference) {
                final Object ref = ((Reference) proxy).get();
                if (ref != null) {
                    return ref;
                }
            } else {
                return proxy;
            }
        }
        final Object opengis = new Export(adapters);
        proxy = new WeakReference(opengis);
        return opengis;
    }
    
    
    
    
    /////////////////////////////////////////////////////////////////////////
    ////////////////                                         ////////////////
    ////////////////             OPENGIS ADAPTER             ////////////////
    ////////////////                                         ////////////////
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Wrap a {@link MathTransformFactory} for use with OpenGIS. This wrapper is a good
     * place to check for non-implemented OpenGIS methods (just check for methods throwing
     * {@link UnsupportedOperationException}). This class is suitable for RMI use.
     *
     * @version $Id: MathTransformFactory.java,v 1.28 2003/08/04 17:11:17 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private final class Export extends UnicastRemoteObject implements CT_MathTransformFactory {
        /**
         * The originating adapter.
         */
        protected final Adapters adapters;
        
        /**
         * Construct a remote object.
         */
        protected Export(final Object adapters) throws RemoteException {
            super(); // TODO: Fetch the port number from the adapter.
            this.adapters = (Adapters)adapters;
        }
        
        /**
         * Creates an affine transform from a matrix.
         */
        public CT_MathTransform createAffineTransform(final PT_Matrix matrix)
            throws RemoteException
        {
            return adapters.export(MathTransformFactory.this.createAffineTransform(
                    adapters.wrap(matrix)));
        }
        
        /**
         * Creates a transform by concatenating two existing transforms.
         */
        public CT_MathTransform createConcatenatedTransform(final CT_MathTransform transform1,
                                                            final CT_MathTransform transform2)
            throws RemoteException
        {
            return adapters.export(MathTransformFactory.this.createConcatenatedTransform(
                    adapters.wrap(transform1), adapters.wrap(transform2)));
        }
        
        /**
         * Creates a transform which passes through a subset of ordinates to another transform.
         */
        public CT_MathTransform createPassThroughTransform(final int firstAffectedOrdinate,
                                                           final CT_MathTransform subTransform)
            throws RemoteException
        {
            return adapters.export(MathTransformFactory.this.createPassThroughTransform(
                    firstAffectedOrdinate, adapters.wrap(subTransform), 0));
        }
        
        /**
         * Creates a transform from a classification name and parameters.
         */
        public CT_MathTransform createParameterizedTransform(final String classification,
                                                             final CT_Parameter[] parameters)
            throws RemoteException
        {
            try {
                return adapters.export(MathTransformFactory.this.createParameterizedTransform(
                        classification, adapters.wrap(parameters)));
            } catch (FactoryException exception) {
                throw Adapters.serverException(exception);
            }
        }
        
        /**
         * Creates a math transform from a Well-Known Text string.
         */
        public CT_MathTransform createFromWKT(final String text)
            throws RemoteException
        {
            try {
                return adapters.export(MathTransformFactory.this.createFromWKT(text));
            } catch (FactoryException exception) {
                throw Adapters.serverException(exception);
            }
        }
        
        /**
         * Creates a math transform from XML.
         */
        public CT_MathTransform createFromXML(final String xml)
            throws RemoteException
        {
            throw new UnsupportedOperationException("XML parsing not yet implemented");
        }
        
        /**
         * Tests whether parameter is angular.
         */
        public boolean isParameterAngular(final String parameterName)
            throws RemoteException
        {
            final Unit unit = getParameterUnit(parameterName);
            return (unit!=null) && Unit.DEGREE.canConvert(unit);
        }
        
        /**
         * Tests whether parameter is linear.
         */
        public boolean isParameterLinear(final String parameterName)
            throws RemoteException
        {
            final Unit unit = getParameterUnit(parameterName);
            return (unit!=null) && Unit.METRE.canConvert(unit);
        }
    }
}
