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

// J2SE dependencies
import java.util.Locale;
import java.awt.geom.Point2D;

// Geotools dependencies
import org.geotools.cs.Projection;
import org.geotools.cs.Ellipsoid;
import org.geotools.pt.Latitude;

// Resources
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Projection conique conforme de Lambert. Les aires et les formes sont déformées
 * à mesure que l'on s'éloigne de parallèles standards. Les angles sont vrais dans
 * une région limitée. Cette projection est utilisée pour les cartes de l'Amérique
 * du Nord. Elle utilise par défaut une latitude centrale de 40°N.
 * <br><br>
 *
 * Référence: John P. Snyder (Map Projections - A Working Manual,
 *            U.S. Geological Survey Professional Paper 1395, 1987)
 *
 * @version $Id: LambertConformalProjection.java,v 1.4 2003/04/18 15:22:34 desruisseaux Exp $
 * @author André Gosselin
 * @author Martin Desruisseaux
 */
final class LambertConformalProjection extends ConicProjection {
    /**
     * Standards parallels in radians, for
     * {@link #toString} implementation.
     */
    private final double phi1, phi2;
    
    /**
     * The semi-major axis length time the scale factor.
     */
    private final double a;
    
    /**
     * Internal variables for computation.
     */
    private final double n,F,rho0;
    
    /**
     * Construct a new map projection from the suplied parameters.
     *
     * @param  parameters The parameter values in standard units.
     * @throws MissingParameterException if a mandatory parameter is missing.
     */
    protected LambertConformalProjection(final Projection parameters)
        throws MissingParameterException
    {
        this(parameters, true);
    }

