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

// Time
import java.util.Date;
import java.util.TimeZone;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.pt.Envelope;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * A one-dimensional coordinate system suitable for time measurements.
 *
 * @version $Id: TemporalCoordinateSystem.java,v 1.8 2003/05/13 10:58:47 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class TemporalCoordinateSystem extends CoordinateSystem {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 4436983518157910233L;
    
    /**
     * The temporal datum.
     */
    private final TemporalDatum datum;
    
    /**
     * Axis details for time dimension within coordinate system.
     */
    private final AxisInfo axis;
    
    /**
     * Units used along the time axis.
     */
    private final Unit unit;
    
    /**
     * The epoch, in milliseconds since January 1, 1970, 00:00:00 UTC.
     */
    private final long epoch;
    
    /**
     * Creates a temporal coordinate system. Datum is UTC,
     * units are days and values are increasing toward future.
     *
     * @param name  Name to give new object.
     * @param epoch The epoch (i.e. date of origin).
     */
    public TemporalCoordinateSystem(final CharSequence name, final Date epoch) {
        this(name, TemporalDatum.UTC, Unit.DAY, epoch, AxisInfo.TIME);
    }
    
    /**
     * Creates a temporal coordinate system from a datum and time units.
     *
     * @param name  Name to give new object.
     * @param datum Datum to use for new coordinate system.
     * @param unit  Units to use for new coordinate system.
     * @param epoch The epoch (i.e. date of origin).
     * @param axis  Axis to use for new coordinate system.
     */
    public TemporalCoordinateSystem(final CharSequence  name,
                                    final TemporalDatum datum,
                                    final Unit          unit,
                                    final Date          epoch,
                                    final AxisInfo      axis)
    {
        super(name);
        ensureNonNull("datum", datum);
        ensureNonNull("unit",  unit );
        ensureNonNull("epoch", epoch);
        ensureNonNull("axis",  axis );
        this.datum    = datum;
        this.unit     = unit;
        this.epoch    = epoch.getTime();
        this.axis     = axis;
        ensureTimeUnit(unit);
        checkAxis(datum.getDatumType());
    }
    
    /**
     * Returns the dimension of this coordinate system, which is 1.
     */
    public final int getDimension() {
        return 1;
    }
    
    /**
     * Gets the temporal datum, which indicates the measurement method.
     *
     * @task REVISIT: in a future version (when J2SE 1.5 will be available), we <em>may</em>
     *                make this method public, change its return type to {@link TemporalDatum}
     *                and deprecate the {@link #getTemporalDatum} method.
     */
    final Datum getDatum() {
        return getTemporalDatum();
    }
    
    /**
     * Gets the temporal datum, which indicates the measurement method.
     */
    public TemporalDatum getTemporalDatum() {
        return datum;
    }
    
    /**
     * Returns the epoch. The epoch is the origin of
     * the time axis, i.e. the date for value zero.
     */
    public Date getEpoch() {
        return new Date(epoch);
    }
    
    /**
     * Gets axis details for temporal dimension within coordinate system.
     * A temporal coordinate system has only one axis, always at index 0.
     *
     * @param dimension Zero based index of axis.
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
     * A temporal coordinate system has only one unit,
     * always at index 0.
     *
     * @param dimension Must be 0.
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
            final TemporalCoordinateSystem that = (TemporalCoordinateSystem) object;
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
        return "TEMPORAL_CS";
    }
}
