/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
/*
** Permission is hereby granted, free of charge, to any person obtaining
** a copy of this software and associated documentation files (the
** "Software"), to deal in the Software without restriction, including
** without limitation the rights to use, copy, modify, merge, publish,
** distribute, sublicense, and/or sell copies of the Software, and to
** permit persons to whom the Software is furnished to do so, subject to
** the following conditions:
**
** The above copyright notice and this permission notice shall be
** included in all copies or substantial portions of the Software.
**
** THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
** EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
** MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
** IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
** CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
** TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
** SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package org.geotools.ct.proj;

// J2SE dependencies
import java.util.Locale;
import java.awt.geom.Point2D;

// Geotools dependencies
import org.geotools.cs.Projection;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MissingParameterException;

// Resources
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Transverse Mercator Projection (EPSG code 9807). This
 * is a cylindrical projection, in which the cylinder has been rotated 90°.
 * Instead of being tangent to the equator (or to an other standard latitude),
 * it is tangent to a central meridian. Deformation are more important as we
 * are going futher from the central meridian. The Transverse Mercator
 * projection is appropriate for region wich have a greater extent north-south
 * than east-west.
 * <br><br>
 *
 * There are a number of versions of the transverse mercator projection 
 * including the Universal (UTM) and Modified (MTM) Transverses Mercator 
 * projections. In these cases the earth is divided into zones. For the UTM
 * the zones are 6 degrees wide, numbered from 1 to 60 proceeding east from 
 * 180 degrees longitude, and between lats 84 degrees North and 80 
 * degrees South. The central meridian is taken as the center of the zone
 * and the latitude of origin is the equator. A scale factor of 0.9996 and 
 * false easting of 500000m is used for all zones and a false northing of 10000000m
 * is used for zones in the southern hemisphere.
 * <br><br>
 *
 * NOTE: formulas used below are not those of Snyder, but rather those
 *       from the <code>proj</code> package of the USGS survey, which
 *       have been reproduced verbatim. USGS work is acknowledged here.
 * <br><br>
 *
 * <strong>References:</strong><ul>
 *   <li> Proj-4.4.6 available at <A HREF="http://www.remotesensing.org/proj">www.remotesensing.org/proj</A><br>
 *        Relevent files are: PJ_tmerc.c, pj_mlfn.c, pj_fwd.c and pj_inv.c </li>
 *   <li> John P. Snyder (Map Projections - A Working Manual,
 *        U.S. Geological Survey Professional Paper 1395, 1987)</li>
 *   <li> "Coordinate Conversions and Transformations including Formulas",
 *        EPSG Guidence Note Number 7, Version 19.</li>
 * </ul>
 *
 * @see <A HREF="http://mathworld.wolfram.com/MercatorProjection.html">Transverse Mercator projection on MathWorld</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/transverse_mercator.html">"Transverse_Mercator" on Remote Sensing</A>
 *
 * @version $Id: TransverseMercator.java,v 1.5 2004/01/11 16:49:31 desruisseaux Exp $
 * @author André Gosselin
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 */
public class TransverseMercator extends CylindricalProjection {
    /*
     * A derived quantity of excentricity, computed
     * by <code>e'² = (a²-b²)/b² = es/(1-es)</code>
     * where <var>a</var> is the semi-major axis length
     * and <var>b</bar> is the semi-minor axis length.
     */
    private final double esp;
    
    /*
     * meridian distance at the <code>latitudeOfOrigin</code>.
     * Used for calculations for the ellipsoid.
     */
    private final double ml0;

     /**
     * Constant needed for the <code>mlfn<code> method.
     * Setup at construction time.
     */
    private final double en0,en1,en2,en3,en4;
    
