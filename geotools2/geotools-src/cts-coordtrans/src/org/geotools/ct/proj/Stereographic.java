/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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

// J2SE dependencies
import java.util.Locale;
import java.awt.geom.Point2D;

// Geotools dependencies
import org.geotools.pt.Latitude;
import org.geotools.cs.Ellipsoid;
import org.geotools.cs.Projection;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransformProvider;
import org.geotools.ct.MissingParameterException;

// Resources
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;

/**
 * Stereographic Projection. The directions starting from the central point are true,
 * but the areas and the lengths become increasingly deformed as one moves away from
 * the center.  This projection is used to represent polar areas. It can be adapted
 * for other areas having a circular form.
 * <br><br>
 *
 * This implementation provides transforms for three cases of the stereographic projection:
 * <ul>
 *   <li><code>Polar_Stereographic</code> (EPSG code 9810, uses itteration for the inverse)</li>
 *   <li><code>Oblique_Stereographic</code>(<strong>Similar</strong> to EPSG code 9809</li>
 *   <li><code>Stereographic</code></li>
 *   <!--<li><code>Polar_Stereographic_EPSG</code> (EPSG code 9810, uses a series for the inverse)<li>-->
 *   <li><code>Oblique_Stereographic_EPSG</code> (EPSG code 9809)<li>
 * </ul>
 * Both the USGS equations of Snyder and the EPSG equations are implemented here. For the
 * USGS ellipse case the conformal latitude of each point on the sphere is computed. The
 * EPSG Polar is the same as the USGS, but the Oblique / Equatorial case (EPSG code 9809)
 * uses only a single conformal sphere at the origin point. The EPSG considers both methods
 * to be valid, but considers the USGS method to be a different coordinate operation method. 
 * <br><br>
 * The <code>latitude_true_scale</code> parameter is not specified by the EPSG and is
 * only used for the Polar_Stereographic. 
 *
 * <strong>References:</strong><ul>
 *   <li>John P. Snyder (Map Projections - A Working Manual,<br>
 *       U.S. Geological Survey Professional Paper 1395, 1987)</li>
 *   <li>&quot;Coordinate Conversions and Transformations including Formulas&quot;,<br>
 *       EPSG Guidence Note Number 7, Version 19.</li>
 * </ul>
 *
 * @see <A HREF="http://mathworld.wolfram.com/StereographicProjection.html">Stereographic projection on MathWorld</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/polar_stereographic.html">Polar_Stereographic</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/oblique_stereographic.html">Oblique_Stereographic</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/stereographic.html">Stereographic</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/random_issues.html#stereographic">Some Random Stereographic Issues</A>
 *
 * @version $Id: Stereographic.java,v 1.2 2003/07/28 09:43:02 desruisseaux Exp $
 * @author André Gosselin
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 */
public class Stereographic extends PlanarProjection {
    /**
     * <code>true</code> if the series form should be used for calculating 
     * the inverse polar, or <code>false</code> for using the iterations.
     */
    private static final boolean OPTIMIZE_FOR_SPEED = false;

    /**
     * Maximum number of itterations for the inverse calculation.
     */
    private static final int MAX_ITER = 10;

    /** Projection mode for switch statement. */ private static final int   NORTH      = 0;
    /** Projection mode for switch statement. */ private static final int   SOUTH      = 1;
    /** Projection mode for switch statement. */ private static final int   OBLIQUE    = 2;
    /** Projection mode for switch statement. */ private static final int   EQUATORIAL = 3;
    
    /**
     * Projection mode. It must be one of the following constants:
     * {@link #NORTH}, {@link #SOUTH}, {@link #OBLIQUE} or {@link #EQUATORIAL}.
     */
    final int mode;
            
    /**
     * Global scale factor. Value <code>a</code>
     * is equals to <code>semiMajor*scaleFactor</code>.
     */
    protected final double a;
    
    /**
     * A constant used in the transformations. This is not equal
     * to the <code>scaleFactor</code>.
     */
    private final double k0;
    
    /**
     * <code>a*k0</code>.
     */
    private final double ak0;
    
    /**
     * Latitude of true scale, in radians. Used
     * for {@link #toString} implementation.
     */
    protected final double latitudeTrueScale;
    
