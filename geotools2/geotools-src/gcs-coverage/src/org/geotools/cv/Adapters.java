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
 */
package org.geotools.cv;

// J2SE dependencies
import java.awt.Image;
import java.awt.Color;
import java.util.List;
import java.util.Locale;
import java.util.Arrays;
import java.util.ArrayList;
import java.lang.ref.Reference;
import java.rmi.RemoteException;

// JAI dependencies
import javax.media.jai.util.Range;
import javax.media.jai.PropertySource;

// OpenGIS dependencies
import org.opengis.cv.CV_Coverage;
import org.opengis.cv.CV_SampleDimension;
import org.opengis.cv.CV_SampleDimensionType;
import org.opengis.cv.CV_ColorInterpretation;
import org.opengis.cv.CV_PaletteInterpretation;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.pt.Envelope;
import org.geotools.pt.CoordinatePoint;
import org.geotools.ct.MathTransform1D;
import org.geotools.resources.XArray;
import org.geotools.resources.RemoteProxy;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * <FONT COLOR="#FF6633">Provide methods for interoperability with
 * <code>org.opengis.cv</code> package.</FONT>  All methods accept
 * null argument. This class has no default instance, since the
 * {@link org.geotools.gp.Adapters org.geotools.<strong>gp</strong>.Adapters}
 * implementation cover this case.
 *
 * @version $Id: Adapters.java,v 1.9 2003/04/10 20:41:08 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see org.geotools.gp.Adapters#getDefault()
 */
public class Adapters {
    /**
     * The adapters from <cite>Coordinate Transformation Services</cite>
     * (package <code>org.geotools.ct</code>).
     */
    public final org.geotools.ct.Adapters CTS;
    
    /**
     * Default constructor. A shared instance of <code>Adapters</code> can
     * be obtained with {@link org.geotools.gp.Adapters#getDefault()}.
     *
     * @param CTS The adapters from <cite>Coordinate Transformation Services</cite>
     *            (package <code>org.geotools.ct</code>).
     */
    protected Adapters(final org.geotools.ct.Adapters CTS) {
        this.CTS = CTS;
    }

    /**
     * Returns an OpenGIS enumeration for a color interpretation.
     *
     * @param  The Geotools object.
     * @return The OpenGIS  object. 
     */
    public CV_ColorInterpretation export(final ColorInterpretation colors) {
        if (colors == null) {
            return null;
        }
        return new CV_ColorInterpretation(colors.getValue());
    }

    /**
     * Returns an OpenGIS enumeration for a color interpretation.
     *
     * @param  The Geotools object.
     * @return The OpenGIS  object. 
     */
    public CV_SampleDimensionType export(final SampleDimensionType type) {
        if (type == null) {
            return null;
        }
        return new CV_SampleDimensionType(type.getValue());
    }

    /**
     * Returns an OpenGIS interface for a sample dimension.
     *
     * @param  The Geotools object.
     * @return The OpenGIS  object. 
     */
    public CV_SampleDimension export(final SampleDimension dimension) {
        if (dimension == null) {
            return null;
        }
        return (CV_SampleDimension) dimension.toOpenGIS(this);
    }

    /**
     * Returns an OpenGIS interface for a coverage.
     *
     * @param  The Geotools object.
     * @return The OpenGIS  object. 
     */
    public CV_Coverage export(final Coverage coverage) {
        if (coverage == null) {
            return null;
        }
        Object proxy = coverage.proxy;
        if (proxy instanceof Reference) {
            proxy = ((Reference) proxy).get();
        }
        if (proxy != null) {
            return (CV_Coverage) proxy;
        }
        return doExport(coverage);
    }

    /**
     * Performs the wrapping of a Geotools object. This method is invoked by
     * {@link #export(Coverage)} if an OpenGIS object is not already presents
     * in the cache. Subclasses should override this method instead of
     * {@link #export(Coverage)}.
     *
     * @param  The Geotools object.
     * @return The OpenGIS  object. 
     */
    protected CV_Coverage doExport(final Coverage coverage) {
        return coverage.new Export(this);
    }

