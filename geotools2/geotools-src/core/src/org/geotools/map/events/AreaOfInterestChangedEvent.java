package org.geotools.map.events;

import java.util.EventObject;
import com.vividsolutions.jts.geom.Envelope;
import org.opengis.cs.CS_CoordinateSystem;

 /**
 * Event data passed when AreaOfInterest has changed.
 *
 * @version $Id: AreaOfInterestChangedEvent.java,v 1.2 2002/07/12 15:20:22 loxnard Exp $
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