    /*
     * Constants used to calculate {@link #en0}, {@link #en1},
     * {@link #en2}, {@link #en3}, {@link #en4}.
     */
    private static final double C00= 1.0,
                                C02= 0.25,
                                C04= 0.046875,
                                C06= 0.01953125,
                                C08= 0.01068115234375,
                                C22= 0.75,
                                C44= 0.46875,
                                C46= 0.01302083333333333333,
                                C48= 0.00712076822916666666,
                                C66= 0.36458333333333333333,
                                C68= 0.00569661458333333333,
                                C88= 0.3076171875;
    /*
     * Contants used for the forward and inverse transform for the eliptical
     * case of the Transverse Mercator.
     */
    private static final double FC1= 1.00000000000000000000000,  // 1/1
                                FC2= 0.50000000000000000000000,  // 1/2
                                FC3= 0.16666666666666666666666,  // 1/6
                                FC4= 0.08333333333333333333333,  // 1/12
                                FC5= 0.05000000000000000000000,  // 1/20
                                FC6= 0.03333333333333333333333,  // 1/30
                                FC7= 0.02380952380952380952380,  // 1/42
                                FC8= 0.01785714285714285714285;  // 1/56

    /**
     * Relative precision used in the <code>mlfn<code> method.
     */
    private static final double EPS11 = 1E-11;
    
    /**
     * Informations about a {@link TransverseMercator}.
     *
     * @version $Id: TransverseMercator.java,v 1.5 2004/01/11 16:49:31 desruisseaux Exp $
     * @author Martin Desruisseaux
     * @author Rueben Schulz
     */
    static final class Provider extends org.geotools.ct.proj.Provider {
        /**
         * Constant for Universal Transverse Mercator projection (UTM).
         */
        public static final int UTM = 1;
        
        /**
         * Constant for Modified Transverse Mercator projection (MTM).
         */
        public static final int MTM = 2;
        
        /**
         * Provider that does not set any default values.
         */
        public Provider() {
            this(0);
        }
        
        /**
         * Construct a new registration.
         *
         * @param type The transform type, {@link UTM} or {@link MTM}.
         * @task REVISIT: Should the UTM case set the false_northing for the
         *                southern case?
         */
        public Provider(final int type) {
            super("Transverse_Mercator", ResourceKeys.TRANSVERSE_MERCATOR_PROJECTION);
            switch (type) {
                case UTM: {
                    put("false_easting", 500000.0, null);
                    put("scale_factor",  0.9996,   POSITIVE_RANGE);
                    break;
                }
                case MTM: {
                    put("false_easting", 304800.0, null);
                    put("scale_factor",  0.9999,   POSITIVE_RANGE);
                    break;
                }
            }
        }

        /**
         * Create a new map projection.
         */
        public MathTransform create(final Projection parameters) throws MissingParameterException {
            if (isSpherical(parameters)) {
                return new Spherical(parameters);
            } else {
                return new TransverseMercator(parameters);
            }
        }
    }

    
    /**
     * Construct a new map projection from the suplied parameters.
     *
     * @param  parameters The parameter values in standard units.
     * @throws MissingParameterException if a mandatory parameter is missing.
     */
    protected TransverseMercator(final Projection parameters) throws MissingParameterException {
        //  Fetch parameters
        super(parameters);
        
        //  Compute constants
        esp = es / (1.0 - es);
          
        double t;
        en0 = C00 - es * (C02 + es * (C04 + es * (C06 + es * C08)));
        en1 = es * (C22 - es * (C04 + es * (C06 + es * C08)));
        en2 = (t = es * es) * (C44 - es * (C46 + es * C48));
        en3 = (t *= es) * (C66 - es * C68);
        en4 = t * es * C88;
        ml0 = mlfn(latitudeOfOrigin, Math.sin(latitudeOfOrigin), Math.cos(latitudeOfOrigin));
    }
    
