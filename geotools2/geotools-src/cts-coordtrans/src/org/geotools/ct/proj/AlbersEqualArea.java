/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
 *    This package contains formulas from the PROJ package of USGS.
 *    USGS's work is fully acknowledged here.
 */
/*
 *****************************************************************************
 * Copyright (c) 1995, Gerald Evenden
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 ******************************************************************************
 */
package org.geotools.ct.proj;

// J2SE dependencies
import java.util.Locale;
import java.awt.geom.Point2D;

// Geotools dependencies
import org.geotools.pt.Latitude;
import org.geotools.cs.Projection;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MissingParameterException;

// Resources
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Albers Equal Area Projection (EPSG code 9822). This is a conic projection
 * with parallels being unequally spaced arcs of concentric circles, more
 * closely spaced at north and south edges of the map. Merideans
 * are equally spaced radii of the same circles and intersect parallels at right 
 * angles. As the name implies, this projection minimizes distortion in areas.
 * <br><br>
 *
 * NOTE: formulae used below are from a port, to java, of the 
 *       'proj' package of the USGS survey. USGS work is acknowledged here.
 * <br><br>
 *
 * <strong>References:</strong><ul>
 *   <li> Proj-4.4.7 available at <A HREF="http://www.remotesensing.org/proj">www.remotesensing.org/proj</A><br>
 *        Relevent files are: PJ_aea.c, pj_fwd.c and pj_inv.c </li>
 *   <li> John P. Snyder (Map Projections - A Working Manual,
 *        U.S. Geological Survey Professional Paper 1395, 1987)</li>
 *   <li> "Coordinate Conversions and Transformations including Formulas",
 *        EPSG Guidence Note Number 7, Version 19.</li>
 * </ul>
 *
 * @see <A HREF="http://mathworld.wolfram.com/AlbersEqual-AreaConicProjection.html/">Albers Equal-Area Conic Projection on MathWorld</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/albers_equal_area_conic.html"> "Albers_Conic_Equal_Area" on www.remotesensing.org</A>
 * @see <A HREF="http://srmwww.gov.bc.ca/gis/bceprojection.html">British Columbia Albers Standard Projection</A>
 *
 * @version $Id: AlbersEqualArea.java,v 1.4 2004/02/23 12:28:22 desruisseaux Exp $
 * @author Rueben Schulz
 */
public class AlbersEqualArea extends ConicProjection {
    /**
     * Maximum difference allowed when comparing real numbers.
     */
    private static final double EPS = 1E-7;
    
    /**
     * Maximum number of itterations for the inverse calculation.
     */
    private static final int MAX_ITER = 15;
    
    /**
     * Constants used by the spherical and elliptical Albers projection. 
     */
    private final double n, c, rho0;
    
    /**
     * An error condition indicating itteration will not converge for the 
     * inverse ellipse. See Snyder (14-20)
     */
    private final double ec;
    
    /**
     * Standards parallels in radians, for {@link #toString} implementation.
     */
    private final double phi1, phi2;

    /**
     * Informations about a {@link AlbersEqualArea}.
     *
     * @version $Id: AlbersEqualArea.java,v 1.4 2004/02/23 12:28:22 desruisseaux Exp $
     * @author Rueben Schulz
     */
    static final class Provider extends org.geotools.ct.proj.Provider {
        /**
         * Construct a new provider.
         */
        public Provider() {
            super("Albers_Conic_Equal_Area",
                  ResourceKeys.ALBERS_EQUAL_AREA_PROJECTION);
            remove("scale_factor");
            put("standard_parallel_1", 50.0, LATITUDE_RANGE);
            put("standard_parallel_2", 58.5, LATITUDE_RANGE);
        }

        /**
         * Create a new map projection based on the parameters.
         */
        public MathTransform create(final Projection parameters) throws MissingParameterException {
            return new AlbersEqualArea(parameters);
        }
    }
    
