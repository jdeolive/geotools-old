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
import java.util.Locale;
import java.text.Format;
import java.text.NumberFormat;
import java.awt.RenderingHints;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.units.UnitException;
import org.geotools.resources.Utilities;


/**
 * A graduation using numbers on a linear axis.
 *
 * @version $Id: NumberGraduation.java,v 1.2 2003/05/13 10:58:46 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class NumberGraduation extends AbstractGraduation {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -3074504745332240845L;

    /**
     * The minimal value for this graduation. Default to 0.
     */
    private double minimum = 0;

    /**
     * The maximal value for this graduation. Default to 10.
     */
    private double maximum = 10;

    /**
     * Construct a graduation with the supplied units.
     *
     * @param units The axis's units, or <code>null</code> if unknow.
     */
    public NumberGraduation(final Unit unit) {
        super(unit);
    }

    /**
     * Set the minimum value for this graduation. If the new minimum is greater
     * than the current maximum, then the maximum will also be set to a value
     * greater than or equals to the minimum.
     *
     * @param  value The new minimum in {@link #getUnit} units.
     * @return <code>true</code> if the state of this graduation changed
     *         as a result of this call, or <code>false</code> if the new
     *         value is identical to the previous one.
     * @throws IllegalArgumentException Si <code>value</code> is NaN ou infinite.
     *
     * @see #getMinimum
     * @see #setMaximum(double)
     */
    public synchronized boolean setMinimum(final double value) throws IllegalArgumentException {
        ensureFinite("minimum", value);
        double old = minimum;
        minimum    = value;
        final Double valueObject = new Double(value);
        listenerList.firePropertyChange("minimum", new Double(old), valueObject);
        if (maximum < value) {
            old = maximum;
            maximum = value;
            listenerList.firePropertyChange("maximum", new Double(old), valueObject);
            return true;
        }
        return Double.doubleToLongBits(value) != Double.doubleToLongBits(old);
    }

    /**
     * Set the maximum value for this graduation. If the new maximum is less
     * than the current minimum, then the minimum will also be set to a value
     * less than or equals to the maximum.
     *
     * @param  value The new maximum in {@link #getUnit} units.
     * @return <code>true</code> if the state of this graduation changed
     *         as a result of this call, or <code>false</code> if the new
     *         value is identical to the previous one.
     * @throws IllegalArgumentException If <code>value</code> is NaN ou infinite.
     *
     * @see #getMaximum
     * @see #setMinimum(double)
     */
    public synchronized boolean setMaximum(final double value) throws IllegalArgumentException {
        ensureFinite("maximum", value);
        double old = maximum;
        maximum    = value;
        final Double valueObject = new Double(value);
        listenerList.firePropertyChange("maximum", new Double(old), valueObject);
        if (minimum > value) {
            old = minimum;
            minimum = value;
            listenerList.firePropertyChange("minimum", new Double(old), valueObject);
            return true;
        }
        return Double.doubleToLongBits(value) != Double.doubleToLongBits(old);
    }

    /**
     * Returns the minimal value for this graduation.
     * @return The minimal value in {@link #getUnit} units.
     *
     * @see #setMinimum(double)
     * @see #getMaximum
     * @see #getRange
     */
    public double getMinimum() {
        return minimum;
    }

    /**
     * Returns the maximal value for this graduation.
     * @return The maximal value in {@link #getUnit} units.
     *
     * @see #setMaximum(double)
     * @see #getMinimum
     * @see #getRange
     */
    public double getMaximum() {
        return maximum;
    }

    /**
     * Returns the graduation's range. This is equivalents to computing
     * <code>{@link #getMaximum}-{@link #getMinimum}</code>.
     */
    public synchronized double getRange() {
        return (maximum-minimum);
    }

    /**
     * Sets the graduation's minimum, maximum and units.
     * This method will fire property change events for
     * <code>"minimum"</code>, <code>"maximum"</code>
     * and <code>"unit"</code> property names.
     */
    public void setRange(final double min, final double max, final Unit unit) {
        final Double oldMin;
        final Double oldMax;
        synchronized (this) {
            oldMin  = new Double(minimum);
            oldMax  = new Double(maximum);
            this.minimum = Math.min(min, max);
            this.maximum = Math.max(min, max);
            setUnit(unit);
        }
        listenerList.firePropertyChange("minimum", oldMin,  new Double(min));
        listenerList.firePropertyChange("maximum", oldMax,  new Double(max));
    }

    /**
     * Changes the graduation's units. This method will automatically
     * convert minimum and maximum values from the old units to the
     * new one.
     *
     * @param newUnit The new units, or <code>null</code> if unknow.
     *        If null, minimum and maximum values are not converted.
     * @throws UnitException if units are not convertible.
     */
    public synchronized void setUnit(final Unit newUnit) throws UnitException {
        double min = minimum;
        double max = maximum;
        final Unit unit = getUnit();
        if (unit!=null && newUnit!=null) {
            min = newUnit.convert(min, unit);
            max = newUnit.convert(max, unit);
        }
        setRange(min, max, newUnit);
    }

    /**
     * Returns the format to use for formatting labels. The format really used by
     * {@link TickIterator#currentLabel} may not be the same. For example, some
     * iterators may adjust automatically the number of fraction digits.
     */
    public Format getFormat() {
        return NumberFormat.getNumberInstance(getLocale());
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
                                                     final TickIterator reuse)
    {
        final float visualAxisLength  = getVisualAxisLength (hints);
        final float visualTickSpacing = getVisualTickSpacing(hints);
        double minimum = this.minimum;
        double maximum = this.maximum;
        if (!(minimum<maximum)) {
            minimum = (minimum+maximum)*0.5-0.5;
            maximum = minimum+1;
        }
        final NumberIterator it = getTickIterator(reuse, getLocale());
        it.init(minimum, maximum, visualAxisLength, visualTickSpacing);
        return it;
    }

    /**
     * Construct or reuse an iterator. This method is
     * overriden by {@link LogarithmicNumberGraduation}.
     */
    NumberIterator getTickIterator(final TickIterator reuse, final Locale locale) {
        if (reuse!=null && reuse.getClass().equals(NumberIterator.class)) {
            final NumberIterator it = (NumberIterator) reuse;
            it.setLocale(locale);
            return it;
        } else {
            return new NumberIterator(locale);
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
            final NumberGraduation that = (NumberGraduation) object;
            return Double.doubleToLongBits(this.minimum) == Double.doubleToLongBits(that.minimum) &&
                   Double.doubleToLongBits(this.maximum) == Double.doubleToLongBits(that.maximum);
        }
        return false;
    }

    /**
     * Returns a hash value for this graduation.
     */
    public int hashCode() {
        final long code = Double.doubleToLongBits(minimum) +
                       37*Double.doubleToLongBits(maximum);
        return (int)code ^ (int)(code >>> 32) ^ super.hashCode();
    }
}
