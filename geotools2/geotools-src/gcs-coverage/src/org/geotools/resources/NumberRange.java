/*
 * Geotools - OpenSource mapping toolkit
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
package org.geotools.resources;

// JAI dependencies
import javax.media.jai.util.Range;

// Geotools dependencies
import org.geotools.resources.ClassChanger;


/**
 * A range of numbers. {@linkplain #union Union} and {@linkplain #intersect intersection}
 * are computed as usual, except that widening conversions will be applied as needed.
 *
 * @version $Id: NumberRange.java,v 1.2 2003/04/13 17:21:19 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public final class NumberRange extends Range {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -9094125174113731181L;

    /**
     * Construct an inclusive range of <code>byte</code> values.
     *
     * @param minimum The minimum value, inclusive.
     * @param maximum The maximum value, <strong>inclusive</strong>.
     */
    public NumberRange(final byte minimum, final byte maximum) {
        super(Byte.class, new Byte(minimum), new Byte(maximum));
    }

    /**
     * Construct an inclusive range of <code>short</code> values.
     *
     * @param minimum The minimum value, inclusive.
     * @param maximum The maximum value, <strong>inclusive</strong>.
     */
    public NumberRange(final short minimum, final short maximum) {
        super(Short.class, new Short(minimum), new Short(maximum));
    }

    /**
     * Construct an inclusive range of <code>int</code> values.
     *
     * @param minimum The minimum value, inclusive.
     * @param maximum The maximum value, <strong>inclusive</strong>.
     */
    public NumberRange(final int minimum, final int maximum) {
        super(Integer.class, new Integer(minimum), new Integer(maximum));
    }

    /**
     * Construct an inclusive range of <code>long</code> values.
     *
     * @param minimum The minimum value, inclusive.
     * @param maximum The maximum value, <strong>inclusive</strong>.
     */
    public NumberRange(final long minimum, final long maximum) {
        super(Long.class, new Long(minimum), new Long(maximum));
    }

    /**
     * Construct an inclusive range of <code>float</code> values.
     *
     * @param minimum The minimum value, inclusive.
     * @param maximum The maximum value, <strong>inclusive</strong>.
     */
    public NumberRange(final float minimum, final float maximum) {
        super(Float.class, new Float(minimum), new Float(maximum));
    }

    /**
     * Construct an inclusive range of <code>double</code> values.
     *
     * @param minimum The minimum value, inclusive.
     * @param maximum The maximum value, <strong>inclusive</strong>.
     */
    public NumberRange(final double minimum, final double maximum) {
        super(Double.class, new Double(minimum), new Double(maximum));
    }

    /**
     * Construct an inclusive range of {@link Number} objects.
     *
     * @param classe One of {@link Byte}, {@link Short}, {@link Integer}, {@link Long},
     *               {@link Float} or {@link Double}.
     * @param minimum The minimum value, inclusive.
     * @param maximum The maximum value, <strong>inclusive</strong>.
     */
    public NumberRange(final Class classe, final Number minimum, final Number maximum) {
        super(classe, (Comparable)minimum, (Comparable)maximum);
    }

    /**
     * Construct a range of {@link Number} objects.
     */
    public NumberRange(final Class classe, final Number minimum, final boolean isMinIncluded,
                                           final Number maximum, final boolean isMaxIncluded)
    {
        super(classe, (Comparable)minimum, isMinIncluded, (Comparable)maximum, isMaxIncluded);
    }

    /**
     * Cast the specified {@link Range} to a <code>NumberRange</code> object. If the specified
     * range is already an instance of <code>NumberRange</code>, then it is returned unchanged.
     *
     * @param  range The range to cast
     * @return The same range than <code>range</code> as a <code>NumberRange</code> object.
     */
    public static NumberRange cast(final Range range) {
        if (range instanceof NumberRange) {
            return (NumberRange) range;
        }
        return new NumberRange(range.getElementClass(),
                               (Number)range.getMinValue(), range.isMinIncluded(),
                               (Number)range.getMaxValue(), range.isMaxIncluded());
    }

    /**
     * Cast this range to the specified type.
     *
     * @param  type The class to cast to. Must be one of {@link Byte}, {@link Short},
     *              {@link Integer}, {@link Long}, {@link Float} or {@link Double}.
     * @return The casted range, or <code>this</code>.
     */
    private static NumberRange cast(final Range r, final Class type) {
        if (r==null || type.equals(r.getElementClass())) {
            return cast(r);
        }
        return new NumberRange(type,
                               ClassChanger.cast((Number)r.getMinValue(), type), r.isMinIncluded(),
                               ClassChanger.cast((Number)r.getMaxValue(), type), r.isMaxIncluded());
    }

    /**
     * Cast this range to the specified type.
     *
     * @param  type The class to cast to. Must be one of {@link Byte}, {@link Short},
     *              {@link Integer}, {@link Long}, {@link Float} or {@link Double}.
     * @return The casted range, or <code>this</code> if this range already uses
     *         the specified type.
     */
    public NumberRange cast(final Class type) {
        return cast(this, type);
    }

    /**
     * Returns the union of this range with the given range.
     * Widening conversions will be applied as needed.
     *
     * @task TODO: The return type will be changed to <code>NumberRange</code> when
     *             J2SE 1.5 will be available.
     */
    public Range union(final Range range) {
        final Class type = ClassChanger.getWidestClass(getElementClass(), range.getElementClass());
        return cast(cast(type).union0(cast(range, type)));
    }

    /**
     * Performs the union (no type check).
     */
    private Range union0(final Range range) {
        return super.union(range);
    }

    /**
     * Returns the intersection of this range with the given range.
     * Widening conversions will be applied as needed.
     *
     * @task TODO: The return type will be changed to <code>NumberRange</code> when
     *             J2SE 1.5 will be available.
     */
    public Range intersect(final Range range) {
        Class type = ClassChanger.getWidestClass(getElementClass(), range.getElementClass());
        final Range result = cast(type).intersect0(cast(range, type));
        /*
         * Use a finer type capable to holds the result (since the intersection may have
         * reduced the range), but not finer than the finest type of the ranges used in
         * the intersection calculation.
         */
        type = ClassChanger.getFinestClass(getElementClass(), range.getElementClass());
        return cast(result,
                    ClassChanger.getWidestClass(type, 
                    ClassChanger.getWidestClass(
                    ClassChanger.getFinestClass(((Number)result.getMinValue()).doubleValue()),
                    ClassChanger.getFinestClass(((Number)result.getMaxValue()).doubleValue()))));
    }

    /**
     * Performs the intersection (no type check).
     */
    private Range intersect0(final Range range) {
        return super.intersect(range);
    }

    /**
     * Returns the {@linkplain #getMinValue minimum value} as a <code>double</code>.
     * If this range is unbounded, then {@link Double#NEGATIVE_INFINITY} is returned.
     */
    public double getMinimum() {
        final Number value = (Number) getMinValue();
        return (value!=null) ? value.doubleValue() : Double.NEGATIVE_INFINITY;
    }

    /**
     * Returns the {@linkplain #getMaxValue maximum value} as a <code>double</code>.
     * If this range is unbounded, then {@link Double#POSITIVE_INFINITY} is returned.
     */
    public double getMaximum() {
        final Number value = (Number) getMaxValue();
        return (value!=null) ? value.doubleValue() : Double.POSITIVE_INFINITY;
    }
}
