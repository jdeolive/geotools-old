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
import org.opengis.cs.CS_CoordinateSystem;
import org.opengis.cs.CS_CompoundCoordinateSystem;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.pt.Envelope;
import org.geotools.pt.CoordinatePoint;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;

// J2SE dependencies
import java.rmi.RemoteException;


/**
 * An aggregate of two coordinate systems.
 * One of these is usually a two-dimensional coordinate system such as a
 * geographic or a projected coordinate system with a horizontal datum.
 * The other is a one-dimensional coordinate system with a vertical datum.
 *
 * @version $Id: CompoundCoordinateSystem.java,v 1.6 2003/01/20 23:16:07 desruisseaux Exp $
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_CompoundCoordinateSystem
 */
public class CompoundCoordinateSystem extends CoordinateSystem {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -488997059924367289L;
    
    /**
     * A default three-dimensional coordinate system for use with geographic
     * coordinates with heights above the ellipsoid. The head coordinate
     * system is {@link GeographicCoordinateSystem#WGS84} and the tail
     * coordinate system is {@link VerticalCoordinateSystem#ELLIPSOIDAL}.
     */
    public static final CompoundCoordinateSystem WGS84 = (CompoundCoordinateSystem) pool.canonicalize(
                    new CompoundCoordinateSystem("WGS84", GeographicCoordinateSystem.WGS84,
                                                          VerticalCoordinateSystem.ELLIPSOIDAL));
    
    /**
     * First sub-coordinate system.
     */
    private final CoordinateSystem head;
    
    /**
     * Second sub-coordinate system.
     */
    private final CoordinateSystem tail;
    
    /**
     * Creates a compound coordinate system.
     *
     * @param name Name to give new object.
     * @param head Coordinate system to use for earlier ordinates.
     * @param tail Coordinate system to use for later ordinates.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createCompoundCoordinateSystem
     */
    public CompoundCoordinateSystem(final CharSequence     name,
                                    final CoordinateSystem head,
                                    final CoordinateSystem tail)
    {
        super(name);
        this.head = head;
        this.tail = tail;
        ensureNonNull("head", head);
        ensureNonNull("tail", tail);
        checkAxis(null);
    }
    
    /**
     * Returns the first sub-coordinate system.
     *
     * @see org.opengis.cs.CS_CompoundCoordinateSystem#getHeadCS()
     */
    public CoordinateSystem getHeadCS() {
        return head;
    }
    
    /**
     * Returns the second sub-coordinate system.
     *
     * @see org.opengis.cs.CS_CompoundCoordinateSystem#getTailCS()
     */
    public CoordinateSystem getTailCS() {
        return tail;
    }
    
    /**
     * Returns the dimension of the coordinate system.
     *
     * @see org.opengis.cs.CS_CompoundCoordinateSystem#getDimension()
     */
    public int getDimension() {
        return head.getDimension() + tail.getDimension();
    }
    
    /**
     * Gets axis details for dimension within coordinate system.
     * Each dimension in the coordinate system has a corresponding axis.
     *
     * @see org.opengis.cs.CS_CompoundCoordinateSystem#getAxis(int)
     */
    public AxisInfo getAxis(final int dimension) {
        if (dimension >= 0) {
            final int headDim = head.getDimension();
            if (dimension < headDim) {
                return head.getAxis(dimension);
            }
            final int dim = dimension-headDim;
            if (dim < tail.getDimension()) {
                return tail.getAxis(dim);
            }
        }
        throw new IndexOutOfBoundsException(Resources.format(
                    ResourceKeys.ERROR_INDEX_OUT_OF_BOUNDS_$1, new Integer(dimension)));
    }
    
    /**
     * Gets units for dimension within coordinate system.
     * Each dimension in the coordinate system has corresponding units.
     *
     * @see org.opengis.cs.CS_CompoundCoordinateSystem#getUnits(int)
     */
    public Unit getUnits(final int dimension) {
        if (dimension >= 0) {
            final int headDim = head.getDimension();
            if (dimension < headDim) {
                return head.getUnits(dimension);
            }
            final int dim = dimension-headDim;
            if (dim < tail.getDimension()) {
                return head.getUnits(dim);
            }
        }
        throw new IndexOutOfBoundsException(Resources.format(
                    ResourceKeys.ERROR_INDEX_OUT_OF_BOUNDS_$1, new Integer(dimension)));
    }
    
    /**
     * Gets default envelope of coordinate system.
     *
     * @see org.opengis.cs.CS_CompoundCoordinateSystem#getDefaultEnvelope()
     */
    public Envelope getDefaultEnvelope() {
        final Envelope    headEnv = head.getDefaultEnvelope();
        final Envelope    tailEnv = tail.getDefaultEnvelope();
        final int         headDim = headEnv.getDimension();
        final int         tailDim = tailEnv.getDimension();
        final CoordinatePoint min = new CoordinatePoint(headDim+tailDim);
        final CoordinatePoint max = new CoordinatePoint(headDim+tailDim);
        for (int i=0; i<headDim; i++) {
            min.ord[i] = headEnv.getMinimum(i);
            max.ord[i] = headEnv.getMaximum(i);
        }
        for (int i=0; i<tailDim; i++) {
            min.ord[headDim+i] = tailEnv.getMinimum(i);
            max.ord[headDim+i] = tailEnv.getMaximum(i);
        }
        return new Envelope(min,max);
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
            final CompoundCoordinateSystem that = (CompoundCoordinateSystem) object;
            return equals(this.head, that.head, compareNames) &&
                   equals(this.tail, that.tail, compareNames);
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
            37*(head.hashCode() +
            37*(tail.hashCode()));
    }

    /**
     * Fill the part inside "[...]".
     * Used for formatting Well Known Text (WKT).
     */
    String addString(final StringBuffer buffer, final Unit context) {
        buffer.append(", ");
        buffer.append(head);
        buffer.append(", ");
        buffer.append(tail);
        return "COMPD_CS";
    }
    
    /**
     * Returns an OpenGIS interface for this compound coordinate
     * system. The returned object is suitable for RMI use.
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
     * Wrap a {@link CompoundCoordinateSystem} object for use with OpenGIS.
     * This class is suitable for RMI use.
     */
    private final class Export extends CoordinateSystem.Export implements CS_CompoundCoordinateSystem {
        /**
         * Constructs a remote object.
         */
        protected Export(final Object adapters) {
            super(adapters);
        }
        
        /**
         * Gets first sub-coordinate system.
         */
        public CS_CoordinateSystem getHeadCS() throws RemoteException {
            return adapters.export(CompoundCoordinateSystem.this.getHeadCS());
        }
        
        /**
         * Gets second sub-coordinate system.
         */
        public CS_CoordinateSystem getTailCS() throws RemoteException {
            return adapters.export(CompoundCoordinateSystem.this.getTailCS());
        }
    }
}
