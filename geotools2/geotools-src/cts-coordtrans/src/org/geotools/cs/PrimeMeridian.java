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
import org.opengis.cs.CS_AngularUnit;
import org.opengis.cs.CS_PrimeMeridian;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.resources.Utilities;

// J2SE dependencies
import java.rmi.RemoteException;


/**
 * A meridian used to take longitude measurements from.
 *
 * @version $Id: PrimeMeridian.java,v 1.3 2002/06/05 16:09:01 loxnard Exp $
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_PrimeMeridian
 */
public class PrimeMeridian extends Info {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 7570594768127669147L;
    
    /**
     * The Greenwich meridian, with angular measurements in degrees.
     */
    public static final PrimeMeridian GREENWICH = (PrimeMeridian) pool.canonicalize(
                    new PrimeMeridian("Greenwich", Unit.DEGREE, 0));
    
    /**
     * The angular units.
     */
    private final Unit unit;
    
    /**
     * The longitude value relative to the Greenwich Meridian.
     */
    private final double longitude;
    
    /**
     * Creates a prime meridian, relative to Greenwich.
     *
     * @param name      Name to give new object.
     * @param unit      Angular units of longitude.
     * @param longitude Longitude of prime meridian in supplied angular units
     *                  East of Greenwich.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createPrimeMeridian
     */
    public PrimeMeridian(final CharSequence name, final Unit unit, final double longitude) {
        super(name);
        this.unit      = unit;
        this.longitude = longitude;
        ensureNonNull("unit", unit);
        ensureAngularUnit(unit);
    }
    
    /**
     * Returns the longitude value relative to the Greenwich Meridian.
     * The longitude is expressed in this object's angular units.
     *
     * @see org.opengis.cs.CS_PrimeMeridian#getLongitude()
     */
    public double getLongitude() {
        return longitude;
    }
    
    /**
     * Returns the longitude value relative to the Greenwich Meridian,
     * expressed in the specified units. This convenience method makes it
     * easier to obtain longitude in degrees
     * (<code>getLongitude(Unit.DEGREE)</code>), regardless of the underlying
     * angular units of this prime meridian.
     *
     * @param targetUnit The unit in which to express longitude.
     */
    public double getLongitude(final Unit targetUnit) {
        return targetUnit.convert(getLongitude(), getAngularUnit());
    }
    
    /**
     * Returns the angular units.
     *
     * @see org.opengis.cs.CS_PrimeMeridian#getAngularUnit()
     */
    public Unit getAngularUnit() {
        return unit;
    }
    
    /**
     * Returns a hash value for this prime meridian.
     */
    public int hashCode() {
        final long code = Double.doubleToLongBits(longitude);
        return super.hashCode()*37 + ((int)(code >>> 32) ^ (int)code);
    }
    
    /**
     * Compares the specified object with
     * this prime meridian for equality.
     */
    public boolean equals(final Object object) {
        if (super.equals(object)) {
            final PrimeMeridian that = (PrimeMeridian) object;
            return Double.doubleToLongBits(this.longitude) ==
                   Double.doubleToLongBits(that.longitude) &&
                   Utilities.equals(this.unit, that.unit);
        }
        return false;
    }
    
    /**
     * Fills the part inside "[...]".
     * Used for formatting Well Known Text (WKT).
     */
    String addString(final StringBuffer buffer, Unit context) {
        if (context == null) {
            // If the PrimeMeridian is written inside a "GEOGCS",
            // then OpenGIS say that it must be written into the
            // unit of the enclosing geographic coordinate system.
            // Otherwise, default to degrees.
            context = Unit.DEGREE;
        }
        buffer.append(", ");
        buffer.append(context.convert(longitude, unit));
        return "PRIMEM";
    }
    
    /**
     * Returns an OpenGIS interface for this prime meridian.
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
     * Wraps a {@link PrimeMeridian} object for use with OpenGIS.
     * This class is suitable for RMI use.
     */
    private final class Export extends Info.Export implements CS_PrimeMeridian {
        /**
         * Constructs a remote object.
         */
        protected Export(final Object adapters) {
            super(adapters);
        }
        
        /**
         * Returns the longitude value relative to the Greenwich Meridian.
         */
        public double getLongitude() throws RemoteException {
            return PrimeMeridian.this.getLongitude();
        }
        
        /**
         * Returns the AngularUnits.
         *
         * @throws RemoteException if a remote method call fails.
         */
        public CS_AngularUnit getAngularUnit() throws RemoteException {
            return (CS_AngularUnit) adapters.export(PrimeMeridian.this.getAngularUnit());
        }
    }
}
