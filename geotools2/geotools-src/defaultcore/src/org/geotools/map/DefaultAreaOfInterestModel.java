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

/**
 * AreaOfInterestModel stores AreaOfInterest associated with a geographic map.
 * Geotools uses a Model-View-Control (MVC) design to control maps.
 * The Tools classes process key and mouse actions, and the Renderers handle
 * displaying of the data.
 *
 * @version $Id: DefaultAreaOfInterestModel.java,v 1.4 2002/07/16 15:16:41 jmacgill Exp $
 * @author Cameron Shorter
 * 
 */

import java.util.Vector;
import org.opengis.cs.*;
import com.vividsolutions.jts.geom.Envelope;
import javax.swing.event.EventListenerList;
import org.geotools.map.events.*;

public class DefaultAreaOfInterestModel implements AreaOfInterestModel {
    
    private Envelope areaOfInterest;
    private CS_CoordinateSystem coordinateSystem;
    private EventListenerList listenerList = new EventListenerList();
   
    /**
     * Initialise the model.
     */
    public DefaultAreaOfInterestModel(
            Envelope areaOfInterest,
            CS_CoordinateSystem coordinateSystem)
    {
        this.areaOfInterest = areaOfInterest;
        this.coordinateSystem = coordinateSystem;
    }
    /**
     * Register interest in receiving an AreaOfInterestChangedEvent.
     * @param ecl The object to notify when AreaOfInterest has changed.
     */
    public void addAreaOfInterestChangedListener(
            AreaOfInterestChangedListener ecl){
        listenerList.add(AreaOfInterestChangedListener.class, ecl);
    }

    /**
     * Remove interest in receiving an AreaOfInterestChangedEvent.
     * @param ecl The object to stop sending AreaOfInterestChanged Events.
     */
    public void removeAreaOfInterestChangedListener(
            AreaOfInterestChangedListener ecl) {
        listenerList.remove(AreaOfInterestChangedListener.class, ecl);
    }

    /**
     * Notify all listeners that have registered interest for
     * notification an AreaOfInterestChangedEvent.
     */
    protected void fireAreaOfInterestChangedListener() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        AreaOfInterestChangedEvent ece = new AreaOfInterestChangedEvent(
                this,
                this.areaOfInterest,
                this.coordinateSystem);
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == AreaOfInterestChangedListener.class) {
                ((AreaOfInterestChangedListener)
                    listeners[i + 1]).areaOfInterestChanged(ece);
            }
        }
    }
    
    /**
     * Set a new AreaOfInterest and trigger an AreaOfInterestEvent.
     * @param areaOfInterest The new areaOfInterest.
     * @param coordinateSystem The coordinate system being using by this model.
     */
    public void setAreaOfInterest(
            Envelope areaOfInterest,
            CS_CoordinateSystem coordinateSystem)
    {
        this.areaOfInterest = areaOfInterest;
        this.coordinateSystem = coordinateSystem;
        fireAreaOfInterestChangedListener();
    }
    
    /**
     * Gets the current AreaOfInterest.
     * @return Current AreaOfInterest
     * HACK: should return a clone as direct ref to aoi could be bad.
     */
    public Envelope getAreaOfInterest(){
        return areaOfInterest;
    }

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
    public void changeRelativeAreaOfInterest(
            float deltaMinX,
            float deltaMaxX,
            float deltaMinY,
            float deltaMaxY)
    {
        Envelope newAreaOfInterest = new Envelope(
            areaOfInterest.getMinX() + (areaOfInterest.getWidth() * deltaMinX),
            areaOfInterest.getMaxX() + (areaOfInterest.getWidth() * deltaMaxX),
            areaOfInterest.getMinY() + (areaOfInterest.getWidth() * deltaMinY),
            areaOfInterest.getMaxY() + (areaOfInterest.getWidth() * deltaMaxY));
        areaOfInterest = null;
        areaOfInterest = newAreaOfInterest;
        fireAreaOfInterestChangedListener();
    }

    /**
     * Set the coordinateSystem.
     */
    public void setCoordinateSystem(CS_CoordinateSystem coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
    }

    /**
     * Get the coordinateSystem.
     */
    public CS_CoordinateSystem getCoordinateSystem() {
        return this.coordinateSystem;
    }
}
