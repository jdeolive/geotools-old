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
package org.geotools.ct;

// J2SE and JAI dependencies
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.Locale;
import java.util.Arrays;
import java.util.Collection;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListDescriptor;

// Geotools dependencies
import org.geotools.pt.Matrix;
import org.geotools.pt.Latitude;
import org.geotools.pt.Longitude;
import org.geotools.cs.Projection;

// Resources
import org.geotools.resources.Geometry;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Provides transformation services between ellipsoidal and cartographic
 * projections. Ellipsoidal height values remain unchanged.
 *
 * @see AffineTransform
 * @see PerspectiveTransform
 *
 * @version $Id: MapProjection.java,v 1.2 2002/07/10 18:19:45 desruisseaux Exp $
 * @author André Gosselin
 * @author Martin Desruisseaux
 */
abstract class MapProjection extends AbstractMathTransform implements MathTransform2D {
    /**
     * Maximal error (in metres) tolerated in assertion, in enabled. When
     * assertions are enabled, every direct projection is followed by an
     * inverse projection, and the result is compared to the original
     * coordinate. If a distance greater than <code>MAX_ERROR</code> is
     * found, then an {@link AssertionError} will be thrown.
     */
    private static final double MAX_ERROR = 1;
    
    /**
     * Maximum difference allowed when comparing real numbers.
     */
    static final double EPS=1.0E-6;
    
    /**
     * Difference allowed in iterative computations.
     */
    static final double TOL=1E-10;
    
    /**
     * Classification string for this projection
     * (e.g. "Transverse_Mercator").
     */
    private final String classification;
    
    /**
     * The parameter list descriptor.
     */
    private final ParameterListDescriptor descriptor;
    
    /**
     * Tells if the ellipsoid in spherical. Value <code>true</code> means
     * that fields {@link #semiMajor} and {@link #semiMinor} must have the
     * same value.
     */
    protected final boolean isSpherical;
    
    /**
     * Ellipsoid excentricity. Value 0 means that the ellipsoid is
     * spherical, i.e. {@link #isSpherical} is <code>true</code>.
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
     * Central longitude in <u>radians</u>.  Default value is 0, the Greenwich
     * meridian.
     */
    protected final double centralMeridian;
    
    /**
     * Central latitude in <u>radians</u>. Default value is 0, the equator.
     * <strong>Consider this field as final</strong>. It is not final only
     * because some class need to modify it at construction time.
     */
    protected double centralLatitude;
    
    /**
     * The scale factor.
     */
    protected final double scaleFactor;
    
    /**
     * False easting, in metres. Default value is 0.
     */
    protected final double falseEasting;
    
    /**
     * False northing, in metres. Default value is 0.
     */
    protected final double falseNorthing;
    
