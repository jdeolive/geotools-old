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
import org.opengis.cs.CS_Unit;
import org.opengis.cs.CS_AxisInfo;
import org.opengis.pt.PT_Envelope;
import org.opengis.cs.CS_CoordinateSystem;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.pt.Envelope;
import org.geotools.pt.Dimensioned;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;

// J2SE dependencies
import java.rmi.RemoteException;


/**
 * Base class for all coordinate systems.
 * A coordinate system is a mathematical space, where the elements of
 * the space are called positions.  Each position is described by a list
 * of numbers.  The length of the list corresponds to the dimension of
 * the coordinate system.  So in a 2D coordinate system each position is
 * described by a list containing 2 numbers.
 * <br><br>
 * However, in a coordinate system, not all lists of numbers correspond
 * to a position - some lists may be outside the domain of the coordinate
 * system.  For example, in a 2D Lat/Lon coordinate system, the list (91,91)
 * does not correspond to a position.
 * <br><br>
 * Some coordinate systems also have a mapping from the mathematical space
 * into locations in the real world.  So in a Lat/Lon coordinate system, the
 * mathematical position (lat, long) corresponds to a location on the surface
 * of the Earth.  This mapping from the mathematical space into real-world
 * locations is called a Datum.
 *
 * @version 1.00
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_CoordinateSystem
 */
public abstract class CoordinateSystem extends Info implements Dimensioned {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4539963180028417479L;
    
    /**
     * Construct a coordinate system.
     *
     * @param name The coordinate system name.
     */
    public CoordinateSystem(final CharSequence name) {
        super(name);
    }
    
    /**
     * Make sure there is no axis among the same direction
     * (e.g. two north axis, or a east and a west axis).
     * This methods may be invoked from subclasses constructors.
     *
     * @param  type The datum type, or <code>null</code> if unknow.
     * @throws IllegalArgumentException if two axis have the same direction.
     */
    final void checkAxis(final DatumType type) throws IllegalArgumentException {
        final int  dimension = getDimension();
        for (int i=0; i<dimension; i++) {
            AxisOrientation check = getAxis(i).orientation;
            if (type!=null && !type.isCompatibleOrientation(check)) {
                throw new IllegalArgumentException(Resources.format(
                            ResourceKeys.ERROR_ILLEGAL_AXIS_ORIENTATION_$2,
                            check.getName(null), Utilities.getShortClassName(this)));
            }
            check = check.absolute();
            if (!check.equals(AxisOrientation.OTHER)) {
                for (int j=i+1; j<dimension; j++) {
                    if (check.equals(getAxis(j).orientation.absolute())) {
                        final String nameI = getAxis(i).orientation.getName(null);
                        final String nameJ = getAxis(j).orientation.getName(null);
                        throw new IllegalArgumentException(Resources.format(
                                    ResourceKeys.ERROR_COLINEAR_AXIS_$2, nameI, nameJ));
                    }
                }
            }
        }
    }
    
    /**
     * Returns the dimension of the coordinate system.
     *
     * @see org.opengis.cs.CS_CoordinateSystem#getDimension()
     */
    public abstract int getDimension();
    
    /**
     * Gets axis details for dimension within coordinate system.
     * Each dimension in the coordinate system has a corresponding axis.
     *
     * @param dimension Zero based index of axis.
     *
     * @see org.opengis.cs.CS_CoordinateSystem#getAxis(int)
     */
    public abstract AxisInfo getAxis(int dimension);
    
    /**
     * Gets units for dimension within coordinate system.
     * Each dimension in the coordinate system has corresponding units.
     *
     * @param dimension Zero based index of axis.
     *
     * @see org.opengis.cs.CS_CoordinateSystem#getUnits(int)
     */
    public abstract Unit getUnits(int dimension);
    
    /**
     * If all dimensions use the same units, returns this
     * units. Otherwise, returns <code>null</code>.
     */
    final Unit getUnits() {
        Unit units = null;
        for (int i=getDimension(); --i>=0;) {
            final Unit check = getUnits(i);
            if (units==null) {
                units = check;
            } else if (!units.equals(check)) {
                return null;
            }
        }
        return units;
    }
    
    /**
     * Returns the datum.
     */
    Datum getDatum() {
        return null;
    }
    
    /**
     * Gets default envelope of coordinate system.
     * Coordinate systems which are bounded should return the minimum bounding
     * box of their domain.  Unbounded coordinate systems should return a box
     * which is as large as is likely to be used.  For example, a (lon,lat)
     * geographic coordinate system in degrees should return a box from
     * (-180,-90) to (180,90), and a geocentric coordinate system could return
     * a box from (-r,-r,-r) to (+r,+r,+r) where r is the approximate radius
     * of the Earth.
     * <br><br>
     * The default implementation returns an envelope with infinite bounds.
     *
     * @see org.opengis.cs.CS_CoordinateSystem#getDefaultEnvelope()
     */
    public Envelope getDefaultEnvelope() {
        final int     dimension = getDimension();
        final Envelope envelope = new Envelope(dimension);
        for (int i=dimension; --i>=0;) {
            envelope.setRange(i, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        }
        return envelope;
    }
    
    /**
     * Returns  <code>true</code> if this coordinate system is equivalents to
     * the specified coordinate system. Two coordinate systems are considered
     * equivalent if the {@link net.seagis.ct.CoordinateTransformation} from
     * <code>this</code> to <code>cs</code> would be the identity transform.
     * The <code>equivalents</code> method is less strict than <code>equals</code>
     * in that it doesn't compare names, alias, authority codes or others similar
     * informations.
     *
     * @param  cs The coordinate system (may be <code>null</code>).
     * @return <code>true</code> if both coordinate systems are equivalent.
     */
    public boolean equivalents(final CoordinateSystem cs) {
        return (cs!=null) && cs.getClass().equals(getClass());
    }
    
    /**
     * Compares the specified object with
     * this coordinate system for equality.
     */
    public boolean equals(final Object object) {
        if (object==this) {
            // Slight optimization
            return true;
        }
        return super.equals(object) && equivalents((CoordinateSystem)object);
    }
    
    /**
     * Returns an OpenGIS interface for this coordinate
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
     * Wrap a {@link CoordinateSystem} object for use with OpenGIS.
     * This class is suitable for RMI use.
     *
     * @version 1.0
     * @author Martin Desruisseaux
     */
    class Export extends Info.Export implements CS_CoordinateSystem {
        /**
         * Construct a remote object.
         */
        protected Export(final Object adapters) {
            super(adapters);
        }
        
        /**
         * Dimension of the coordinate system.
         */
        public int getDimension() throws RemoteException {
            return CoordinateSystem.this.getDimension();
        }
        
        /**
         * Gets axis details for dimension within coordinate system.
         */
        public CS_AxisInfo getAxis(final int dimension) throws RemoteException {
            return adapters.export(CoordinateSystem.this.getAxis(dimension));
        }
        
        /**
         * Gets units for dimension within coordinate system.
         */
        public CS_Unit getUnits(final int dimension) throws RemoteException {
            return adapters.export(CoordinateSystem.this.getUnits(dimension));
        }
        
        /**
         * Gets default envelope of coordinate system.
         */
        public PT_Envelope getDefaultEnvelope() throws RemoteException {
            return adapters.PT.export(CoordinateSystem.this.getDefaultEnvelope());
        }
    }
}
