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
 * Stores Extent and Coordinate System associated with a Map Context.
 * Note that there is no setCoordinateSystem, this is to ensure that this object
 * doesn't depend on CoordinateTransform classes.  If you want to change
 * CoordinateSystem, use the setExtent(extent,coordinateSystem) method and
 * transform the coordinates in the calling application.
 *
 * @version $Id: DefaultAreaOfInterestModel.java,v 1.7 2002/12/03 19:39:08 camerons Exp $
 * @author Cameron Shorter
 * 
 */

import java.lang.IllegalArgumentException;
import java.util.Vector;
import java.util.EventObject;
//import org.opengis.cs.*;
import com.vividsolutions.jts.geom.Envelope;
import javax.swing.event.EventListenerList;
import org.geotools.cs.CoordinateSystem;
import org.geotools.map.events.*;

public class DefaultAreaOfInterestModel {
    
    private Envelope areaOfInterest;
    private CoordinateSystem coordinateSystem;
    private EventListenerList listenerList = new EventListenerList();
   
    /**
     * Initialise the model.
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public DefaultAreaOfInterestModel(
            Envelope areaOfInterest,
            CoordinateSystem coordinateSystem) throws IllegalArgumentException
    {
        if ((areaOfInterest==null) || (coordinateSystem==null)){
            throw new IllegalArgumentException();
        }
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
        EventObject ece = new EventObject(
                this);
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == AreaOfInterestChangedListener.class) {
                ((AreaOfInterestChangedListener)
                    listeners[i + 1]).areaOfInterestChanged(ece);
            }
        }
    }
    
    /**
     * Set a new AreaOfInterest and trigger an AreaOfInterestEvent.
     * Note that this is the only method to change coordinateSystem.  A
     * <code>setCoordinateSystem</code> method is not provided to ensure
     * this class is not dependant on transform classes.
     * @param areaOfInterest The new areaOfInterest.
     * @param coordinateSystem The coordinate system being using by this model.
     */
    public void setAreaOfInterest(
            Envelope areaOfInterest,
            CoordinateSystem coordinateSystem)
    {
        this.areaOfInterest = areaOfInterest;
        this.coordinateSystem = coordinateSystem;
        fireAreaOfInterestChangedListener();
    }
    
    /**
     * Set a new AreaOfInterest and trigger an AreaOfInterestEvent.
     * @param areaOfInterest The new areaOfInterest.
     */
    public void setAreaOfInterest(
            Envelope areaOfInterest)
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
     */


    /**
     * Get the coordinateSystem.
     */
    public CoordinateSystem getCoordinateSystem() {
        return this.coordinateSystem;
    }
    
    /**
     * Show the Envelope extent.
     */
    public String toString() {
        return "("+areaOfInterest.getMinX()+","+areaOfInterest.getMinY()+"),"
            +"("+areaOfInterest.getMaxX()+","+areaOfInterest.getMaxY()+")";
    }
}
