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
import org.opengis.cs.CS_HorizontalDatum;
import org.opengis.cs.CS_HorizontalCoordinateSystem;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;

// J2SE dependencies
import java.rmi.RemoteException;


/**
 * A 2D coordinate system suitable for positions on the Earth's surface.
 *
 * @version $Id: HorizontalCoordinateSystem.java,v 1.9 2003/05/13 10:58:47 desruisseaux Exp $
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_HorizontalCoordinateSystem
 */
public abstract class HorizontalCoordinateSystem extends CoordinateSystem {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 350661821531782559L;
    
    /**
     * The horizontal datum.
     */
    private final HorizontalDatum datum;
    
    /**
     * Details of 0th ordinates.
     */
    private final AxisInfo axis0;
    
    /**
     * Details of 1th ordinates.
     */
    private final AxisInfo axis1;
    
    /**
     * Constructs a coordinate system.
     *
     * @param name  The coordinate system name.
     * @param datum The horizontal datum.
     * @param axis0 Details of 0th ordinates in created coordinate system.
     * @param axis1 Details of 1st ordinates in created coordinate system.
     */
    public HorizontalCoordinateSystem(final CharSequence    name,
                                      final HorizontalDatum datum,
                                      final AxisInfo        axis0,
                                      final AxisInfo        axis1) {
        super(name);
        this.datum = datum;
        this.axis0 = axis0;
        this.axis1 = axis1;
        ensureNonNull("datum", datum);
        ensureNonNull("axis0", axis0);
        ensureNonNull("axis1", axis1);
        checkAxis(datum.getDatumType());
    }
    
    /**
     * Returns the dimension of this coordinate system, which is 2.
     *
     * @see org.opengis.cs.CS_HorizontalCoordinateSystem#getDimension()
     */
    public final int getDimension() {
        return 2;
    }
    
    /**
     * Returns the horizontal datum.
     *
     * @task REVISIT: in a future version (when J2SE 1.5 will be available), we <em>may</em>
     *                make this method public, change its return type to {@link HorizontalDatum}
     *                and deprecate the {@link #getHorizontalDatum} method.
     */
    final Datum getDatum() {
        return getHorizontalDatum();
    }
    
    /**
     * Returns the horizontal datum.
     *
     * @see org.opengis.cs.CS_HorizontalCoordinateSystem#getHorizontalDatum()
     */
    public HorizontalDatum getHorizontalDatum() {
        return datum;
    }
    
    /**
     * Gets axis details for dimension within coordinate system.
     *
     * @param dimension Zero based index of axis.
     *
     * @see org.opengis.cs.CS_HorizontalCoordinateSystem#getAxis(int)
     */
    public AxisInfo getAxis(final int dimension) {
        switch (dimension) {
            case 0:  return axis0;
            case 1:  return axis1;
            default: throw new IndexOutOfBoundsException(Resources.format(
                        ResourceKeys.ERROR_INDEX_OUT_OF_BOUNDS_$1, new Integer(dimension)));
        }
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
            final HorizontalCoordinateSystem that = (HorizontalCoordinateSystem) object;
            return equals(this.datum, that.datum, compareNames) &&
                   equals(this.axis0, that.axis0              ) &&
                   equals(this.axis1, that.axis1              );
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
            37*(axis0.hashCode() +
            37*(axis1.hashCode())));
    }
    
    /**
     * Fills the part inside "[...]".
     * Used for formatting Well Known Text (WKT).
     */
    String addString(final StringBuffer buffer, final Unit context) {
        buffer.append(", ");
        buffer.append(datum);
        buffer.append(", ");
        buffer.append(axis0);
        buffer.append(", ");
        buffer.append(axis1);
        return "HORZ_CS";
    }
    
    /**
     * Returns an OpenGIS interface for this horizontal coordinate
     * system. The returned object is suitable for RMI use.
     *
     * Note: The returned type is a generic {@link Object} in order
     *       to avoid premature class loading of OpenGIS interface.
     */
    Object toOpenGIS(final Object adapters) throws RemoteException {
        return new Export(adapters);
    }
    
    
    
    
    /////////////////////////////////////////////////////////////////////////
    ////////////////                                         ////////////////
    ////////////////             OPENGIS ADAPTER             ////////////////
    ////////////////                                         ////////////////
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Wrap a {@link HorizontalCoordinateSystem} object for use with OpenGIS.
     * This class is suitable for RMI use.
     */
    class Export extends CoordinateSystem.Export implements CS_HorizontalCoordinateSystem {
        /**
         * Constructs a remote object.
         */
        protected Export(final Object adapters) throws RemoteException {
            super(adapters);
        }
        
        /**
         * Returns the HorizontalDatum.
         */
        public CS_HorizontalDatum getHorizontalDatum() throws RemoteException {
            return adapters.export(HorizontalCoordinateSystem.this.getHorizontalDatum());
        }
    }
}
