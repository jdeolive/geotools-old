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
package org.geotools.ct.proj;

// J2SE dependencies
import java.util.Locale;
import java.awt.geom.Point2D;

// Geotools dependencies
import org.geotools.cs.Projection;
import org.geotools.cs.Ellipsoid;
import org.geotools.pt.Latitude;
import org.geotools.ct.MissingParameterException;
import org.geotools.ct.MathTransform;

// Resources
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Mercator Cylindrical Projection. The parallels and the meridians are straight lines and
 * cross at right angles; this projection thus produces rectangular charts. The scale is true
 * along the equator (by default) or along two parallels equidistant of the equator (if a scale
 * factor other than 1 is used). This projection is used to represent areas close to the equator.
 * It is also often used for maritime navigation because all the straight lines on the chart are
 * <em>loxodrome</em> lines, i.e. a ship following this line would keep a constant azimuth on its
 * compass.
 *<br><br>
 *
 * This implementation handles both the 1 and 2 stardard parallel cases.
 * For Mercator_1SP (EPSG code 9804), the line of contact is the equator. 
 * For Mercator_2SP (EPSG code 9805) lines of contact are symmetrical 
 * about the equator.
 *<br><br>
 *
 * Référence: John P. Snyder (Map Projections - A Working Manual,
 *            U.S. Geological Survey Professional Paper 1395, 1987)
 *
 *            'Coordinate Conversions and Transformations including Formulas',
 *            EPSG Guidence Note Number 7, Version 19.
 *
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/mercator_1sp.html">&quot;mercator_1sp&quot; on Remote Sensing</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/mercator_2sp.html">&quot;mercator_2sp&quot; on Remote Sensing</A>
 * @see <A HREF="http://mathworld.wolfram.com/MercatorProjection.html">Mercator projection on MathWorld</A>
 * 
 * @version $Id: Mercator.java,v 1.2 2003/04/16 19:26:59 desruisseaux Exp $
 * @author André Gosselin
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 *
 * Revisit - how to access from MathTransformFactory (other than making this public)
 *         - hashcode and equals may need to cheak standardParallel
 */
public class Mercator extends CylindricalProjection {
    
    /**
     * Global scale factor. Value <code>ak0</code>
     * is equals to <code>{@link #semiMajor}*k0</code>.
     */
    protected final double ak0;
    
    /**
     * Standard Parallel used for the Mercator_2SP case.
     */
    protected final double standardParallel;
    
    /**
     * <code>true</code> for 2SP, or <code>false</code> for 1SP projection.
     * Only used by toString().
     */
    private final boolean sp2;
    
    /**
     * Construct a new map projection from the supplied parameters.
     *
     * @param  parameters The parameter values in standard units.
     * @param  sp2 Indicates if this is a 1 or 2 standard parallel case of the mercator projection.
     * @throws MissingParameterException if a mandatory parameter is missing.
     */
    protected Mercator(final Projection parameters, final boolean sp2) throws MissingParameterException {
        //Fetch parameters 
        super(parameters);
        this.sp2 = sp2;
        
        //This will be 0 in the 1SP case when this parameter is not set
        standardParallel = latitudeToRadians(Math.abs(parameters.getValue("standard_parallel", 0)), false);

        // Compute constants 
        if (isSpherical()) {
            ak0 = scaleFactor * semiMajor*Math.cos(standardParallel);
        }  else {
            ak0 = scaleFactor * semiMajor*msfn(Math.sin(standardParallel), Math.cos(standardParallel));
        }
        assert latitudeOfOrigin == 0 : latitudeOfOrigin;
    }
        
