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
package org.geotools.graph.build;

import org.geotools.feature.Feature;
import java.util.Comparator;


/**
 * A FeatureComparator compares two features to one another.
 */
public abstract class FeatureComparator implements Comparator {
    /** constant NO_RELATIONSHIP */
    public static final int NO_RELATIONSHIP = -1;

    /**
     * Calls compare(Feature,Feature).
     *
     * @see Comparator#compare(Object, Object)
     */
    public int compare(Object o1, Object o2) {
        return (compare((Feature) o1, (Feature) o2));
    }

    /**
     * Compares one Feature to another.
     *
     * @param f1
     * @param f2
     *
     * @return An integer describing the relationship betweent the two
     *         features.
     */
    public abstract int compare(Feature f1, Feature f2);
}
