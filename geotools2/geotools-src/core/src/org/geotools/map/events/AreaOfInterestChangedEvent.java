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
import com.vividsolutions.jts.geom.Envelope;
import org.opengis.cs.CS_CoordinateSystem;

 /**
 * Event data passed when AreaOfInterest has changed.
 *
 * @version $Id: AreaOfInterestChangedEvent.java,v 1.3 2002/08/09 12:50:24 camerons Exp $
 * @author Cameron Shorter
 */
public class AreaOfInterestChangedEvent extends EventObject {

    private Envelope areaOfInterest;
    private CS_CoordinateSystem coordinateSystem;

    /**
     * @param source The source of the event.
     * @param areaOfInterest The new areaOfInterest.
     * @param coordinateSystem The current coordindateSystem.
     */
    public AreaOfInterestChangedEvent(
            final Object source,
            final Envelope areaOfInterest,
            final CS_CoordinateSystem coordinateSystem) {
        super(source);
        this.areaOfInterest = areaOfInterest;
        this.coordinateSystem = coordinateSystem;
    }
    /** Get the new areaOfInterest.
     * @return The new areaOfInterest.
     */
    public Envelope getAreaOfInterest() {
        return areaOfInterest;
    }

    /** Get the current Coordinate System.
     * @return The current coordinate system.
     */
    public CS_CoordinateSystem getCoordinateSystem() {
        return this.coordinateSystem;
    }
}