    /**
     * Construct a new map projection from the supplied parameters.
     *
     * @param  parameters The parameter values in standard units.
     * @throws MissingParameterException if a mandatory parameter is missing.
     */
    protected AlbersEqualArea(final Projection parameters) throws MissingParameterException {
        //Fetch parameters
        super(parameters);
        phi1 = latitudeToRadians(parameters.getValue("standard_parallel_1", 50.0), true);
        phi2 = latitudeToRadians(parameters.getValue("standard_parallel_2", 58.5), true);

	//Compute Constants
        if (Math.abs(phi1 + phi2) < TOL) 
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_ANTIPODE_LATITUDES_$2,
                    new Latitude(Math.toDegrees(phi1)),
                    new Latitude(Math.toDegrees(phi2))));
         
        double  sinphi = Math.sin(phi1);
        double  cosphi = Math.cos(phi1);
        double  n      = sinphi;
        boolean secant = (Math.abs(phi1 - phi2) >= TOL);
        if (isSpherical) {
            if (secant) {
                n = 0.5 * (n + Math.sin(phi2));
            }           
            c    = cosphi * cosphi + n*2 * sinphi;
            rho0 = Math.sqrt(c - n*2 * Math.sin(latitudeOfOrigin)) /n;
            ec   = Double.NaN;
        } else {
            double m1 = msfn(sinphi, cosphi);
            double q1 = qsfn(sinphi);
            if (secant) { /* secant cone */
                sinphi    = Math.sin(phi2);
                cosphi    = Math.cos(phi2);
                double m2 = msfn(sinphi, cosphi);
                double q2 = qsfn(sinphi);
                n = (m1 * m1 - m2 * m2) / (q2 - q1);
            }
            c = m1 * m1 + n * q1;
            rho0 = Math.sqrt(c - n * qsfn(Math.sin(latitudeOfOrigin))) /n;
            ec = 1.0 - .5 * (1.0-es) * Math.log((1.0 - e) / (1.0 + e)) / e;
        }
        this.n = n;
    }
    
    /**
     * Returns a human readable name localized for the specified locale.
     */
    public String getName(final Locale locale) {
        return Resources.getResources(locale).getString(ResourceKeys.ALBERS_EQUAL_AREA_PROJECTION);
    }

    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate (units in radians)
     * and stores the result in <code>ptDst</code> (linear distance on a unit sphere).
     */
    protected Point2D transformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException
    {
        x *= n;
        double rho;
        if (isSpherical) {
            rho = c - n*2 * Math.sin(y);
        } else {
            rho = c - n * qsfn(Math.sin(y));
        }

        if (rho < 0.0) {
            // TODO: fix message (and check when this condition will occur)
            // is this only checking for an impossible divide by 0 condition?
            throw new ProjectionException("Tolerance condition error");
        }
        rho = Math.sqrt(rho) / n;
        y   = rho0 - rho * Math.cos(x);
        x   =        rho * Math.sin(x);

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
        y = rho0 - y;
        double rho = Math.sqrt(x*x + y*y);
        if (rho  != 0.0) {
            if (n < 0.0) {
                rho = -rho;
                x   = -x;
                y   = -y;
            }
            x = Math.atan2(x, y) / n;
            y =  rho*n;
            if (isSpherical) {
                y = (c - y * y) / (n*2);
                if (Math.abs(y) <= 1.0){
                    y = Math.asin(y);
                }
                else {
                    y = (y < 0.0) ? -Math.PI/2.0 : Math.PI/2.0;
                }     
            } else {
                y = (c - y*y) / n;
                if (Math.abs(ec - Math.abs(y)) > EPS) {
                    y = phi1(y);
                } else {
                    y = (y < 0.0) ? -Math.PI/2.0 : Math.PI/2.0;
                } 
            }   
        } else {
            x = 0.0;
            y = n > 0.0 ? Math.PI/2.0 : - Math.PI/2.0;
        }

        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }

    /**
     * Iteratively solves equation (3-16) from Snyder.
     *
     * @param qs arcsin(q/2), used in the first step of itteration
     * @return the latitude
     */
    private double phi1(double qs) throws ProjectionException {
        final double tone_es = 1 - es;
        double phi = Math.asin(0.5 * qs);
        if (e < EPS) {
            return phi;
        }
        for (int i=0; i<MAX_ITER; i++) {
            final double sinpi = Math.sin(phi);
            final double cospi = Math.cos(phi);
            final double con   = e * sinpi;
            final double com   = 1.0 - con*con;
            final double dphi  = 0.5 * com*com / cospi * 
                                 (qs/tone_es - sinpi / com + 0.5/e * 
                                 Math.log((1. - con) / (1. + con)));
            phi += dphi;
            if (Math.abs(dphi) <= TOL) {
                return phi;
            }
        } 
        throw new ProjectionException(Resources.format(ResourceKeys.ERROR_NO_CONVERGENCE));
    }
    
    /** 
     * Calculates q, Snyder equation (3-12)
     *
     * @param sinphi sin of the latitude q is calculated for
     * @return q from Snyder equation (3-12)
     */
    private double qsfn(double sinphi) {
        final double one_es = 1 - es;
        if (e >= EPS) {
            final double con = e * sinphi;
            return (one_es * (sinphi / (1. - con*con) -
                   (0.5/e) * Math.log((1.-con) / (1.+con))));
        } else {
            return sinphi + sinphi;
        }
    }
    
    /**
     * Returns a hash value for this projection.
     */
    public int hashCode() {
        final long code = Double.doubleToLongBits(c);
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
            final AlbersEqualArea that = (AlbersEqualArea) object;
            return equals(this.n    , that.n   ) &&
                   equals(this.c    , that.c   ) &&
                   equals(this.rho0 , that.rho0) &&
                   equals(this.phi1 , that.phi1) &&
                   equals(this.phi2 , that.phi2);
        }
        return false;
    }
    
    /**
     * Complete the WKT for this map projection.
     */
    void toString(final StringBuffer buffer) {
        super.toString(buffer);
        addParameter(buffer, "standard_parallel_1", Math.toDegrees(phi1));
        addParameter(buffer, "standard_parallel_2", Math.toDegrees(phi2));
    }
}
