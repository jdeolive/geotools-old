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
import org.geotools.cs.Projection;
import org.geotools.cs.Ellipsoid;
import org.geotools.pt.Latitude;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MissingParameterException;

// Resources
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Lambert Conical Conformal Projection.  Areas and shapes are deformed
 * as one moves away from standard parallels.  The angles are true in
 * a limited area.  This projection is used for the charts of North America.
 * It uses a default central latitude of 40°N.
 * <br><br>
 *
 * This implementation provides transforms for three cases of the lambert conic 
 * conformal projection:
 * <ul>
 *   <li><code>Lambert_Conformal_Conic_1SP</code> (EPSG code 9801)</li>
 *   <li><code>Lambert_Conformal_Conic_2SP</code> (EPSG code 9802)</li>
 *   <li><code>Lambert_Conic_Conformal_2SP_Belgium</code> (EPSG code 9803)</li>
 * </ul>
 * For the 1SP case the latitude of origin is used as the standard parallel (SP). 
 * To use a 1SP with a latitude of origin different from the SP, use the 2SP
 * and set both the SP1 and SP2 to the single SP. 
 * <br><br>
 *
 * <strong>References:</strong><ul>
 *   <li>John P. Snyder (Map Projections - A Working Manual,<br>
 *       U.S. Geological Survey Professional Paper 1395, 1987)</li>
 *   <li>&quot;Coordinate Conversions and Transformations including Formulas&quot;,<br>
 *       EPSG Guidence Note Number 7, Version 19.</li>
 * </ul>
 *
 * @see <A HREF="http://mathworld.wolfram.com/LambertConformalConicProjection.html">Lambert conformal conic projection on MathWorld</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/lambert_conic_conformal_1sp.html">lambert_conic_conformal_1sp</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/lambert_conic_conformal_2sp.html">lambert_conic_conformal_2sp</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/lambert_conic_conformal_2sp_belgium.html">lambert_conic_conformal_2sp_belgium</A>
 *
 * @version $Id: LambertConformal.java,v 1.2 2003/08/04 13:53:16 desruisseaux Exp $
 * @author André Gosselin
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 */
public class LambertConformal extends ConicProjection {
    /** 
     * Constant for the belgium 2SP case. This is 29.2985 seconds, given here in radians.
     */
    private static final double BELGE_A = 0.00014204313635987700;

    /**
     * Standards parallels in radians, for {@link #toString} implementation.
     */
    protected final double phi1, phi2;
    
    /**
     * Global scale factor. Value <code>ak0</code> is equals
     * to {@link #semiMajor}&times;{@link #scaleFactor}.
     */
    protected final double ak0;

    /**
     * Internal variables for computation.
     */
    private final double n,F,rho0;
    
    /**
     * <code>true</code> for 2SP, or <code>false</code> for 1SP projection.
     */
    private final boolean sp2;
    
    /**
     * <code>true</code> for Belgium 2SP.
     */
    private final boolean belgium;
    
    /**
     * Informations about a {@link LambertConformalProjection}.
     *
     * @version $Id: LambertConformal.java,v 1.2 2003/08/04 13:53:16 desruisseaux Exp $
     * @author Martin Desruisseaux
     * @author Rueben Schulz
     */
    static final class Provider extends org.geotools.ct.proj.Provider {
        /**
         * <code>true</code> for 2SP, or <code>false</code> for 1SP projection.
         */
        private final boolean sp2;
        
        /**
         * <code>true</code> for Belgium 2SP
         */
        private final boolean belgium;
        
