/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
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
 */

package org.geotools.display.canvas;

// J2SE and JAI dependencies
import java.awt.RenderingHints;


/**
 * The key for hints enumerated in the {@link AbstractCanvas} insteface.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class HintKey extends RenderingHints.Key {
    /**
     * Base class of all values for this key.
     */
    private final Class valueClass;

    /**
     * Constructs a new key.
     *
     * @param id An ID. Must be unique for all instances of {@link Key}.
     * @param valueClass Base class of all valid values.
     */
    public HintKey(final int id, final Class valueClass) {
        super(id);
        this.valueClass = valueClass;
    }

    /**
     * Returns {@code true} if the specified object is a valid value for this key.
     *
     * @param  value The object to test for validity.
     * @return {@code true} if the value is valid; {@code false} otherwise.
     */
    public boolean isCompatibleValue(final Object value) {
        if (value == null) {
            return false;
        }
        if (!valueClass.isAssignableFrom(value.getClass())) {
            return false;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue() >= 0;
        }
        return true;
    }
}
