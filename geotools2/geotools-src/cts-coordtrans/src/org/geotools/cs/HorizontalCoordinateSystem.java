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
import org.opengis.cs.CS_HorizontalDatum;
import org.opengis.cs.CS_HorizontalCoordinateSystem;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;

// J2SE dependencies
import java.rmi.RemoteException;


/**
 * A 2D coordinate system suitable for positions on the Earth's surface.
 *
 * @version 1.00
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
     * Construct a coordinate system.
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
     * Override {@link CoordinateSystem#getDatum()}.
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
     * Returns  <code>true</code> if this coordinate system is equivalents to
     * the specified coordinate system. Two coordinate systems are considered
     * equivalent if the {@link org.geotools.ct.CoordinateTransformation} from
     * <code>this</code> to <code>cs</code> would be the identity transform.
     * The default implementation compare datum, units and axis, but ignore
     * name, alias and other meta-data informations.
     *
     * @param  cs The coordinate system (may be <code>null</code>).
     * @return <code>true</code> if both coordinate systems are equivalent.
     */
    public boolean equivalents(final CoordinateSystem cs) {
        if (cs==this) {
            return true;
        }
        if (super.equivalents(cs)) {
            final HorizontalCoordinateSystem that = (HorizontalCoordinateSystem) cs;
            return Utilities.equals(this.datum, that.datum) &&
                   Utilities.equals(this.axis0, that.axis0) &&
                   Utilities.equals(this.axis1, that.axis1);
        }
        return false;
    }
    
    /**
     * Fill the part inside "[...]".
     * Used for formatting Well Know Text (WKT).
     */
    String addString(final StringBuffer buffer, Object context) {
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
     *       to avoid too early class loading of OpenGIS interface.
     */
    Object toOpenGIS(final Object adapters) {
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
     *
     * @version 1.0
     * @author Martin Desruisseaux
     */
    class Export extends CoordinateSystem.Export implements CS_HorizontalCoordinateSystem {
        /**
         * Construct a remote object.
         */
        protected Export(final Object adapters) {
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