    /**
     * Constants used for equitorial and oblique projections. 
     */
    protected final double sinphi0, cosphi0, chi1, sinChi1, cosChi1;
    
    /**
     * <code>true</code> if using the EPSG equations. Used in equals() comparison.
     */
    private final boolean EPSG;
    
    /**
     * Constants used for the inverse polar series
     */
    private final double A, B;
    
    /**
     * Constants used for the inverse polar series
     */
    private double C, D;
    
    /**
     *  The <code>latitudeTrueScale</code> constant used for the polar series inverse.
     */
    private final double k0_series;
    
    /**
     * Informations about a {@link Stereographic} projection.
     *
     * @version $Id: Stereographic.java,v 1.2 2003/07/28 09:43:02 desruisseaux Exp $
     * @author Martin Desruisseaux
     * @author Rueben Schulz
     */
    static final class Provider extends org.geotools.ct.proj.Provider {
        /**
         * <code>true</code> for polar stereographic, or
         * <code>false</code> for equatorial and oblique
         * stereographic.
         */
        private final boolean polar;

        /**
         * <code>true</code> if polar/oblique/equatorial
         * stereographic can be automatically choosen.
         */
        private final boolean auto;
        
        /**
         * <code>true</code> if using the EPSG equations.
         */
        private final boolean EPSG;

        /**
         * Construct a new provider. The type (polar, oblique
         * or equatorial) will be choosen automatically according
         * the latitude or origin.
         */
        public Provider() {
            super("Stereographic", ResourceKeys.STEREOGRAPHIC_PROJECTION);
            put("latitude_true_scale", 90.0, LATITUDE_RANGE);
            polar = true;
            auto  = true;
            EPSG = false;
        }

        /**
         * Construct an object for polar or oblique stereographic.
         *
         * @param polar <code>true</code> for polar stereographic, or
         *              <code>false</code> for equatorial and oblique
         *              stereographic.
         */
        public Provider(final boolean polar) {
            this(polar, false);
        }
        
        /**
         * Construct an object for polar or oblique stereographic.
         *
         * @param polar <code>true</code> for polar stereographic, or
         *              <code>false</code> for equatorial and oblique
         *              stereographic.
         * @param EPSG <code>true</code> for EPSG equations, or
         *              <code>false</code> for USGS equations.
         */
        public Provider(final boolean polar, final boolean EPSG) {
            super(EPSG ? (polar ? "Polar_Stereographic" : "Oblique_Stereographic_EPSG") :
                         (polar ? "Polar_Stereographic" : "Oblique_Stereographic"), 
                          ResourceKeys.STEREOGRAPHIC_PROJECTION);
            if (polar && !EPSG) {
                put("latitude_true_scale", polar ? 90.0 : 0.0, LATITUDE_RANGE);
            }
            this.polar = polar;
            this.auto  = false;
            this.EPSG  = EPSG;
        }

        /**
         * Create a new stereographic projection.
         */
        public MathTransform create(final Projection parameters) throws MissingParameterException {
            if (isSpherical(parameters)) {
                return new Spherical(parameters, polar, auto);
            } else {
                if (EPSG && !polar) {
                    return new ObliqueEPSG(parameters, polar, auto);
                } else {
                    return new Stereographic(parameters, polar,auto, false);
                }
            }
        }
    }