        /**
         * Construct a new provider.
         *
         * @param sp2 <code>true</code> for 2SP, or <code>false</code> for 1SP.
         * @param belgium <code>true</code> for the Belgium 2SP case.
         * @param <code>true</code> for using OGC name, or <code>false</code> for
         *        inverting "Conformal" and "Conic" order. The later is used by EPSG.
         */
        public Provider(final boolean sp2, final boolean belgium, final boolean ogc) {
            super(ogc ?
                    (sp2 ? 
                        (belgium ? "Lambert_Conformal_Conic_2SP_Belgium" :
                                   "Lambert_Conformal_Conic_2SP") :
                                   "Lambert_Conformal_Conic_1SP") :
                    (sp2 ? 
                        (belgium ? "Lambert_Conic_Conformal_2SP_Belgium" :
                                   "Lambert_Conic_Conformal_2SP") :
                                   "Lambert_Conic_Conformal_1SP"),
                ResourceKeys.LAMBERT_CONFORMAL_PROJECTION);
                if (sp2) {
                    remove("scale_factor");
                    put("standard_parallel1", 30.0, LATITUDE_RANGE);
                    put("standard_parallel2", 45.0, LATITUDE_RANGE);
                } else {
                    // Change the default value from 0 (which fails) to "no default value".
                    put("latitude_of_origin", Double.NaN, LATITUDE_RANGE);
                }
                this.sp2     = sp2;
                this.belgium = belgium;
        }
        
        /**
         * Create a new map projection.
         */
        public MathTransform create(final Projection parameters) throws MissingParameterException {
            return new LambertConformal(parameters, sp2, belgium);
        }
    }

    /**
     * Construct a new projection from the supplied parameters.
     *
     * @param  parameters The parameter values in standard units.
     * @throws MissingParameterException if a mandatory parameter is missing.
     */
    protected LambertConformal(final Projection parameters) throws MissingParameterException {
        this(parameters, contains(parameters, "2SP"), contains(parameters, "Belgium"));
    }
    
