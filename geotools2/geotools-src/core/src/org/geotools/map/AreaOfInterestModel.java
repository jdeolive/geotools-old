package org.geotools.map;

/**
 * AreaOfInterestModel stores AreaOfInterest associated with a geographic map.
 * Geotools uses a Model-View-Control (MVC) design to control maps.
 * The Tools classes process key and mouse actions, and the Renderers handle
 * displaying of the data.
 *
 * @version $Id: AreaOfInterestModel.java,v 1.2 2002/07/12 15:22:43 loxnard Exp $
 * @author Cameron Shorter
 * 
 */

import org.opengis.cs.*;
import com.vividsolutions.jts.geom.Envelope;
import org.geotools.map.events.*;

public interface AreaOfInterestModel {
    
    /**
     * Register interest in receiving an AreaOfInterestChangedEvent.
     * @param ecl The object to notify when AreaOfInterest has changed.
     */
    void addAreaOfInterestChangedListener(
            AreaOfInterestChangedListener ecl);

    /**
     * Remove interest in receiving an AreaOfInterestChangedEvent.
     * @param ecl The object to stop sending AreaOfInterestChanged Events.
     */
    void removeAreaOfInterestChangedListener(
            AreaOfInterestChangedListener ecl);

 
    void setAreaOfInterest(
            Envelope areaOfInterest,
            CS_CoordinateSystem coordinateSystem);

    /**
     * Change the AreaOfInterest using relative parameters.
     * Relative parameters are used so that tools do not need to know the 
     * units of the coordinate system.
     * For instance, if a map zooms to the left by half a map width,
     * then deltaMinX=-0.5, deltaMaxX=-0.5, deltaMinY=0, deltaMaxY=0.
     * @param deltaMinX The relative change in the bottom left X coordinate.
     * @param deltaMinY The relative change in the bottom left Y coordinate.
     * @param deltaMaxX The relative change in the top right X coordinate.
     * @param deltaMaxY The relative change in the top right Y coordinate.
     */
    void changeRelativeAreaOfInterest(
            float deltaMinX,
            float deltaMaxX,
            float deltaMinY,
            float deltaMaxY);

    /**
     * Set the coordinateSystem.
     */
    void setCoordinateSystem(CS_CoordinateSystem coordinateSystem);

    /**
     * Get the coordinateSystem.
     */
    CS_CoordinateSystem getCoordinateSystem();
}
