/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.referencing.factory.epsg;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * A (name, description) pair for a coordinate system axis.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class AxisName {
    /**
     * The coordinate system axis name (never {@code null}).
     */
    public final String name;

    /**
     * The coordinate system axis description, or {@code null} if none.
     */
    public final String description;

    /**
     * Creates a new coordinate system axis name.
     */
    public AxisName(final String name, final String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Returns a hash code for this object.
     */
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Compare this name with the specified object for equality.
     */
    public boolean equals(final Object object) {
        if (object instanceof AxisName) {
            final AxisName that = (AxisName) object;
            return Utilities.equals(this.name,        that.name) &&
                   Utilities.equals(this.description, that.description);
        }
        return false;
    }

    /**
     * Returns a string representation of this object, for debugging purpose only.
     */
    public String toString() {
        return name;
    }
}