    /**
     * Construct a stereographic transformation from the specified parameters.
     */
    Stereographic(final Projection parameters,
                  final boolean polar,
                  final boolean auto,
                  final boolean EPSG) 
            throws MissingParameterException 
    {
        // Fetch parameters
        super(parameters);
        final double defaultLatitude = parameters.getValue("latitude_of_origin", polar ? 90 : 0);

        latitudeTrueScale = Math.abs(latitudeToRadians(
                parameters.getValue("latitude_true_scale", defaultLatitude), true));
        this.EPSG = EPSG;
        
	//Compute Constants 
        if (auto ? (Math.abs(Math.abs(latitudeOfOrigin)-(Math.PI/2)) < EPS) : polar) {
            if (latitudeOfOrigin < 0) {
                latitudeOfOrigin = -(Math.PI/2);
                mode = SOUTH;
            } else {
                latitudeOfOrigin = +(Math.PI/2);
                mode = NORTH;
            }
        } else if (Math.abs(latitudeOfOrigin) < EPS) {
            latitudeOfOrigin = 0;
            mode = EQUATORIAL;
            assert (latitudeOfOrigin == latitudeTrueScale) || 
                   (latitudeOfOrigin == -latitudeTrueScale) : latitudeTrueScale;
        } else {
            mode = OBLIQUE;
            assert (latitudeOfOrigin == latitudeTrueScale) || 
                   (latitudeOfOrigin == -latitudeTrueScale) : latitudeTrueScale;
        }

        if (mode == EQUATORIAL) {
            cosphi0 = 1.0;
            sinphi0 = 0.0;
            chi1    = 0.0;
            cosChi1 = 1.0;
            sinChi1 = 0.0;
        } else {
            cosphi0 = Math.cos(latitudeOfOrigin);
            sinphi0 = Math.sin(latitudeOfOrigin);
            chi1    = 2.0 * Math.atan(ssfn(latitudeOfOrigin, sinphi0)) - (Math.PI/2);
            cosChi1 = Math.cos(chi1);
            sinChi1 = Math.sin(chi1);
        }
    
        //  Compute k0 and ak0  //
        // spherical k0 now calculated in its subclass
        switch (mode) {
            default: {
                // Should not happen.
                throw new AssertionError(mode);
            }
            
            case NORTH:
            case SOUTH: {
                if (Math.abs(latitudeTrueScale-(Math.PI/2)) >= EPS) {
                    final double t = Math.sin(latitudeTrueScale);
                    k0 = msfn(t,Math.cos(latitudeTrueScale)) /
                    tsfn(latitudeTrueScale, t);  //derives from (21-32 and 21-33)
                } else {
                    // True scale at pole (part of (21-33))
                    k0 = 2.0 / Math.sqrt(Math.pow(1+e, 1+e)*Math.pow(1-e, 1-e));
                }
                break;
            }
            
            case OBLIQUE:
            case EQUATORIAL: {
                // part of (14 - 15)
                k0 = 2.0*msfn(sinphi0, cosphi0);
                break;
            }
        }
        this.a = semiMajor * scaleFactor;
        this.ak0 = k0 * a;
        
        //constants for the series form of the polar inverse
        if (OPTIMIZE_FOR_SPEED) {
            //See Snyde P. 19, "Computation of Series"
            final double e6 = es*es*es;
            final double e8 = es*es*es*es;
            C = 7.0*e6/120.0 + 81.0*e8/1120.0;
            D = 4279.0*e8/161280.0;
            A = es/2.0 + 5.0*es*es/24.0 + e6/12.0 + 13.0*e8/360.0 - C;
            B = 2.0*(7.0*es*es/48.0 + 29.0*e6/240.0 + 811.0*e8/11520.0) - 4.0*D;
            C *= 4.0;
            D *= 8.0;
            
            if (Math.abs(latitudeTrueScale-(Math.PI/2)) >= EPS) {
                final double t = Math.sin(latitudeTrueScale);
                k0_series = msfn(t,Math.cos(latitudeTrueScale))* Math.sqrt(Math.pow(1+e, 1+e)*Math.pow(1-e, 1-e)) /
                (2.0*tsfn(latitudeTrueScale, t));
            } else {
                k0_series = 1.0;
            }
        } else {
            A = Double.NaN;
            B = Double.NaN;
            C = Double.NaN;
            D = Double.NaN;
            k0_series = Double.NaN;
        }
       
    }

