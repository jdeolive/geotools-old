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
import org.geotools.ct.MathTransform;
import org.geotools.ct.MissingParameterException;

// Resources
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Stereographic Projection. The directions starting from the central point are true,
 * but the areas and the lengths become increasingly deformed as one moves away from
 * the center.  This projection is used to represent polar areas.  It can be adapted
 * for other areas having a circular form.
 * <br><br>
 *
 * This implementation, and its subclasses, provides transforms for four cases of the  
 * stereographic projection:
 * <ul>
 *   <li><code>"Oblique_Stereographic"</code> (EPSG code 9809)</li>
 *   <li><code>"Polar_Stereographic"</code> (EPSG code 9810, uses iteration for the inverse)</li>
 *   <li><code>"Oblique_Stereographic_USGS"</code> (<strong>NOT</strong> EPSG code 9809)</li>
 *   <li><code>"Polar_Stereographic_Series"</code> (EPSG code 9810, uses a series for the inverse)</li>
 * </ul>   
 *
 * The <code>"Oblique_Stereographic_USGS"</code> case uses the USGS equations of Snyder.
 * This computes the conformal latitude of each point on the sphere.
 *
 * The <code>"Oblique_Stereographic"</code> case uses equations from the EPSG and only uses
 * a single conformal sphere at the origin point. Here the transform involves two parts: 
 * 1) a conformal transformation of the geographic coordinates to a sphere and 2) a spherical
 * Stereographic projection. Because of this, this projection is sometimes called the 
 * "Double Stereographic". The EPSG considers both methods to be valid, but considers
 * the USGS method to be a different coordinate operation method. The equations
 * given by the EPSG are used in New Brunswick (Canada) and the Netherlands.
 * <br><br>
 *
 * In ESRI's ArcGIS 8.x product the <code>"Oblique_Stereographic_USGS"</code> is named
 * the "Stereographic" and the <code>"Oblique_Stereographic"</code> is called the
 * "Double Stereographic".
 * <br><br>
 *
 * WARNING: Tests points calculated with ArcGIS's "Double Stereographic" are
 * not always equal to points calculated with the <code>"Oblique_Stereographic"</code>.
 * However, where there are differences, two different implementations of these equations
 * (EPSG guidence note 7 and libproj) calculate the same values. Until these 
 * differences are resolved, please be careful when using this projection.
 * <br><br>
 *
 * If a <code>"latitude_of_origin"</code> parameter is supplied and is not consistent with the
 * projection classification (for example a latitude different from ±90° for the polar case),
 * then the oblique or polar case will be automatically inferred from the latitude. In other
 * words, the latitude of origin has precedence on the projection classification. If ommited,
 * then the default value is 90°N for <code>"Polar_Stereographic"</code> and 0° for
 * <code>"Oblique_Stereographic"</code>.
 * <br><br>
 *
 * The <code>"latitude_true_scale"</code> parameter is not specified by the EPSG and is
 * only used for the <code>"Polar_Stereographic"</code> case.
 * The <code>"Polar_Stereographic_Series"</code> does not include this parameter.
 *
 * <strong>References:</strong><ul>
 *   <li>John P. Snyder (Map Projections - A Working Manual,<br>
 *       U.S. Geological Survey Professional Paper 1395, 1987)</li>
 *   <li>Gerald Evenden "Coordinate Conversions and Transformations including Formulas",<br>
 *       EPSG Guidence Note Number 7, Version 19.</li>
 *   <li>"Supplementary PROJ.4 Notes - Oblique Stereographic Alternative" available at:
 *       <A HREF="http://members.bellatlantic.net/~vze2hc4d/proj4/sterea.pdf">sterea.pdf</A></li>
 * </ul>
 *
 * @see <A HREF="http://mathworld.wolfram.com/StereographicProjection.html">Stereographic projection on MathWorld</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/polar_stereographic.html">Polar_Stereographic</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/oblique_stereographic.html">Oblique_Stereographic</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/stereographic.html">Stereographic</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/random_issues.html#stereographic">Some Random Stereographic Issues</A>
 *
 * @version $Id: Stereographic.java,v 1.7 2004/02/23 12:28:23 desruisseaux Exp $
 * @author André Gosselin
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 */
public abstract class Stereographic extends PlanarProjection {
    /**
     * Maximum number of itterations for the inverse calculation.
     */
    static final int MAX_ITER = 10;

