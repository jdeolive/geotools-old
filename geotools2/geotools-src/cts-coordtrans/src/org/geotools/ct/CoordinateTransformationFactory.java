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

// J2SE dependencies
import java.util.Arrays;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.server.RemoteObject;
import java.util.NoSuchElementException;
import java.awt.geom.AffineTransform;
import java.lang.ref.WeakReference;
import java.lang.ref.Reference;

// JAI dependencies
import javax.media.jai.ParameterList;
import javax.vecmath.SingularMatrixException;

// OpenGIS dependencies
import org.opengis.cs.CS_CoordinateSystem;
import org.opengis.ct.CT_CoordinateTransformation;
import org.opengis.ct.CT_CoordinateTransformationFactory;

// Geotools dependencies
import org.geotools.pt.Matrix;
import org.geotools.cs.AxisInfo;
import org.geotools.cs.Ellipsoid;
import org.geotools.cs.Projection;
import org.geotools.cs.PrimeMeridian;
import org.geotools.cs.VerticalDatum;
import org.geotools.cs.HorizontalDatum;
import org.geotools.cs.AxisOrientation;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.WGS84ConversionInfo;
import org.geotools.cs.CompoundCoordinateSystem;
import org.geotools.cs.ProjectedCoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.cs.GeocentricCoordinateSystem;
import org.geotools.cs.HorizontalCoordinateSystem;
import org.geotools.cs.VerticalCoordinateSystem;
import org.geotools.cs.TemporalCoordinateSystem;
import org.geotools.pt.Dimensioned;

// Resources
import org.geotools.units.Unit;
import org.geotools.resources.Utilities;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Creates coordinate transformations.
 *
 * @version $Id: CoordinateTransformationFactory.java,v 1.6 2002/10/09 19:36:34 desruisseaux Exp $
 * @author <A HREF="http://www.opengis.org">OpenGIS</A>
 * @author Martin Desruisseaux
 *
 * @see org.opengis.ct.CT_CoordinateTransformationFactory
 */
public class CoordinateTransformationFactory {
    /**
     * The default coordinate transformation factory.
     * Will be constructed only when first needed.
     */
    private static CoordinateTransformationFactory DEFAULT;
    
    /**
     * Number for temporary created objects. This number is incremented
     * every time {@link #getTemporaryName(CoordinateSystem)} is invoked.
     */
    private static volatile int temporaryID;
    
    /**
     * The underlying math transform factory.
     */
    private final MathTransformFactory factory;

    /**
     * OpenGIS object returned by {@link #toOpenGIS}.
     * It may be a hard or a weak reference.
     */
    private transient Object proxy;
    
    /**
     * Construct a coordinate transformation factory.
     *
     * @param factory The math transform factory to use.
     */
    public CoordinateTransformationFactory(final MathTransformFactory factory) {
        this.factory = factory;
    }
    
    /**
     * Returns the default coordinate transformation factory.
     */
    public static synchronized CoordinateTransformationFactory getDefault() {
        if (DEFAULT==null) {
            DEFAULT = new CoordinateTransformationFactory(MathTransformFactory.getDefault());
        }
        return DEFAULT;
    }
    
    /**
     * Returns the underlying math transform factory. This factory
     * is used for constructing {@link MathTransform} objects for
     * all {@link CoordinateTransformation}.
     */
    public final MathTransformFactory getMathTransformFactory() {
        return factory;
    }
    
