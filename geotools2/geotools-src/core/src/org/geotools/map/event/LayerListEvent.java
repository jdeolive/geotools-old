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
package org.geotools.map.event;

// J2SE dependencies
import java.util.EventObject;

// Geotools dependencies
import org.geotools.map.LayerList;  // For Javadoc


/**
 * Event fired when some {@linkplain LayerList layer list} property changes.
 *
 * @author Martin Desruisseaux
 * @version $Id: LayerListEvent.java,v 1.1 2003/08/18 16:32:31 desruisseaux Exp $
 *
 * @see LayerList
 * @see LayerListListener
 */
public class LayerListEvent extends EventObject {
    /**
     * Creates a new instance of <code>LayerListEvent</code>.
     *
     * @param source The source of the event change.
     */
    public LayerListEvent(final Object source) {
        super(source);
    }
}
