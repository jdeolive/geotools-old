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
package org.geotools.feature;

/**
 * A list of Features.  Allows getting and setting of Features at an index.
 *
 * @author Ian Schneider
 *
 * @task REVISIT: make closer to List interface?  Specify the exceptions  to be
 *       thrown and have set return the Feature replaced?
 */
public interface FeatureList extends FeatureCollection {
    /**
     * Returns the feature at the specified index of the list.
     *
     * @param idx the index of the feature to return.
     *
     * @return the feature at the specified position of the list.
     */
    Feature getFeature(int idx);

    /**
     * Replaces the element at the specified position in this list with the
     * specified element.
     *
     * @param idx index of feature to replace.
     * @param f feature to be stored at the specified position.
     */
    void setFeature(int idx, Feature f);
}