    /**
     * The inverse of this map projection.
     * Will be created only when needed.
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
        descriptor      =                    parameters.getParameters().getParameterListDescriptor();
        classification  =                    parameters.getClassName();
        semiMajor       =                    parameters.getValue("semi_major");
        semiMinor       =                    parameters.getValue("semi_minor");
        centralMeridian = longitudeToRadians(parameters.getValue("central_meridian",   0), true);
        centralLatitude =  latitudeToRadians(parameters.getValue("latitude_of_origin", 0), true);
        scaleFactor     =                    parameters.getValue("scale_factor",       1);
        falseEasting    =                    parameters.getValue("false_easting",      0);
        falseNorthing   =                    parameters.getValue("false_northing",     0);
        isSpherical     = (semiMajor==semiMinor);
        es = 1.0 - (semiMinor*semiMinor)/(semiMajor*semiMajor);
        e  = Math.sqrt(es);
    }
    
    /**
     * Returns a human readable name localized for the specified locale.
     */
    public abstract String getName(final Locale locale);
    
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
     * Check point for private use by {@link #checkTransform} and
     * {@link #checkInverseTransform}. This class is necessary in
     * order to avoid never-ending loop in <code>assert</code>
     * statements (when an <code>assert</code> calls <code>transform</code>,
     * which calls <code>inverseTransform</code>, which calls
     * <code>transform</code>, etc.).
     */
    private static final class CheckPoint extends Point2D.Double {
        public CheckPoint(final Point2D point) {
            super(point.getX(), point.getY());
        }
    }
    
    /**
     * Check if the transform of <code>point</code> is close enough to
     * <code>target</code>. "Close enough" means that the two points are
     * separated by a distance shorter than {@link #MAX_ERROR}. This method
     * is used for assertions with JDK 1.4.
     *
     * @param  point  Point to transform, in degrees if <code>inverse</code> is false.
     * @param  target Point to compare to, in metres if <code>inverse</code> is false.
     * @param inverse <code>true</code> for an inverse transform instead of a direct one.
     * @return <code>true</code> if the two points are close enough.
     * @throws TransformException if a transformation failed.
     */
    private boolean checkTransform(Point2D point, final Point2D target, final boolean inverse) {
        if (!(point instanceof CheckPoint)) try {
            point = new CheckPoint(point);
            final double distance;
            if (inverse) {
                point = inverseTransform(point, point);
                final double y1 = Math.toRadians(point .getY());
                final double y2 = Math.toRadians(target.getY());
                final double dx = Math.toRadians(Math.abs(target.getX()-point.getX()) % 360);
                double rho = Math.sin(y1)*Math.sin(y2) + Math.cos(y1)*Math.cos(y2)*Math.cos(dx);
                if (rho>+1) {assert rho<=+(1+EPS) : rho; rho=+1;}
                if (rho<-1) {assert rho>=-(1+EPS) : rho; rho=-1;}
                distance = Math.acos(rho)*semiMajor;
                // Computed orthodromic distance (spherical model) in metres.
            } else {
                point = transform(point, point);
                distance = point.distance(target);
            }
            if (!(distance <= MAX_ERROR)) { // Do not accept NaN as valid value.
                throw new AssertionError(distance);
            }
        } catch (TransformException exception) {
            final AssertionError error = new AssertionError(exception.getLocalizedMessage());
            error.initCause(exception);
            throw error;
        }
        return true;
    }
    
    
    
    
    //////////////////////////////////////////////////////////////////////
    ////                                                              ////
    ////                          TRANSFORMS                          ////
    ////                                                              ////
    //////////////////////////////////////////////////////////////////////
    
    /**
     * Transforms the specified coordinate and stores the result in <code>ptDst</code>.
     * This method is guaranteed to be invoked with values of <var>x</var> in the range
     * <code>[-PI..PI]</code> and values of <var>y</var> in the range <code>[-PI/2..PI/2]</code>.
     *
     * @param x     The longitude of the coordinate, in <strong>radians</strong>.
     * @param x     The  latitude of the coordinate, in <strong>radians</strong>.
     * @param ptDst the specified coordinate point that stores the
     *              result of transforming <code>ptSrc</code>, or
     *              <code>null</code>. Ordinates will be in metres.
     * @return the coordinate point after transforming <code>ptSrc</code>
     *         and stroring the result in <code>ptDst</code>.
     * @throws TransformException if the point can't be transformed.
     */
    protected abstract Point2D transform(double x, double y, final Point2D ptDst)
            throws TransformException;
    
    /**
     * Transforms the specified <code>ptSrc</code>
     * and stores the result in <code>ptDst</code>.
     *
     * @param ptSrc the specified coordinate point to be transformed.
     *              Ordinates must be in degrees.
     * @param ptDst the specified coordinate point that stores the
     *              result of transforming <code>ptSrc</code>, or
     *              <code>null</code>. Ordinates will be in metres.
     * @return the coordinate point after transforming <code>ptSrc</code>
     *         and stroring the result in <code>ptDst</code>.
     * @throws TransformException if the point can't be transformed.
     */
    public final Point2D transform(final Point2D ptSrc, Point2D ptDst) throws TransformException {
        final double x=ptSrc.getX();
        final double y=ptSrc.getY();
        if (!(x>=Longitude.MIN_VALUE && x<=Longitude.MAX_VALUE)) {
            throw new TransformException(Resources.format(
                    ResourceKeys.ERROR_LONGITUDE_OUT_OF_RANGE_$1, new Longitude(x)));
        }
        if (!(y>=Latitude.MIN_VALUE && y<=Latitude.MAX_VALUE)) {
            throw new TransformException(Resources.format(
                    ResourceKeys.ERROR_LATITUDE_OUT_OF_RANGE_$1, new Latitude(y)));
        }
        ptDst = transform(Math.toRadians(x), Math.toRadians(y), ptDst);
        assert checkTransform(ptDst, (ptSrc!=ptDst) ? ptSrc : new Point2D.Double(x,y), true);
        return ptDst;
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     * Ordinates must be (<var>longitude</var>,<var>latitude</var>)
     * pairs in degrees.
     *
     * @throws TransformException if a point can't be transformed. This method try
     *         to transform every points even if some of them can't be transformed.
     *         Non-transformable points will have value {@link Double#NaN}. If more
     *         than one point can't be transformed, then this exception may be about
     *         an arbitrary point.
     */
    public final void transform(final double[] src,  int srcOffset,
                                final double[] dest, int dstOffset, int numPts)
        throws TransformException
    {
        /*
         * Vérifie s'il faudra parcourir le tableau en sens inverse.
         * Ce sera le cas si les tableaux source et destination se
         * chevauchent et que la destination est après la source.
         */
        final boolean reverse = (src==dest && srcOffset<dstOffset &&
                                 srcOffset+(numPts << 1)>dstOffset);
        if (reverse) {
            srcOffset += 2*numPts;
            dstOffset += 2*numPts;
        }
        final Point2D.Double point=new Point2D.Double();
        TransformException firstException=null;
        while (--numPts>=0) {
            try {
                point.x = src[srcOffset++];
                point.y = src[srcOffset++];
                transform(point, point);
                dest[dstOffset++] = point.x;
                dest[dstOffset++] = point.y;
            } catch (TransformException exception) {
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
     * Transforms a list of coordinate point ordinal values.
     * Ordinates must be (<var>longitude</var>,<var>latitude</var>)
     * pairs in degrees.
     *
     * @throws TransformException if a point can't be transformed. This method try
     *         to transform every points even if some of them can't be transformed.
     *         Non-transformable points will have value {@link Float#NaN}. If more
     *         than one point can't be transformed, then this exception may be about
     *         an arbitrary point.
     */
    public final void transform(final float[] src,  int srcOffset,
                                final float[] dest, int dstOffset, int numPts)
        throws TransformException {
        final boolean reverse = (src==dest && srcOffset<dstOffset &&
                                 srcOffset+(numPts << 1)>dstOffset);
        if (reverse) {
            srcOffset += 2*numPts;
            dstOffset += 2*numPts;
        }
        final Point2D.Double point = new Point2D.Double();
        TransformException firstException=null;
        while (--numPts>=0) {
            try {
                point.x = src[srcOffset++];
                point.y = src[srcOffset++];
                transform(point, point);
                dest[dstOffset++] = (float) point.x;
                dest[dstOffset++] = (float) point.y;
            } catch (TransformException exception) {
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
     * Transforme la forme géométrique <code>shape</code> spécifiée.
     * Cette projection peut remplacer certaines lignes droites
     * par des courbes. Tous les points de la forme géométrique
     * seront copiés. Cette méthode n'est donc pas à conseiller
     * si <code>shape</code> est volumineux, par exemple s'il
     * représente une bathymétrie entière.
     *
     * @param shape Forme géométrique à transformer. Les coordonnées des points
     *              de cette forme doivent être exprimées en degrés de latitudes
     *              et de longitudes.
     * @return      Forme géométrique transformée. Les coordonnées des points de
     *              cette forme seront exprimées en mètres.
     * @throws TransformException si une transformation a échouée.
     */
    public final Shape createTransformedShape(final Shape shape) throws TransformException {
        return createTransformedShape(shape, null, null, Geometry.HORIZONTAL);
    }
    
    
    
    
    //////////////////////////////////////////////////////////////////////
    ////                                                              ////
    ////                      INVERSE TRANSFORMS                      ////
    ////                                                              ////
    //////////////////////////////////////////////////////////////////////
    
    /**
     * Transforms the specified coordinate and stores the result in <code>ptDst</code>.
     * This method shall returns <var>x</var> values in the range <code>[-PI..PI]</code>
     * and <var>y</var> values in the range <code>[-PI/2..PI/2]</code>. It will be checked
     * by the caller, so this method doesn't need to performs this check.
     *
     * @param x     The longitude of the coordinate, in metres.
     * @param x     The  latitude of the coordinate, in metres.
     * @param ptDst the specified coordinate point that stores the
     *              result of transforming <code>ptSrc</code>, or
     *              <code>null</code>. Ordinates will be in <strong>radians</strong>.
     * @return the coordinate point after transforming <code>ptSrc</code>
     *         and stroring the result in <code>ptDst</code>.
     * @throws TransformException if the point can't be transformed.
     */
    protected abstract Point2D inverseTransform(double x, double y, final Point2D ptDst)
            throws TransformException;
    
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
     * @throws TransformException if the point can't be transformed.
     */
    public final Point2D inverseTransform(final Point2D ptSrc, Point2D ptDst)
            throws TransformException
    {
        final double x0 = ptSrc.getX();
        final double y0 = ptDst.getY();
        ptDst = inverseTransform(x0, y0, ptDst);
        final double x = Math.toDegrees(ptDst.getX());
        final double y = Math.toDegrees(ptDst.getY());
        ptDst.setLocation(x,y);
        if (!(x>=Longitude.MIN_VALUE && x<=Longitude.MAX_VALUE)) {
            throw new TransformException(Resources.format(
                    ResourceKeys.ERROR_LONGITUDE_OUT_OF_RANGE_$1, new Longitude(x)));
        }
        if (!(y>=Latitude.MIN_VALUE && y<=Latitude.MAX_VALUE)) {
            throw new TransformException(Resources.format(
                    ResourceKeys.ERROR_LATITUDE_OUT_OF_RANGE_$1, new Latitude(y)));
        }
        assert checkTransform(ptDst, (ptSrc!=ptDst) ? ptSrc : new Point2D.Double(x0, y0), false);
        return ptDst;
    }
    
    /**
     * Inverse transforms a list of coordinate point ordinal values.
     * Ordinates must be (<var>x</var>,<var>y</var>) pairs in metres.
     *
     * @throws TransformException if a point can't be transformed. This method try
     *         to transform every points even if some of them can't be transformed.
     *         Non-transformable points will have value {@link Double#NaN}. If more
     *         than one point can't be transformed, then this exception may be about
     *         an arbitrary point.
     */
    public final void inverseTransform(final double[] src,  int srcOffset,
                                       final double[] dest, int dstOffset, int numPts)
        throws TransformException
    {
        /*
         * Vérifie s'il faudra parcourir le tableau en sens inverse.
         * Ce sera le cas si les tableaux source et destination se
         * chevauchent et que la destination est après la source.
         */
        final boolean reverse = (src==dest && srcOffset<dstOffset &&
                                 srcOffset+(numPts << 1)>dstOffset);
        if (reverse) {
            srcOffset += 2*numPts;
            dstOffset += 2*numPts;
        }
        final Point2D.Double point=new Point2D.Double();
        TransformException firstException=null;
        while (--numPts>=0) {
            try {
                point.x = src[srcOffset++];
                point.y = src[srcOffset++];
                inverseTransform(point, point);
                dest[dstOffset++] = point.x;
                dest[dstOffset++] = point.y;
            } catch (TransformException exception) {
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
     * @throws TransformException if a point can't be transformed. This method try
     *         to transform every points even if some of them can't be transformed.
     *         Non-transformable points will have value {@link Float#NaN}. If more
     *         than one point can't be transformed, then this exception may be about
     *         an arbitrary point.
     */
    public final void inverseTransform(final float[] src,  int srcOffset,
                                       final float[] dest, int dstOffset, int numPts)
        throws TransformException
    {
        final boolean reverse = (src==dest && srcOffset<dstOffset &&
                                 srcOffset+(numPts << 1)>dstOffset);
        if (reverse) {
            srcOffset += 2*numPts;
            dstOffset += 2*numPts;
        }
        final Point2D.Double point=new Point2D.Double();
        TransformException firstException=null;
        while (--numPts>=0) {
            try {
                point.x = src[srcOffset++];
                point.y = src[srcOffset++];
                inverseTransform(point, point);
                dest[dstOffset++] = (float) point.x;
                dest[dstOffset++] = (float) point.y;
            } catch (TransformException exception) {
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
    
    
    
    //////////////////////////////////////////////////////////////////////
    ////                                                              ////
    ////             INTERNAL COMPUTATIONS FOR SUBCLASSES             ////
    ////                                                              ////
    //////////////////////////////////////////////////////////////////////
    
    /**
     * Iteratively solve equation (7-9) from Snyder.
     */
    final double cphi2(final double ts) throws TransformException {
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
        throw new TransformException(Resources.format(ResourceKeys.ERROR_NO_CONVERGENCE));
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
     * Compute function (15-9) from Snyder
     * equivalent to negative of function (7-7).
     */
    final double tsfn(final double phi, double sinphi) {
        sinphi *= e;
        /*
         * NOTE: change sign to get the equivalent of Snyder (7-7).
         */
        return Math.tan(0.5 * ((Math.PI/2) - phi)) /
               Math.pow((1-sinphi)/(1+sinphi), 0.5*e);
    }
    
    
    
    
    //////////////////////////////////////////////////////////////////////
    ////                                                              ////
    ////                        MISCELLANEOUS                         ////
    ////                                                              ////
    //////////////////////////////////////////////////////////////////////
    
    /**
     * Returns the inverse of this map projection.
     */
    public final synchronized MathTransform inverse() {
        if (inverse==null) {
            inverse=new Inverse();
        }
        return inverse;
    }
    
    /**
     * Returns a hash value for this map projection.
     */
    public int hashCode() {
        long code =      Double.doubleToLongBits(semiMajor);
        code = code*37 + Double.doubleToLongBits(semiMinor);
        code = code*37 + Double.doubleToLongBits(centralMeridian);
        code = code*37 + Double.doubleToLongBits(centralLatitude);
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
            return Double.doubleToLongBits(this.semiMajor)       == Double.doubleToLongBits(that.semiMajor)       &&
                   Double.doubleToLongBits(this.semiMinor)       == Double.doubleToLongBits(that.semiMinor)       &&
                   Double.doubleToLongBits(this.centralMeridian) == Double.doubleToLongBits(that.centralMeridian) &&
                   Double.doubleToLongBits(this.centralLatitude) == Double.doubleToLongBits(that.centralLatitude) &&
                   Double.doubleToLongBits(this.scaleFactor)     == Double.doubleToLongBits(that.scaleFactor)     &&
                   Double.doubleToLongBits(this.falseEasting)    == Double.doubleToLongBits(that.falseEasting)    &&
                   Double.doubleToLongBits(this.falseNorthing)   == Double.doubleToLongBits(that.falseNorthing);
        }
        return false;
    }
    
    /**
     * Retourne une chaîne de caractères représentant cette projection cartographique.
     * Cette chaîne de caractères contiendra entre autres le nom de la projection, les
     * coordonnées du centre et celles de l'origine.
     */
    public final String toString() {
        final StringBuffer buffer=paramMT(classification);
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
        addParameter(names, buffer, "latitude_of_origin", Math.toDegrees(centralLatitude));
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
     * Inverse of a map projection.
     *
     * @version $Id: MapProjection.java,v 1.2 2002/07/10 18:19:45 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private final class Inverse extends AbstractMathTransform.Inverse implements MathTransform2D {
        public Point2D transform(final Point2D source, final Point2D dest)
            throws TransformException
        {
            return MapProjection.this.inverseTransform(source, dest);
        }
        
        public void transform(final double[] source, final int srcOffset,
                              final double[] dest,   final int dstOffset, final int length)
            throws TransformException
        {
            MapProjection.this.inverseTransform(source, srcOffset, dest, dstOffset, length);
        }
        
        public void transform(final float[] source, final int srcOffset,
                              final float[] dest,   final int dstOffset, final int length)
            throws TransformException
        {
            MapProjection.this.inverseTransform(source, srcOffset, dest, dstOffset, length);
        }
        
        public Shape createTransformedShape(final Shape shape) throws TransformException {
            return this.createTransformedShape(shape, null, null, Geometry.HORIZONTAL);
        }
    }
    
    /**
     * Informations about a {@link MapProjection}.
     *
     * @version $Id: MapProjection.java,v 1.2 2002/07/10 18:19:45 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    static abstract class Provider extends MathTransformProvider {
        /**
         * Construct a new provider.
         *
         * @param classification The classification name.
         * @param nameKey Resources key for a human readable name.
         *        This is used for {@link #getName} implementation.
         */
        protected Provider(final String classname, final int nameKey) {
            super(classname, nameKey, DEFAULT_PROJECTION_DESCRIPTOR);
        }
        
        /**
         * Create a new map projection for a parameter list.
         */
        public final MathTransform create(final ParameterList parameters) {
            return (MathTransform)create(new Projection("Generated", getClassName(), parameters));
        }
        
        /**
         * Create a new map projection.  NOTE: The returns type should
         * be {@link MathTransform}, but as of JDK 1.4-beta3, it force
         * class loading for all projection classes (MercatorProjection,
         * etc.) before than necessary. Changing the returns type to
         * Object is a trick to avoid too early class loading...
         */
        protected abstract Object create(final Projection parameters);
    }
}
