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
import java.awt.geom.Point2D;

// Geotools dependencies
import org.geotools.cs.Projection;
import org.geotools.ct.MissingParameterException;

// Resources
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * The polar case of the {@linkplain Stereographic stereographic} projection.
 * This default implementation uses USGS equation (i.e. iteration) for computing
 * the {@linkplain #inverseTransformNormalized inverse transform}.
 *
 * @version $Id: PolarStereographic.java,v 1.6 2004/05/03 07:36:47 desruisseaux Exp $
 * @author André Gosselin
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 */
public class PolarStereographic extends Stereographic {
    /**
     * A constant used in the transformations.
     * This is <strong>not</strong> equal to the {@link #scaleFactor}.
     */
    private final double k0;

    /**
     * Latitude of true scale, in radians.
     */
    final double latitudeTrueScale;

    /**
     * <code>true</code> if this projection is for the south pole, or <code>false</code>
     * if it is for the north pole.
     */
    final boolean southPole;

    /**
     * Construct a polar stereographic projection.
     *
     * @param parameters The parameter values in standard units.  
     * @throws MissingParameterException if a mandatory parameter is missing.
     */
    protected PolarStereographic(final Projection parameters) throws MissingParameterException {
        super(parameters);
        southPole = (latitudeOfOrigin < 0);
        latitudeOfOrigin = (southPole) ? -(Math.PI/2) : +(Math.PI/2);
        latitudeTrueScale = latitudeToRadians(parameters.getValue("latitude_true_scale",
                            southPole ? -90 : 90), true);
    
        if (Math.abs(Math.abs(latitudeTrueScale)-(Math.PI/2)) >= EPS) {
            final double t = Math.sin(Math.abs(latitudeTrueScale));
            k0 = msfn(t ,Math.cos(Math.abs(latitudeTrueScale))) /
                 tsfn(Math.abs(latitudeTrueScale), t);  //derives from (21-32 and 21-33)
        } else {
            // True scale at pole (part of (21-33))
            k0 = 2.0 / Math.sqrt(Math.pow(1+e, 1+e)*Math.pow(1-e, 1-e));
        }
    }

    /**
     * Returns <code>true</code> if this class is using EPSG equations, or <code>false</code>
     * if it is using USGS equations. The default implementation returns <code>false</code>.
     */
    boolean isEPSG() {
        return false;
    }
    
    /**
     * Construct a string version of this projection.
     */
    final void toString(final StringBuffer buffer) {
        super.toString(buffer);
        if (!isEPSG()) {
            addParameter(buffer, "latitude_true_scale", Math.toDegrees(latitudeTrueScale));
        }
    }

    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate (units in radians)
     * and stores the result in <code>ptDst</code> (linear distance on a unit sphere).
     *
     * @param  x The longitude in radians.
     * @param  y The latitude in radians.
     * @param  ptDst The destination point, or <code>null</code>.
     * @return The projected point as a linear distance on a unit sphere.
     * @throws ProjectionException if the projection failed.
     */
    protected Point2D transformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException
    {
        final double sinlat = Math.sin(y);
        final double coslon = Math.cos(x);
        final double sinlon = Math.sin(x);
        if (southPole) {
            final double rho = k0 * tsfn(-y, -sinlat);
            x = rho * sinlon;
            y = rho * coslon;
        } else {
            final double rho = k0 * tsfn(y, sinlat);
            x =  rho * sinlon;
            y = -rho * coslon;
	}

        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }

    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate (units in meters)
     * and stores the result in <code>ptDst</code> (linear distance on a unit sphere).
     *
     * @param  x The <var>x</var> ordinate as a linear distance.
     * @param  y The <var>y</var> ordinate as a linear distance.
     * @param  ptDst The destination point, or <code>null</code>.
     * @return The geographic point in radians.
     * @throws ProjectionException if the projection failed.
     */
    protected Point2D inverseTransformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException
    {

        final double rho = Math.sqrt(x*x + y*y);
        if (southPole) {
            y = -y;
        }
        /*
         * Compute latitude using iterative technique.
         */
        final double t = rho/k0;
        final double halfe = e/2.0;
        double phi0 = 0;
        for (int i=MAX_ITER;;) {
            final double esinphi = e * Math.sin(phi0);
            final double phi = (Math.PI/2) - 
                               2.0*Math.atan(t*Math.pow((1-esinphi)/(1+esinphi), halfe));
            if (Math.abs(phi-phi0) < TOL) {
                x = (Math.abs(rho)<TOL) ? 0 :
                     Math.atan2(x, -y);
                y = (southPole) ? -phi : phi;
                break;
            }
            phi0 = phi;
            if (--i < 0) {
                throw new ProjectionException(Resources.format(ResourceKeys.ERROR_NO_CONVERGENCE));
            }
        }

        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }

    /**
     * Returns a hash value for this map projection.
     */
    public int hashCode() {
        final long code = Double.doubleToLongBits(k0);
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
            final PolarStereographic that = (PolarStereographic) object;
            return        this.southPole         == that.southPole       &&
                   equals(this.k0,                  that.k0)             &&
                   equals(this.latitudeTrueScale,   that.latitudeTrueScale);
        }
        return false;
    }




    /**
     * Provides the transform equations for the spherical case of the polar
     * stereographic projection.
     *
     * @version $Id: PolarStereographic.java,v 1.6 2004/05/03 07:36:47 desruisseaux Exp $
     * @author Martin Desruisseaux
     * @author Rueben Schulz
     */
    static final class Spherical extends PolarStereographic {
        /**
         * A constant used in the transformations. This constant hides the <code>k0</code>
         * constant from the ellipsoidal case. The spherical and ellipsoidal <code>k0</code>
         * are not computed in the same way, and we preserve the ellipsoidal <code>k0</code>
         * in {@link Stereographic} in order to allow assertions to work.
         */
        private final double k0;

        /**
         * Construct a new map projection from the suplied parameters.
         *
         * @param  parameters The parameter values in standard units.
         * @throws MissingParameterException if a mandatory parameter is missing.
         */
        protected Spherical(final Projection parameters) throws MissingParameterException {
            super(parameters);
            assert isSpherical;
            if (Math.abs(Math.abs(latitudeTrueScale) - (Math.PI/2)) >= EPS) {
                k0 = 1 + Math.sin(Math.abs(latitudeTrueScale));     //derived from (21-7)
            } else {
                k0 = 2;
            }
        }

        /**
         * Transforms the specified (<var>x</var>,<var>y</var>) coordinate (units in radians)
         * and stores the result in <code>ptDst</code> (linear distance on a unit sphere).
         */
        protected Point2D transformNormalized(double x, double y, Point2D ptDst)
                throws ProjectionException
        {
            //Compute using ellipsoidal formulas, for comparaison later.
            assert (ptDst = super.transformNormalized(x, y, ptDst)) != null;

            final double coslat = Math.cos(y);
            final double sinlat = Math.sin(y);
            final double coslon = Math.cos(x);
            final double sinlon = Math.sin(x);

            if (southPole) {
                if (!(Math.abs(1-sinlat) >= TOL)) {
                    throw new ProjectionException(Resources.format(
                        ResourceKeys.ERROR_VALUE_TEND_TOWARD_INFINITY));
                }
                // (21-12)
                final double f = k0 * coslat / (1-sinlat); // == tan (pi/4 + phi/2)
                x = f * sinlon; // (21-9)
                y = f * coslon; // (21-10)
            } else {
                if (!(Math.abs(1+sinlat) >= TOL)) {
                    throw new ProjectionException(Resources.format(
                        ResourceKeys.ERROR_VALUE_TEND_TOWARD_INFINITY));
                }
                // (21-8)
                final double f = k0 * coslat / (1+sinlat); // == tan (pi/4 - phi/2)
                x =  f * sinlon; // (21-5)
                y = -f * coslon; // (21-6)
	    }

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
         * and stores the result in <code>ptDst</code>.
         */
        protected Point2D inverseTransformNormalized(double x, double y, Point2D ptDst)
                throws ProjectionException
        {
            // Compute using ellipsoidal formulas, for comparaison later.
            assert (ptDst = super.inverseTransformNormalized(x, y, ptDst)) != null;

            final double rho = Math.sqrt(x*x + y*y);

            if (!southPole) {
                y = -y;
            }
            // (20-17) call atan2(x,y) to properly deal with y==0
            x = (Math.abs(x)<TOL && Math.abs(y)<TOL) ? 0.0 : Math.atan2(x, y);
            if (Math.abs(rho) < TOL) {
                y = latitudeOfOrigin;
            } else {
                final double c = 2.0 * Math.atan(rho/k0);
                final double cosc = Math.cos(c);
                y = (southPole) ? Math.asin(-cosc) : Math.asin(cosc);
                // (20-14) with phi1=90
            }

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
     * Overides {@link PolarStereographic} to use the a series for the
     * {@link #inverseTransformNormalized inverseTransformNormalized(...)}
     * method. This is the equation specified by the EPSG. Allows for a 
     * <code>"latitude_true_scale"<code> parameter to be used, but this
     * parameter is not listed by the EPSG and is not given as a parameter
     * by the provider.
     * <br><br>
     * Compared to the default {@link PolarStereographic} implementation, the series
     * implementation is a little bit faster at the expense of a little bit less
     * accuracy. The default {@link PolarStereographic} implementation implementation
     * is an derivated work of Proj4, and is therefor better tested.
     *
     * @version $Id: PolarStereographic.java,v 1.6 2004/05/03 07:36:47 desruisseaux Exp $
     * @author Rueben Schulz
     */
    static final class Series extends PolarStereographic {
        /**
         * Constants used for the inverse polar series
         */
        private final double A, B;

        /**
         * Constants used for the inverse polar series
         */
        private double C, D;

        /**
         * A constant used in the transformations. This constant hides the <code>k0</code>
         * constant from the USGS case. The EPSG and USGS <code>k0</code> are not computed
         * in the same way, and we preserve the USGS <code>k0</code> in order to allow
         * assertions to work.
         */
        private final double k0;

        /**
         * Construct a new map projection from the suplied parameters.
         *
         * @param  parameters The parameter values in standard units.
         * @throws MissingParameterException if a mandatory parameter is missing.
         */
        protected Series(final Projection parameters) throws MissingParameterException {
            super(parameters);
            //See Snyde P. 19, "Computation of Series"
            final double e6 = es*es*es;
            final double e8 = es*es*es*es;
            C = 7.0*e6/120.0 + 81.0*e8/1120.0;
            D = 4279.0*e8/161280.0;
            A = es/2.0 + 5.0*es*es/24.0 + e6/12.0 + 13.0*e8/360.0 - C;
            B = 2.0*(7.0*es*es/48.0 + 29.0*e6/240.0 + 811.0*e8/11520.0) - 4.0*D;
            C *= 4.0;
            D *= 8.0;

            if (Math.abs(Math.abs(latitudeTrueScale)-(Math.PI/2)) >= EPS) {
                final double t = Math.sin(Math.abs(latitudeTrueScale));
                k0 = msfn(t, Math.cos(Math.abs(latitudeTrueScale))) *
                          Math.sqrt(Math.pow(1+e, 1+e)*Math.pow(1-e, 1-e)) /
                          (2.0*tsfn(Math.abs(latitudeTrueScale), t));
            } else {
                k0 = 1.0;
            }
        }

        /**
         * Returns <code>true</code> since this class is using EPSG equations.
         */
        boolean isEPSG() {
            return true;
        }

        /**
         * Transforms the specified (<var>x</var>,<var>y</var>) coordinate
         * and stores the result in <code>ptDst</code>.
         */
        protected Point2D inverseTransformNormalized(double x, double y, Point2D ptDst)
                throws ProjectionException
        {
            // Compute using itteration formulas, for comparaison later.
            assert (ptDst = super.inverseTransformNormalized(x, y, ptDst)) != null;

            final double rho = Math.sqrt(x*x + y*y);
            if (southPole) {
                y = -y;
            }
            // The series form
            final double t = (rho/k0) * Math.sqrt(Math.pow(1+e, 1+e)*Math.pow(1-e, 1-e)) / 2;
            final double chi = Math.PI/2 - 2*Math.atan(t);

            x = (Math.abs(rho)<TOL) ? 0.0 : Math.atan2(x, -y);

            //See Snyde P. 19, "Computation of Series"
            final double sin2chi = Math.sin(2.0*chi);
            final double cos2chi = Math.cos(2.0*chi);
            y = chi + sin2chi*(A + cos2chi*(B + cos2chi*(C + D*cos2chi)));
            y = (southPole) ? -y : y;

            assert Math.abs(ptDst.getX()-x) <= EPS : x;
            assert Math.abs(ptDst.getY()-y) <= EPS : y;
            if (ptDst != null) {
                ptDst.setLocation(x,y);
                return ptDst;
            }
            return new Point2D.Double(x,y);
        }
    }
}
