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

// Miscellaneous
import java.util.Locale;


/**
 * Provides the mechanism for {@link Graduation} objects to return the
 * values and labels of their ticks one tick at a time. This interface
 * returns tick values from some minimal value up to some maximal value,
 * using some increment value. Note that the increment value <strong>may
 * not be constant</strong>. For example, a graduation for the time axis
 * may use a slightly variable increment between differents months, since
 * all months doesn't have the same number of days.
 *
 * @version $Id: TickIterator.java,v 1.3 2003/05/13 10:58:46 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public interface TickIterator {
    /**
     * Tests if the iterator has more ticks.
     */
    public abstract boolean hasNext();

    /**
     * Tests if the current tick is a major one.
     *
     * @return <code>true</code> if current tick is a major tick,
     *         or <code>false</code> if it is a minor tick.
     */
    public abstract boolean isMajorTick();

    /**
     * Returns the position where to draw the current tick.  The position is scaled
     * from the graduation's minimum to maximum.    This is usually the same number
     * than {@link #currentValue}. The mean exception is for logarithmic graduation,
     * in which the tick position is not proportional to the tick value.
     */
    public abstract double currentPosition();

    /**
     * Returns the value for current tick. The
     * current tick may be major or minor.
     */
    public abstract double currentValue();

    /**
     * Returns the label for current tick. This method is usually invoked
     * only for major ticks, but may be invoked for minor ticks as well.
     * This method returns <code>null</code> if it can't produces a label
     * for current tick.
     */
    public abstract String currentLabel();

    /**
     * Moves the iterator to the next minor or major tick.
     */
    public abstract void next();

    /**
     * Moves the iterator to the next major tick. This move
     * ignore any minor ticks between current position and
     * the next major tick.
     */
    public abstract void nextMajor();

    /**
     * Reset the iterator on its first tick.
     * All other properties are left unchanged.
     */
    public abstract void rewind();

    /**
     * Returns the locale used for formatting tick labels.
     */
    public abstract Locale getLocale();
}
