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

// J2SE and Java3D dependencies
import java.util.Locale;
import java.util.Arrays;
import java.rmi.RemoteException;
import javax.vecmath.MismatchedSizeException;

// OpenGIS dependencies
import org.opengis.cs.CS_LocalDatum;
import org.opengis.cs.CS_LocalCoordinateSystem;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;

/**
 * A local coordinate system, with uncertain relationship to the world.
 * In general, a local coordinate system cannot be related to other
 * coordinate systems. However, if two objects supporting this interface
 * have the same dimension, axes, units and datum then client code
 * is permitted to assume that the two coordinate systems are identical.
 * This allows several datasets from a common source (e.g. a CAD system)
 * to be overlaid.
 * In addition, some implementations of the Coordinate Transformation (CT)
 * package may have a mechanism for correlating local datums.
 * (E.g. from a database of transformations, which is created and
 * maintained from real-world measurements.)
 *
 * @version $Id: LocalCoordinateSystem.java,v 1.15 2004/03/08 11:30:55 desruisseaux Exp $
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_LocalCoordinateSystem
 */
public class LocalCoordinateSystem extends CoordinateSystem {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2067954038057402418L;

    /**
     * A cartesian local coordinate system.
     */
    private static final class Cartesian extends LocalCoordinateSystem {
        /** The set of axis. */
        private static final AxisInfo[] AXIS = new AxisInfo[] {AxisInfo.X, AxisInfo.Y};

        /** Construct a coordinate system with the given name. */
        public Cartesian(final String name) {
            super(name, LocalDatum.UNKNOW, Unit.METRE, AXIS);
        }

        /** Returns the localized name for "Cartesian". */
        public String getName(final Locale locale) {
            return Resources.getResources(locale).getString(ResourceKeys.CARTESIAN);
        }

        // TODO: What about serialization?
    }

    /**
     * A two-dimensional cartesian coordinate system with
     * {@linkplain AxisInfo#X x},{@linkplain AxisInfo#Y y} axis in
     * {@linkplain Unit#METRE metres}. By default, this coordinate system has no transformation
     * path to any other coordinate system  (i.e. a map using this CS can't be reprojected to a
     * {@linkplain GeographicCoordinateSystem geographic coordinate system} for example).  This
     * is therefore a strict cartesian CS.
     */
    public static final LocalCoordinateSystem CARTESIAN = new Cartesian("Cartesian");

    /**
     * A two-dimensional wildcard coordinate system with
     * {@linkplain AxisInfo#X x},{@linkplain AxisInfo#Y y} axis in
     * {@linkplain Unit#METRE metres}. At the difference of {@link #CARTESIAN},
     * this coordinate system is treated specially by the default {@linkplain
     * org.geotools.ct.CoordinateTransformationFactory coordinate transformation factory}
     * with loose transformation rules: if no transformation path were found (for example
     * through a {@linkplain FittedCoordinateSystem fitted coordinate system}), then the
     * transformation from this CS to any CS with a compatible number of dimensions is
     * assumed to be the identity transform. This CS is usefull as a kind of wildcard
     * when no CS were explicitly specified.
     */
    public static final LocalCoordinateSystem PROMISCUOUS = new Cartesian("Promiscuous");
    
    /**
     * The local datum.
     */
    private final LocalDatum datum;
    
    /**
     * Units used along all axes.
     */
    private final Unit[] units;
    
    /**
     * Axes details.
     */
    private final AxisInfo[] axes;

    /**
     * Creates an array of units. We had to create a method
     * for this because the constructor needs to invoke it
     * before invoking <code>this(...)</code>.
     */
    static Unit[] expand(final Unit unit, final int count) {
        final Unit[] units = new Unit[count];
        Arrays.fill(units, unit);
        return units;
    }
    
    /**
     * Creates a local coordinate system. The dimension of the local coordinate
     * system is determined by the size of the axis array.  All the axes will
     * have the same units.
     *
     * @param name  Name to give new object.
     * @param datum Local datum to use in created coordinate system.
     * @param unit  Units to use for all axes in created coordinate system.
     * @param axes  Axes to use in created coordinate system.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createLocalCoordinateSystem
     */
    public LocalCoordinateSystem(final CharSequence name,
                                 final LocalDatum   datum,
                                 final Unit         unit,
                                 final AxisInfo[]   axes)
    {
        this(name, datum, expand(unit, axes.length), axes);
    }

