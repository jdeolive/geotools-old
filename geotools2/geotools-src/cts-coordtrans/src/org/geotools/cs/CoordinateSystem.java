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

// OpenGIS 1.0 dependencies
import org.opengis.cs.CS_Unit;
import org.opengis.cs.CS_AxisInfo;
import org.opengis.pt.PT_Envelope;
import org.opengis.cs.CS_CoordinateSystem;

// OpenGIS 2.0 dependencies
import org.opengis.sc.CoordinateReferenceSystem;

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
 * <br><br>
 * <STRONG>NOTE:</STRONG> This <code>CoordinateSystem</code> class implements the OpenGIS&reg;
 * Coordinate System specification 1.0, not 2.0. The {@link CoordinateReferenceSystem} interface
 * come from specification 2.0 and appears in the <code>implements</code> clause only as a patch
 * for anticipating a future transition to OGC spec. 2.0.
 *
 * @version $Id: CoordinateSystem.java,v 1.14 2004/03/08 11:30:55 desruisseaux Exp $
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_CoordinateSystem
 */
public abstract class CoordinateSystem extends Info implements Dimensioned,
                                                               CoordinateReferenceSystem
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4539963180028417479L;
        
    /**
     * Constructs a coordinate system.
     *
     * @param name The coordinate system name.
     */
    public CoordinateSystem(final CharSequence name ) {
        super(name);        
    }
    
    /**
     * Makes sure there is no axis along the same direction
     * (e.g. two north axes, or an east and a west axis).
     * These methods may be invoked from subclass constructors.
     *
     * @param  type The datum type, or <code>null</code> if unknown.
     * @throws IllegalArgumentException if two axes have the same direction.
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
     * If all dimensions use the same units, returns these units.
     * Otherwise, returns <code>null</code>.
     *
     * @task REVISIT: This method is currently used in place of  <code>getLinearUnit()</code>  and
     *                <code>getAngularUnit()</code> which, according OpenGIS specification, should
     *                appears in various subclasses.  Geotools implementation ommits those methods
     *                because they don't bring much, since we are using a common <code>Unit</code>
     *                class for all units rather than <code>CS_[Linear/Angular]Unit</code>. Should
     *                we make this method public? It would be usefull to
     *                <code>org.geotools.io.coverage.PropertyParser.UNITS</code>, but we need to
     *                decide what to do if units are not the same for all axis (returning null is
     *                probably not the best thing to do).
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
     * Returns the datum, which indicates the measurement method.
     *
     * @throws IllegalStateException If this coordinate system has more than one datum.
     *         This exception can occurs if this coordinate system is an instance of
     *         {@link CompoundCoordinateSystem}.
     *
     * @task REVISIT: in a future version (when J2SE 1.5 will be available), we <em>may</em> make
     *                this method public. It should probably be abstract. This method would be
     *                usefull to <code>org.geotools.io.coverage.PropertyParser.DATUM</code>.
     */
    Datum getDatum() throws IllegalStateException {
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
     * Returns an OpenGIS interface for this coordinate
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
     * Wrap a {@link CoordinateSystem} object for use with OpenGIS.
     * This class is suitable for RMI use.
     */
    class Export extends Info.Export implements CS_CoordinateSystem {
        /**
         * Constructs a remote object.
         */
        protected Export(final Object adapters) throws RemoteException {
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
            return adapters.export(CoordinateSystem.this.getDefaultEnvelope());
        }
    }
}
