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
import org.opengis.cs.CS_LocalDatum;
import org.opengis.cs.CS_LocalCoordinateSystem;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.pt.Envelope;
import org.geotools.pt.CoordinatePoint;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;

// J2SE dependencies
import java.util.Arrays;
import java.rmi.RemoteException;


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
 * @version 1.00
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
     * The local datum.
     */
    private final LocalDatum datum;
    
    /**
     * Units used along all axis.
     */
    private final Unit[] units;
    
    /**
     * Axes details.
     */
    private final AxisInfo[] axes;
    
    /**
     * Creates a local coordinate system. The dimension of the local coordinate
     * system is determined by the size of the axis array.  All the axes will
     * have the same units.  If you want to make a coordinate system with mixed
     * units, then you can make a compound coordinate system from different local
     * coordinate systems.
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
        super(name);
        ensureNonNull("datum", datum);
        ensureNonNull("unit",  unit );
        ensureNonNull("axes",  axes );
        this.datum = datum;
        this.units = new Unit[axes.length];
        this.axes  = (AxisInfo[])axes.clone();
        for (int i=0; i<this.axes.length; i++) {
            this.units[i] = unit;
            ensureNonNull("axes", this.axes, i);
        }
        checkAxis(datum.getDatumType());
    }
    
    /**
     * Override {@link CoordinateSystem#getDatum()}.
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
            final LocalCoordinateSystem that = (LocalCoordinateSystem) cs;
            return Utilities.equals(this.datum, that.datum) &&
                   Arrays   .equals(this.units, that.units) &&
                   Arrays   .equals(this.axes , that.axes );
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
     *       to avoid too early class loading of OpenGIS interface.
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
     * Wrap a {@link LocalCoordinateSystem} object for use with OpenGIS.
     * This class is suitable for RMI use.
     *
     * @version 1.0
     * @author Martin Desruisseaux
     */
    private final class Export extends CoordinateSystem.Export implements CS_LocalCoordinateSystem {
        /**
         * Construct a remote object.
         */
        protected Export(final Object adapters) {
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