    /**
     * Construct an 1SP or 2SP projection.
     *
     * @param  parameters The parameter values in standard units.
     * @param  sp2 <code>true</code> for 2SP, or <code>false</code> for 1SP.
     * @param  belgium <code>true</code> for the Belgium 2SP case.
     * @throws MissingParameterException if a mandatory parameter is missing.
     */
    LambertConformal(final Projection parameters, final boolean sp2, final boolean belgium)
            throws MissingParameterException
    {
        // Fetch parameters
        super(parameters);
        this.sp2         = sp2;
        this.belgium     = belgium;
        if (sp2) {
            phi1 = latitudeToRadians(parameters.getValue("standard_parallel1", 30), true);
            phi2 = latitudeToRadians(parameters.getValue("standard_parallel2", 45), true);
        } else {
            if (belgium) {
                throw new IllegalArgumentException();
            }
            // EPSG says the 1sp case uses the latitude of origin as the SP
            phi1 = phi2 = latitudeOfOrigin;
        }
        // Compute constants
        this.ak0 = semiMajor * scaleFactor;
        if (Math.abs(phi1 + phi2) < EPS) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_ANTIPODE_LATITUDES_$2,
                    new Latitude(Math.toDegrees(phi1)),
                    new Latitude(Math.toDegrees(phi2))));
        }
        final double  cosphi1 = Math.cos(phi1);
        final double  sinphi1 = Math.sin(phi1);
        final boolean  secant = Math.abs(phi1-phi2) > EPS; // Should be 'true' for 2SP case.
        if (isSpherical) {
            if (secant) {
                n = Math.log(cosphi1 / Math.cos(phi2)) /
                    Math.log(Math.tan((Math.PI/4) + 0.5*phi2) /
                             Math.tan((Math.PI/4) + 0.5*phi1));
            } else {
                n = sinphi1;
            }
            F = cosphi1 * Math.pow(Math.tan((Math.PI/4) + 0.5*phi1), n) / n;
            if (Math.abs(Math.abs(latitudeOfOrigin) - (Math.PI/2)) >= EPS) {
                rho0 = F * Math.pow(Math.tan((Math.PI/4) + 0.5*latitudeOfOrigin), -n);
            } else {
                rho0 = 0.0;
            }
        } else {
            final double m1 = msfn(sinphi1, cosphi1);
            final double t1 = tsfn(phi1, sinphi1);
            if (secant) {
                final double sinphi2 = Math.sin(phi2);
                final double m2 = msfn(sinphi2, Math.cos(phi2));
                final double t2 = tsfn(phi2, sinphi2);
                n = Math.log(m1/m2) / Math.log(t1/t2);
            } else {
                n = sinphi1;
            }
            F = m1 * Math.pow(t1, -n) / n;
            if (Math.abs(Math.abs(latitudeOfOrigin) - (Math.PI/2)) >= EPS) {
                rho0 = F * Math.pow(tsfn(latitudeOfOrigin, Math.sin(latitudeOfOrigin)), n);
            } else {
                rho0 = 0.0;
            }
        } 
    }
    
    /**
     * Returns a human readable name localized for the specified locale.
     */
    public String getName(final Locale locale) {
        return Resources.getResources(locale).getString(ResourceKeys.LAMBERT_CONFORMAL_PROJECTION);
    }
    
    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate (units in radians)
     * and stores the result in <code>ptDst</code> (units in meters).
     */
    protected Point2D transform(double x, double y, final Point2D ptDst)
            throws ProjectionException
    {
        double rho;
        //Snyder p. 108
        if (Math.abs(Math.abs(y) - (Math.PI/2)) < EPS) {
            if (y*n <= 0) {
                throw new ProjectionException(Resources.format(
                        ResourceKeys.ERROR_POLE_PROJECTION_$1,
                        new Latitude(Math.toDegrees(y))));
            } else {
                rho = 0;
            }
        } else if (isSpherical) {
            rho = F * Math.pow(Math.tan((Math.PI/4) + 0.5*y), -n);
        } else {
            rho = F * Math.pow(tsfn(y, Math.sin(y)), n);
        }
        x = ensureInRange(x-centralMeridian);
        x *= n;
        if (belgium) {
            x -= BELGE_A;
        }
        y = ak0 * (rho0 - rho * Math.cos(x)) + falseNorthing;
        x = ak0 * (       rho * Math.sin(x)) + falseEasting;
        
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
        double theta;
        x =        (x-falseEasting)/ak0;
        y = rho0 - (y-falseNorthing)/ak0;
        double rho = Math.sqrt(x*x + y*y);  // Zero when the latitude ist 90 degrees.
        if (rho > EPS) {
            if (n < 0) {
                rho = -rho;
                x = -x;
                y = -y;
            }
            theta = Math.atan2(x, y);
            if (belgium) {
                theta += BELGE_A;
            }
            x = ensureInRange(centralMeridian + theta/n);
            if (isSpherical) {
                y = 2.0 * Math.atan(Math.pow(F/rho, 1.0/n)) - (Math.PI/2);
            } else {
                y = cphi2(Math.pow(rho/F, 1.0/n));
            }
        } else {
            x = centralMeridian;
            y = n < 0 ? -(Math.PI/2) : (Math.PI/2);
        }
        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }

    /**
     * Returns a hash value for this projection.
     */
    public int hashCode() {
        /*
         * This code should be computed fast. Consequently, we do not use all fields
         * in this object.  Two <code>LambertConformal</code> objects with different
         * {@link #phi1} and {@link #phi2} should compute a F value different enough.
         */
        final long code = Double.doubleToLongBits(F);
        return ((int)code ^ (int)(code >>> 32)) + 37*super.hashCode();
    }
    
    /**
     * Compares the specified object with this map projection for equality.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            // Slight optimization
            return true;
        }
        if (super.equals(object)) {
            final LambertConformal that = (LambertConformal) object;
            return (this.sp2 == that.sp2) && (this.belgium == that.belgium) &&
                   equals(this.n,      that.n)    &&
                   equals(this.F,      that.F)    &&
                   equals(this.rho0,   that.rho0) &&
                   equals(this.ak0,    that.ak0)  &&
                   equals(this.phi1,   that.phi1) &&
                   equals(this.phi2,   that.phi2);
        }
        return false;
    }
    
    /**
     * Complete the WKT for this map projection.
     */
    void toString(final StringBuffer buffer) {
        super.toString(buffer);
        if (sp2) {
            addParameter(buffer, "standard_parallel1", Math.toDegrees(phi1));
            addParameter(buffer, "standard_parallel2", Math.toDegrees(phi2));
        }
    }
}