     /**
     * Returns a human readable name localized for the specified locale.
     */
    public String getName(final Locale locale) {
        return Resources.getResources(locale).getString(ResourceKeys.CYLINDRICAL_MERCATOR_PROJECTION);
    }
    
    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate (units in radians)
     * and stores the result in <code>ptDst</code> (units in meters).
     */
    protected Point2D transform(double x, double y, final Point2D ptDst)
            throws ProjectionException
    {
        x = ensureInRange(x-centralMeridian);
        final double coslat = Math.cos(y);
        final double sinlat = Math.sin(y);
        final double coslon = Math.cos(x);
        final double sinlon = Math.sin(x);

        switch (mode) {
            default: {
                // Should not happen.
                throw new AssertionError(mode);
            }

            case NORTH: {
                final double rho = ak0 * tsfn(y, sinlat);
                x =  rho * sinlon;
                y = -rho * coslon;
                break;
            }
            case SOUTH: {
                final double rho = ak0 * tsfn(-y, -sinlat);
                x = rho * sinlon;
                y = rho * coslon;
                break;
            }
            case EQUATORIAL: {
                final double chi = 2.0 * Math.atan(ssfn(y, sinlat)) - (Math.PI/2);
                final double sinChi = Math.sin(chi);
                final double cosChi = Math.cos(chi);
                final double A = ak0 / (1.0 + cosChi*coslon);    //typo in (12-29)
                x = A * cosChi*sinlon;
                y = A * sinChi;
                break;
            }
            case OBLIQUE: {
                final double chi = 2.0 * Math.atan(ssfn(y, sinlat)) - (Math.PI/2);
                final double sinChi = Math.sin(chi);
                final double cosChi = Math.cos(chi);
                final double cosChi_coslon = cosChi*coslon;
                final double A = ak0 / cosChi1 / (1 + sinChi1*sinChi + cosChi1*cosChi_coslon);
                x = A * cosChi*sinlon;
                y = A * (cosChi1*sinChi - sinChi1*cosChi_coslon);
                break;
            }                
        }
        
        x += falseEasting;
        y += falseNorthing; 
        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }
    
    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate
     * and stores the result in <code>ptDst</code>.
     */
    protected Point2D inverseTransform(double x, double y, final Point2D ptDst)
            throws ProjectionException
    {
        x = (x-falseEasting)/a;    
        y = (y-falseNorthing)/a;
        final double rho = Math.sqrt(x*x + y*y);

choice: switch (mode) {
            default: {
                // Should not happen.
                throw new AssertionError(mode);
            }
            case SOUTH: {
                y = -y;
                // fallthrough
            }
            case NORTH: {
                if (OPTIMIZE_FOR_SPEED) {
                    //the series form                    
                    final double t = (rho/k0_series) * Math.sqrt(Math.pow(1+e, 1+e)*Math.pow(1-e, 1-e)) / 2;
                    final double chi = Math.PI/2 - 2*Math.atan(t);

                    x = (Math.abs(rho)<TOL) ? centralMeridian :
                            Math.atan2(x, -y) + centralMeridian;
                        
                    //See Snyde P. 19, "Computation of Series"               
                    final double sin2chi = Math.sin(2.0*chi);
                    final double cos2chi = Math.cos(2.0*chi);
                    y = chi + sin2chi*(A + cos2chi*(B + cos2chi*(C + D*cos2chi)));
                    y = (mode==NORTH) ? y : -y;
                } else {
                    //uses itteration
                    final double t = rho/k0;
                    x = (Math.abs(rho)<TOL) ? centralMeridian :
                            Math.atan2(x, -y) + centralMeridian;
                    double phi = cphi2(t);  // solves (7-9)
                    y = (mode==NORTH) ? phi : -phi;
                }         
                break;    
            }
            case OBLIQUE: {
                // fallthrough
            }
            case EQUATORIAL: {
                final double ce = 2.0 * Math.atan2(rho*cosChi1, k0);
                final double cosce = Math.cos(ce);
                final double since = Math.sin(ce);
                final double chi = (Math.abs(rho)>=TOL) ? Math.asin(cosce*sinChi1 + (y*since*cosChi1 / rho)) : chi1;
                final double t = Math.tan(Math.PI/4.0 + chi/2.0);
                /*
                 * Compute lat using iterative technique. (3-4)
                 */
                final double halfe = e/2.0;
                double phi0=chi;
                for (int i=MAX_ITER; --i>=0;) {
                    final double esinphi = e*Math.sin(phi0);
                    final double phi = 2.0 * Math.atan(t*Math.pow((1+esinphi)/(1-esinphi), halfe)) - (Math.PI/2);
                    if (Math.abs(phi-phi0) < TOL) {
                        x = (Math.abs(rho)<TOL) ? centralMeridian :
                            Math.atan2(x*since, rho*cosChi1*cosce - y*sinChi1*since) + centralMeridian;
                        y = phi;
                        break choice;
                    }
                    phi0 = phi;
                }
                throw new ProjectionException(Resources.format(ResourceKeys.ERROR_NO_CONVERGENCE));
            }
        }

        x = ensureInRange(x);
        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }
    
