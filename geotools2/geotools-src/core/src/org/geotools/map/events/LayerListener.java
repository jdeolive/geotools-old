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
package org.geotools.map.events;

import java.util.EventListener;
import java.util.EventObject;

/**
 * Methods to handle a change in the Layers.
 * @author Cameron Shorter
 * @version $Id: LayerListener.java,v 1.1 2003/07/10 16:04:56 ianturton Exp $
 */
public interface LayerListener extends EventListener { 

    /**
     * Process an LayerChangedEvent, probably involves a redraw.
     * @param LayerChangedEvent The new extent.
     */
    void LayerChanged(
            EventObject LayerChangedEvent);
}