    /**
     * Returns a human readable name localized for the specified locale.
     */
    public String getName(final Locale locale) {
        return Resources.getResources(locale).getString(
               ResourceKeys.TRANSVERSE_MERCATOR_PROJECTION);
    }
    
    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate (units in radians)
     * and stores the result in <code>ptDst</code> (units in meters).
     */
    protected Point2D transformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException 
    {
        double sinphi = Math.sin(y);
        double cosphi = Math.cos(y);
        
        double t = Math.abs(cosphi)>TOL ? sinphi/cosphi : 0;
        t *= t;
        double al = cosphi*x;
        double als = al*al;
        al /= Math.sqrt(1.0 - es * sinphi*sinphi);
        double n = esp * cosphi*cosphi;

        /* NOTE: meridinal distance at latitudeOfOrigin is always 0 */
        y = (mlfn(y, sinphi, cosphi) - ml0 + 
            sinphi*al*x*
            FC2 * ( 1.0 +
            FC4 * als * (5.0 - t + n*(9.0 + 4.0*n) +
            FC6 * als * (61.0 + t * (t - 58.0) + n*(270.0 - 330.0*t) +
            FC8 * als * (1385.0 + t * ( t*(543.0 - t) - 3111.0))))));
        
        x = al*(FC1 + FC3 * als*(1.0 - t + n +
            FC5 * als * (5.0 + t*(t - 18.0) + n*(14.0 - 58.0*t) +
            FC7 * als * (61.0+ t*(t*(179.0 - t) - 479.0 )))));
               
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
    protected Point2D inverseTransformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException 
    {
        
        double phi = inv_mlfn(ml0 + y);
        
        if (Math.abs(phi) >= (Math.PI/2)) {
            y = y<0.0 ? -(Math.PI/2) : (Math.PI/2);
            x = 0.0;
        } else {
            double sinphi = Math.sin(phi);
            double cosphi = Math.cos(phi);
            double t = (Math.abs(cosphi) > TOL) ? sinphi/cosphi : 0.0;
            double n = esp * cosphi*cosphi;
            double con = 1.0 - es * sinphi*sinphi;
            double d = x*Math.sqrt(con);
            con *= t;
            t *= t;
            double ds = d*d;
            
            y = phi - (con*ds / (1.0 - es)) *
                FC2 * (1.0 - ds *
                FC4 * (5.0 + t*(3.0 - 9.0*n) + n*(1.0 - 4*n) - ds *
                FC6 * (61.0 + t*(90.0 - 252.0*n + 45.0*t) + 46.0*n - ds *
                FC8 * (1385.0 + t*(3633.0 + t*(4095.0 + 1574.0*t))))));
            
            x = d*(FC1 - ds * FC3 * (1.0 + 2.0*t + n -
                ds*FC5*(5.0 + t*(28.0 + 24* t + 8.0*n) + 6.0*n -
                ds*FC7*(61.0 + t*(662.0 + t*(1320.0 + 720.0*t))))))/cosphi;
        }
        
        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }
    
   
    /**
     * Provides the transform equations for the spherical case of the
     * TransverseMercator projection.
     *
     * @author André Gosselin
     * @author Martin Desruisseaux
     * @author Rueben Schulz
     */
    private static final class Spherical extends TransverseMercator {
        /**
         * Construct a new map projection from the suplied parameters.
         *
         * @param  parameters The parameter values in standard units.
         * @throws MissingParameterException if a mandatory parameter is missing.
         */
        protected Spherical(final Projection parameters) throws MissingParameterException {
            super(parameters);
            assert isSpherical;
        }
        
        /**
         * Transforms the specified (<var>x</var>,<var>y</var>) coordinate
         * and stores the result in <code>ptDst</code> using equations for a Sphere.
         */
        protected Point2D transformNormalized(double x, double y, Point2D ptDst)
                throws ProjectionException 
        {
            // Compute using ellipsoidal formulas, for comparaison later.
            assert (ptDst = super.transformNormalized(x, y, ptDst)) != null;
                       
            double cosphi = Math.cos(y);
            double b = cosphi * Math.sin(x);
            if (Math.abs(Math.abs(b) - 1.0) <= TOL) {
                throw new ProjectionException(Resources.format(
                ResourceKeys.ERROR_VALUE_TEND_TOWARD_INFINITY));
            }
            
            double yy = cosphi * Math.cos(x) / Math.sqrt(1.0-b*b);
            x = 0.5 * Math.log((1.0+b)/(1.0-b));    /* Snyder 8-1 */
            
            if ((b=Math.abs(yy)) >= 1.0) {
                if ((b-1.0) > TOL) {
                    throw new ProjectionException(Resources.format(
                        ResourceKeys.ERROR_VALUE_TEND_TOWARD_INFINITY));
                } else {
                    yy = 0.0;
                }
            } else {
                yy = Math.acos(yy);
            }
            if (y < 0) {
                yy = -yy;
            }
            y = (yy - latitudeOfOrigin);
          
            assert Math.abs(ptDst.getX()-x) <= EPS*globalScale : x;
            assert Math.abs(ptDst.getY()-y) <= EPS*globalScale : y;
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
        protected Point2D inverseTransformNormalized(double x, double y, Point2D ptDst)
                throws ProjectionException 
        {
            // Compute using ellipsoidal formulas, for comparaison later.
            assert (ptDst = super.inverseTransformNormalized(x, y, ptDst)) != null;
            
            double t = Math.exp(x);
            double d = 0.5 * (t-1.0/t);
            t = Math.cos(latitudeOfOrigin + y);
            double phi = Math.asin(Math.sqrt((1.0-t*t)/(1.0+d*d)));
            y = y<0.0 ? -phi : phi;
            x = (Math.abs(d)<=TOL && Math.abs(t)<=TOL) ? 
                    0.0 :
                    Math.atan2(d,t);

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
     * Calculates the meridian distance. This is the distance along the central 
     * meridian from the equator to <code>phi</code>. Accurate to < 1e-5 meters 
     * when used in conjuction with typical major axis values.
     *
     * @param phi latitude to calculate meridian distance for.
     * @param sphi sin(phi).
     * @param cphi cos(phi).
     * @return meridian distance for the given latitude.
     */
    private final double mlfn(final double phi, double sphi, double cphi) {        
        cphi *= sphi;
        sphi *= sphi;
        return en0 * phi - cphi *
              (en1 + sphi *
              (en2 + sphi *
              (en3 + sphi *
              (en4))));
    }
    
    /**
     * Calculates the latitude (<code>phi</code>) from a meridian distance.
     * Determines phi to EPS11 (1e-11) radians, about 1e-6 seconds.
     * 
     * @param arg meridian distance to calulate latitude for.
     * @return the latitude of the meridian distance.
     * @throws ProjectionException if the itteration does not converge.
     */
    private final double inv_mlfn(double arg) throws ProjectionException {
        double s, t, phi, k = 1.0/(1.0 - es);
	int i;
	phi = arg;
        for (i=10; true;) { // rarely goes over 5 iterations
            if (--i < 0) {
                throw new ProjectionException(Resources.format(
                            ResourceKeys.ERROR_NO_CONVERGENCE));
            }
            s = Math.sin(phi);
            t = 1.0 - es * s * s;
            t = (mlfn(phi, s, Math.cos(phi)) - arg) * (t * Math.sqrt(t)) * k;
            phi -= t;
            if (Math.abs(t) < EPS11) {
                return phi;
            }
	}
    }
    
    /**
     * Returns a hash value for this projection.
     */
    public int hashCode() { 
        final long code = Double.doubleToLongBits(ml0);
        return ((int)code ^ (int)(code >>> 32)) + 37*super.hashCode();
    }
    
    /**
     * Compares the specified object with
     * this map projection for equality.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            // Slight optimization
            return true;
        }
        // Relevant parameters are already compared in MapProjection
        return super.equals(object);
    }
    

    /**
     * Convenience method computing the zone code from the central meridian.
     * Information about zones convention must be specified in argument. Two
     * widely set of arguments are of Universal Transverse Mercator (UTM) and
     * Modified Transverse Mercator (MTM) projections:<br>
     * <br>
     *
     * UTM projection (zones numbered from 1 to 60):<br>
     * <br>
     *        <code>getZone(-177, 6);</code><br>
     * <br>
     * MTM projection (zones numbered from 1 to 120):<br>
     * <br>
     *        <code>getZone(-52.5, -3);</code><br>
     *
     * @param  centralLongitudeZone1 Longitude in the middle of zone 1, in degrees
     *         relative to Greenwich. Positive longitudes are toward east, and negative
     *         longitudes toward west.
     * @param  zoneWidth Number of degrees of longitudes in one zone. A positive value
     *         means that zones are numbered from west to east (i.e. in the direction of
     *         positive longitudes). A negative value means that zones are numbered from
     *         east to west.
     * @return The zone number. First zone is numbered 1.
     */
    private int getZone(final double centralLongitudeZone1, final double zoneWidth) {
        final double zoneCount = Math.abs(360/zoneWidth);
        double t;
        t  = centralLongitudeZone1 - 0.5*zoneWidth; // Longitude at the beginning of the first zone.
        t  = Math.toDegrees(centralMeridian) - t;   // Degrees of longitude between the central longitude and longitude 1.
        t  = Math.floor(t/zoneWidth + EPS);         // Number of zones between the central longitude and longitude 1.
        t -= zoneCount*Math.floor(t/zoneCount);     // If negative, bring back to the interval 0 to (zoneCount-1).
        return ((int) t)+1;
    }
    
    /**
     * Convenience method returning the meridian in the middle of
     * current zone. This meridian is typically the central meridian.
     * This method may be invoked to make sure that the central meridian
     * is correctly set.
     *
     * @param  centralLongitudeZone1 Longitude in the middle of zone 1, in degrees
     *         relative to Greenwich. Positive longitudes are toward east, and negative
     *         longitudes toward west.
     * @param  zoneWidth Number of degrees of longitudes in one zone. A positive value
     *         means that zones are numbered from west to east (i.e. in the direction of
     *         positive longitudes). A negative value means that zones are numbered from
     *         east to west.
     * @return The central meridian.
     */
    private double getCentralMedirian(final double centralLongitudeZone1, final double zoneWidth) {
        double t;
        t  = centralLongitudeZone1 + (getZone(centralLongitudeZone1, zoneWidth)-1)*zoneWidth;
        t -= 360*Math.floor((t+180)/360); // Bring back into [-180..+180] range.
        return t;
    }
    
    /**
     * Convenience method computing the zone code from the central meridian.
     *
     * @return The zone number, using the scalefactor and false easting 
     *         to decide if this is a UTM or MTM case. Returns 0 if the 
     *         case of the projection cannot be determined.
     */
    public int getZone() {
        //UTM
        if (scaleFactor == 0.9996 && falseEasting == 500000.0) {
            return(getZone(-177, 6));
        }
        //MTM
        if (scaleFactor == 0.9999 && falseEasting == 304800.0){
            return(getZone(-52.5, -3));
        }
        //unknown
        throw new IllegalStateException();
    }
    
    /**
     * Convenience method returning the meridian in the middle of
     * current zone. This meridian is typically the central meridian.
     * This method may be invoked to make sure that the central meridian
     * is correctly set.
     *
     * @return The central meridian, using the scalefactor and false easting 
     *         to decide if this is a UTM or MTM case. Returns Double.NaN if the 
     *         case of the projection cannot be determined.
     */
    public double getCentralMeridian() {
        //UTM
        if (scaleFactor == 0.9996 && falseEasting == 500000.0) {
            return(getCentralMedirian(-177, 6));
        }
        //MTM
        if (scaleFactor == 0.9999 && falseEasting == 304800.0){
            return(getCentralMedirian(-52.5, -3));
        }
        //unknown
        throw new IllegalStateException();
    }
}
