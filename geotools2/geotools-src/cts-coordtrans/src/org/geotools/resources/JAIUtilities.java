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
 */
package org.geotools.resources;

// J2SE dependencies
import java.util.NoSuchElementException;

// JAI dependencies
import javax.media.jai.IntegerSequence;


/**
 * A set of utilities methods working on JAI classes. Those method should be considered
 * as temporary. They will be removed if Sun extends their class with the fonctionality
 * provided here.
 *
 * @version $Id: JAIUtilities.java,v 1.1 2003/05/12 21:27:56 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public final class JAIUtilities {
    /**
     * Do not allow instanciation of this class.
     */
    private JAIUtilities() {
    }

    /**
     * Fill the specified integer sequence with numbers from <code>lower</code> inclusive
     * to <code>upper</code> exclusive. Nothing is done if the <code>sequence</code> is
     * <code>null</code>.
     *
     * @param sequence The sequence to fill, or <code>null</code>.
     * @param lower The lower value, inclusive.
     * @param upper The upper value, exclusive.
     */
    public static void fill(final IntegerSequence sequence, int lower, final int upper) {
        if (sequence != null) {
            while (lower < upper) {
                sequence.insert(lower++);
            }
        }
    }

    /**
     * Copy all the value from <code>toAdd</code> in the given <code>sequence</code> while adding
     * the specified <code>offset</code> to each value. Nothing is done if <code>sequence</code>
     * is <code>null</code>.
     */
    public static void add(final IntegerSequence sequence,
                           final IntegerSequence toAdd, final int offset)
    {
        if (sequence != null) {
            for (toAdd.startEnumeration(); toAdd.hasMoreElements();) {
                sequence.insert(toAdd.nextElement() + offset);
            }
        }
    }

    /**
     * Returns the minimum (first) value in the specified sequence.
     *
     * @param  sequence The sequence to test for the minimum value.
     * @return The minimum (first) value in the given sequence.
     * @throws NoSuchElementException if there is no element in the given sequence.
     */
    public static int getMinimum(final IntegerSequence sequence) throws NoSuchElementException {
        sequence.startEnumeration();
        return sequence.nextElement();
    }

    /**
     * Returns the maximum (last) value in the specified sequence.
     *
     * @param  sequence The sequence to test for the minimum value.
     * @return The maximum (last) value in the given sequence.
     * @throws NoSuchElementException if there is no element in the given sequence.
     */
    public static int getMaximum(final IntegerSequence sequence) throws NoSuchElementException {
        sequence.startEnumeration();
        int n; do {
            n = sequence.nextElement();
        } while (sequence.hasMoreElements());
        return n;
    }

    /**
     * Returns <code>true</code> if the given sequence contains the given value.
     *
     * @param  sequence The sequence to test.
     * @param  value The value to search.
     * @return <code>true</code> if the sequence contains the given value.
     */
    public static boolean contains(final IntegerSequence sequence, final int value) {
        sequence.startEnumeration();
        while (sequence.hasMoreElements()) {
            if (sequence.nextElement() == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the given sequence contains all values in the given range.
     *
     * @param  sequence The sequence to test.
     * @param  lower The lower value, inclusive.
     * @param  upper The upper value, exclusive.
     * @return <code>true</code> if the sequence contains all values in the given range.
     */
    public static boolean containsAll(final IntegerSequence sequence, int lower, final int upper) {
        sequence.startEnumeration();
        while (sequence.hasMoreElements()) {
            final int n = sequence.nextElement();
            if (n < lower) {
                continue;
            }
            if (n == lower) {
                if (++lower == upper) {
                    return true;
                }
                continue;
            }
            break;
        }
        return lower >= upper;
    }

    /**
     * Returns <code>true</code> if the given sequence contains any value in the given range.
     *
     * @param  sequence The sequence to test.
     * @param  lower The lower value, inclusive.
     * @param  upper The upper value, exclusive.
     * @return <code>true</code> if the sequence contains at least one value in the given range.
     */
    public static boolean containsAny(final IntegerSequence sequence,
                                      final int lower, final int upper)
    {
        if (upper <= lower) {
            return true;
        }
        sequence.startEnumeration();
        while (sequence.hasMoreElements()) {
            final int n = sequence.nextElement();
            if (n>=lower && n<upper) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all integers in the given sequence.
     *
     * Will be replaced by <code>new IntegerSequence(min, max)</code> when the bug will be fixed.
     */
    public static int[] toArray(final IntegerSequence sequence) {
        final int[] array = new int[sequence.getNumElements()];
        int i=0;
        for (sequence.startEnumeration(); sequence.hasMoreElements();) {
            array[i++] = sequence.nextElement();
        }
        return XArray.resize(array, i);
    }

    /**
     * Workaround for IntegerSequence constructor bug.
     */
    public static IntegerSequence createSequence(int min, final int max) {
        final IntegerSequence sequence = new IntegerSequence(min, max);
        while (min <= max) {
            sequence.insert(min++);
        }
        return sequence;
    }
}
