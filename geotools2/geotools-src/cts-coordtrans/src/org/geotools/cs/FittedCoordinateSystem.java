/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2003, Centre for Computational Geography
 * (C) 2003, Institut de Recherche pour le Développement
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
import org.opengis.cs.CS_FittedCoordinateSystem;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.ct.MathTransform;
import org.geotools.pt.MismatchedDimensionException;

// J2SE and Java3D dependencies
import java.util.Arrays;
import java.rmi.RemoteException;


/**
 * A coordinate system which sits inside another coordinate system. 
 * The fitted coordinate system can be rotated and shifted, or use
 * any other math transform to inject itself into the base coordinate
 * system.
 *
 * @version $Id: FittedCoordinateSystem.java,v 1.1 2003/01/20 23:16:09 desruisseaux Exp $
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_FittedCoordinateSystem
 */
public class FittedCoordinateSystem extends CoordinateSystem {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -3915469486263759761L;

    /**
     * The base coordinate system.
     */
    private final CoordinateSystem base;

    /**
     * The transform from this CS to base CS.
     */
    private final MathTransform toBase;

    /**
     * Axes details.
     */
    private final AxisInfo[] axes;

    /**
     * Creates a fitted coordinate system. 
     * The units of the axes in the fitted coordinate system will be inferred
     * from the units of the base coordinate system.  If the affine map
     * performs a rotation, then any mixed axes must have identical units.
     * For example, a (<var>lat_deg</var>,<var>lon_deg</var>,<var>height_feet</var>)
     * system can be rotated in the (<var>lat</var>,<var>lon</var>) plane, since both
     * affected axes are in degrees.  But you should not rotate this coordinate system
     * in any other plane.
     *
     * @param name   Name to give new object.
     * @param base   Coordinate system to base the fitted CS on.
     * @param toBase The transform from created CS to base CS.
     * @param axes   Axes for fitted coordinate system. The number of axes must match
     *               the source dimension of the transform <code>toBase</code>. If this
     *               argument is <code>null</code>, then axes will be infered from the
     *               base coordinate system.
     * @throws MismatchedDimensionException if source and target dimensions of <code>toBase</code>
     *         don't match the number to axes and the base coordinate system dimensions.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createFittedCoordinateSystem
     */
    public FittedCoordinateSystem(final CharSequence     name,
                                  final CoordinateSystem base,
                                  final MathTransform  toBase,
                                  final AxisInfo[]       axes)
    {
        super(name);
        ensureNonNull(  "base",   base);
        ensureNonNull("toBase", toBase);
        int dim1, dim2;
        if ((dim1=toBase.getDimTarget()) != (dim2=base.getDimension())) {
            throw new MismatchedDimensionException(dim1, dim2);
        }
        if ((dim1=toBase.getDimSource()) != (dim2=axes.length)) {
            throw new MismatchedDimensionException(dim1, dim2);
        }
        this.base   = base;
        this.toBase = toBase;
        if (axes != null) {
            this.axes = (AxisInfo[])axes.clone();
        } else {
            this.axes = new AxisInfo[base.getDimension()];
            for (int i=0; i<this.axes.length; i++) {
                this.axes[i] = base.getAxis(i);
            }
        }
        for (int i=0; i<this.axes.length; i++) {
            ensureNonNull("axes", this.axes, i);
        }
    }

    /**
     * Returns the underlying coordinate system. Its dimension is equals to
     * <code>{@link #getToBase}.getDimTarget()</code>.
     *
     * @see org.opengis.cs.CS_FittedCoordinateSystem#getBaseCoordinateSystem()
     */
    public CoordinateSystem getBaseCoordinateSystem() {
        return base;
    }

    /**
     * Returns the math transform to the base coordinate system.
     * The dimension of this fitted coordinate system is
     * determined by the source dimension of the math transform.  
     * The transform should be one-to-one within this coordinate
     * system's domain, and the base coordinate system dimension must be
     * at least as big as the dimension of this coordinate system.
     *
     * @see org.opengis.cs.CS_FittedCoordinateSystem#getToBase()
     */
    public MathTransform getToBase() {
        return toBase;
    }
    
    /**
     * Returns the dimension of the coordinate system. This is equals to
     * <code>{@link #getToBase}.getDimSource()</code>.
     *
     * @see org.opengis.cs.CS_FittedCoordinateSystem#getDimension()
     */
    public int getDimension() {
        return axes.length;
    }
    
    /**
     * Gets axis details for dimension within coordinate system.
     *
     * @param dimension Zero based index of axis.
     *
     * @see org.opengis.cs.CS_FittedCoordinateSystem#getAxis(int)
     */
    public AxisInfo getAxis(final int dimension) {
        return axes[dimension];
    }
    
    /**
     * Gets units for dimension within coordinate system.
     *
     * @param dimension Zero based index of axis.
     *
     * @see org.opengis.cs.CS_FittedCoordinateSystem#getUnits(int)
     */
    public Unit getUnits(final int dimension) {
        return base.getUnits(dimension);
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
            final FittedCoordinateSystem that = (FittedCoordinateSystem) object;
            return        equals(this.base,   that.base, compareNames) &&
                          equals(this.toBase, that.toBase) &&
                   Arrays.equals(this.axes,   that.axes);
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
            37*(base  .hashCode() +
            37*(toBase.hashCode()));
        // 'axes' would need special handling since it is an array...
    }
    
    /**
     * Fills the part inside "[...]".
     * Used for formatting Well Known Text (WKT).
     */
    String addString(final StringBuffer buffer, final Unit context) {
        buffer.append(", ");
        buffer.append(toBase);
        buffer.append(", ");
        buffer.append(base);
        return "FITTED_CS";
    }
    
    /**
     * Returns an OpenGIS interface for this local coordinate
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
     * Wraps a {@link LocalCoordinateSystem} object for use with OpenGIS.
     * This class is suitable for RMI use.
     */
    private final class Export extends CoordinateSystem.Export implements CS_FittedCoordinateSystem {
        /**
         * Constructs a remote object.
         */
        protected Export(final Object adapters) {
            super(adapters);
        }
        
        /**
         * Gets underlying coordinate system.
         */
        public CS_CoordinateSystem getBaseCoordinateSystem() throws RemoteException {
            return adapters.export(base);
        }
        
        /**
         * Gets Well-Known Text of a math transform to the base coordinate system.
         */
        public String getToBase() throws RemoteException {
            return toBase.toString();
        }
    }
}
