/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
import org.opengis.cs.CS_LinearUnit;
import org.opengis.cs.CS_VerticalDatum;
import org.opengis.cs.CS_VerticalCoordinateSystem;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.pt.Envelope;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;

// J2SE dependencies
import java.rmi.RemoteException;


/**
 * A one-dimensional coordinate system suitable for vertical measurements.
 *
 * @version $Id: VerticalCoordinateSystem.java,v 1.11 2003/11/20 22:18:25 jive Exp $
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_VerticalCoordinateSystem
 */
public class VerticalCoordinateSystem extends CoordinateSystem {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8629573233560414552L;
    
    /**
     * Default vertical coordinate system using ellipsoidal datum.
     * Ellipsoidal heights are measured along the normal to the
     * ellipsoid used in the definition of horizontal datum.
     */
    public static final VerticalCoordinateSystem ELLIPSOIDAL = (VerticalCoordinateSystem) pool.canonicalize(
                    new VerticalCoordinateSystem("Ellipsoidal", VerticalDatum.ELLIPSOIDAL));
    
    /**
     * The vertical datum.
     */
    private final VerticalDatum datum;
    
    /**
     * Units used along the vertical axis.
     */
    private final Unit unit;
    
    /**
     * Axis details for vertical dimension within coordinate system.
     */
    private final AxisInfo axis;
    
    /**
     * Creates a vertical coordinate system from a datum. Units
     * will be metres and values will be increasing upward.
     *
     * @param name  Name to give new object.
     * @param datum Datum to use for new coordinate system.
     */
    public VerticalCoordinateSystem(final CharSequence name, final VerticalDatum datum) {
        this(name, datum, Unit.METRE, AxisInfo.ALTITUDE);
    }
    
    /**
     * Creates a vertical coordinate system from a datum and linear units.
     *
     * @param name  Name to give new object.
     * @param datum Datum to use for new coordinate system.
     * @param unit  Units to use for new coordinate system.
     * @param axis  Axis to use for new coordinate system.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createVerticalCoordinateSystem
     */
    public VerticalCoordinateSystem(final CharSequence  name,
                                    final VerticalDatum datum,
                                    final Unit          unit,
                                    final AxisInfo      axis)
    {
        super(name);
        this.datum = datum;
        this.unit  = unit;
        this.axis  = axis;
        ensureNonNull("datum", datum);
        ensureNonNull("unit",  unit );
        ensureNonNull("axis",  axis );
        ensureLinearUnit(unit);
        checkAxis(datum.getDatumType());
    }
 
    /**
     * Returns the dimension of this coordinate system, which is 1.
     *
     * @see org.opengis.cs.CS_VerticalCoordinateSystem#getDimension()
     */
    public final int getDimension() {
        return 1;
    }
    
    /**
     * Gets the vertical datum, which indicates the measurement method.
     *
     * @task REVISIT: in a future version (when J2SE 1.5 will be available), we <em>may</em>
     *                make this method public, change its return type to {@link VerticalDatum}
     *                and deprecate the {@link #getVerticalDatum} method.
     */
    final Datum getDatum() {
        return getVerticalDatum();
    }
    
    /**
     * Gets the vertical datum, which indicates the measurement method.
     *
     * @see org.opengis.cs.CS_VerticalCoordinateSystem#getVerticalDatum()
     */
    public VerticalDatum getVerticalDatum() {
        return datum;
    }
    
    /**
     * Gets axis details for vertical dimension within coordinate system.
     * A vertical coordinate system has only one axis, always at index 0.
     *
     * @param dimension Zero based index of axis.
     *
     * @see org.opengis.cs.CS_VerticalCoordinateSystem#getAxis(int)
     */
    public AxisInfo getAxis(final int dimension) {
        final int maxDim = getDimension();
        if (dimension>=0 && dimension<maxDim) {
            return axis;
        }
        throw new IndexOutOfBoundsException(Resources.format(
                ResourceKeys.ERROR_INDEX_OUT_OF_BOUNDS_$1, new Integer(dimension)));
    }
    
    /**
     * Gets units for dimension within coordinate system.
     * A vertical coordinate system has only one unit,
     * always at index 0.
     *
     * @param dimension Must be 0.
     *
     * @see org.opengis.cs.CS_VerticalCoordinateSystem#getUnits(int)
     * @see org.opengis.cs.CS_VerticalCoordinateSystem#getVerticalUnit()
     */
    public Unit getUnits(final int dimension) {
        final int maxDim = getDimension();
        if (dimension>=0 && dimension<maxDim) {
            return unit;
        }
        throw new IndexOutOfBoundsException(Resources.format(
                ResourceKeys.ERROR_INDEX_OUT_OF_BOUNDS_$1, new Integer(dimension)));
    }
    
    /**
     * Compare this coordinate system with the specified object for equality.
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
            final VerticalCoordinateSystem that = (VerticalCoordinateSystem) object;
            return equals(this.datum, that.datum, compareNames) &&
                   equals(this.unit , that.unit               ) &&
                   equals(this.axis , that.axis               );
        }
        return false;
    }

    /**
     * Returns a hash value for this coordinate system. {@linkplain #getName Name},
     * {@linkplain #getAlias alias}, {@linkplain #getAuthorityCode authority code}
     * and the like are not taken in account. In other words, two coordinate systems
     * will return the same hash value if they are equal in the sense of
     * <code>{@link #equals equals}(Info, <strong>false</strong>)</code>.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        return (int)serialVersionUID +
            37*(datum.hashCode() +
            37*(unit .hashCode() +
            37*(axis .hashCode())));
    }
    
    /**
     * Fills the part inside "[...]".
     * Used for formatting Well Known Text (WKT).
     */
    String addString(final StringBuffer buffer, final Unit context) {
        buffer.append(", ");
        buffer.append(datum);
        buffer.append(", ");
        addUnit(buffer, unit);
        buffer.append(", ");
        buffer.append(axis);
        return "VERT_CS";
    }
    
    /**
     * Returns an OpenGIS interface for this vertical coordinate
     * system. The returned object is suitable for RMI use.
     *
     * Note: The returned type is a generic {@link Object} in order
     *       to avoid premature class loading of OpenGIS interface.
     */
    final Object toOpenGIS(final Object adapters) throws RemoteException {
        return new Export(adapters);
    }
    
    
    
    
    /////////////////////////////////////////////////////////////////////////
    ////////////////                                         ////////////////
    ////////////////             OPENGIS ADAPTER             ////////////////
    ////////////////                                         ////////////////
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Wraps a {@link VerticalCoordinateSystem} object for use with OpenGIS.
     * This class is suitable for RMI use.
     */
    private final class Export extends CoordinateSystem.Export implements CS_VerticalCoordinateSystem {
        /**
         * Constructs a remote object.
         */
        protected Export(final Object adapters) throws RemoteException {
            super(adapters);
        }
        
        /**
         * Gets the vertical datum, which indicates the measurement method.
         */
        public CS_VerticalDatum getVerticalDatum() throws RemoteException {
            return adapters.export(VerticalCoordinateSystem.this.getVerticalDatum());
        }
        
        /**
         * Gets the units used along the vertical axis.
         */
        public CS_LinearUnit getVerticalUnit() throws RemoteException {
            return (CS_LinearUnit) adapters.export(VerticalCoordinateSystem.this.getUnits());
        }
    }
}