    /**
     * Returns a color interpretation from an OpenGIS's enumeration.
     *
     * @param  The OpenGIS  object.
     * @return The Geotools object. 
     */
    public ColorInterpretation wrap(final CV_ColorInterpretation colors) {
        return (colors!=null) ? ColorInterpretation.getEnum(colors.value) : null;
    }

    /**
     * Returns a sample type from an OpenGIS's enumeration.
     *
     * @param  The OpenGIS  object.
     * @return The Geotools object. 
     */
    public SampleDimensionType wrap(final CV_SampleDimensionType type) {
        return (type!=null) ? SampleDimensionType.getEnum(type.value) : null;
    }
    
    /**
     * Returns a sample dimension from an OpenGIS's interface.
     *
     * @param  The OpenGIS  object.
     * @return The Geotools object. 
     * @throws RemoteException if an operation failed while querying the OpenGIS object.
     */
    public SampleDimension wrap(final CV_SampleDimension sd) throws RemoteException {
        if (sd == null) {
            return null;
        }
        if (sd instanceof RemoteProxy) {
            return (SampleDimension) ((RemoteProxy) sd).getImplementation();
        }
        return doWrap(sd);
    }

    /**
     * Returns a sample dimension from an OpenGIS's interface. This implementation is made
     * available separatly from {@link #wrap(CV_SampleDimension)}  in order to allow tests
     * with JUnit.
     *
     * @param  The OpenGIS  object.
     * @return The Geotools object. 
     * @throws RemoteException if an operation failed while querying the OpenGIS object.
     */
    final SampleDimension doWrap(final CV_SampleDimension sd) throws RemoteException {
        if (sd.getPaletteInterpretation().value != CV_PaletteInterpretation.CV_RGB) {
            throw new UnsupportedOperationException("Not yet implemented");
        }
        String        description = sd.getDescription();
        SampleDimensionType  type = SampleDimensionType.getEnum(sd.getSampleDimensionType().value);
        ColorInterpretation color = ColorInterpretation.getEnum(sd.getColorInterpretation().value);
        int[][]           palette = sd.getPalette();
        String[]       categories = sd.getCategoryNames();
        double[]           nodata = sd.getNoDataValue();
        double            minimum = sd.getMinimumValue();
        double            maximum = sd.getMaximumValue();
        double              scale = sd.getScale();
        double             offset = sd.getOffset();
        Unit                units = CTS.wrap(sd.getUnits());
        Color[]            colors = null;
        if (palette != null) {
            colors = new Color[palette.length];
            for (int i=0; i<colors.length; i++) {
                final int[] RGBA = palette[i];
                int R=0, G=0, B=0, A=255;
                switch (RGBA.length) {
                    default:           // fall through
                    case 4: A=RGBA[3]; // fall through
                    case 3: B=RGBA[2]; // fall through
                    case 2: G=RGBA[1]; // fall through
                    case 1: R=RGBA[0]; // fall through
                    case 0: break;
                }
                colors[i] = new Color(R,G,B,A);
            }
        }
        if (Double.isNaN(minimum) || Double.isInfinite(minimum)) {
            minimum = 0;
        }
        if (Double.isNaN(maximum) || Double.isInfinite(maximum) || (minimum==0 && maximum==0)) {
            maximum = (palette!=null && palette.length!=0) ? palette.length-1 : 255;
        }
        return new SampleDimension(description, type, color, colors, categories,
                                   nodata, minimum, maximum, scale, offset, units);
    }
    
    /**
     * Returns a coverage from an OpenGIS's interface. If the OpenGIS object is actually
     * an instance of {@link org.opengis.gc.GC_GridCoverage}  <strong>and</strong>  this
     * <code>Adapters</code> is an instance of the {@link org.geotools.gc.Adapters
     * org.geotools.<strong>gc</strong>.Adapters} class, then the returned coverage
     * implementation will be an instance of {@link org.geotools.gc.GridCoverage}.
     *
     * @param  The OpenGIS  object.
     * @return The Geotools object. 
     * @throws RemoteException if an operation failed while querying the OpenGIS object.
     */
    public Coverage wrap(final CV_Coverage coverage) throws RemoteException {
        if (coverage == null) {
            return null;
        }
        if (coverage instanceof Coverage.Export) {
            return ((Coverage.Export) coverage).getImplementation();
        }
        final Coverage wrapped = doWrap(coverage);
        wrapped.proxy = coverage;
        return wrapped;
    }

