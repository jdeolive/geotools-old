/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.data;

import org.geotools.feature.Feature;


/**
 * Represents the extent of a loaded Feature set.
 *
 * @author Ray Gallagher
 * @version $Id: Extent.java,v 1.8 2003/05/16 15:49:06 jmacgill Exp $
 */
public interface Extent {
    /**
     * Gets the Extent which represents the intersection between this Extent
     * and other.
     *
     * @param other The extent to test against.
     *
     * @return An Extent representing the intersecting area between two Extents
     *         (null if there is no overlap).
     */
    Extent intersection(Extent other);

    /**
     * Gets the difference, represented by another Extent, between this Extent
     * and other.
     *
     * @param other The extent to test against.
     *
     * @return An array of Extents making up the total area of other not taken
     *         up by this Extent. If there is no overlap between the two, this
     *         returns the same extent as other.
     */
    Extent[] difference(Extent other);

    /**
     * Produces the smallest extent that will hold both the existing extent and
     * that of the extent passed in.
     *
     * @param other The extent to combine with this extent.
     *
     * @return The new, larger, extent.
     *
     * @task REVISIT: Think about implication of combining.  New extent may
     *       contain areas which were in neither.
     */
    Extent combine(Extent other);

    /**
     * Tests whether the given Feature is within this Extent. This Extent
     * implementation must know and be able to read the object types contained
     * in the Feature.
     *
     * @param feature The Feature to test.
     *
     * @return True if the Feature is within this Extent, otherwise false.
     */
    boolean containsFeature(Feature feature);
}