    /**
     * Creates a local coordinate system. The dimension of the local coordinate
     * system is determined by the size of the axis array.  All the axes will
     * have the same units.
     *
     * @param name  Name to give new object.
     * @param datum Local datum to use in created coordinate system.
     * @param units Units to use in created coordinate system.
     * @param axes  Axes to use in created coordinate system.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createLocalCoordinateSystem
     */
    public LocalCoordinateSystem(final CharSequence name,
                                 final LocalDatum   datum,
                                 final Unit[]       units,
                                 final AxisInfo[]   axes)
    {
        super(name);
        ensureNonNull("datum", datum);
        ensureNonNull("units", units);
        ensureNonNull("axes",  axes );
        if (units.length != axes.length) {
            throw new MismatchedSizeException();
        }
        this.datum = datum;
        this.units = (Unit[])units.clone();
        this.axes  = (AxisInfo[])axes.clone();
        for (int i=0; i<this.axes.length; i++) {
            ensureNonNull("units", this.units, i);
            ensureNonNull("axes",  this.axes,  i);
        }
        checkAxis(datum.getDatumType());
    }
    
    /**
     * Gets the local datum.
     *
     * @task REVISIT: in a future version (when J2SE 1.5 will be available), we <em>may</em>
     *                make this method public, change its return type to {@link LocalDatum}
     *                and deprecate the {@link #getLocalDatum} method.
     */
    final Datum getDatum() {
        return getLocalDatum();
    }
    
    /**
     * Gets the local datum.
     *
     * @see org.opengis.cs.CS_LocalCoordinateSystem#getLocalDatum()
     */
    public LocalDatum getLocalDatum() {
        return datum;
    }
    
    /**
     * Dimension of the coordinate system.
     *
     * @see org.opengis.cs.CS_LocalCoordinateSystem#getDimension()
     */
    public int getDimension() {
        return axes.length;
    }
    
    /**
     * Gets axis details for dimension within coordinate system.
     *
     * @param dimension Zero based index of axis.
     *
     * @see org.opengis.cs.CS_LocalCoordinateSystem#getAxis(int)
     */
    public AxisInfo getAxis(final int dimension) {
        return axes[dimension];
    }
    
    /**
     * Gets units for dimension within coordinate system.
     *
     * @param dimension Zero based index of axis.
     *
     * @see org.opengis.cs.CS_LocalCoordinateSystem#getUnits(int)
     */
    public Unit getUnits(final int dimension) {
        return units[dimension];
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
            final LocalCoordinateSystem that = (LocalCoordinateSystem) object;
            return        equals(this.datum, that.datum, compareNames) &&
                   Arrays.equals(this.units, that.units) &&
                   Arrays.equals(this.axes , that.axes );
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
            37*(datum.hashCode());
        // 'units' and 'axes' would need special handling since they are arrays...
    }
    
    /**
     * Fills the part inside "[...]".
     * Used for formatting Well Known Text (WKT).
     */
    String addString(final StringBuffer buffer, final Unit context) {
        buffer.append(", ");
        buffer.append(datum);
        buffer.append(", ");
        addUnit(buffer, getUnits());
        for (int i=0; i<axes.length; i++) {
            buffer.append(", ");
            buffer.append(axes[i]);
        }
        return "LOCAL_CS";
    }
    
    /**
     * Returns an OpenGIS interface for this local coordinate
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
     * Wraps a {@link LocalCoordinateSystem} object for use with OpenGIS.
     * This class is suitable for RMI use.
     */
    private final class Export extends CoordinateSystem.Export implements CS_LocalCoordinateSystem {
        /**
         * Constructs a remote object.
         */
        protected Export(final Object adapters) throws RemoteException {
            super(adapters);
        }
        
        /**
         * Gets the local datum.
         */
        public CS_LocalDatum getLocalDatum() throws RemoteException {
            return adapters.export(LocalCoordinateSystem.this.getLocalDatum());
        }
    }
}
