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
import java.util.EventObject;

import java.util.EventListener;

/**
 * Methods to handle a change in AreaOfInterest
 * @author <a href="mailto:cameron@shorter.net">Cameron Shorter</a>
 * @version $Id: AreaOfInterestChangedListener.java,v 1.3 2002/09/22 03:38:03 camerons Exp $
 */
public interface AreaOfInterestChangedListener extends EventListener {

    /**
     * Process an AreaOfInterestChangedEvent, probably involves a redraw.
     * @param areaOfInterestChangedEvent The new extent.
     */
    void areaOfInterestChanged(
            EventObject areaOfInterestChangedEvent);
}
