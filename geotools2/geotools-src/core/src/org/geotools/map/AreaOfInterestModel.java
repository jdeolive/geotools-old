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
package org.geotools.map;

import org.opengis.cs.CS_CoordinateSystem;
import com.vividsolutions.jts.geom.Envelope;
import org.geotools.map.events.AreaOfInterestChangedListener;

/**
 * AreaOfInterestModel stores AreaOfInterest associated with a geographic
 * map.
 * Geotools uses a Model-View-Control (MVC) design to control maps.
 * The Tools classes process key and mouse actions, and the Renderers handle
 * displaying of the data.
 *
 * @author Cameron Shorter
 * @version $Id: AreaOfInterestModel.java,v 1.6 2002/09/01 12:02:32 camerons Exp $
 * @task TODO Foreach method, check AreaOfInterest==null and return a new
 * exception if appropriate.
 */
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

    /**
     * Set a new AreaOfInterest and trigger an AreaOfInterestEvent.
     * @param areaOfInterest The new areaOfInterest.
     * @param coordinateSystem The coordinate system being using by this model.
     */
    void setAreaOfInterest(
            Envelope areaOfInterest,
            CS_CoordinateSystem coordinateSystem);
    
     /**
     * Gets the current AreaOfInterest.  Returns null if AreaOfInterest has not
     * been set.
     * @return Current AreaOfInterest
     */
    Envelope getAreaOfInterest();

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
     * @param coordinateSystem The coordinate system being using by this model.
     */
    void setCoordinateSystem(CS_CoordinateSystem coordinateSystem);

    /**
     * Get the coordinateSystem.
     * @return The coordinate system being using by this model.
     */
    CS_CoordinateSystem getCoordinateSystem();
}
