/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.collection;

import java.util.List;


/**
 * List support close( iterator ) used to handle heavyweight resources.
 * <p>
 * The idea of a closable iterator is inspired by the use of hibernate.
 * </p>
 * @author Jody Garnett, Refractions Research Inc.
 */
public interface ResourceList extends List, ResourceCollection {
    /**
     * Remove indicated range.
     * <p>
     * This is used by subList( fromIndex, toIndex ).clear() for
     * efficient removal, based on the JDK api for AbstractList.
     * @param fromIndex
     * @param toIndex
     */
    void removeRange(int fromIndex, int toIndex);
}
