/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2006, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.display.style;

// J2SE dependencies
import java.beans.PropertyChangeEvent;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * A pair of old and new values in a property change event. The old value can never change,
 * since it is the old value for the first event of a given name. The new value can change
 * however, since it is replaced by every new event since the creation of this object.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class ValuePair {
    /**
     * The old value.
     */
    final Object oldValue;

    /**
     * The new value.
     */
    Object newValue;

    /**
     * Creates a pair for the specified event.
     */
    public ValuePair(final PropertyChangeEvent change) {
        oldValue = change.getOldValue();
        newValue = change.getNewValue();
    }

    /**
     * Concatenate this change with the specified one. This {@code ValuePair} is setup as if
     * {@code this} change was applied first, followed by the {@code next} change. If and only
     * if the concatenation results in an identity change ({@linkplain #oldValue old} and
     * {@linkplain #newValue new} values being equals), then this method returns {@code true}.
     */
    public boolean concatenate(final ValuePair next) {
        newValue = next.newValue;
        return Utilities.equals(oldValue, newValue);
    }
}
