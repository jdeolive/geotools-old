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




/**
 * A FeatureRelator determines the logical relationship between two features.
 * <p>
 * A FeatureRelator uses a  {@link FeatureComparator} to determine the kind of
 * relationship present  between two Features, and then determines if this
 * relationship should  be represented explicitly in thh graph.
 * </p>
 * <p>
 * Questions for JG:
 * </p>
 * <ul>
 * <li>
 * Q: Do we need to specify any mathmatical limits on this relationship
 * (bidirectional Y?, reflecive N?, transitive N? )?</li>
 * <li>
 * Q: should we just let the result not make sense if the relationship does not
 * make sense (garbage in, garbage out)?</li>
 * <li>
 * Q: is this the kind of thing that should be defined by the given
 * GraphBuilder's themselves - a directed graph builder may not wish to work
 * with a reflective relationship?
 * </li>
 * </ul>
 * </p>
 * <p>
 * <b>Note:</b><br>
 * If featureRelator provided some kind of metadata on these issues,
 * especially for ones limited to specific Geometry types like LineString,
 * we could have a factory that matched relationship with the most efficient
 * kind of Graph representation.
 * </p>
 * <p>
 * A good example relationship is touches, it is bidirectional. It is not
 * reflexive. It is not transitive.
 * </p> 
 */
public abstract class FeatureRelator {
    /** FeatureComparator  */
    private FeatureComparator m_comparator;

    /**
     * Creates a new FeatureRelator object.
     *
     * @param comparator FeatureComparator
     * @see FeatureComparator
     */
    public FeatureRelator(FeatureComparator comparator) {
        m_comparator = comparator;
    }

    /**
     * Returns the FeatureComparator used by the relator.
     *
     * @return FeatureComparator
     * @see FeatureComparator
     */
    public FeatureComparator getComparator() {
        return (m_comparator);
    }

    /**
     * Relates two features.
     *
     * @return True if there is a relationship present, otherwise false.
     */
    public abstract boolean relate(Feature f1, Feature f2);
}