    /*
     * Provides the transform equations for the ObliqueEPSG case. Uses 
     * the equations from the 'EPSG Guidence Note Number 7'. Note that the inverse
     * fails (calculates longitude - 180 degs) for coordinates greater that 90 
     * degrees from the central meridian. Also warnings from the EPSG about 
     * projections centred in the southern hemisphere are not clear and require 
     * further examination.
     *
     * @version $Id: Stereographic.java,v 1.2 2003/07/28 09:43:02 desruisseaux Exp $
     * @author Rueben Schulz
     */
    private static final class ObliqueEPSG extends Stereographic {
        
        /**
         * Constants used for the Oblique EPSG
         */
        private final double R, n, c, chi0, sinchi0, coschi0;
        
        /*
         * Constant used in the oblique transform. Equal to
         * <code>2*R*scaleFactor</code>.
         */
        private final double k0;
                
        /**
         * Construct a new map projection from the suplied parameters.
         *
         * @param  parameters The parameter values in standard units.
         * @param  sp2 Indicates if this is a 1 or 2 standard parallel case of the mercator projection.
         * @throws MissingParameterException if a mandatory parameter is missing.
         */
        protected ObliqueEPSG (final Projection parameters,
                            final boolean polar,
                            final boolean auto) 
                throws MissingParameterException 
        {
            super(parameters, polar, auto, true);
            
            //compute constants
            switch (mode) {
                default: {
                    // Should not happen.
                    throw new AssertionError(mode);
                }
                
                case NORTH:
                case SOUTH: {
                    throw new AssertionError(mode);
                }
                
                case OBLIQUE:
                case EQUATORIAL: {
                    final double p0 = semiMajor*(1 - es) / Math.pow(1 - es*sinphi0*sinphi0,1.5);
                    final double v0 = semiMajor / Math.sqrt(1 - es*sinphi0*sinphi0);
                    R = Math.sqrt(p0*v0);
                    n = Math.sqrt(1 + (es*Math.pow(cosphi0,4)) / (1 - es));
                    
                    final double w1 = Math.pow(((1 + sinphi0) / (1 - sinphi0)) *
                    Math.pow((1-e*sinphi0) / (1 + e*sinphi0),e), n);
                    final double sinChi0 = (w1 - 1) / (w1 + 1);
                    c = (n + sinphi0) * (1 - sinChi0) / ((n - sinphi0) * (1 + sinChi0));
                    
                    final double w2 = c*w1;
                    chi0 = Math.asin((w2 - 1) / (w2 + 1));
                    sinchi0 = Math.sin(chi0);
                    coschi0 = Math.cos(chi0);

                    k0 = 2*R*scaleFactor;
                    break;
                }
            }

	}
        
        /**
	 * Transforms the specified (<var>x</var>,<var>y</var>) coordinate
         * and stores the result in <code>ptDst</code> using equations for a Sphere.
	 */
        protected Point2D transform(double x, double y, Point2D ptDst)
                throws ProjectionException
        {
            x = ensureInRange(x-centralMeridian);
            switch (mode) {
                default: {
                    // Should not happen.
                    throw new AssertionError(mode);
                }

                case NORTH: 
                case SOUTH: { 
                    throw new AssertionError(mode);
                }
            
                case OBLIQUE: 
                case EQUATORIAL: {
                    
                    final double sinlat = Math.sin(y);
                    final double w = c* Math.pow(((1 + sinlat) / (1 - sinlat)) * 
                                             Math.pow((1-e*sinlat) / (1 + e*sinlat),e), n);
                    final double lambda = n*x;
                    final double chi = Math.asin((w-1) / (w+1));
                    final double sinchi = Math.sin(chi);
                    final double coschi = Math.cos(chi);
                    final double coslambda = Math.cos(lambda);
                    final double B = 1 + sinchi*sinchi0 + coschi*coschi0*coslambda;

                    x = k0*coschi*Math.sin(lambda) / B;
                    y = k0*(sinchi*coschi0 - coschi*sinchi0*coslambda) / B;
                    
                    break;
                }
            }
            
            x += falseEasting;
            y += falseNorthing;

            if (ptDst != null) {
                ptDst.setLocation(x,y);
                return ptDst;
            }
            return new Point2D.Double(x,y);
        }

