/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2001, Institut de Recherche pour le Développement
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.cs;

// OpenGIS dependencies
import org.opengis.cs.CS_Ellipsoid;
import org.opengis.cs.CS_HorizontalDatum;
import org.opengis.cs.CS_WGS84ConversionInfo;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.resources.Utilities;

// J2SE dependencies
import java.rmi.RemoteException;


/**
 * Procedure used to measure positions on the surface of the Earth.
 *
 * @version $Id: HorizontalDatum.java,v 1.5 2002/10/10 23:14:09 desruisseaux Exp $
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_HorizontalDatum
 */
public class HorizontalDatum extends Datum {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1424482162002300865L;
    
    /**
     * The default WGS 1984 datum.
     */
    public static final HorizontalDatum WGS84 = (HorizontalDatum) pool.canonicalize(
                    new HorizontalDatum("WGS84", DatumType.GEOCENTRIC, Ellipsoid.WGS84, null));
    
    /**
     * The ellipsoid for this datum.
     */
    private final Ellipsoid ellipsoid;
    
    /**
     * Preferred parameters for a Bursa Wolf transformation.
     */
    private final WGS84ConversionInfo parameters;
    
    /**
     * Creates horizontal datum from an ellipsoid. The datum
     * type will be {@link DatumType.Horizontal#OTHER}.
     *
     * @param name      Name to give new object.
     * @param ellipsoid Ellipsoid to use in new horizontal datum.
     */
    public HorizontalDatum(final CharSequence name, final Ellipsoid ellipsoid) {
        this(name, DatumType.Horizontal.OTHER, ellipsoid, null);
    }
    
    /**
     * Creates horizontal datum from ellipsoid and Bursa-Wolf parameters.
     *
     * @param name      Name to give new object.
     * @param type      Type of horizontal datum to create.
     * @param ellipsoid Ellipsoid to use in new horizontal datum.
     * @param toWGS84   Suggested approximate conversion from new datum to WGS84,
     *                  or <code>null</code> if there is none.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createHorizontalDatum
     */
    public HorizontalDatum(final CharSequence         name,
                           final DatumType.Horizontal type,
                           final Ellipsoid            ellipsoid,
                           final WGS84ConversionInfo  parameters)
    {
        super(name, type);
        this.ellipsoid  = ellipsoid;
        this.parameters = (parameters!=null) ? (WGS84ConversionInfo)parameters.clone() : null;
        ensureNonNull("ellipsoid", ellipsoid);
    }
    
    /**
     * Gets the type of the datum as an enumerated code.
     *
     * Note: return type will be changed to {@link DatumType.Horizontal}
     *       when are able to use generic types (with JDK 1.5).
     *
     * @see org.opengis.cs.CS_HorizontalDatum#getDatumType()
     */
    public DatumType/*.Horizontal*/ getDatumType() {
        return (DatumType.Horizontal) super.getDatumType();
    }
    
    /**
     * Returns the ellipsoid.
     *
     * @see org.opengis.cs.CS_HorizontalDatum#getEllipsoid()
     */
    public Ellipsoid getEllipsoid() {
        return ellipsoid;
    }
    
    /**
     * Gets preferred parameters for a Bursa Wolf transformation into WGS84.
     * The 7 returned values correspond to (dx,dy,dz) in meters, (ex,ey,ez)
     * in arc-seconds, and scaling in parts-per-million.  This method will
     * always return <code>null</code> for horizontal datums with type
     * {@link DatumType.Horizontal#OTHER}. This method may also return
     * <code>null</code> if no suitable transformation is available.
     *
     * @see org.opengis.cs.CS_HorizontalDatum#getWGS84Parameters()
     */
    public WGS84ConversionInfo getWGS84Parameters() {
        return (parameters!=null) ? (WGS84ConversionInfo)parameters.clone() : null;
    }
    
    /**
     * Fills the part inside "[...]".
     * Used for formatting Well Known Text (WKT).
     */
    String addString(final StringBuffer buffer, final Unit context) {
        super.addString(buffer, context);
        buffer.append(", ");
        buffer.append(ellipsoid);
        if (parameters!=null) {
            buffer.append(", ");
            buffer.append(parameters);
        }
        return "DATUM";
    }
    
    /**
     * Compare this datum with the specified object for equality.
     *
     * @param  object The object to compare to <code>this</code>.
     * @param  compareNames <code>true</code> to comparare the {@linkplain #getName name},
     *         {@linkplain #getAlias alias}, {@linkplain #getAuthorityCode authority
     *         code}, etc. as well, or <code>false</code> to compare only properties
     *         relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final Info object, final boolean compareNames) {
        if (object == this) {
            return true;
        }
        if (super.equals(object, compareNames)) {
            final HorizontalDatum that = (HorizontalDatum) object;
            return equals(this.ellipsoid,  that.ellipsoid, compareNames) &&
                   equals(this.parameters, that.parameters             );
        }
        return false;
    }
    
    /**
     * Returns an OpenGIS interface for this datum.
     * The returned object is suitable for RMI use.
     *
     * Note: The returned type is a generic {@link Object} in order
     *       to avoid premature class loading of OpenGIS interface.
     */
    final Object toOpenGIS(final Object adapters) {
        return new Export(adapters);
    }
    
    
    
    
    /////////////////////////////////////////////////////////////////////////
    ////////////////                                         ////////////////
    ////////////////             OPENGIS ADAPTER             ////////////////
    ////////////////                                         ////////////////
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Wraps a {@link HorizontalDatum} object for use with OpenGIS.
     * This class is suitable for RMI use.
     */
    private final class Export extends Datum.Export implements CS_HorizontalDatum {
        /**
         * Constructs a remote object.
         */
        protected Export(final Object adapters) {
            super(adapters);
        }
        
        /**
         * Returns the Ellipsoid.
         */
        public CS_Ellipsoid getEllipsoid() throws RemoteException {
            return adapters.export(HorizontalDatum.this.getEllipsoid());
        }
        
        /**
         * Gets preferred parameters for a Bursa Wolf transformation into WGS84.
         */
        public CS_WGS84ConversionInfo getWGS84Parameters() throws RemoteException {
            return adapters.export(HorizontalDatum.this.getWGS84Parameters());
        }
    }
}
