/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.geotools.feature;

/**
 * Interface to be implemented by all listeners of CollectionEvents.
 *
 * @version $Id: CollectionListener.java,v 1.4 2002/07/12 15:10:17 loxnard Exp $
 * @author Ray Gallagher
 */
public interface CollectionListener extends java.util.EventListener {
    /** 
     * Gets called when a CollectionEvent is fired.
     * Typically fired to signify that a change has occurred in the collection.
     * @param tce The CollectionEvent
     */
    void collectionChanged(CollectionEvent tce);
}

