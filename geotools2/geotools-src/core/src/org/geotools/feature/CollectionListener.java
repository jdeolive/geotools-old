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
 * Interface to be implemented by all listeners of CollectionEvents.
 *
 * @author Ray Gallagher
 * @version $Id: CollectionListener.java,v 1.5 2003/05/07 16:53:04 jmacgill Exp $
 */
public interface CollectionListener extends java.util.EventListener {
    /**
     * Gets called when a CollectionEvent is fired. Typically fired to signify
     * that a change has occurred in the collection.
     *
     * @param tce The CollectionEvent
     */
    void collectionChanged(CollectionEvent tce);
}