    /**
     * Creates a transformation between two coordinate systems. This method
     * will examine the coordinate systems  and delegate the work to one or
     * many <code>createTransformationStep(...)</code> methods. This method
     * fails if no path between the coordinate systems is found.
     *
     * @param  sourceCS Input coordinate system.
     * @param  targetCS Output coordinate system.
     * @return A coordinate transformation from <code>sourceCS</code> to <code>targetCS</code>.
     * @throws CannotCreateTransformException if no transformation path has been found.
     *
     * @see org.opengis.ct.CT_CoordinateTransformationFactory#createFromCoordinateSystems
     */
    public CoordinateTransformation createFromCoordinateSystems(
                                        final CoordinateSystem sourceCS,
                                        final CoordinateSystem targetCS)
        throws CannotCreateTransformException
    {
        /////////////////////////////////////////////////////////////////////
        ////                                                             ////
        ////     Geographic  -->  Geographic, Projected or Geocentric    ////
        ////                                                             ////
        /////////////////////////////////////////////////////////////////////
        if (sourceCS instanceof GeographicCoordinateSystem) {
            final GeographicCoordinateSystem source = (GeographicCoordinateSystem) sourceCS;
            if (targetCS instanceof GeographicCoordinateSystem) {
                return createTransformationStep(source, (GeographicCoordinateSystem) targetCS);
            }
            if (targetCS instanceof ProjectedCoordinateSystem) {
                return createTransformationStep(source, (ProjectedCoordinateSystem) targetCS);
            }
            if (targetCS instanceof GeocentricCoordinateSystem) {
                return createTransformationStep(source, null, (GeocentricCoordinateSystem) targetCS);
            }
        }
        /////////////////////////////////////////////////////////////////////
        ////                                                             ////
        ////     Projected  -->  Projected, Geographic or Geocentric     ////
        ////                                                             ////
        /////////////////////////////////////////////////////////////////////
        if (sourceCS instanceof ProjectedCoordinateSystem) {
            final ProjectedCoordinateSystem source = (ProjectedCoordinateSystem) sourceCS;
            if (targetCS instanceof ProjectedCoordinateSystem) {
                return createTransformationStep(source, (ProjectedCoordinateSystem) targetCS);
            }
            if (targetCS instanceof GeographicCoordinateSystem) {
                return createTransformationStep(source, (GeographicCoordinateSystem) targetCS);
            }
            if (targetCS instanceof GeocentricCoordinateSystem) {
                return createTransformationStep(source, null, (GeocentricCoordinateSystem) targetCS);
            }
        }
        /////////////////////////////////////////////////////////////////////
        ////                                                             ////
        ////     Geocentric  -->  Geocentric, Horizontal or Compound     ////
        ////                                                             ////
        /////////////////////////////////////////////////////////////////////
        if (sourceCS instanceof GeocentricCoordinateSystem) {
            final GeocentricCoordinateSystem source = (GeocentricCoordinateSystem) sourceCS;
            if (targetCS instanceof GeocentricCoordinateSystem) {
                return createTransformationStep(source, (GeocentricCoordinateSystem) targetCS);
            }
            final HorizontalCoordinateSystem hCS = CTSUtilities.getHorizontalCS(targetCS);
            final VerticalCoordinateSystem   vCS = CTSUtilities.getVerticalCS  (targetCS);
            if (hCS instanceof GeographicCoordinateSystem) {
                return concatenate(null,
                    createTransformationStep(source, (GeographicCoordinateSystem) hCS, vCS), targetCS);
            }
            if (hCS instanceof ProjectedCoordinateSystem) {
                return concatenate(null,
                    createTransformationStep(source, (ProjectedCoordinateSystem) hCS, vCS), targetCS);
            }
        }
        /////////////////////////////////////////
        ////                                 ////
        ////     Vertical  -->  Vertical     ////
        ////                                 ////
        /////////////////////////////////////////
        if (sourceCS instanceof VerticalCoordinateSystem) {
            final VerticalCoordinateSystem source = (VerticalCoordinateSystem) sourceCS;
            if (targetCS instanceof VerticalCoordinateSystem) {
                return createTransformationStep(source, (VerticalCoordinateSystem) targetCS);
            }
        }
        /////////////////////////////////////////
        ////                                 ////
        ////     Temporal  -->  Temporal     ////
        ////                                 ////
        /////////////////////////////////////////
        if (sourceCS instanceof TemporalCoordinateSystem) {
            final TemporalCoordinateSystem source = (TemporalCoordinateSystem) sourceCS;
            if (targetCS instanceof TemporalCoordinateSystem) {
                return createTransformationStep(source, (TemporalCoordinateSystem) targetCS);
            }
        }
        ///////////////////////////////////////////
        ////                                   ////
        ////     Compound  -->  various CS     ////
        ////                                   ////
        ///////////////////////////////////////////
        if (sourceCS instanceof CompoundCoordinateSystem) {
            if (targetCS instanceof GeocentricCoordinateSystem) {
                final GeocentricCoordinateSystem target = (GeocentricCoordinateSystem) targetCS;
                final HorizontalCoordinateSystem hCS = CTSUtilities.getHorizontalCS(sourceCS);
                final VerticalCoordinateSystem   vCS = CTSUtilities.getVerticalCS  (sourceCS);
                if (hCS instanceof GeographicCoordinateSystem) {
                    return concatenate(sourceCS,
                        createTransformationStep((GeographicCoordinateSystem) hCS, vCS, target), null);
                }
                if (hCS instanceof ProjectedCoordinateSystem) {
                    return concatenate(sourceCS,
                        createTransformationStep((ProjectedCoordinateSystem) hCS, vCS, target), null);
                }
            }
            final CompoundCoordinateSystem source = (CompoundCoordinateSystem) sourceCS;
            if (targetCS instanceof CompoundCoordinateSystem) {
                return createTransformationStep(source, (CompoundCoordinateSystem) targetCS);
            }
            /*
             * Try a loosely transformation. For example, the source CS may be
             * a geographic + vertical coordinate systems,  will the target CS
             * may be only the geographic part.     The code below will try to
             * discart one or more dimension.
             */
            final CoordinateSystem headSourceCS = source.getHeadCS();
            final CoordinateSystem tailSourceCS = source.getTailCS();
            final int dimHeadCS = headSourceCS.getDimension();
            final int dimSource = source.getDimension();
            assert (dimHeadCS < dimSource);
            CoordinateTransformation step2;
            int lower, upper;
            try {
                lower = 0;
                upper = dimHeadCS;
                step2 = createFromCoordinateSystems(headSourceCS, targetCS);
            } catch (CannotCreateTransformException exception) {
                /*
                 * If we can't construct a transformation from the head CS,
                 * then try a transformation from the tail CS. If this step
                 * fails also, then the head CS will be taken as the raison
                 * for the failure.
                 */
                try {
                    lower = dimHeadCS;
                    upper = dimSource;
                    step2  = createFromCoordinateSystems(tailSourceCS, targetCS);
                } catch (CannotCreateTransformException ignore) {
                    CannotCreateTransformException e = new CannotCreateTransformException(sourceCS, targetCS);
                    e.initCause(exception);
                    throw e;
                }
            }
            /*
             * A coordinate transformation from the head or tail part of 'sourceCS'
             * has been succesfully contructed. Now, build a matrix transform that
             * will select only the corresponding ordinates from input arrays, and
             * pass them to the transform.
             */
            final MathTransform step1 = factory.createSubMathTransform(
                    lower, upper, factory.createIdentityTransform(dimSource));
            final MathTransform transform = factory.createConcatenatedTransform(
                    step1, step2.getMathTransform());
            return createFromMathTransform(sourceCS, targetCS, transform, step2.getTransformType());
        }
        throw new CannotCreateTransformException(sourceCS, targetCS);
    }





    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    ////////////                                                         ////////////
    ////////////               C O N C A T E N A T I O N S               ////////////
    ////////////                                                         ////////////
    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    /**
     * Concatenate two transformation steps.
     *
     * @param  step1 The first  step, or <code>null</code> for the identity transform.
     * @param  step2 The second step, or <code>null</code> for the identity transform.
     * @return A concatenated transform, or <code>null</code> if all arguments was nul.
     */
    private CoordinateTransformation concatenate(final CoordinateTransformation step1,
                                                 final CoordinateTransformation step2)
    {
        if (step1==null) return step2;
        if (step2==null) return step1;
        assert step1.getTargetCS().equals(step2.getSourceCS(), false) : step1;

        final MathTransform step = factory.createConcatenatedTransform(step1.getMathTransform(),
                                                                       step2.getMathTransform());
        final TransformType type = step1.getTransformType().concatenate(
                                   step2.getTransformType());
        return createFromMathTransform(step1.getSourceCS(), step2.getTargetCS(), step, type);
    }
    
    /**
     * Concatenate three transformation steps.
     *
     * @param  step1 The first  step, or <code>null</code> for the identity transform.
     * @param  step2 The second step, or <code>null</code> for the identity transform.
     * @param  step3 The third  step, or <code>null</code> for the identity transform.
     * @return A concatenated transform, or <code>null</code> if all arguments was nul.
     */
    private CoordinateTransformation concatenate(final CoordinateTransformation step1,
                                                 final CoordinateTransformation step2,
                                                 final CoordinateTransformation step3)
    {
        if (step1==null) return concatenate(step2, step3);
        if (step2==null) return concatenate(step1, step3);
        if (step3==null) return concatenate(step2, step3);
        assert step1.getTargetCS().equals(step2.getSourceCS(), false) : step1;
        assert step2.getTargetCS().equals(step3.getSourceCS(), false) : step3;

        final MathTransform step = factory.createConcatenatedTransform(step1.getMathTransform(),
                                   factory.createConcatenatedTransform(step2.getMathTransform(),
                                                                       step3.getMathTransform()));
        final TransformType type = step1.getTransformType().concatenate(
                                   step2.getTransformType().concatenate(
                                   step3.getTransformType()));
        return createFromMathTransform(step1.getSourceCS(), step3.getTargetCS(), step, type);
    }