        /**
         * Transforms the specified (<var>x</var>,<var>y</var>) coordinate
         * and stores the result in <code>ptDst</code> using equations for a sphere.
         */
        protected Point2D inverseTransform(double x, double y, Point2D ptDst)
                throws ProjectionException
        {
            x = (x-falseEasting);    
            y = (y-falseNorthing);
            
choice:     switch (mode) {
                default: {
                    // Should not happen.
                    throw new AssertionError(mode);
                }

                case SOUTH:
                case NORTH: { 
                    throw new AssertionError(mode);
                }
            
                case OBLIQUE: 
                    //fallthrough
                case EQUATORIAL: {
                    final double g = k0*Math.tan(0.5*(Math.PI/2 - chi0));
                    final double h = 2*k0*Math.tan(chi0) + g;
                    final double i = Math.atan(x/(h + y));
                    final double j = Math.atan(x/(g - y)) - i;
                    final double chi = chi0 + 2*Math.atan((y - x*Math.tan(j/2)) / (2*R*scaleFactor));
                    final double lambda = j + 2*i;   //geodetic longitude
                    
                    final double sinchi = Math.sin(chi);
                    final double psi0 = 0.5 * Math.log((1+sinchi)/(c*(1-sinchi)))/n;  //isometric latitude
                    double phi0 = 2*Math.atan(Math.exp(psi0)) - Math.PI/2;
                    
                    for (int count=MAX_ITER; --count >= 0;) {
                        final double esinphi = e*Math.sin(phi0);
                        final double psi = Math.log(Math.tan(phi0/2 + Math.PI/4)*
                                                    Math.pow((1-esinphi)/(1+esinphi),0.5*e));
                        final double phi = phi0 - (psi - psi0) * Math.cos(phi0)*(1-esinphi*esinphi)/(1-es);
                        if (Math.abs(phi-phi0) < TOL) {
                            x = lambda/n + centralMeridian;
                            y = phi;
                            break choice;
                        }
                        phi0 = phi;
                    }
                    throw new ProjectionException(Resources.format(ResourceKeys.ERROR_NO_CONVERGENCE));
                }
            }

            x = ensureInRange(x);
            if (ptDst != null) {
                ptDst.setLocation(x,y);
                return ptDst;
            }
            return new Point2D.Double(x,y);
	}     
    }

    
    /*
     * Provides the transform equations for the spherical case of the stereographic projection.
     *
     * @version $Id: Stereographic.java,v 1.2 2003/07/28 09:43:02 desruisseaux Exp $
     * @author Martin Desruisseaux
     * @author Rueben Schulz
     */
    private static final class Spherical extends Stereographic {
        /**
         * A constant used in the transformations. This constant hide the <code>k0</code>
         * constant for the ellipsoidal case. The spherical and ellipsoidal <code>k0</code>
         * are not computed in the same way, and we preserve the ellipsoidal <code>k0</code>
         * in {@link Stereographic} in order to allow assertions to work.
         */
        private final double k0;

        /**
         * <code>a*k0</code>. This constant hide the <code>ak0</code> constant for the
         * ellipsoidal case.
         */
        private final double ak0;
        
        /**
         * Construct a new map projection from the suplied parameters.
         *
         * @param  parameters The parameter values in standard units.
         * @param  sp2 Indicates if this is a 1 or 2 standard parallel case of the mercator projection.
         * @throws MissingParameterException if a mandatory parameter is missing.
         */
        protected Spherical(final Projection parameters,
                            final boolean polar,
                            final boolean auto) 
                throws MissingParameterException 
        {
            super(parameters, polar, auto, false);
            assert isSpherical;
            
            switch (mode) {
                default: {
                    // Should not happen.
                    throw new AssertionError(mode);
                }
                case NORTH:
                case SOUTH: {
                    if (Math.abs(latitudeTrueScale - (Math.PI/2)) >= EPS) {
                        k0 = 1 + Math.sin(latitudeTrueScale);     //derived from (21-7)
                    } else {
                        k0 = 2;
                    }
                    break;
                }
                case OBLIQUE:
                case EQUATORIAL: {
                    k0 = 2;
                    break;
                }
            }
            this.ak0 = k0 * a;    
	}

