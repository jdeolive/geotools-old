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
 * Projection cylindrique de Mercator. Les parallèles et les méridients apparaissent
 * comme des lignes droites et se croisent à angles droits; cette projection produit
 * donc des cartes rectangulaires. L'échelle est vrai le long de l'équateur (par défaut)
 * ou le long de deux parallèles équidistants de l'équateur. Cette projection est utilisée
 * pour représenter des régions près de l'équateur. Elle est aussi souvent utilisée pour la
 * navigation maritime parce que toutes les lignes droites sur la carte sont des lignes
 * <em>loxodromiques</em>, c'est-à-dire qu'un navire suivant cette ligne garderait un azimuth
 * constant sur son compas.
 * <br><br>
 *
 * Référence: John P. Snyder (Map Projections - A Working Manual,
 *            U.S. Geological Survey Professional Paper 1395, 1987)
 *
 * @version $Id: MercatorProjection.java,v 1.2 2003/01/18 12:58:32 desruisseaux Exp $
 * @author André Gosselin
 * @author Martin Desruisseaux
 */
final class MercatorProjection extends CylindricalProjection {
    /**
     * Global scale factor. Value <code>ak0</code>
     * is equals to <code>{@link #semiMajor}*k0</code>.
     */
    private final double ak0;
    
    /**
     * Construct a new map projection from the suplied parameters.
     *
     * @param  parameters The parameter values in standard units.
     * @throws MissingParameterException if a mandatory parameter is missing.
     */
    protected MercatorProjection(final Projection parameters) throws MissingParameterException {
        //////////////////////////
        //   Fetch parameters   //
        //////////////////////////
        super(parameters);
        centralLatitude = latitudeToRadians(parameters.getValue("latitude_of_origin", 0), false);
        final double latitudeTrueScale = Math.abs(centralLatitude);
        
        //////////////////////////
        //  Compute constants   //
        //////////////////////////
        if (isSpherical) {
            ak0 = scaleFactor * semiMajor*Math.cos(latitudeTrueScale);
        }  else {
            ak0 = scaleFactor * semiMajor*msfn(Math.sin(latitudeTrueScale), Math.cos(latitudeTrueScale));
        }
    }
    
    /**
     * Returns a human readable name localized for the specified locale.
     */
    public String getName(final Locale locale) {
        final Resources resources = Resources.getResources(locale);
        return resources.getString(ResourceKeys.CYLINDRICAL_MERCATOR_PROJECTION);
    }
    
    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate
     * and stores the result in <code>ptDst</code>.
     */
    protected Point2D transform(double x, double y, final Point2D ptDst)
        throws TransformException
    {
        if (Math.abs(y) > (Math.PI/2 - EPS)) {
            throw new TransformException(Resources.format(
                    ResourceKeys.ERROR_POLE_PROJECTION_$1, new Latitude(Math.toDegrees(y))));
        }
        x = (x-centralMeridian)*ak0;
        if (isSpherical) {
            y =  ak0*Math.log(Math.tan((Math.PI/4) + 0.5*y));
        } else {
            y = -ak0*Math.log(tsfn(y, Math.sin(y)));
        }
        x += falseEasting;
        y += falseNorthing;
        if (ptDst!=null) {
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
        throws TransformException
    {
        x = (x-falseEasting)/ak0 + centralMeridian;
        y = Math.exp(-(y-falseNorthing)/ak0);
        if (isSpherical) {
            y = (Math.PI/2) - 2.0*Math.atan(y);
        } else {
            y = cphi2(y);
        }
        if (ptDst!=null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }
    
    /**
     * Returns a hash value for this projection.
     */
    public int hashCode() {
        final long code = Double.doubleToLongBits(ak0);
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
            final MercatorProjection that = (MercatorProjection) object;
            return Double.doubleToLongBits(this.ak0) == Double.doubleToLongBits(that.ak0);
        }
        return false;
    }
    
    /**
     * Informations about a {@link MercatorProjection}.
     *
     * @version $Id: MercatorProjection.java,v 1.2 2003/01/18 12:58:32 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    static final class Provider extends MapProjection.Provider {
        /**
         * Construct a new provider.
         */
        public Provider() {
            super("Mercator_1SP", ResourceKeys.CYLINDRICAL_MERCATOR_PROJECTION);
        }
        
        /**
         * Create a new map projection.
         */
        protected Object create(final Projection parameters) throws MissingParameterException {
            return new MercatorProjection(parameters);
        }
    }
}
