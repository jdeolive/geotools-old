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

import java.util.EventObject;

/**
 * A simple event object to represent all events triggered by
 * FeatureCollection instances (typically change events).
 *
 * @version $Id: CollectionEvent.java,v 1.3 2002/06/04 14:42:20 loxnard Exp $
 * @author Ray Gallagher
 */
public class CollectionEvent extends EventObject {
    /**
     * Constructs a new CollectionEvent.
     * TODO: potential for reason codes here later
     * @param source the collection which triggered the event
     */ 
    public CollectionEvent(Object source){
        super(source);
    }
}