	/**
	 * Transforms the specified (<var>x</var>,<var>y</var>) coordinate
         * and stores the result in <code>ptDst</code> using equations for a Sphere.
	 */
        protected Point2D transform(double x, double y, Point2D ptDst)
                throws ProjectionException
        {

            //Compute using ellipsoidal formulas, for comparaison later.
            assert (ptDst = super.transform(x, y, ptDst)) != null;

            x = ensureInRange(x-centralMeridian);
            final double coslat = Math.cos(y);
            final double sinlat = Math.sin(y);
            final double coslon = Math.cos(x);
            final double sinlon = Math.sin(x);

            switch (mode) {
                default: {
                    // Should not happen.
                    throw new AssertionError(mode);
                }
                case NORTH: {
                    if (!(Math.abs(1+sinlat) >= TOL)) {
                        throw new ProjectionException(Resources.format(
                        ResourceKeys.ERROR_VALUE_TEND_TOWARD_INFINITY));
                    }
                    // (21-8)
                    final double f= ak0 * coslat / (1+sinlat);// == tan (pi/4 - phi/2)
                    x =  f * sinlon; // (21-5)
                    y = -f * coslon; // (21-6)
                    break;
                }
                case SOUTH: {
                    if (!(Math.abs(1-sinlat) >= TOL)) {
                        throw new ProjectionException(Resources.format(
                        ResourceKeys.ERROR_VALUE_TEND_TOWARD_INFINITY));
                    }
                    // (21-12)
                    final double f= ak0 * coslat / (1-sinlat);// == tan (pi/4 + phi/2)
                    x = f * sinlon; // (21-9)
                    y = f * coslon; // (21-10)
                    break;
                }
                case EQUATORIAL: {
                    double f = 1.0 + coslat*coslon;
                    if (!(f >= TOL)) {
                        throw new ProjectionException(Resources.format(
                        ResourceKeys.ERROR_VALUE_TEND_TOWARD_INFINITY));
                    }
                    f = ak0/f;  // (21-14)
                    x = f * coslat * sinlon;  // (21-2)
                    y = f * sinlat;           // (21-13)
                    break;
                }
                case OBLIQUE: {
                    double f = 1.0 + sinphi0*sinlat + cosphi0*coslat*coslon; // (21-4)
                    if (!(f >= TOL)) {
                        throw new ProjectionException(Resources.format(
                        ResourceKeys.ERROR_VALUE_TEND_TOWARD_INFINITY));
                    }
                    f = ak0/f;
                    x = f * coslat * sinlon;                               // (21-2)
                    y = f * (cosphi0 * sinlat - sinphi0 * coslat * coslon);// (21-3)
                    break;
                }
            }
            x += falseEasting;
            y += falseNorthing;
            
            assert Math.abs(ptDst.getX()-x)/a <= EPS : x;
            assert Math.abs(ptDst.getY()-y)/a <= EPS : y;
            if (ptDst != null) {
                ptDst.setLocation(x,y);
                return ptDst;
            }
            return new Point2D.Double(x,y);
        }