    /**
     * Returns a human readable name localized for the specified locale.
     */
    public String getName(final Locale locale) {
        final Resources resources = Resources.getResources(locale);
        return resources.getString(ResourceKeys.CYLINDRICAL_MERCATOR_PROJECTION);
    }
    
    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate (units in radians)
     * and stores the result in <code>ptDst</code> (units in meters). 
     */
    protected Point2D transform(double x, double y, final Point2D ptDst)
        throws ProjectionException
    {
        if (Math.abs(y) > (Math.PI/2 - EPS)) {
            throw new ProjectionException(Resources.format(
                    ResourceKeys.ERROR_POLE_PROJECTION_$1, new Latitude(Math.toDegrees(y))));
        }
        x = (x-centralMeridian)*ak0;
        y = -ak0*Math.log(tsfn(y, Math.sin(y)));
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
        x = (x-falseEasting)/ak0 + centralMeridian;
        y = Math.exp(-(y-falseNorthing)/ak0);
        y = cphi2(y);

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
        final long code = Double.doubleToLongBits(ak0);
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
        if (super.equals(object)) {
            final Mercator that = (Mercator) object;
            return Double.doubleToLongBits(this.ak0) == Double.doubleToLongBits(that.ak0) &&
                   Double.doubleToLongBits(this.standardParallel) ==
                   Double.doubleToLongBits(that.standardParallel);
        }
        return false;
    }
    
    /**
     * Complete the WKT for this map projection.
     */
    void toString(final StringBuffer buffer) {
        super.toString(buffer);
        if (sp2) {
            addParameter(buffer, "standard_parallel", Math.toDegrees(standardParallel));
        }
    }
    
    /**
     * Provides the transform equations for the spherical case of the 
     * Mercator projection.
     */
    protected static final class Spherical extends Mercator {
        
        /**
         * Construct a new map projection from the suplied parameters.
         *
         * @param  parameters The parameter values in standard units.
         * @param  sp2 Indicates if this is a 1 or 2 standard parallel case of the mercator projection.
         * @throws MissingParameterException if a mandatory parameter is missing.
         */
        protected Spherical(final Projection parameters, final boolean sp2) throws MissingParameterException {
            super(parameters, sp2);
            assert isSpherical();
	}

	/**
	 * Transforms the specified (<var>x</var>,<var>y</var>) coordinate
         * and stores the result in <code>ptDst</code> using equations for a Sphere.
	 */
        protected Point2D transform(double x, double y, Point2D ptDst)
             throws ProjectionException
        {
            if (Math.abs(y) > (Math.PI/2 - EPS)) {
                throw new ProjectionException(Resources.format(
                        ResourceKeys.ERROR_POLE_PROJECTION_$1, new Latitude(Math.toDegrees(y))));
            }
            // Compute using ellipsoidal formulas, for comparaison later.
            assert (ptDst = super.transform(x, y, ptDst)) != null;

            x = (x-centralMeridian)*ak0;
	    y =  ak0*Math.log(Math.tan((Math.PI/4) + 0.5*y));
            x += falseEasting;
            y += falseNorthing;

            assert Math.abs(ptDst.getX()/x - 1) < EPS : x;
            assert Math.abs(ptDst.getY()/y - 1) < EPS : y;
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

	    x = (x-falseEasting)/ak0 + centralMeridian;
            y = Math.exp(-(y-falseNorthing)/ak0);
            y = (Math.PI/2) - 2.0*Math.atan(y);

            assert Math.abs(ptDst.getX() - x) < EPS : x;
            assert Math.abs(ptDst.getY() - y) < EPS : y;
            if (ptDst != null) {
                ptDst.setLocation(x,y);
                return ptDst;
            }
            return new Point2D.Double(x,y);
	}
    }


    /**
     * Informations about a {@link Mercator}.
     *
     * @version $Id: Mercator.java,v 1.2 2003/04/16 19:26:59 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    public static final class Provider extends org.geotools.ct.proj.Provider {
        /**
         * <code>true</code> for 2SP, or <code>false</code> for 1SP projection.
         */
        private final boolean sp2;
        
        /**
         * Construct a new provider. 
         * @param sp2 <code>false</code> for a Mercator_1SP provider,
         *            <code>true</code> for a Mercator_2SP provider.
         */
        public Provider(boolean sp2) {
            super(sp2 ? "Mercator_2SP": "Mercator_1SP", 
                  ResourceKeys.CYLINDRICAL_MERCATOR_PROJECTION);
            remove("latitude_of_origin");
            if (sp2) {
                remove("scale_factor");
                put("standard_parallel", 0, LATITUDE_RANGE); 
            }
            this.sp2 = sp2;
        }
        
        /**
         * Create a new map projection based on the parameters. 
         */
        public MathTransform create(final Projection parameters) throws MissingParameterException {
            if (isSpherical(parameters)) {
                return new Spherical(parameters, sp2);
            } else {
                return new Mercator(parameters, sp2);
            }
        }
    }
}