    /**
     * Concatenate three transformation steps, where the first and last steps
     * are infered from <code>sourceCS</code> and <code>lastCS</code>.
     *
     * @param  sourceCS  The source coordinate system, or <code>null</code>.
     * @param  transform The second step, or <code>null</code> for the identity transform.
     * @param  targetCS  The destination coordinate system, or <code>null</code>.
     * @return A concatenated transform, or <code>null</code> if all arguments was nul.
     * @throws CoordinateTransformation If a transformation can't be constructed.
     */
    private CoordinateTransformation concatenate(final CoordinateSystem         sourceCS,
                                                 final CoordinateTransformation transform,
                                                 final CoordinateSystem         targetCS)
        throws CannotCreateTransformException
    {
        final CoordinateTransformation step1, step3;
        step1 = (sourceCS!=null) ? createFromCoordinateSystems(sourceCS, transform.getSourceCS()) : null;
        step3 = (targetCS!=null) ? createFromCoordinateSystems(transform.getTargetCS(), targetCS) : null;
        return concatenate(step1, transform, step3);
    }
    
    /**
     * Create a coordinate transform from a math transform.
     * If the specified math transform is already a coordinate transform,  and if source
     * and target coordinate systems match, then <code>transform</code> is returned with
     * no change. Otherwise, a new coordinate transform is created.
     *
     * @param  sourceCS  The source coordinate system.
     * @param  targetCS  The destination coordinate system.
     * @param  transform The math transform.
     * @param  type      The transform type. If ommited, then
     *                   {@link TransformType#CONVERSION} is assumed.
     * @return A coordinate transform using the specified math transform.
     */
    private static CoordinateTransformation createFromMathTransform(
                                                final CoordinateSystem sourceCS,
                                                final CoordinateSystem targetCS,
                                                final MathTransform    transform,
                                                final TransformType    type)
    {
        if (transform instanceof CoordinateTransformation) {
            final CoordinateTransformation ct = (CoordinateTransformation) transform;
            if (Utilities.equals(ct.getSourceCS(), sourceCS) &&
                Utilities.equals(ct.getTargetCS(), targetCS))
            {
                return ct;
            }
        }
        return (CoordinateTransformation) MathTransformFactory.pool.canonicalize(
                new CoordinateTransformation(null, sourceCS, targetCS, type, transform));
    }
    