        /**
         * Transforms the specified (<var>x</var>,<var>y</var>) coordinate
         * and stores the result in <code>ptDst</code> using equations for a sphere.
         */
        protected Point2D inverseTransform(double x, double y, Point2D ptDst)
                throws ProjectionException
        {
            // Compute using ellipsoidal formulas, for comparaison later.
            assert (ptDst = super.inverseTransform(x, y, ptDst)) != null;

            x = (x-falseEasting)/a;
            y = (y-falseNorthing)/a;
            final double rho = Math.sqrt(x*x + y*y);
            
            switch (mode) {
                default: {
                    // Should not happen.
                    throw new AssertionError(mode);
                }
                case NORTH: {
                    y = -y;
                    // fallthrough
                }
                case SOUTH: {
                    // (20-17) call atan2(x,y) to properly deal with y==0
                    x = (Math.abs(x)<TOL && Math.abs(y)<TOL) ? centralMeridian :
                        Math.atan2(x, y) + centralMeridian;
                    if (Math.abs(rho)<TOL) {
                        y = latitudeOfOrigin;
                    } else {
                        final double c = 2.0 * Math.atan(rho/k0);
                        final double cosc = Math.cos(c);
                        y = (mode==NORTH) ? Math.asin(cosc) : Math.asin(-cosc);
                        // (20-14) with phi1=90
                    }
                    break;
                }
                case EQUATORIAL: {
                    if (Math.abs(rho)<TOL) {
                        y = 0.0;
                        x = centralMeridian;
                    } else {
                        final double c = 2.0 * Math.atan(rho/k0);
                        final double cosc = Math.cos(c);
                        final double sinc = Math.sin(c);
                        y = Math.asin(y * sinc/rho); // (20-14)  with phi1=0
                        final double t  = x*sinc;
                        final double ct = rho*cosc;
                        x = (Math.abs(t)<TOL && Math.abs(ct)<TOL) ? centralMeridian :
                            Math.atan2(t, ct) + centralMeridian;
                    }
                    break;
                }
                case OBLIQUE: {
                    if (Math.abs(rho) < TOL) {
                        y = latitudeOfOrigin;
                        x = centralMeridian;
                    } else {
                        final double c = 2.0 * Math.atan(rho/k0);
                        final double cosc = Math.cos(c);
                        final double sinc = Math.sin(c);
                        final double ct = rho*cosphi0*cosc - y*sinphi0*sinc; // (20-15)
                        final double t  = x*sinc;
                        y = Math.asin(cosc*sinphi0 + y*sinc*cosphi0/rho); // (20-14)
                        x = (Math.abs(ct)<TOL && Math.abs(t)<TOL) ? centralMeridian :
                            Math.atan2(t, ct) + centralMeridian;
                    }
                    break;
                }
            }
 
            x = ensureInRange(x);
            assert Math.abs(ptDst.getX()-x) <= EPS : x;
            assert Math.abs(ptDst.getY()-y) <= EPS : y;
            if (ptDst != null) {
                ptDst.setLocation(x,y);
                return ptDst;
            }
            return new Point2D.Double(x,y);
	}
    }
    
    /**
     * Compute part of function (3-1) from Snyder
     */
    private double ssfn(double phi, double sinphi) {
        sinphi *= e;
        return Math.tan((Math.PI/4.0) + phi/2.0) *
               Math.pow((1-sinphi) / (1+sinphi), e/2.0);
    }
    
    /**
     * Construct a string version of this projection.
     */
    void toString(final StringBuffer buffer) {
        super.toString(buffer);
        if (mode == NORTH || mode == SOUTH) {
            addParameter(buffer, "latitude_true_scale", Math.toDegrees(latitudeTrueScale));
        }
    }
     
    /**
     * Returns a hash value for this map projection.
     */
    public int hashCode() {
        final long code = Double.doubleToLongBits(k0);
        return ((int)code ^ (int)(code >>> 32)) + 37*super.hashCode();
    }
    
    /**
     * Compares the specified object with
     * this map projection for equality.
     */
    public boolean equals(final Object object) {
        if (object==this) {
            // Slight optimization
            return true;
        }
        if (super.equals(object)) {
            final Stereographic that = (Stereographic) object;
            return Double.doubleToLongBits(this.     k0) == Double.doubleToLongBits(that.     k0) &&
                   Double.doubleToLongBits(this.    ak0) == Double.doubleToLongBits(that.    ak0) &&
                   Double.doubleToLongBits(this.sinphi0) == Double.doubleToLongBits(that.sinphi0) &&
                   Double.doubleToLongBits(this.cosphi0) == Double.doubleToLongBits(that.cosphi0) &&
                   Double.doubleToLongBits(this.   chi1) == Double.doubleToLongBits(that.   chi1) &&
                   Double.doubleToLongBits(this.sinChi1) == Double.doubleToLongBits(that.sinChi1) &&
                   Double.doubleToLongBits(this.cosChi1) == Double.doubleToLongBits(that.cosChi1) &&
                   this.EPSG == that.EPSG &&
                   this.OPTIMIZE_FOR_SPEED == that.OPTIMIZE_FOR_SPEED;
        }
        return false;
    }
    

}