    /**
     * Performs the wrapping of an OpenGIS's interface. This method is invoked by
     * {@link #wrap(CV_Coverage)} if a Geotools object is not already presents in
     * the cache. Subclasses should override this method instead of
     * {@link #wrap(CV_Coverage)}.
     *
     * @param  The OpenGIS  object.
     * @return The Geotools object. 
     * @throws RemoteException if an operation failed while querying the OpenGIS object.
     */
    protected Coverage doWrap(final CV_Coverage coverage) throws RemoteException {
        try {
            return new CoverageProxy(coverage);
        } catch (RuntimeException exception) {
            final Throwable cause = exception.getCause();
            if (cause instanceof RemoteException) {
                // May occurs when the PropertySourceImpl constructor
                // fetch values from the CoverageProperties adapter.
                throw (RemoteException) cause;
            }
            throw exception;
        }
    }

    /**
     * Returns the {@link PropertySource} for the specified OpenGIS's coverage. If the
     * specified coverage already implements {@link PropertySource}, then it is returned.
     * Otherwise, the coverage is wrapped in an object with the following behaviour:
     * <ul>
     *   <li>{@link PropertySource#getPropertyNames()} delegates to
     *       {@link CV_Coverage#getMetadataNames()}.</li>
     *   <li>{@link PropertySource#getProperty(String)} delegates to
     *       {@link CV_Coverage#getMetadataValue(String)}.</li>
     *    <li>If a checked {@link RemoteException} is thrown, it is
     *        wrapped in an unchecked {@link CannotEvaluateException}.</li>
     * </ul>
     *
     * @param  The OpenGIS coverage.
     * @return The property source for the specified coverage.
     */
    protected PropertySource getPropertySource(final CV_Coverage coverage) {
        if (coverage == null) {
            return null;
        }
        if (coverage instanceof PropertySource) {
            return (PropertySource) coverage;
        }
        return new CoverageProperties(coverage);
    }




    /**
     * An adapter class for {@link CV_Coverage}. Every call to an <code>evaluate</code>
     * method is delegates to the underlying {@link CV_Coverage}, which may be executed
     * on a remote machine. {@link RemoteException} are catched and rethrown as a
     * {@link CannotEvaluateException}.
     *
     * @version $Id: Adapters.java,v 1.9 2003/04/10 20:41:08 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private final class CoverageProxy extends Coverage {
        /**
         * The wrapped OpenGIS coverage. This reference
         * must be the same than {@link Coverage#proxy}.
         */
        private final CV_Coverage coverage;

        /**
         * The bounding box for the coverage domain in coordinate system coordinates.
         */
        private final Envelope envelope;

        /**
         * The names of each dimension in this coverage.
         */
        private final String[] dimensionNames;

        /**
         * The sample dimensions for this coverage.
         * Will be fetch only when first requested.
         */
        private SampleDimension[] dimensions;

        /**
         * Construct a new coverage wrapping the specified OpenGIS object.
         *
         * @param  coverage The OpenGIS coverage.
         * @throws RemoteException if a remote call failed.
         * @throws CannotEvaluateException if the construction failed for some other reason.
         *         The cause for this exception may be a {@link RemoteException}. Callers
         *         should check the cause and throw the underlying {@link RemoteException}
         *         if applicable.
         */
        CoverageProxy(CV_Coverage coverage) throws RemoteException {
            super(null, CTS.wrap(coverage.getCoordinateSystem()), getPropertySource(coverage), null);
            this.coverage  = coverage;
            this.proxy     = coverage;
            this.envelope  = CTS.wrap(coverage.getEnvelope());
            dimensionNames = coverage.getDimensionNames();
        }