    /**
     * Create a coordinate transform from a math transform.
     * The transform type is assumed to be {@link TransformType#CONVERSION}.
     *
     * @param  sourceCS  The source coordinate system.
     * @param  targetCS  The destination coordinate system.
     * @param  transform The math transform.
     * @return A coordinate transform using the specified math transform.
     */
    private static CoordinateTransformation createFromMathTransform(
                                                final CoordinateSystem sourceCS,
                                                final CoordinateSystem targetCS,
                                                final MathTransform    transform)
    {
        return createFromMathTransform(sourceCS, targetCS, transform, TransformType.CONVERSION);
    }




    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    ////////////                                                         ////////////
    ////////////            A X I S   O R I E N T A T I O N S            ////////////
    ////////////                                                         ////////////
    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Returns <code>true</code> if the specified coordinate system use standard axis
     * and the specified units.
     *
     * @param cs   The coordinate system to test.
     * @paral unit The expected units (usually {@link Unit#DEGREE} or {@link Unit#METRE}).
     */
    private static boolean hasStandardAxis(final HorizontalCoordinateSystem cs, final Unit unit) {
        return cs.getDimension()==2 /* Just a paranoiac check */       &&
               unit                 .equals(cs.getUnits(0))            &&
               unit                 .equals(cs.getUnits(1))            &&
               AxisOrientation.EAST .equals(cs.getAxis(0).orientation) &&
               AxisOrientation.NORTH.equals(cs.getAxis(1).orientation);
    }
    
    /**
     * Returns the axis orientation for the specified coordinate system.
     * If <code>cs</code> is <code>null</code>, then an array of length
     * <code>dim.getDimension()</code> is created and filled with
     * <code>(x,y,z,t)</code> axis orientations.
     *
     * @param  cs The coordinate system, or <code>null</code>.
     * @param  dimension The expected dimension. Used as a fallback if <code>cs</code> was null.
     * @return The axis orientations for the specified coordinate system.
     */
    private static AxisOrientation[] getAxisOrientations(final CoordinateSystem cs,
                                                         final Dimensioned dimension)
    {
        final AxisOrientation[] axis;
        if (cs != null) {
            axis = new AxisOrientation[cs.getDimension()];
            for (int i=0; i<axis.length; i++) {
                axis[i] = cs.getAxis(i).orientation;
            }
        } else {
            axis = new AxisOrientation[dimension.getDimension()];
            switch (axis.length) {
                default: for (int i=axis.length; --i>=4;) {
                             axis[i] = AxisOrientation.OTHER;
                         } // fall through
                case 4:  axis[3] = AxisOrientation.FUTURE; // fall through
                case 3:  axis[2] = AxisOrientation.UP;     // fall through
                case 2:  axis[1] = AxisOrientation.NORTH;  // fall through
                case 1:  axis[0] = AxisOrientation.EAST;   // fall through
                case 0:  break;
            }
        }
        assert axis.length == dimension.getDimension();
        return axis;
    }
    
    /**
     * Returns an affine transform between two coordinate systems. Only units and
     * axis order (e.g. transforming from (NORTH,WEST) to (EAST,NORTH)) are taken
     * in account. Other attributes (especially the datum) must be checked before
     * invoking this method.
     * <br><br>
     * Example: If coordinates in <code>sourceCS</code> are (x,y) pairs in metres and
     * coordinates in <code>targetCS</code> are (-y,x) pairs in centimetres, then the
     * transformation can be performed as below:
     *
     * <pre><blockquote>
     *          [-y(cm)]   [ 0  -100    0 ] [x(m)]
     *          [ x(cm)] = [ 100   0    0 ] [y(m)]
     *          [ 1    ]   [ 0     0    1 ] [1   ]
     * </blockquote><pre>
     *
     * @param  sourceCS The source coordinate system. If <code>null</code>, then
     *         (x,y,z,t) axis order is assumed.
     * @param  targetCS The target coordinate system. If <code>null</code>, then
     *         (x,y,z,t) axis order is assumed.
     * @return The transformation from <code>sourceCS</code> to <code>targetCS</code> as
     *         an affine transform. Only axis orientation and units are taken in account.
     * @throws CannotCreateTransformException If the affine transform can't be constructed.
     */
    private Matrix swapAndScaleAxis(final CoordinateSystem sourceCS,
                                    final CoordinateSystem targetCS)
        throws CannotCreateTransformException
    {
        final AxisOrientation[] sourceAxis = getAxisOrientations(sourceCS, targetCS);
        final AxisOrientation[] targetAxis = getAxisOrientations(targetCS, sourceCS);
        final Matrix matrix;
        try {
            matrix = Matrix.createAffineTransform(sourceAxis, targetAxis);
        } catch (RuntimeException exception) {
            final CannotCreateTransformException e = new CannotCreateTransformException(sourceCS, targetCS);
            e.initCause(exception);
            throw e;
        }
        assert Arrays.equals(sourceAxis, targetAxis) == matrix.isIdentity();
        /*
         * The previous code computed a matrix for swapping axis. Usually, this
         * matrix contains only 0 and 1 values with only one "1" value by row.
         * For example, the matrix operation for swapping x and y axis is:
         *
         *          [y]   [ 0  1  0 ] [x]
         *          [x] = [ 1  0  0 ] [y]
         *          [1]   [ 0  0  1 ] [1]
         *
         * Now, take in account units conversions. Each matrix's element (j,i)
         * is multiplied by the conversion factor from sourceCS.getUnit(i) to
         * targetCS.getUnit(j). This is a element-by-element multiplication,
         * not a matrix multiplication. The last column is process in a special
         * way, since it contains the offset values.
         */
        final int sourceDim = matrix.getNumCol()-1;
        final int targetDim = matrix.getNumRow()-1;
        assert sourceDim == sourceCS.getDimension();
        assert targetDim == targetCS.getDimension();
        for (int j=0; j<targetDim; j++) {
            final Unit targetUnit = targetCS.getUnits(j);
            for (int i=0; i<sourceDim; i++) {
                final double element = matrix.getElement(j,i);
                if (element == 0) {
                    // There is no dependency between source[i] and target[j]
                    // (i.e. axis are orthogonal).
                    continue;
                }
                final Unit sourceUnit = sourceCS.getUnits(i);
                if (Utilities.equals(sourceUnit, targetUnit)) {
                    // There is no units conversion to apply
                    // between source[i] and target[j].
                    continue;
                }
                // TODO: check if units conversion is really linear. We
                //       use here a temporary patch, just checking DMS unit.
                if (sourceUnit==Unit.DMS || targetUnit==Unit.DMS) {
                    // We should create an UnitTransform object instead.
                    throw new org.geotools.units.UnitException("Not implemented");
                }
                final double offset = targetUnit.convert(0, sourceUnit);
                final double scale  = targetUnit.convert(1, sourceUnit)-offset;
                matrix.setElement(j,i, scale*element);
                matrix.setElement(j,sourceDim, matrix.getElement(j,sourceDim) + element*offset);
            }
        }
        return matrix;
    }
    
    /**
     * Returns an affine transform between two geographic coordinate systems. Only
     * units, axis order (e.g. transforming from (NORTH,WEST) to (EAST,NORTH)) and
     * prime meridian are taken in account. Other attributes (especially the datum)
     * must be checked before invoking this method.
     *
     * @param  sourceCS The source coordinate system.
     * @param  targetCS The target coordinate system.
     * @return The transformation from <code>sourceCS</code> to <code>targetCS</code> as
     *         an affine transform.  Only axis orientation, units and prime meridian are
     *         taken in account.
     * @throws CannotCreateTransformException If the affine transform can't be constructed.
     */
    private Matrix swapAndScaleGeoAxis(final GeographicCoordinateSystem sourceCS,
                                       final GeographicCoordinateSystem targetCS)
        throws CannotCreateTransformException
    {
        final Matrix matrix = swapAndScaleAxis(sourceCS, targetCS);
        for (int i=targetCS.getDimension(); --i>=0;) {
            final AxisOrientation orientation = targetCS.getAxis(i).orientation;
            if (AxisOrientation.EAST.equals(orientation.absolute())) {
                /*
                 * A longitude ordinate has been found (i.e. the axis is oriented toward EAST or
                 * WEST). Compute the amount of angle to add to the source longitude in order to
                 * get the destination longitude. This amount is measured in units of the target
                 * axis.  The affine transform is then updated in order to take this rotation in
                 * account. Note that the resulting longitude may be outside the usual [-180..180°]
                 * range.
                 */
                final Unit              unit = targetCS.getUnits(i);
                final double sourceLongitude = sourceCS.getPrimeMeridian().getLongitude(unit);
                final double targetLongitude = targetCS.getPrimeMeridian().getLongitude(unit);
                final int   lastMatrixColumn = matrix.getNumCol()-1;
                double rotate = sourceLongitude - targetLongitude;
                if (AxisOrientation.WEST.equals(orientation)) {
                    rotate = -rotate;
                }
                rotate += matrix.getElement(i, lastMatrixColumn);
                matrix.setElement(i, lastMatrixColumn, rotate);
            }
        }
        return matrix;
    }




    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    ////////////                                                         ////////////
    ////////////        T R A N S F O R M A T I O N S   S T E P S        ////////////
    ////////////                                                         ////////////
    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a transformation between two temporal coordinate systems.
     * The default implementation checks if both coordinate systems use
     * the same datum, and then adjusts for axis orientation, units and
     * epoch.
     *
     * @param  sourceCS Input coordinate system.
     * @param  targetCS Output coordinate system.
     * @return A coordinate transformation from <code>sourceCS</code> to <code>targetCS</code>.
     * @throws CannotCreateTransformException if no transformation path has been found.
     */
    protected CoordinateTransformation createTransformationStep(
                                        final TemporalCoordinateSystem sourceCS,
                                        final TemporalCoordinateSystem targetCS)
        throws CannotCreateTransformException
    {
        if (!Utilities.equals(sourceCS.getTemporalDatum(), targetCS.getTemporalDatum())) {
            throw new CannotCreateTransformException(sourceCS, targetCS);
        }
        /*
         * Compute the epoch shift.  The epoch is the time "0" in a particular coordinate
         * system. For example, the epoch for java.util.Date object is january 1, 1970 at
         * 00:00 UTC.  We compute how much to add to a time in 'sourceCS' in order to get
         * a time in 'targetCS'. This "epoch shift" is in units of 'targetCS'.
         */
        double epochShift = sourceCS.getEpoch().getTime() - targetCS.getEpoch().getTime();
        epochShift = targetCS.getUnits(0).convert(epochShift / (24*60*60*1000), Unit.DAY);
        /*
         * Check axis orientation.  The method 'swapAndScaleAxis' should returns a matrix
         * of size 2x2. The element at index (0,0) may be 1 if sourceCS and targetCS axis
         * are in the same direction, or -1 if there are in opposite direction (e.g.
         * "PAST" vs "FUTURE"). This number may be something else than -1 or +1 if a unit
         * conversion was applied too,  for example 60 if time in 'sourceCS' was in hours
         * while time in 'targetCS' was in minutes.
         *
         * The "epoch shift" previously computed is a translation.
         * Consequently, it is added to element (0,1).
         */
        final Matrix matrix = swapAndScaleAxis(sourceCS, targetCS);
        final int translationColumn = matrix.getNumCol()-1;
        if (translationColumn >= 0) { // Paranoiac check: should always be 1.
            final double translation = matrix.getElement(0, translationColumn);
            matrix.setElement(0, translationColumn, translation+epochShift);
        }
        final MathTransform transform = factory.createAffineTransform(matrix);
        return createFromMathTransform(sourceCS, targetCS, transform);
    }
    
    /**
     * Creates a transformation between two vertical coordinate systems. The
     * default implementation checks if both coordinate systems use the same
     * datum, and then adjusts for axis orientation and units.
     *
     * @param  sourceCS Input coordinate system.
     * @param  targetCS Output coordinate system.
     * @return A coordinate transformation from <code>sourceCS</code> to <code>targetCS</code>.
     * @throws CannotCreateTransformException if no transformation path has been found.
     */
    protected CoordinateTransformation createTransformationStep(
                                        final VerticalCoordinateSystem sourceCS,
                                        final VerticalCoordinateSystem targetCS)
        throws CannotCreateTransformException
    {
        if (!Utilities.equals(sourceCS.getVerticalDatum(), targetCS.getVerticalDatum())) {
            throw new CannotCreateTransformException(sourceCS, targetCS);
        }
        final Matrix matrix = swapAndScaleAxis(sourceCS, targetCS);
        final MathTransform transform = factory.createAffineTransform(matrix);
        return createFromMathTransform(sourceCS, targetCS, transform);
    }
    
    /**
     * Creates a transformation between two geographic coordinate systems. The default
     * implementation can adjust axis order and orientation (e.g. transforming from
     * <code>(NORTH,WEST)</code> to <code>(EAST,NORTH)</code>), performs units conversion
     * and apply Bursa Wolf transformation if needed.
     *
     * @param  sourceCS Input coordinate system.
     * @param  targetCS Output coordinate system.
     * @return A coordinate transformation from <code>sourceCS</code> to <code>targetCS</code>.
     * @throws CannotCreateTransformException if no transformation path has been found.
     *
     * @task TODO: When rotating the prime meridian, we should ensure that
     *             transformed longitudes stay in the range [-180..+180°].
     */
    protected CoordinateTransformation createTransformationStep(
                                        final GeographicCoordinateSystem sourceCS,
                                        final GeographicCoordinateSystem targetCS)
        throws CannotCreateTransformException
    {
        final HorizontalDatum sourceDatum = sourceCS.getHorizontalDatum();
        final HorizontalDatum targetDatum = targetCS.getHorizontalDatum();
        if (sourceDatum.equals(targetDatum, false)) {
            /*
             * If both geographic CS use the same datum, then there is no need for a datum shift.
             * Just swap axis order, and rotate the longitude coordinate if prime meridians are
             * different.
             *
             * TODO: We should ensure that longitude is in range [-180..+180°].
             */
            final Matrix matrix = swapAndScaleGeoAxis(sourceCS, targetCS);
            final MathTransform transform = factory.createAffineTransform(matrix);
            return createFromMathTransform(sourceCS, targetCS, transform);
        }
        /*
         * If the two geographic coordinate systems use different ellipsoids,
         * convert from the source to target ellipsoid through the geocentric
         * coordinate system. The transformation chain is:
         *
         *     source geographic CS             -->
         *     geocentric CS with source datum  -->
         *     geocentric CS with target datum  -->
         *     target geographic CS
         */
        final String                     name = getTemporaryName(sourceCS);
        final GeocentricCoordinateSystem gcs1 = new GeocentricCoordinateSystem(name, sourceDatum);
        final GeocentricCoordinateSystem gcs3 = new GeocentricCoordinateSystem(name, targetDatum);
        final CoordinateTransformation  step1 = createTransformationStep(sourceCS, null, gcs1);
        final CoordinateTransformation  step2 = createTransformationStep(gcs1, gcs3);
        final CoordinateTransformation  step3 = createTransformationStep(gcs3, targetCS, null);
        return concatenate(step1, step2, step3);
    }
    
    /**
     * Creates a transformation between two projected coordinate systems. The default
     * implementation can adjust axis order and orientation. It also performs units
     * conversion if it is the only extra change needed. Otherwise, it performs three
     * steps:
     *
     * <ol>
     *   <li>Unproject <code>sourceCS</code>.</li>
     *   <li>Transform from <code>sourceCS.geographicCS</code> to <code>targetCS.geographicCS</code>.</li>
     *   <li>Project <code>targetCS</code>.</li>
     * </ol>
     *
     * @param  sourceCS Input coordinate system.
     * @param  targetCS Output coordinate system.
     * @return A coordinate transformation from <code>sourceCS</code> to <code>targetCS</code>.
     * @throws CannotCreateTransformException if no transformation path has been found.
     *
     * @task REVISIT: What to do about prime meridian?
     */
    protected CoordinateTransformation createTransformationStep(
                                        final ProjectedCoordinateSystem sourceCS,
                                        final ProjectedCoordinateSystem targetCS)
        throws CannotCreateTransformException
    {
        if (sourceCS.getProjection()     .equals(targetCS.getProjection(),      false) &&
            sourceCS.getHorizontalDatum().equals(targetCS.getHorizontalDatum(), false))
        {
            /*
             * If both projected CS use the same projection and the same horizontal datum,
             * then only axis orientation and units may have been changed. We do not need
             * to perform the tedious  ProjectedCS --> GeographicCS --> ProjectedCS  chain.
             * We can apply a much shorter transformation using only an affine transform.
             *
             * This shorter path is essential for proper working of 
             * createTransformationStep(GeographicCS,ProjectedCS).
             *
             * TODO: What to do about prime meridian here?
             */
            final Matrix matrix = swapAndScaleAxis(sourceCS, targetCS);
            final MathTransform transform = factory.createAffineTransform(matrix);
            return createFromMathTransform(sourceCS, targetCS, transform);
        }
        /*
         * Apply the transformation in 3 steps (the 3 arrows below):
         *
         *     source projected CS   -->
         *     source geographic CS  -->
         *     target geographic CS  -->
         *     target projected CS
         */
        final GeographicCoordinateSystem sourceGeo = sourceCS.getGeographicCoordinateSystem();
        final GeographicCoordinateSystem targetGeo = targetCS.getGeographicCoordinateSystem();
        final CoordinateTransformation step1 = createTransformationStep(sourceCS,  sourceGeo);
        final CoordinateTransformation step2 = createTransformationStep(sourceGeo, targetGeo);
        final CoordinateTransformation step3 = createTransformationStep(targetGeo, targetCS );
        return concatenate(step1, step2, step3);
    }
    
    /**
     * Makes sure that the specified {@link GeographicCoordinateSystem} use standard axis
     * (longitude and latitude in degrees), Greenwich prime meridian and an ellipsoid
     * matching projection's parameters. If <code>cs</code> already meets all those conditions,
     * then it is returned unchanged. Otherwise, a new normalized geographic coordinate system
     * is created and returned.
     *
     * @param  cs The geographic coordinate system to normalize.
     * @param  projection The projection to apply, or <code>null</code> to bypass the check.
     * @return The normalized coordinate system.
     */
    private static GeographicCoordinateSystem normalize(GeographicCoordinateSystem cs,
                                                        final Projection projection)
    {
        HorizontalDatum datum = cs.getHorizontalDatum();
        String           name = null;
        if (projection != null) {
            Ellipsoid ellipsoid = datum.getEllipsoid();
            final double semiMajorEll = ellipsoid.getSemiMajorAxis();
            final double semiMinorEll = ellipsoid.getSemiMinorAxis();
            final double semiMajorPrj = projection.getValue("semi_major", semiMajorEll);
            final double semiMinorPrj = projection.getValue("semi_minor", semiMinorEll);
            if (semiMajorEll!=semiMajorPrj || semiMinorEll!=semiMinorPrj) {
                /*
                 * If the projection use a different ellipsoid than the geographic coordinate
                 * system one, then create a new datum with the projection's ellipsoid.
                 */
                name      = getTemporaryName(cs);
                ellipsoid = Ellipsoid.createEllipsoid(name, semiMajorPrj, semiMinorPrj, Unit.METRE);
                datum     = new HorizontalDatum(name, ellipsoid);
                cs        = null; // Signal that it needs to be reconstructed.
            }
        }
        if (cs==null || !hasStandardAxis(cs, Unit.DEGREE) ||
            cs.getPrimeMeridian().getLongitude(Unit.DEGREE)!=0)
        {
            /*
             * The specified geographic coordinate system doesn't use standard axis
             * (EAST, NORTH) or Greenwich meridian, or the datum need to be changed.
             */
            if (name == null) {
                name = getTemporaryName(cs);
            }
            cs = new GeographicCoordinateSystem(name, datum);
        }
        return cs;
    }
    
    /**
     * Makes sure that a {@link ProjectedCoordinateSystem} use standard axis (x and y in metres)
     * and a normalized {@link GeographicCoordinateSystem}. If <code>cs</code> already meets all
     * those conditions, then it is returned unchanged. Otherwise, a new normalized projected
     * coordinate system is created and returned.
     *
     * @param  cs The projected coordinate system to normalize.
     * @param  projection The projection to apply.
     * @return The normalized coordinate system.
     */
    private static ProjectedCoordinateSystem normalize(final ProjectedCoordinateSystem cs) {
        final Projection                      projection = cs.getProjection();
        final GeographicCoordinateSystem           geoCS = cs.getGeographicCoordinateSystem();
        final GeographicCoordinateSystem normalizedGeoCS = normalize(geoCS, projection);
        assert normalize(normalizedGeoCS, projection) == normalizedGeoCS;
        
        if (hasStandardAxis(cs, Unit.METRE) && normalizedGeoCS==geoCS) {
            return cs;
        }
        final String name = getTemporaryName(cs, normalizedGeoCS);
        return new ProjectedCoordinateSystem(name, normalizedGeoCS, projection);
    }
    
    /**
     * Creates a transformation from a geographic to a projected coordinate systems.
     *
     * @param  sourceCS Input coordinate system.
     * @param  targetCS Output coordinate system.
     * @return A coordinate transformation from <code>sourceCS</code> to <code>targetCS</code>.
     * @throws CannotCreateTransformException if no transformation path has been found.
     */
    protected CoordinateTransformation createTransformationStep(
                                        final GeographicCoordinateSystem sourceCS,
                                        final ProjectedCoordinateSystem targetCS)
        throws CannotCreateTransformException
    {
        final ProjectedCoordinateSystem stepProjCS = normalize(targetCS);
        final GeographicCoordinateSystem stepGeoCS = stepProjCS.getGeographicCoordinateSystem();
        final Projection                projection = stepProjCS.getProjection();
        assert normalize(stepProjCS) == stepProjCS;
        assert normalize(stepGeoCS, projection) == stepGeoCS;
        assert projection.equals(targetCS.getProjection(), false);
        /*
         * Apply the projection with the following steps:
         *
         *     source geographics CS   -->
         *     standard geographic CS  -->
         *     standard projected CS   -->
         *     target projected CS
         */
        final MathTransform mapProjection;
        try {
            mapProjection = factory.createParameterizedTransform(projection);
        } catch (NoSuchElementException exception) {
            // REVISIT: Should MathTransform throws FactoryException instead?
            throw new CannotCreateTransformException(exception);
        }
        final CoordinateTransformation step1 = createTransformationStep(sourceCS, stepGeoCS);
        final CoordinateTransformation step2 = createFromMathTransform(stepGeoCS, stepProjCS, mapProjection);
        final CoordinateTransformation step3 = createTransformationStep(stepProjCS, targetCS);
        return concatenate(step1, step2, step3);
    }
    
    /**
     * Creates a transformation from a projected to a geographic coordinate systems.
     * The default implementation returns
     * <code>{@link #createTransformationStep(GeographicCoordinateSystem, ProjectedCoordinateSystem)
     * createTransformationStep}(targetCS, sourceCS).{@link MathTransform#inverse() inverse()}</code>.
     *
     * @param  sourceCS Input coordinate system.
     * @param  targetCS Output coordinate system.
     * @return A coordinate transformation from <code>sourceCS</code> to <code>targetCS</code>.
     * @throws CannotCreateTransformException if no transformation path has been found.
     */
    protected CoordinateTransformation createTransformationStep(
                                        final ProjectedCoordinateSystem sourceCS,
                                        final GeographicCoordinateSystem targetCS)
        throws CannotCreateTransformException
    {
        try {
            return createTransformationStep(targetCS, sourceCS).inverse();
        } catch (NoninvertibleTransformException exception) {
            final CannotCreateTransformException e = new CannotCreateTransformException(sourceCS, targetCS);
            e.initCause(exception);
            throw e;
        }
    }
    
    /**
     * Creates a transformation between two geocentric coordinate systems.
     * The default implementation can adjust for axis order and orientation,
     * performs units conversion and apply Bursa Wolf transformation if needed.
     *
     * @param  sourceCS Input coordinate system.
     * @param  targetCS Output coordinate system.
     * @return A coordinate transformation from <code>sourceCS</code> to <code>targetCS</code>.
     * @throws CannotCreateTransformException if no transformation path has been found.
     */
    protected CoordinateTransformation createTransformationStep(
                                        final GeocentricCoordinateSystem sourceCS,
                                        final GeocentricCoordinateSystem targetCS)
        throws CannotCreateTransformException
    {
        final HorizontalDatum sourceHD = sourceCS.getHorizontalDatum();
        final HorizontalDatum targetHD = targetCS.getHorizontalDatum();
        if (sourceHD.equals(targetHD, false)) {
            if (sourceCS.getPrimeMeridian().equals(targetCS.getPrimeMeridian(), false)) {
                /*
                 * If both coordinate systems use the same datum and the same prime meridian,
                 * then the transformation is probably just axis swap or unit conversions.
                 */
                final Matrix matrix = swapAndScaleAxis(sourceCS, targetCS);
                final MathTransform transform = factory.createAffineTransform(matrix);
                return createFromMathTransform(sourceCS, targetCS, transform);
            }
            // If prime meridians are not the same, performs the full transformation.
        }
        if (!PrimeMeridian.GREENWICH.equals(sourceCS.getPrimeMeridian()) ||
            !PrimeMeridian.GREENWICH.equals(targetCS.getPrimeMeridian()))
        {
            throw new CannotCreateTransformException("Rotation of prime meridian not yet implemented");
        }
        /*
         * Transform between differents ellipsoids using Bursa Wolf parameters.
         * The Bursa Wolf parameters are used with "standard" geocentric CS, i.e.
         * with x axis towards the prime meridian, y axis towards East and z axis
         * toward North. The following steps are applied:
         *
         *     source CS                      -->
         *     standard CS with source datum  -->
         *     standard CS with target datum  -->
         *     target CS
         */
        final Matrix step1 = swapAndScaleAxis(sourceCS, GeocentricCoordinateSystem.DEFAULT);
        final Matrix step2 = getWGS84Parameters(sourceHD);
        final Matrix step3 = getWGS84Parameters(targetHD);
        final Matrix step4 = swapAndScaleAxis(GeocentricCoordinateSystem.DEFAULT, targetCS);
        if (step2==null || step3==null) {
            throw new CannotCreateTransformException(Resources.format(
                        ResourceKeys.BURSA_WOLF_PARAMETERS_REQUIRED));
        }
        /*
         * Since all steps are matrix, we can multiply them into a single matrix operation.
         * Note: GMatrix.mul(GMatrix) is equivalents to AffineTransform.concatenate(...):
         *       First transform by the supplied transform and then transform the result
         *       by the original transform.
         */
        try {
            step3.invert();   // Invert in place.
            step4.mul(step3); // step4 = step4*step3
            step4.mul(step2); // step4 = step4*step3*step2
            step4.mul(step1); // step4 = step4*step3*step2*step1
        } catch (SingularMatrixException exception) {
            final CannotCreateTransformException e = new CannotCreateTransformException(sourceCS, targetCS);
            e.initCause(exception);
            throw e;
        }
        final MathTransform transform = factory.createAffineTransform(step4);
        return createFromMathTransform(sourceCS, targetCS, transform);
    }
    
    /**
     * Creates a transformation from a geographic to a geocentric coordinate systems.
     * Since the source coordinate systems doesn't have a vertical axis, height above the
     * ellipsoid is assumed equals to zero everywhere.
     *
     * @param  sourceCS   Input geographic coordinate system.
     * @param  verticalCS Input vertical coordinate system, or <code>null</code> if none.
     * @param  targetCS   Output coordinate system.
     * @return A coordinate transformation from <code>sourceCS</code> to <code>targetCS</code>.
     * @throws CannotCreateTransformException if no transformation path has been found.
     */
    protected CoordinateTransformation createTransformationStep(
                                        final GeographicCoordinateSystem sourceCS,
                                        final VerticalCoordinateSystem verticalCS,
                                        final GeocentricCoordinateSystem targetCS)
        throws CannotCreateTransformException
    {
        /*
         * This transformation is a 3 steps process:
         *
         *    geographic source CS        -->
         *    standardized geographic CS  -->
         *    standardized geocentric CS  -->
         *    geocentric target CS
         *
         * "Standardized" means that axis point toward standards direction (East, North, etc.),
         * units are metres or degrees, prime meridian is Greenwich and height is measured above
         * the ellipsoid. However, the horizontal datum is preserved.
         */
        final CoordinateTransformation step1, step2, step3;
        final HorizontalDatum              datum = sourceCS.getHorizontalDatum();
        final GeographicCoordinateSystem stepCS1 = normalize(sourceCS, null);
        final GeocentricCoordinateSystem stepCS2 = new GeocentricCoordinateSystem(
                                                   getTemporaryName(sourceCS, stepCS1), datum);
        /*
         * First step: transform coordinate points from 'sourceCS' to a standardized
         * geographic coordinate system. If a vertical axis is used, then we need to
         * apply the transformation using a compound coordinate system.
         */
        if (verticalCS == null) {
            step1 = createTransformationStep(sourceCS, stepCS1);
        } else {
            final String name = stepCS2.getName(null);
            final CompoundCoordinateSystem cs1, cs2;
            cs1 = new CompoundCoordinateSystem(name, sourceCS, verticalCS);
            cs2 = new CompoundCoordinateSystem(name, stepCS1, VerticalCoordinateSystem.ELLIPSOIDAL);
            step1 = createTransformationStep(cs1, cs2);
        }
        /*
         * Second step: create the transformation from geographic to geocentric coordinate
         * systems. The transformation use the ellipsoid from 'sourceCS'. Input and output
         * axis directions and units are "standardized".
         */
        final String classification = "Ellipsoid_To_Geocentric";
        final Ellipsoid   ellipsoid = datum.getEllipsoid();
        final Unit             unit = ellipsoid.getAxisUnit();
        final int   sourceDimension = step1.getTargetCS().getDimension();
        ParameterList param = factory.getMathTransformProvider(classification).getParameterList();
        param = param.setParameter("semi_major", Unit.METRE.convert(ellipsoid.getSemiMajorAxis(), unit));
        param = param.setParameter("semi_minor", Unit.METRE.convert(ellipsoid.getSemiMinorAxis(), unit));
        try {
            param = param.setParameter("dim_geoCS", sourceDimension);
        } catch (IllegalArgumentException exception) {
            // The "dim_geoCS" is a custom argument needed for our Geotools
            // implementation. It is not part of OpenGIS's specification.
            // But if the required dimension is not 3, we can't finish
            // the operation (TODO: What should we do? Open question...)
            if (sourceDimension != 3) {
                throw exception;
            }
        }
        final MathTransform transform = factory.createParameterizedTransform(classification, param);
        /*
         * Last steps: create the transformation from the "standardized"
         * to the target geocentric coordinate systems.
         */
        step2 = createFromMathTransform(step1.getTargetCS(), stepCS2, transform);
        step3 = createTransformationStep(stepCS2, targetCS);
        return concatenate(step1, step2, step3);
    }
    
    /**
     * Creates a transformation from a geocentric to a geographic coordinate systems.
     * The default implementation returns
     * <code>{@link #createTransformationStep(GeographicCoordinateSystem, VerticalCoordinateSystem
     * GeocentricCoordinateSystem) createTransformationStep}(targetCS, verticalCS, sourceCS).{@link
     * MathTransform#inverse() inverse()}</code>.
     *
     * @param  sourceCS Input coordinate system.
     * @param  targetCS Output coordinate system.
     * @param  verticalCS Output vertical coordinate system, or <code>null</code> if none.
     * @return A coordinate transformation from <code>sourceCS</code> to <code>targetCS</code>.
     * @throws CannotCreateTransformException if no transformation path has been found.
     */
    protected CoordinateTransformation createTransformationStep(
                                        final GeocentricCoordinateSystem sourceCS,
                                        final GeographicCoordinateSystem targetCS,
                                        final VerticalCoordinateSystem verticalCS)
        throws CannotCreateTransformException
    {
        try {
            return createTransformationStep(targetCS, verticalCS, sourceCS).inverse();
        } catch (NoninvertibleTransformException exception) {
            final CannotCreateTransformException e = new CannotCreateTransformException(sourceCS, targetCS);
            e.initCause(exception);
            throw e;
        }
    }
    
    /**
     * Creates a transformation from a projected to a geocentric coordinate systems.
     *
     * @param  sourceCS Input projected coordinate system.
     * @param  verticalCS Input vertical coordinate system, or <code>null</code> if none.
     * @param  targetCS Output coordinate system.
     * @return A coordinate transformation from <code>sourceCS</code> to <code>targetCS</code>.
     * @throws CannotCreateTransformException if no transformation path has been found.
     */
    protected CoordinateTransformation createTransformationStep(
                                        final ProjectedCoordinateSystem  sourceCS,
                                        final VerticalCoordinateSystem verticalCS,
                                        final GeocentricCoordinateSystem targetCS)
        throws CannotCreateTransformException
    {
        final GeographicCoordinateSystem sourceGCS = sourceCS.getGeographicCoordinateSystem();
        final CoordinateTransformation step1 = createTransformationStep(sourceCS, sourceGCS);
        final CoordinateTransformation step2 = createTransformationStep(sourceGCS, verticalCS, targetCS);
        return concatenate(step1, step2);
    }
    
    /**
     * Creates a transformation from a geocentric to a projected coordinate systems.
     *
     * @param  sourceCS Input projected coordinate system.
     * @param  targetCS Output coordinate system.
     * @param  verticalCS Output vertical coordinate system, or <code>null</code> if none.
     * @return A coordinate transformation from <code>sourceCS</code> to <code>targetCS</code>.
     * @throws CannotCreateTransformException if no transformation path has been found.
     */
    protected CoordinateTransformation createTransformationStep(
                                        final GeocentricCoordinateSystem sourceCS,
                                        final ProjectedCoordinateSystem  targetCS,
                                        final VerticalCoordinateSystem verticalCS)
        throws CannotCreateTransformException
    {
        final GeographicCoordinateSystem targetGCS = targetCS.getGeographicCoordinateSystem();
        final CoordinateTransformation step1 = createTransformationStep(sourceCS, targetGCS, verticalCS);
        final CoordinateTransformation step2 = createTransformationStep(targetGCS, targetCS);
        return concatenate(step1, step2);
    }
    
    /**
     * Creates a transformation between two compound coordinate systems.
     *
     * @param  sourceCS Input coordinate system.
     * @param  targetCS Output coordinate system.
     * @return A coordinate transformation from <code>sourceCS</code> to <code>targetCS</code>.
     * @throws CannotCreateTransformException if no transformation path has been found.
     */
    protected CoordinateTransformation createTransformationStep(
                                        final CompoundCoordinateSystem sourceCS,
                                        final CompoundCoordinateSystem targetCS)
        throws CannotCreateTransformException
    {
        final CoordinateSystem headSourceCS = sourceCS.getHeadCS();
        final CoordinateSystem tailSourceCS = sourceCS.getTailCS();
        final CoordinateSystem headTargetCS = targetCS.getHeadCS();
        final CoordinateSystem tailTargetCS = targetCS.getTailCS();
        if (tailSourceCS.equals(tailTargetCS, false)) {
            final CoordinateTransformation tr = createFromCoordinateSystems(headSourceCS, headTargetCS);
            final MathTransform transform = factory.createPassThroughTransform(0, tr.getMathTransform(), tailSourceCS.getDimension());
            return createFromMathTransform(sourceCS, targetCS, transform, tr.getTransformType());
        }
        if (headSourceCS.equals(headTargetCS, false)) {
            final CoordinateTransformation tr = createFromCoordinateSystems(tailSourceCS, tailTargetCS);
            final MathTransform transform = factory.createPassThroughTransform(headSourceCS.getDimension(), tr.getMathTransform(), 0);
            return createFromMathTransform(sourceCS, targetCS, transform, tr.getTransformType());
        }
        // TODO: implement others CompoundCoordinateSystem cases.
        //       We could do it in a more general way be creating
        //       and using a 'CompoundTransform' class instead of
        //       of 'PassThroughTransform'.  PassThroughTransform
        //       is really a special case of a 'CompoundTransform'
        //       where the head transform is the identity transform.
        throw new CannotCreateTransformException(sourceCS, targetCS);
    }




    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    ////////////                                                         ////////////
    ////////////                M I S C E L L A N E O U S                ////////////
    ////////////                                                         ////////////
    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Returns the WGS84 parameters as an affine transform,
     * or <code>null</code> if not available.
     */
    private static Matrix getWGS84Parameters(final HorizontalDatum datum) {
        final WGS84ConversionInfo info = datum.getWGS84Parameters();
        if (info != null) {
            return info.getAffineTransform();
        }
        if (HorizontalDatum.WGS84.equals(datum, false)) {
            return new Matrix(4); // Identity matrix
        }
        return null;
    }
    
    /**
     * Returns a temporary name for generated objects. The first object
     * has a name like "Temporary-1", the second is "Temporary-2", etc.
     *
     * @param source The coordinate system to base name on, or <code>null</code> if none.
     */
    private static String getTemporaryName(final CoordinateSystem source) {
        return "Temporary-" + (++temporaryID);
    }
    
    /**
     * Returns a temporary name for generated objects. The first object
     * has a name like "Temporary-1", the second is "Temporary-2", etc.
     *
     * @param source   The coordinate system to base name on, or <code>null</code> if none.
     * @param existing The coordinate system which may (or may not) has been used already
     *                 created with a temporary name from <code>source</code>.
     */
    private static String getTemporaryName(final CoordinateSystem source,
                                           final CoordinateSystem existing)
    {
        return (source!=existing) ? existing.getName(null) : getTemporaryName(source);
    }

    /**
     * Returns an OpenGIS interface for this info.
     * This method first looks in the cache. If no
     * interface was previously cached, then this
     * method creates a new adapter  and caches the
     * result.
     *
     * Note: The returned type is a generic {@link Object} in order
     *       to avoid premature class loading of OpenGIS interface.
     *
     * @param adapters The originating {@link Adapters}.
     */
    final synchronized Object toOpenGIS(final Object adapters) {
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
    
    /**
     * Wrap a {@link CoordinateTransformationFactory} for use
     * with OpenGIS. This class is suitable for RMI use.
     *
     * @version 1.0
     * @author Martin Desruisseaux
     */
    private final class Export extends RemoteObject implements CT_CoordinateTransformationFactory {
        /**
         * The originating adapter.
         */
        protected final Adapters adapters;
        
        /**
         * Construct a remote object.
         */
        protected Export(final Object adapters) {
            this.adapters = (Adapters)adapters;
        }
        
        /**
         * Creates a transformation between two coordinate systems.
         *
         * @param  sourceCS Input coordinate system.
         * @param  targetCS Output coordinate system.
         * @throws RemoteException if the transform can't be created,
         *                         or if a remote method call failed.
         */
        public CT_CoordinateTransformation createFromCoordinateSystems(
                                        final CS_CoordinateSystem sourceCS,
                                        final CS_CoordinateSystem targetCS)
            throws RemoteException
        {
            try {
                return adapters.export(CoordinateTransformationFactory.this.createFromCoordinateSystems(adapters.CS.wrap(sourceCS), adapters.CS.wrap(targetCS)));
            } catch (CannotCreateTransformException exception) {
                throw new ServerException(exception.getLocalizedMessage(), exception);
            }
        }
    }
}
