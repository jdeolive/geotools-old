/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2000, Institut de Recherche pour le Développement
 * (C) 1999, Pêches et Océans Canada
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
 */
package org.geotools.axis;

// J2SE dependencies
import java.util.Date;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.Format;
import java.awt.RenderingHints;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.units.UnitException;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * A graduation using dates on a linear axis.
 *
 * @version $Id: DateGraduation.java,v 1.4 2004/02/12 20:45:52 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class DateGraduation extends AbstractGraduation {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -7590383805990568769L;

    /**
     * The minimal value for this graduation, in milliseconds ellapsed since January 1st,
     * 1970 (no matter what the graduation units are). Default to current time (today).
     */
    private long minimum = System.currentTimeMillis();

    /**
     * The maximal value for this graduation, in milliseconds ellapsed since January 1st,
     * 1970 (no matter what the graduation units are). Default to tomorrow.
     */
    private long maximum = minimum + 24*60*60*1000L;

    /**
     * The time zone for graduation labels.
     */
    private TimeZone timezone;

    /**
     * Construct a graduation with the supplied time zone.
     * Unit default to {@linkplain Unit#MILLISECOND millisecond}.
     *
     * @param  timezone The timezone.
     */
    public DateGraduation(final TimeZone timezone) {
        this(timezone, Unit.MILLISECOND);
    }

    /**
     * Construct a graduation with the supplied time zone and unit.
     *
     * @param  timezone The timezone.
     * @param  unit The unit. Must be compatible with {@link Unit#MILLISECOND}.
     * @throws UnitException if the supplied unit is not a time unit.
     */
    public DateGraduation(final TimeZone timezone, final Unit unit) throws UnitException {
        super(unit);
        ensureTimeUnit(unit);
        this.timezone = (TimeZone) timezone.clone();
    }

    /**
     * Check if the specified unit is a time unit.
     *
     * @param the unit to check.
     * @throws UnitException if the specified unit is not a time unit.
     */
    private static void ensureTimeUnit(final Unit unit) throws UnitException {
        if (unit==null || !Unit.MILLISECOND.canConvert(unit)) {
            throw new UnitException(Resources.format(ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                                                     "unit", unit));
        }
    }

    /**
     * Set the minimum value for this graduation. If the new minimum is greater
     * than the current maximum, then the maximum will also be set to a value
     * greater than or equals to the minimum.
     *
     * @param  time The new minimum.
     * @return <code>true</code> if the state of this graduation changed
     *         as a result of this call, or <code>false</code> if the new
     *         value is identical to the previous one.
     *
     * @see #setMaximum(Date)
     */
    public synchronized boolean setMinimum(final Date time) {
        final long value = time.getTime();
        long old = minimum;
        minimum = value;
        firePropertyChange("minimum", old, time);
        if (maximum < value) {
            old = maximum;
            maximum = value;
            firePropertyChange("maximum", old, time);
            return true;
        }
        return value != old;
    }

    /**
     * Set the maximum value for this graduation. If the new maximum is less
     * than the current minimum, then the minimum will also be set to a value
     * less than or equals to the maximum.
     *
     * @param  time The new maximum.
     * @return <code>true</code> if the state of this graduation changed
     *         as a result of this call, or <code>false</code> if the new
     *         value is identical to the previous one.
     *
     * @see #setMinimum(Date)
     */
    public synchronized boolean setMaximum(final Date time) {
        final long value = time.getTime();
        long old = maximum;
        maximum = value;
        firePropertyChange("maximum", old, time);
        if (minimum > value) {
            old = minimum;
            minimum = value;
            firePropertyChange("minimum", old, time);
            return true;
        }
        return value != old;
    }

    /**
     * Set the minimum value as a real number. This method converts the value to
     * {@linkplain Unit#MILLISECOND milliseconds} and invokes {@link #setMinimum(Date)}.
     */
    public final synchronized boolean setMinimum(final double value) {
        ensureFinite("minimum", value);
        return setMinimum(new Date(Math.round(Unit.MILLISECOND.convert(value, getUnit()))));
    }

    /**
     * Set the maximum value as a real number. This method converts the value to
     * {@linkplain Unit#MILLISECOND milliseconds} and invokes {@link #setMaximum(Date)}.
     */
    public final synchronized boolean setMaximum(final double value) {
        ensureFinite("maximum", value);
        return setMaximum(new Date(Math.round(Unit.MILLISECOND.convert(value, getUnit()))));
    }

    /**
     * Returns the minimal value for this graduation. The value is in units of {@link #getUnit}.
     * By default, it is the number of millisecondes ellapsed since January 1st, 1970 at 00:00 UTC.
     *
     * @see #setMinimum(double)
     * @see #getMaximum
     * @see #getRange
     */
    public double getMinimum() {
        return getUnit().convert(minimum, Unit.MILLISECOND);
    }

    /**
     * Returns the maximal value for this graduation. The value is in units of {@link #getUnit}.
     * By default, it is the number of millisecondes ellapsed since January 1st, 1970 at 00:00 UTC.
     *
     * @see #setMaximum(double)
     * @see #getMinimum
     * @see #getRange
     */
    public double getMaximum() {
        return getUnit().convert(maximum, Unit.MILLISECOND);
    }

    /**
     * Returns the graduation's range. This is equivalents to computing
     * <code>{@link #getMaximum}-{@link #getMinimum}</code>, but using
     * integer arithmetic.
     */
    public synchronized double getRange() {
        final Unit unit = getUnit();
        if (unit == Unit.MILLISECOND) {
            return maximum - minimum;
        } else {
            // TODO: we would need something similar to AffineTransform.deltaTransform(...)
            //       here in order to performs the conversion in a more efficient way.
            return unit.convert(maximum, Unit.MILLISECOND) -
                   unit.convert(minimum, Unit.MILLISECOND);
        }
    }

    /**
     * Returns the timezone for this graduation.
     */
    public TimeZone getTimeZone() {
        return timezone;
    }

    /**
     * Sets the time zone for this graduation. This
     * affect only the way labels are displayed.
     */
    public void setTimeZone(final TimeZone timezone) {
        this.timezone = (TimeZone) timezone.clone();
    }

    /**
     * Returns a string representation of the time zone for this graduation.
     */
    String getSymbol() {
        return getTimeZone().getDisplayName();
    }

    /**
     * Changes the graduation's units. This method will automatically
     * convert minimum and maximum values from the old units to the
     * new one.
     *
     * @param unit The new units, or <code>null</code> if unknow.
     *        If null, minimum and maximum values are not converted.
     * @throws UnitException if the specified unit is not a time unit.
     */
    public void setUnit(final Unit unit) throws UnitException {
        ensureTimeUnit(unit);
        // Nothing to convert here. The conversions are performed
        // on the fly by 'getMinimum()' / 'getMaximum()'.
        super.setUnit(unit);
    }

    /**
     * Returns the format to use for formatting labels. The format really used by
     * {@link TickIterator#currentLabel} may not be the same. For example, some
     * iterators may choose to show or hide hours, minutes and seconds.
     */
    public Format getFormat() {
        final DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                                                 DateFormat.SHORT, getLocale());
        format.setTimeZone(timezone);
        return format;
    }
    
    /**
     * Returns an iterator object that iterates along the graduation ticks
     * and provides access to the graduation values. If an optional {@link
     * RenderingHints} is specified, tick locations are adjusted according
     * values for {@link #VISUAL_AXIS_LENGTH} and {@link #VISUAL_TICK_SPACING}
     * keys.
     *
     * @param  hints Rendering hints, or <code>null</code> for the default hints.
     * @param  reuse An iterator to reuse if possible, or <code>null</code>
     *         to create a new one. A non-null object may help to reduce the
     *         number of object garbage-collected when rendering the axis.
     * @return A iterator to use for iterating through the graduation. This
     *         iterator may or may not be the <code>reuse</code> object.
     */
    public synchronized TickIterator getTickIterator(final RenderingHints hints,
                                                     final TickIterator   reuse)
    {
        final float visualAxisLength  = getVisualAxisLength (hints);
        final float visualTickSpacing = getVisualTickSpacing(hints);
        long minimum = this.minimum;
        long maximum = this.maximum;
        if (!(minimum<maximum)) {
            minimum = (minimum+maximum)/2 - 12*60*60*1000L;
            maximum = minimum + 24*60*60*1000L;
        }
        final DateIterator it;
        if (reuse instanceof DateIterator) {
            it = (DateIterator) reuse;
            it.setLocale(getLocale());
            it.setTimeZone(getTimeZone());
        } else {
            it = new DateIterator(getTimeZone(), getLocale());
        }
        it.init(minimum, maximum, visualAxisLength, visualTickSpacing);
        return it;
    }

    /**
     * Support for reporting property changes. This method can be called when a
     * property has changed. It will send the appropriate {@link PropertyChangeEvent}
     * to any registered {@link PropertyChangeListeners}.
     *
     * @param propertyName The property whose value has changed.
     * @param oldValue     The property's previous value.
     * @param newValue     The property's new value.
     */
    private final void firePropertyChange(final String propertyName,
                                          final long oldValue, final Date newValue)
    {
        if (oldValue != newValue.getTime()) {
            listenerList.firePropertyChange(propertyName, new Date(oldValue), newValue);
        }
    }

    /**
     * Compare this graduation with the specified object for equality.
     * This method do not compare registered listeners.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final DateGraduation that = (DateGraduation) object;
            return this.minimum == that.minimum &&
                   this.maximum == that.maximum &&
                   Utilities.equals(this.timezone, that.timezone);
        }
        return false;
    }

    /**
     * Returns a hash value for this graduation.
     */
    public int hashCode() {
        final long lcode = minimum + 37*maximum;
        int code = (int)lcode ^ (int)(lcode >>> 32);
        if (timezone != null) {
            code ^= timezone.hashCode();
        }
        return code ^ super.hashCode();
    }
}
