/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
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

// Geotools dependencies
import org.geotools.cs.Projection;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MissingParameterException;

// Resources
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Orthographic Projection. This is a perspective azimuthal (planar) projection
 * that is neither conformal nor equal-area. It resembles a globe and only 
 * one hemisphere can be seen at a time, since it is 
 * a perspectiove projection from infinite distance. While not useful for 
 * accurate measurements, this projection is useful for pictorial views of the
 * world. Only the spherical form is given here.
 * <br><br>
 * 
 * NOTE: formulae used below are from a port, to java, of the 
 *       'proj' package of the USGS survey. USGS work is acknowledged here.
 * <br><br>
 *
 * <strong>References:</strong><ul>
 *   <li> Proj-4.4.7 available at <A HREF="http://www.remotesensing.org/proj">www.remotesensing.org/proj</A><br>
 *        Relevant files are: <code>PJ_ortho.c</code>, <code>pj_fwd.c</code> and <code>pj_inv.c</code>.</li>
 *   <li> John P. Snyder (Map Projections - A Working Manual,
 *        U.S. Geological Survey Professional Paper 1395, 1987)</li>
 * </ul>
 *
 * @see <A HREF="http://mathworld.wolfram.com/OrthographicProjection.html">Orthographic projection on mathworld.wolfram.com</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/orthographic.html">"Orthographic" on www.remotesensing.org</A>
 *
 * @version $Id: Orthographic.java,v 1.1 2004/01/11 17:11:55 desruisseaux Exp $
 * @author Rueben Schulz
 */
public abstract class Orthographic extends PlanarProjection {
    /**
     * Information about a {@link Orthographic} projection. The {@link #create} method infer
     * the type of projection ({@link PolarOrthographic} or {@link ObliqueOrthographic}
     * or {@link EquatorialOrthographic}) from the latitude of origin. 
     *
     * @version $Id: Orthographic.java,v 1.1 2004/01/11 17:11:55 desruisseaux Exp $
     * @author Rueben Schulz
     */
    static final class Provider extends org.geotools.ct.proj.Provider {
        /**
         * Construct a new provider. 
         */
        public Provider() {
            super("Orthographic", ResourceKeys.ORTHOGRAPHIC_PROJECTION);
        }
        
        /**
         * Create a new map projection based on the parameters. 
         * 
         * @throws MissingParameterException if a mandatory parameter is missing.
         * @throws UnsupportedOperationException if elliptical projection 
         *         parameters are given.
         *
         * @task REVISIT: should this throw a CannotCreateTransformException 
         *                for elliptical cases.
         */
        public MathTransform create(final Projection parameters) throws MissingParameterException {
            final double latitudeOfOrigin = Math.abs(
                latitudeToRadians(parameters.getValue("latitude_of_origin", 0), true));
            if (isSpherical(parameters)) {
                // Polar case.
                if (Math.abs(latitudeOfOrigin - Math.PI/2) < EPS) {
                    return new PolarOrthographic(parameters);
                }
                // Equatorial case.
                else if (latitudeOfOrigin < EPS) {
                    return new EquatorialOrthographic(parameters);
                }
                // Generic (oblique) case.
                else {
                    return new ObliqueOrthographic(parameters);
                }
            } else {
                throw new UnsupportedOperationException(Resources.format(
                    ResourceKeys.ERROR_ELLIPTICAL_NOT_SUPPORTED));
            }
        }
    }


    /**
     * Construct a new map projection from the supplied parameters.
     *
     * @param  parameters The parameter values in standard units.
     * @throws MissingParameterException if a mandatory parameter is missing.
     */
    protected Orthographic(final Projection parameters) throws MissingParameterException {
        //Fetch parameters 
        super(parameters);
    }
    
    /**
     * Returns a human readable name localized for the specified locale.
     */
    public String getName(final Locale locale) {
        return Resources.getResources(locale).getString(ResourceKeys.ORTHOGRAPHIC_PROJECTION);
    }
    
    /**
     * Compares the specified object with this map projection for equality.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            // Slight optimization
            return true;
        }
        // Relevant parameters are already compared in MapProjection
        return super.equals(object);
    }
}
