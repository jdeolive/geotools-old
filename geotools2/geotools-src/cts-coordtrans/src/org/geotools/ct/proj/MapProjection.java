/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2001, Institut de Recherche pour le Développement
 * (C) 1999, Fisheries and Oceans Canada
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
 *    This package contains formulas from the PROJ package of USGS.
 *    USGS's work is fully acknowledged here.
 */
package org.geotools.ct.proj;

// J2SE and JAI dependencies
import java.awt.geom.Point2D;
import java.util.Locale;
import java.util.Arrays;
import java.util.Collection;
import java.io.Serializable;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListDescriptor;

// Geotools dependencies
import org.geotools.pt.Matrix;
import org.geotools.pt.Latitude;
import org.geotools.pt.Longitude;
import org.geotools.cs.Projection;

// Resources
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.TransformException;
import org.geotools.ct.AbstractMathTransform;
import org.geotools.ct.MissingParameterException;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Base class for transformation services between ellipsoidal and cartographic projections.
 * This base class provides the basic feature needed for all methods (no need to overrides
 * methods). Subclasses must "only" implements the following methods:
 * <ul>
 *   <li>{@link #getName}</li>
 *   <li>{@link #transform(double,double,Point2D)}</li>
 *   <li>{@link #inverseTransform(double,double,Point2D)}</li>
 * </ul>
 * <br><br>
 * <strong>NOTE:</strong>Serialization of this class is appropriate for short-term storage
 * or RMI use, but will probably not be compatible with future version. For long term storage,
 * WKT (Well Know Text) or XML (not yet implemented) are more appropriate.
 *
 * @version $Id: MapProjection.java,v 1.7 2003/05/12 21:27:56 desruisseaux Exp $
 * @author André Gosselin
 * @author Martin Desruisseaux
 *
 * @see <A HREF="http://mathworld.wolfram.com/MapProjection.html">Map projections on MathWorld</A>
 */
public abstract class MapProjection extends AbstractMathTransform implements MathTransform2D,
                                                                             Serializable
{
    /**
     * Maximal error (in metres) tolerated for assertion, if enabled. When assertions are enabled,
     * every direct projection is followed by an inverse projection, and the result is compared to
     * the original coordinate. If a distance greater than <code>MAX_ERROR</code> is found, then an
     * {@link AssertionError} will be thrown.
     */
    private static final double MAX_ERROR = 1;
    
    /**
     * Maximum difference allowed when comparing real numbers.
     */
    static final double EPS = 1.0E-6;
    
    /**
     * Difference allowed in iterative computations.
     */
    static final double TOL = 1E-10;
    
    /**
     * Classification string for this projection (e.g. "Transverse_Mercator").
     */
    private final String classification;
    
    /**
     * The parameter list descriptor.
     */
    private final ParameterListDescriptor descriptor;
    
    /**
     * Ellipsoid excentricity, equals to <code>sqrt({@link #es})</code>.
     * Value 0 means that the ellipsoid is spherical.
     */
    protected final double e;
    
    /**
     * The square of excentricity: e² = (a²-b²)/a² where
     * <var>a</var> is the semi-major axis length and
     * <var>b</var> is the semi-minor axis length.
     */
    protected final double es;
    
    /**
     * Length of semi-major axis, in metres.
     */
    protected final double semiMajor;
    
    /**
     * Length of semi-minor axis, in metres.
     */
    protected final double semiMinor;
    
    /**
     * Central longitude in <u>radians</u>. Default value is 0, the Greenwich meridian.
     */
    protected final double centralMeridian;
    
    /**
     * Latitude of origin in <u>radians</u>. Default value is 0, the equator.
     * <strong>Consider this field as final</strong>. It is not final only
     * because some class need to modify it at construction time.
     */
    protected double latitudeOfOrigin;
    
    /**
     * The scale factor. Default value is 1.
     * <strong>Consider this field as final</strong>. It is not final only
     * because some class need to modify it at construction time.
     */
    protected double scaleFactor;
    
    /**
     * False easting, in metres. Default value is 0.
     */
    protected final double falseEasting;
    
    /**
     * False northing, in metres. Default value is 0.
     */
    protected final double falseNorthing;
    
    /**
     * The inverse of this map projection. Will be created only when needed.
     */
    private transient MathTransform inverse;
    
    /**
     * Construct a new map projection from the suplied parameters.
     *
     * @param  parameters The parameter values in standard units.
     *         The following parameter are recognized:
     *         <ul>
     *           <li>"semi_major" (mandatory: no default)</li>
     *           <li>"semi_minor" (mandatory: no default)</li>
     *           <li>"central_meridian"   (default to 0°)</li>
     *           <li>"latitude_of_origin" (default to 0°)</li>
     *           <li>"scale_factor"       (default to 1 )</li>
     *           <li>"false_easting"      (default to 0 )</li>
     *           <li>"false_northing"     (default to 0 )</li>
     *         </ul>
     * @throws MissingParameterException if a mandatory parameter is missing.
     */
    protected MapProjection(final Projection parameters) throws MissingParameterException {
        descriptor       =                    parameters.getParameters().getParameterListDescriptor();
        classification   =                    parameters.getClassName();
        semiMajor        =                    parameters.getValue("semi_major");
        semiMinor        =                    parameters.getValue("semi_minor");
        centralMeridian  = longitudeToRadians(parameters.getValue("central_meridian",   0), true);
        latitudeOfOrigin =  latitudeToRadians(parameters.getValue("latitude_of_origin", 0), true);
        scaleFactor      =                    parameters.getValue("scale_factor",       1);
        falseEasting     =                    parameters.getValue("false_easting",      0);
        falseNorthing    =                    parameters.getValue("false_northing",     0);
        es = 1.0 - (semiMinor*semiMinor)/(semiMajor*semiMajor);
        e  = Math.sqrt(es);
    }
    
    /**
     * Convertit en radians une latitude exprimée en degrés. Au passage,
     * cette méthode vérifiera si la latitude est bien dans les limites
     * permises (±90°). Cette méthode est utile pour vérifier la validité
     * des paramètres de la projection, comme {@link #setCentralLongitude}.
     *
     * @param  y Latitude à vérifier, en degrés.
     * @param  edge <code>true</code> pour accepter les latitudes de ±90°.
     * @return Latitude en radians.
     * @throws IllegalArgumentException si la latitude est invalide.
     */
    static double latitudeToRadians(final double y, boolean edge) throws IllegalArgumentException {
        if (edge ? (y>=Latitude.MIN_VALUE && y<=Latitude.MAX_VALUE) :
                   (y> Latitude.MIN_VALUE && y< Latitude.MAX_VALUE))
        {
            return Math.toRadians(y);
        }
        throw new IllegalArgumentException(Resources.format(
                ResourceKeys.ERROR_LATITUDE_OUT_OF_RANGE_$1, new Latitude(y)));
    }
    
    /**
     * Convertit en radians une longitude exprimée en degrés. Au passage,
     * cette méthode vérifiera si la longitude est bien dans les limites
     * permises (±180°). Cette méthode est utile pour vérifier la validité
     * des paramètres de la projection, comme {@link #setCentralLongitude}.
     *
     * @param  x Longitude à vérifier, en degrés.
     * @param  edge <code>true</code> pour accepter les longitudes de ±180°.
     * @return Longitude en radians.
     * @throws IllegalArgumentException si la longitude est invalide.
     */
    static double longitudeToRadians(final double x, boolean edge) throws IllegalArgumentException {
        if (edge ? (x>=Longitude.MIN_VALUE && x<=Longitude.MAX_VALUE) :
                   (x> Longitude.MIN_VALUE && x< Longitude.MAX_VALUE))
        {
            return Math.toRadians(x);
        }
        throw new IllegalArgumentException(Resources.format(
                ResourceKeys.ERROR_LONGITUDE_OUT_OF_RANGE_$1, new Longitude(x)));
    }

    /**
     * Makes sure that the specified longitude stay within ±180 degrees. This methpod should be
     * invoked after {@link #centralMeridian} had been added or removed to <var>x</var>. This
     * method may add or substract an amount of 360° to <var>x</var>.
     *
     * As a special case, we do not check the range if no rotation were applied on <var>x</var>.
     * This is because the user may have a big area ranging from -180° to +180°. With the slight
     * rounding errors related to map projections, the 180° longitude may be slightly over the
     * limit. Doing the check would changes its sign. For example a bounding box from 30° to +180°
     * would become 30° to -180°, which is probably not what the user wanted.
     *
     * @param  x The longitude.
     * @return The longitude in the range +/- 180°.
     */
    final double ensureInRange(double x) {
        if (centralMeridian != 0) {
            if (x > Math.PI) {
                x -= 2*Math.PI;
            } else if (x < -Math.PI) {
                x += 2*Math.PI;
            }
        }
        return x;
    }
    
    /**
     * Returns a human readable name localized for the specified locale.
     */
    public abstract String getName(final Locale locale);

    /**
     * Returns <code>true</code> if this projection uses a spherical model.
     * The model is spherical if {@link #semiMajor} and {@link #semiMinor}
     * axis length are equals.
     */
    public final boolean isSpherical() {
        return semiMajor == semiMinor;
    }

    
    /**
     * Gets the dimension of input points.
     */
    public final int getDimSource() {
        return 2;
    }
    
    /**
     * Gets the dimension of output points.
     */
    public final int getDimTarget() {
        return 2;
    }
    
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                          ////////
    ////////                          TRANSFORMATION METHODS                          ////////
    ////////                                                                          ////////
    ////////             Includes an inner class for inverse projections.             ////////
    ////////                                                                          ////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Check point for private use by {@link #checkTransform}. This class is necessary in order
     * to avoid never-ending loop in <code>assert</code> statements (when an <code>assert</code>
     * calls <code>transform(...)</code>, which calls <code>inverse.transform(...)</code>, which
     * calls <code>transform(...)</code>, etc.).
     */
    private static final class CheckPoint extends Point2D.Double {
        public CheckPoint(final Point2D point) {
            super(point.getX(), point.getY());
        }
    }
    
    /**
     * Check if the transform of <code>point</code> is close enough to <code>target</code>.
     * "Close enough" means that the two points are separated by a distance shorter than
     * {@link #MAX_ERROR}. This method is used for assertions with JDK 1.4.
     *
     * @param  point  Point to transform, in degrees if <code>inverse</code> is false.
     * @param  target Point to compare to, in metres if <code>inverse</code> is false.
     * @param inverse <code>true</code> for an inverse transform instead of a direct one.
     * @return <code>true</code> if the two points are close enough.
     * @throws ProjectionException if a transformation failed.
     */
    private boolean checkTransform(Point2D point, final Point2D target, final boolean inverse) {
        if (!(point instanceof CheckPoint)) try {
            point = new CheckPoint(point);
            final double longitude;
            final double latitude;
            final double distance;
            if (inverse) {
                // Computes orthodromic distance (spherical model) in metres.
                point = ((MathTransform2D)inverse()).transform(point, point);
                final double y1 = Math.toRadians(point .getY());
                final double y2 = Math.toRadians(target.getY());
                final double dx = Math.toRadians(Math.abs(target.getX()-point.getX()) % 360);
                double rho = Math.sin(y1)*Math.sin(y2) + Math.cos(y1)*Math.cos(y2)*Math.cos(dx);
                if (rho>+1) {assert rho<=+(1+EPS) : rho; rho=+1;}
                if (rho<-1) {assert rho>=-(1+EPS) : rho; rho=-1;}
                distance  = Math.acos(rho)*semiMajor;
                longitude = point.getX();
                latitude  = point.getY();
            } else {
                // Computes cartesian distance in metres.
                longitude = point.getX();
                latitude  = point.getY();
                point     = transform(point, point);
                distance  = point.distance(target);
            }
            // Be less strict when the point is near an edge.
            final boolean edge = (Math.abs(longitude) > 179) || (Math.abs(latitude) > 89);
            if (distance > (edge ? 5*MAX_ERROR : MAX_ERROR)) { // Do not fail for NaN values.
                throw new AssertionError(distance);
            }
        } catch (TransformException exception) {
            final AssertionError error = new AssertionError(exception.getLocalizedMessage());
            error.initCause(exception);
            throw error;
        }
        return true;
    }
    
    /**
     * Transforms the specified coordinate and stores the result in <code>ptDst</code>.
     * This method shall returns <var>x</var> values in the range <code>[-PI..PI]</code>
     * and <var>y</var> values in the range <code>[-PI/2..PI/2]</code>. It will be checked
     * by the caller, so this method doesn't need to performs this check.
     *
     * @param x     The longitude of the coordinate, in metres.
     * @param x     The  latitude of the coordinate, in metres.
     * @param ptDst the specified coordinate point that stores the result of transforming
     *              <code>ptSrc</code>, or <code>null</code>. Ordinates will be in
     *              <strong>radians</strong>.
     * @return      the coordinate point after transforming <code>ptSrc</code> and stroring
     *              the result in <code>ptDst</code>.
     * @throws ProjectionException if the point can't be transformed.
     */
    protected abstract Point2D inverseTransform(double x, double y, final Point2D ptDst)
            throws ProjectionException;
    
    /**
     * Transforms the specified coordinate and stores the result in <code>ptDst</code>.
     * This method is guaranteed to be invoked with values of <var>x</var> in the range
     * <code>[-PI..PI]</code> and values of <var>y</var> in the range <code>[-PI/2..PI/2]</code>.
     *
     * @param x     The longitude of the coordinate, in <strong>radians</strong>.
     * @param x     The  latitude of the coordinate, in <strong>radians</strong>.
     * @param ptDst the specified coordinate point that stores the result of transforming
     *              <code>ptSrc</code>, or <code>null</code>. Ordinates will be in metres.
     * @return      the coordinate point after transforming <code>ptSrc</code> and stroring
     *              the result in <code>ptDst</code>.
     * @throws ProjectionException if the point can't be transformed.
     */
    protected abstract Point2D transform(double x, double y, final Point2D ptDst)
            throws ProjectionException;
    
    /**
     * Transforms the specified <code>ptSrc</code> and stores the result in <code>ptDst</code>.
     *
     * @param ptSrc the specified coordinate point to be transformed. Ordinates must be in degrees.
     * @param ptDst the specified coordinate point that stores the result of transforming
     *              <code>ptSrc</code>, or <code>null</code>. Ordinates will be in metres.
     * @return      the coordinate point after transforming <code>ptSrc</code> and stroring
     *              the result in <code>ptDst</code>.
     * @throws ProjectionException if the point can't be transformed.
     */
    public final Point2D transform(final Point2D ptSrc, Point2D ptDst) throws ProjectionException {
        final double x = ptSrc.getX();
        final double y = ptSrc.getY();
        if (x<Longitude.MIN_VALUE-EPS || x>Longitude.MAX_VALUE+EPS) { // Do not fail for NaN values.
            throw new PointOutsideEnvelopeException(Resources.format(
                    ResourceKeys.ERROR_LONGITUDE_OUT_OF_RANGE_$1, new Longitude(x)));
        }
        if (y<Latitude.MIN_VALUE-EPS || y>Latitude.MAX_VALUE+EPS) { // Do not fail for NaN values.
            throw new PointOutsideEnvelopeException(Resources.format(
                    ResourceKeys.ERROR_LATITUDE_OUT_OF_RANGE_$1, new Latitude(y)));
        }
        ptDst = transform(Math.toRadians(x), Math.toRadians(y), ptDst);
        assert checkTransform(ptDst, (ptSrc!=ptDst) ? ptSrc : new Point2D.Double(x,y), true);
        return ptDst;
    }
    
    /**
     * Transforms a list of coordinate point ordinal values. Ordinates must be
     * (<var>longitude</var>,<var>latitude</var>) pairs in degrees.
     *
     * @throws ProjectionException if a point can't be transformed. This method try
     *         to transform every points even if some of them can't be transformed.
     *         Non-transformable points will have value {@link Double#NaN}. If more
     *         than one point can't be transformed, then this exception may be about
     *         an arbitrary point.
     */
    public final void transform(final double[] src,  int srcOffset,
                                final double[] dest, int dstOffset, int numPts)
        throws ProjectionException
    {
        /*
         * Vérifie s'il faudra parcourir le tableau en sens inverse.
         * Ce sera le cas si les tableaux source et destination se
         * chevauchent et que la destination est après la source.
         */
        final boolean reverse = (src==dest && srcOffset<dstOffset &&
                                 srcOffset+(2*numPts) > dstOffset);
        if (reverse) {
            srcOffset += 2*numPts;
            dstOffset += 2*numPts;
        }
        final Point2D.Double point = new Point2D.Double();
        ProjectionException firstException = null;
        while (--numPts>=0) {
            try {
                point.x = src[srcOffset++];
                point.y = src[srcOffset++];
                transform(point, point);
                dest[dstOffset++] = point.x;
                dest[dstOffset++] = point.y;
            } catch (ProjectionException exception) {
                dest[dstOffset++] = Double.NaN;
                dest[dstOffset++] = Double.NaN;
                if (firstException == null) {
                    firstException = exception;
                }
            }
            if (reverse) {
                srcOffset -= 4;
                dstOffset -= 4;
            }
        }
        if (firstException != null) {
            throw firstException;
        }
    }
    
    /**
     * Transforms a list of coordinate point ordinal values. Ordinates must be
     * (<var>longitude</var>,<var>latitude</var>) pairs in degrees.
     *
     * @throws ProjectionException if a point can't be transformed. This method try
     *         to transform every points even if some of them can't be transformed.
     *         Non-transformable points will have value {@link Float#NaN}. If more
     *         than one point can't be transformed, then this exception may be about
     *         an arbitrary point.
     */
    public final void transform(final float[] src,  int srcOffset,
                                final float[] dest, int dstOffset, int numPts)
        throws ProjectionException
    {
        final boolean reverse = (src==dest && srcOffset<dstOffset &&
                                 srcOffset+(2*numPts) > dstOffset);
        if (reverse) {
            srcOffset += 2*numPts;
            dstOffset += 2*numPts;
        }
        final Point2D.Double point = new Point2D.Double();
        ProjectionException firstException=null;
        while (--numPts>=0) {
            try {
                point.x = src[srcOffset++];
                point.y = src[srcOffset++];
                transform(point, point);
                dest[dstOffset++] = (float) point.x;
                dest[dstOffset++] = (float) point.y;
            } catch (ProjectionException exception) {
                dest[dstOffset++] = Float.NaN;
                dest[dstOffset++] = Float.NaN;
                if (firstException == null) {
                    firstException = exception;
                }
            }
            if (reverse) {
                srcOffset -= 4;
                dstOffset -= 4;
            }
        }
        if (firstException != null) {
            throw firstException;
        }
    }
    
    /**
     * Inverse of a map projection.  Will be created by {@link MapProjection#inverse()} only when
     * first required. Implementation of <code>transform(...)</code> methods are mostly identical
     * to <code>MapProjection.transform(...)</code>, except that they will invokes
     * {@link MapProjection#inverseTransform(double,double,Point2D)} instead of
     * {@link MapProjection#transform(double,double,Point2D)}.
     *
     * @version $Id: MapProjection.java,v 1.7 2003/05/12 21:27:56 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private final class Inverse extends AbstractMathTransform.Inverse implements MathTransform2D {
        /**
         * Inverse transforms the specified <code>ptSrc</code>
         * and stores the result in <code>ptDst</code>.
         *
         * @param ptSrc the specified coordinate point to be transformed.
         *              Ordinates must be in metres.
         * @param ptDst the specified coordinate point that stores the
         *              result of transforming <code>ptSrc</code>, or
         *              <code>null</code>. Ordinates will be in degrees.
         * @return the coordinate point after transforming <code>ptSrc</code>
         *         and stroring the result in <code>ptDst</code>.
         * @throws ProjectionException if the point can't be transformed.
         */
        public final Point2D transform(final Point2D ptSrc, Point2D ptDst)
                throws ProjectionException
        {
            final double x0 = ptSrc.getX();
            final double y0 = ptSrc.getY();
            ptDst = inverseTransform(x0, y0, ptDst);
            final double x = Math.toDegrees(ptDst.getX());
            final double y = Math.toDegrees(ptDst.getY());
            ptDst.setLocation(x,y);
            if (x<Longitude.MIN_VALUE-EPS || x>Longitude.MAX_VALUE+EPS) { // Accept NaN values.
                throw new PointOutsideEnvelopeException(Resources.format(
                        ResourceKeys.ERROR_LONGITUDE_OUT_OF_RANGE_$1, new Longitude(x)));
            }
            if (y<Latitude.MIN_VALUE-EPS || y>Latitude.MAX_VALUE+EPS) { // Accept NaN values.
                throw new PointOutsideEnvelopeException(Resources.format(
                        ResourceKeys.ERROR_LATITUDE_OUT_OF_RANGE_$1, new Latitude(y)));
            }
            assert checkTransform(ptDst, (ptSrc!=ptDst) ? ptSrc : new Point2D.Double(x0, y0), false);
            return ptDst;
        }

        /**
         * Inverse transforms a list of coordinate point ordinal values.
         * Ordinates must be (<var>x</var>,<var>y</var>) pairs in metres.
         *
         * @throws ProjectionException if a point can't be transformed. This method try
         *         to transform every points even if some of them can't be transformed.
         *         Non-transformable points will have value {@link Double#NaN}. If more
         *         than one point can't be transformed, then this exception may be about
         *         an arbitrary point.
         */
        public final void transform(final double[] src,  int srcOffset,
                                    final double[] dest, int dstOffset, int numPts)
            throws ProjectionException
        {
            /*
             * Vérifie s'il faudra parcourir le tableau en sens inverse.
             * Ce sera le cas si les tableaux source et destination se
             * chevauchent et que la destination est après la source.
             */
            final boolean reverse = (src==dest && srcOffset<dstOffset &&
                                     srcOffset+(2*numPts) > dstOffset);
            if (reverse) {
                srcOffset += 2*numPts;
                dstOffset += 2*numPts;
            }
            final Point2D.Double point=new Point2D.Double();
            ProjectionException firstException=null;
            while (--numPts>=0) {
                try {
                    point.x = src[srcOffset++];
                    point.y = src[srcOffset++];
                    transform(point, point);
                    dest[dstOffset++] = point.x;
                    dest[dstOffset++] = point.y;
                } catch (ProjectionException exception) {
                    dest[dstOffset++] = Double.NaN;
                    dest[dstOffset++] = Double.NaN;
                    if (firstException == null) {
                        firstException = exception;
                    }
                }
                if (reverse) {
                    srcOffset -= 4;
                    dstOffset -= 4;
                }
            }
            if (firstException != null) {
                throw firstException;
            }
        }

        /**
         * Inverse transforms a list of coordinate point ordinal values.
         * Ordinates must be (<var>x</var>,<var>y</var>) pairs in metres.
         *
         * @throws ProjectionException if a point can't be transformed. This method try
         *         to transform every points even if some of them can't be transformed.
         *         Non-transformable points will have value {@link Float#NaN}. If more
         *         than one point can't be transformed, then this exception may be about
         *         an arbitrary point.
         */
        public final void transform(final float[] src,  int srcOffset,
                                    final float[] dest, int dstOffset, int numPts)
            throws ProjectionException
        {
            final boolean reverse = (src==dest && srcOffset<dstOffset &&
                                     srcOffset+(2*numPts) > dstOffset);
            if (reverse) {
                srcOffset += 2*numPts;
                dstOffset += 2*numPts;
            }
            final Point2D.Double point = new Point2D.Double();
            ProjectionException firstException = null;
            while (--numPts>=0) {
                try {
                    point.x = src[srcOffset++];
                    point.y = src[srcOffset++];
                    transform(point, point);
                    dest[dstOffset++] = (float) point.x;
                    dest[dstOffset++] = (float) point.y;
                } catch (ProjectionException exception) {
                    dest[dstOffset++] = Float.NaN;
                    dest[dstOffset++] = Float.NaN;
                    if (firstException == null) {
                        firstException = exception;
                    }
                }
                if (reverse) {
                    srcOffset -= 4;
                    dstOffset -= 4;
                }
            }
            if (firstException!=null) {
                throw firstException;
            }
        }
    }
    
    /**
     * Returns the inverse of this map projection.
     */
    public final MathTransform inverse() {
        // No synchronization. Not a big deal if this method is invoked in
        // the same time by two threads resulting in two instances created.
        if (inverse == null) {
            inverse = new Inverse();
        }
        return inverse;
    }
    
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                          ////////
    ////////      IMPLEMENTATION OF Object AND MathTransform2D STANDARD METHODS       ////////
    ////////                                                                          ////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Returns a hash value for this map projection.
     */
    public int hashCode() {
        long code =      Double.doubleToLongBits(semiMajor);
        code = code*37 + Double.doubleToLongBits(semiMinor);
        code = code*37 + Double.doubleToLongBits(centralMeridian);
        code = code*37 + Double.doubleToLongBits(latitudeOfOrigin);
        return (int) code ^ (int) (code >>> 32);
    }
    
    /**
     * Compares the specified object with
     * this map projection for equality.
     */
    public boolean equals(final Object object) {
        // Do not check 'object==this' here, since this
        // optimization is usually done in subclasses.
        if (super.equals(object)) {
            final MapProjection that = (MapProjection) object;
            return Double.doubleToLongBits(this.semiMajor)        == Double.doubleToLongBits(that.semiMajor)        &&
                   Double.doubleToLongBits(this.semiMinor)        == Double.doubleToLongBits(that.semiMinor)        &&
                   Double.doubleToLongBits(this.centralMeridian)  == Double.doubleToLongBits(that.centralMeridian)  &&
                   Double.doubleToLongBits(this.latitudeOfOrigin) == Double.doubleToLongBits(that.latitudeOfOrigin) &&
                   Double.doubleToLongBits(this.scaleFactor)      == Double.doubleToLongBits(that.scaleFactor)      &&
                   Double.doubleToLongBits(this.falseEasting)     == Double.doubleToLongBits(that.falseEasting)     &&
                   Double.doubleToLongBits(this.falseNorthing)    == Double.doubleToLongBits(that.falseNorthing);
        }
        return false;
    }
    
    /**
     * Retourne une chaîne de caractères représentant cette projection cartographique.
     * Cette chaîne de caractères contiendra entre autres le nom de la projection, les
     * coordonnées du centre et celles de l'origine.
     *
     * @task REVISIT: part of the implementation is identical to the package-private method
     *       <code>AbstractMathTransform.paramMT(String)</code>.  We should consider moving
     *       it in a formatter class, probably close to WKTParser.
     */
    public final String toString() {
        final StringBuffer buffer = new StringBuffer("PARAM_MT[\"");
        buffer.append(classification);
        buffer.append('"');
        toString(buffer);
        buffer.append(']');
        return buffer.toString();
    }
    
    /**
     * Complete the WKT for this map projection.
     */
    void toString(final StringBuffer buffer) {
        final Collection names = Arrays.asList(descriptor.getParamNames());
        addParameter(names, buffer, "semi_major",         semiMajor);
        addParameter(names, buffer, "semi_minor",         semiMinor);
        addParameter(names, buffer, "central_meridian",   Math.toDegrees(centralMeridian));
        addParameter(names, buffer, "latitude_of_origin", Math.toDegrees(latitudeOfOrigin));
        addParameter(names, buffer, "scale_factor",       scaleFactor);
        addParameter(names, buffer, "false_easting",      falseEasting);
        addParameter(names, buffer, "false_northing",     falseNorthing);
    }
    
    /**
     * Add the <code>", PARAMETER["<name>", <value>]"</code> string
     * to the specified string buffer. This is a convenience method
     * for constructing WKT for "PARAM_MT".
     */
    private static void addParameter(final Collection   names,
                                     final StringBuffer buffer,
                                     final String       key,
                                     final double       value)
    {
        if (names.contains(key)) {
            addParameter(buffer, key, value);
        }
    }
    
    /**
     * Add the <code>", PARAMETER["<name>", <value>]"</code> string
     * to the specified string buffer. This is a convenience method
     * for constructing WKT for "PARAM_MT".
     *
     * @task REVISIT: part of the implementation is identical to the package-private method
     *       <code>AbstractMathTransform.addParameter(StringBuffer, String, double)</code>.
     *       We should consider moving it in a formatter class, probably close to WKTParser.
     */
    static void addParameter(final StringBuffer buffer, final String key, final double value) {
        buffer.append(", PARAMETER[\"");
        buffer.append(key);
        buffer.append("\",");
        buffer.append(value);
        buffer.append(']');
    }
    
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                          ////////
    ////////                           FORMULAS FROM SNYDER                           ////////
    ////////                                                                          ////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Iteratively solve equation (7-9) from Snyder.
     */
    final double cphi2(final double ts) throws ProjectionException {
        final double eccnth = 0.5*e;
        double phi = (Math.PI/2) - 2.0*Math.atan(ts);
        for (int i=0; i<16; i++) {
            final double con  = e*Math.sin(phi);
            final double dphi = (Math.PI/2) - 2.0*Math.atan(ts * Math.pow((1-con)/(1+con), eccnth)) - phi;
            phi += dphi;
            if (Math.abs(dphi) <= TOL) {
                return phi;
            }
        }
        throw new ProjectionException(Resources.format(ResourceKeys.ERROR_NO_CONVERGENCE));
    }
    
    /**
     * Compute function <code>f(s,c,es) = c/sqrt(1 - s²*es)</code>
     * needed for the true scale latitude (Snyder, p. 47), where
     * <var>s</var> and <var>c</var> are the sine and cosine of
     * the true scale latitude, and {@link #es} the eccentricity
     * squared.
     */
    final double msfn(final double s, final double c) {
        return c / Math.sqrt(1.0 - s*s*es);
    }
    
    /**
     * Compute function (15-9) from Snyder equivalent to negative of function (7-7).
     */
    final double tsfn(final double phi, double sinphi) {
        sinphi *= e;
        /*
         * NOTE: change sign to get the equivalent of Snyder (7-7).
         */
        return Math.tan(0.5 * ((Math.PI/2) - phi)) /
               Math.pow((1-sinphi)/(1+sinphi), 0.5*e);
    }
}