    /**
     * Construct an 1SP or 2SP projection.
     */
    LambertConformalProjection(final Projection parameters, final boolean sp2)
        throws MissingParameterException
    {
        //////////////////////////
        //   Fetch parameters   //
        //////////////////////////
        super(parameters);
        if (sp2) {
            phi1= latitudeToRadians(parameters.getValue("standard_parallel1", 30), true);
            phi2= latitudeToRadians(parameters.getValue("standard_parallel2", 45), true);
        } else {
            // TODO: what should be the values for 1SP?
            phi1 = latitudeToRadians(parameters.getValue("standard_parallel2", 49.5), true);
            phi2 = phi1;
        }
        
        //////////////////////////
        //  Compute constants   //
        //////////////////////////
        this.a = semiMajor * scaleFactor;
        if (Math.abs(phi1 + phi2) < EPS) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_ANTIPODE_LATITUDES_$2,
                    new Latitude(Math.toDegrees(phi1)),
                    new Latitude(Math.toDegrees(phi2))));
        }
        final double  cosphi = Math.cos(phi1);
        final double  sinphi = Math.sin(phi1);
        final boolean secant = Math.abs(phi1-phi2) > EPS;
        if (isSpherical) {
            if (secant) {
                n = Math.log(cosphi / Math.cos(phi2)) /
                Math.log(Math.tan((Math.PI/4) + 0.5*phi2) /
                Math.tan((Math.PI/4) + 0.5 * phi1));
            } else {
                n = sinphi;
            }
            F = cosphi * Math.pow(Math.tan((Math.PI/4) + 0.5*phi1), n) / n;
            if (Math.abs(Math.abs(centralLatitude) - (Math.PI/2)) >= EPS) {
                rho0 = F * Math.pow(Math.tan((Math.PI/4) + 0.5*centralLatitude), -n);
            } else {
                rho0 = 0.0;
            }
        } else {
            final double m1 = msfn(sinphi, cosphi);
            final double t1 = tsfn(phi1, sinphi);
            if (secant) {
                final double sinphi2 = Math.sin(phi2);
                final double m2 = msfn(sinphi2, Math.cos(phi2));
                final double t2 = tsfn(phi2, sinphi2);
                n = Math.log(m1/m2) / Math.log(t1/t2);
            } else {
                n = sinphi;
            }
            F = m1 * Math.pow(t1, -n) / n;
            if (Math.abs(Math.abs(centralLatitude) - (Math.PI/2)) >= EPS) {
                rho0 = F * Math.pow(tsfn(centralLatitude, Math.sin(centralLatitude)), n);
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
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate
     * and stores the result in <code>ptDst</code>.
     */
    protected Point2D transform(double x, double y, final Point2D ptDst)
        throws TransformException
    {
        double rho;
        if (Math.abs(Math.abs(y) - (Math.PI/2)) < EPS) {
            if (y*n <= 0) {
                throw new TransformException(Resources.format(
                        ResourceKeys.ERROR_POLE_PROJECTION_$1,
                        new Latitude(Math.toDegrees(y))));
            } else {
                rho = 0;
            }
        } else {
            if (isSpherical) {
                rho = Math.pow(Math.tan((Math.PI/4) + 0.5*y), -n);
            } else {
                rho = Math.pow(tsfn(y, Math.sin(y)), n);
            }
            rho *= F;
        }
        x = n * (x-centralMeridian);
        y = a * (rho0 - rho * Math.cos(x)) + falseNorthing;
        x = a * (       rho * Math.sin(x)) + falseEasting;
        
        if (ptDst!=null) {
            ptDst.setLocation(x,y);
            return ptDst;
        } else {
            return new Point2D.Double(x,y);
        }
    }
    
    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate
     * and stores the result in <code>ptDst</code>.
     */
    protected Point2D inverseTransform(double x, double y, final Point2D ptDst)
        throws TransformException
    {
        x =        (x-falseEasting)/a;
        y = rho0 - (y-falseNorthing)/a;
        double rho = Math.sqrt(x*x + y*y);
        if (rho > EPS) {
            if (n < 0) {
                rho = -rho;
                x = -x;
                y = -y;
            }
            x = centralMeridian + Math.atan2(x, y)/n;
            if (isSpherical) {
                y = 2.0 * Math.atan(Math.pow(F/rho, 1.0/n)) - (Math.PI/2);
            } else {
                y = cphi2(Math.pow(rho/F, 1.0/n));
            }
        } else {
            x = centralMeridian;
            y = n < 0 ? -(Math.PI/2) : (Math.PI/2);
        }
        if (ptDst!=null) {
            ptDst.setLocation(x,y);
            return ptDst;
        } else {
            return new Point2D.Double(x,y);
        }
    }
    
    /**
     * Returns a hash value for this projection.
     */
    public int hashCode() {
        final long code = Double.doubleToLongBits(F);
        return ((int)code ^ (int)(code >>> 32)) + 37*super.hashCode();
    }
    
    /**
     * Compares the specified object with
     * this map projection for equality.
     */
    public boolean equals(final Object object) {
        if (object==this) return true; // Slight optimization
        if (super.equals(object)) {
            final LambertConformalProjection that = (LambertConformalProjection) object;
            return Double.doubleToLongBits(this.n)    == Double.doubleToLongBits(that.n) &&
                   Double.doubleToLongBits(this.F)    == Double.doubleToLongBits(that.F) &&
                   Double.doubleToLongBits(this.rho0) == Double.doubleToLongBits(that.rho0);
        }
        return false;
    }
    
    /**
     * Complete the WKT for this map projection.
     */
    void toString(final StringBuffer buffer) {
        super.toString(buffer);
        addParameter(buffer, "standard_parallel1", Math.toDegrees(phi1));
        addParameter(buffer, "standard_parallel2", Math.toDegrees(phi2));
    }
    
    /**
     * Informations about a {@link LambertConformalProjection}.
     *
     * @version $Id: LambertConformalProjection.java,v 1.4 2003/04/18 15:22:34 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    static final class Provider extends MapProjection.Provider {
        /**
         * <code>true</code> for 2SP, or <code>false</code> for 1SP projection.
         */
        private final boolean sp2;

        /**
         * Construct a new provider.
         *
         * @param sp2 <code>true</code> for 2SP, or <code>false</code> for 1SP.
         * @param <code>true</code> for using OGC name, or <code>false</code> for
         *        inverting "Conformal" and "Conic" order. The later is used by EPSG.
         */
        public Provider(final boolean sp2, final boolean ogc) {
            super(ogc ?
                  (sp2 ? "Lambert_Conformal_Conic_2SP" : "Lambert_Conformal_Conic_1SP") :
                  (sp2 ? "Lambert_Conic_Conformal_2SP" : "Lambert_Conic_Conformal_1SP"),
                  ResourceKeys.LAMBERT_CONFORMAL_PROJECTION);
            if (sp2) {
                remove("scale_factor");
                put("standard_parallel1", 30.0, LATITUDE_RANGE);
                put("standard_parallel2", 45.0, LATITUDE_RANGE);
            }
            this.sp2 = sp2;
        }
        
        /**
         * Create a new map projection.
         */
        protected MathTransform create(final Projection parameters) throws MissingParameterException {
            if (!sp2) {
                java.util.logging.Logger.getLogger("org.geotools.ct")
                    .warning("Lambert_Conformal_Conic_1SP is broken in current implementation. "+
                             "DO NOT RELY ON IT.");
            }
            return new LambertConformalProjection(parameters, sp2);
        }
    }
}