    /**
     * Informations about a {@link Stereographic} projection. The {@link #create} method infer
     * the kind of projection ({@link PolarStereographic} or {@link ObliqueStereographic} from
     * the latitude of origin. If the latitude of origin is not explicitely specified, then the
     * default value is 90°N for <code>"Polar_Stereographic"</code> and 0° for
     * <code>"Oblique_Stereographic"</code>.
     *
     * @version $Id: Stereographic.java,v 1.7 2004/02/23 12:28:23 desruisseaux Exp $
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
         * <code>true</code> if using the EPSG oblique equations or a series for the 
         * inverse polar stereographic.
         */
        private final boolean EPSG;
        
        /*
         * <code>true</code> if using an experimental, testing version of the 
         * epsg oblique equations.
         */
        private final boolean EPSG2;
        
        /**
         * Construct a provider for polar or oblique stereographic using USGS equations.
         *
         * @param polar <code>true</code> for polar stereographic, or
         *              <code>false</code> for equatorial and oblique
         *              stereographic.
         */
        public Provider(final boolean polar) {
            this(polar, false);
        }
        
        /**
         * Construct a provider for polar or oblique stereographic.
         *
         * @param polar <code>true</code> for polar stereographic, or
         *              <code>false</code> for equatorial and oblique
         *              stereographic.
         * @param EPSG <code>true</code> for EPSG oblique equations or to use a series for the polar inverse, or
         *              <code>false</code> for USGS equations.
         */
        public Provider(final boolean polar, final boolean EPSG) {
            this(polar, EPSG, false);
        }

        /**
         * Construct a provider for polar or oblique stereographic.
         *
         * @param polar <code>true</code> for polar stereographic, or
         *              <code>false</code> for equatorial and oblique
         *              stereographic.
         * @param EPSG <code>true</code> for EPSG oblique equations or to use a series for the polar inverse, or
         *              <code>false</code> for USGS equations.
         * @param EPSG2 TEMPORARY PARAMETER USED FOR TESTING.
         *
         * @task TODO: Remove the EPSG2 parameter once the testing is finished
         *             (or document it if we keep it).
         */
        public Provider(final boolean polar, final boolean EPSG, final boolean EPSG2) {
            super(EPSG2 ? "Oblique_Stereographic2" :
                  (EPSG ? (polar ? "Polar_Stereographic_Series" : "Oblique_Stereographic") :
                          (polar ? "Polar_Stereographic"        : "Oblique_Stereographic_USGS")), 
                          ResourceKeys.STEREOGRAPHIC_PROJECTION);
            if (polar && !EPSG) {
                //no default, allows default to be decided in PolarStereographic
                put("latitude_true_scale", Double.NaN, LATITUDE_RANGE);
            }
            this.polar = polar;
            this.EPSG  = EPSG;
            this.EPSG2 = EPSG2;
        }

        /**
         * Create a new stereographic projection. The type of projection (polar or oblique) is
         * automatically inferred from the latitude of origin. If the latitude of origin is not
         * explicitely specified, then the default value is infered from the projection
         * classification.
         */
        public MathTransform create(final Projection parameters) throws MissingParameterException {
            final double latitudeOfOrigin = Math.abs(
                latitudeToRadians(parameters.getValue("latitude_of_origin", polar ? 90 : 0), true));
            final boolean isSpherical = isSpherical(parameters);
            // Polar case.
            if (Math.abs(latitudeOfOrigin - Math.PI/2) < EPS) {
                if (isSpherical) {
                    return new PolarStereographic.Spherical(parameters);
                } else {
                    if (EPSG) {
                        return new PolarStereographic.Series(parameters);
                    } else {
                        return new PolarStereographic(parameters);
                    }
                }
            }
            // Equatorial case.
            if (latitudeOfOrigin < EPS) {
                if (isSpherical) {
                    return new EquatorialStereographic.Spherical(parameters);
                } else if (!EPSG) {
                    return new EquatorialStereographic(parameters);
                }               
            }
            // Generic (oblique) case.
            if (isSpherical) {
                return new ObliqueStereographic.Spherical(parameters);
            } else {
                if (EPSG) { 
                    if (EPSG2) {
                        return new ObliqueStereographic.EPSG2(parameters);
                    } else {
                        return new ObliqueStereographic.EPSG(parameters);
                    }
                } else {
                    return new ObliqueStereographic(parameters);
                }
            }               
        }
    }

    /**
     * Construct a stereographic transformation from the specified parameters.
     *
     * @param parameters The parameter values in standard units.
     * @throws MissingParameterException if a mandatory parameter is missing.
     */
    protected Stereographic(final Projection parameters) throws MissingParameterException {
        super(parameters);
    }

    /**
     * Returns a human readable name localized for the specified locale.
     */
    public String getName(final Locale locale) {
        return Resources.getResources(locale).getString(ResourceKeys.STEREOGRAPHIC_PROJECTION);
    }
}