        /**
         * Returns the bounding box for the coverage domain in coordinate system coordinates.
         */
        public Envelope getEnvelope() {
            return (Envelope) envelope.clone();
        }

        /**
         * Returns the names of each dimension in this coverage.
         */
        public String[] getDimensionNames(final Locale locale) {
            return (String[]) dimensionNames.clone();
        }

        /**
         * Retrieve sample dimension information for the coverage.
         */
        public synchronized SampleDimension[] getSampleDimensions() {
            if (dimensions == null) try {
                dimensions = new SampleDimension[coverage.getNumSampleDimensions()];
                for (int i=0; i<dimensions.length; i++) {
                    dimensions[i] = wrap(coverage.getSampleDimension(i));
                }
            } catch (RemoteException exception) {
                throw new CannotEvaluateException(Resources.format(ResourceKeys.ERROR_RMI_FAILURE, exception));
            }
            return (SampleDimension[]) dimensions.clone();
        }

        /**
         * Returns a sequence of boolean values for a given point in the coverage.
         *
         * @throws CannotEvaluateException If the point can't be evaluated. It may be because
         *         the point is out of the coverage, or because a remote call failed.
         */
        public boolean[] evaluate(final CoordinatePoint coord, final boolean[] dest)
                throws CannotEvaluateException
        {
            try {
                final boolean[] result = coverage.evaluateAsBoolean(CTS.export(coord));
                if (dest != null) {
                    System.arraycopy(result, 0, dest, 0, result.length);
                    return dest;
                } else {
                    return result;
                }
            } catch (RemoteException exception) {
                throw new CannotEvaluateException(Resources.format(ResourceKeys.ERROR_RMI_FAILURE, exception));
            }
        }

        /**
         * Returns a sequence of byte values for a given point in the coverage.
         *
         * @throws CannotEvaluateException If the point can't be evaluated. It may be because
         *         the point is out of the coverage, or because a remote call failed.
         */
        public byte[] evaluate(final CoordinatePoint coord, final byte[] dest)
                throws PointOutsideCoverageException
        {
            try {
                final byte[] result = coverage.evaluateAsByte(CTS.export(coord));
                if (dest != null) {
                    System.arraycopy(result, 0, dest, 0, result.length);
                    return dest;
                } else {
                    return result;
                }
            } catch (RemoteException exception) {
                throw new CannotEvaluateException(Resources.format(ResourceKeys.ERROR_RMI_FAILURE, exception));
            }
        }

        /**
         * Returns a sequence of integer values for a given point in the coverage.
         *
         * @throws CannotEvaluateException If the point can't be evaluated. It may be because
         *         the point is out of the coverage, or because a remote call failed.
         */
        public int[] evaluate(final CoordinatePoint coord, final int[] dest)
                throws PointOutsideCoverageException
        {
            try {
                final int[] result = coverage.evaluateAsInteger(CTS.export(coord));
                if (dest != null) {
                    System.arraycopy(result, 0, dest, 0, result.length);
                    return dest;
                } else {
                    return result;
                }
            } catch (RemoteException exception) {
                throw new CannotEvaluateException(Resources.format(ResourceKeys.ERROR_RMI_FAILURE, exception));
            }
        }

        /**
         * Returns a sequence of double values for a given point in the coverage.
         *
         * @throws CannotEvaluateException If the point can't be evaluated. It may be because
         *         the point is out of the coverage, or because a remote call failed.
         */
        public double[] evaluate(CoordinatePoint coord, final double[] dest)
                throws PointOutsideCoverageException
        {
            try {
                final double[] result = coverage.evaluateAsDouble(CTS.export(coord));
                if (dest != null) {
                    System.arraycopy(result, 0, dest, 0, result.length);
                    return dest;
                } else {
                    return result;
                }
            } catch (RemoteException exception) {
                throw new CannotEvaluateException(Resources.format(ResourceKeys.ERROR_RMI_FAILURE, exception));
            }
        }
    }
}
