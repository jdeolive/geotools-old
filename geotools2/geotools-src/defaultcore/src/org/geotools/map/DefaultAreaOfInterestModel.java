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
 * Stores Extent and CoordinateSystem associated with a Map Context.
 * Note that there is no setCoordinateSystem, this is to ensure that this object
 * doesn't depend on CoordinateTransform classes.  If you want to change
 * CoordinateSystem, use the setExtent(extent,coordinateSystem) method and
 * transform the coordinates in the calling application.<br>
 * Extent and CoordinateSystem are cloned during construction and when returned.
 * This is to ensure only this class can change their values.
 *
 * @version $Id: DefaultAreaOfInterestModel.java,v 1.10 2002/12/15 04:24:08 camerons Exp $
 * @author Cameron Shorter
 * 
 */

import java.lang.Cloneable;
import java.lang.IllegalArgumentException;
import java.util.Vector;
import java.util.EventObject;
//import org.opengis.cs.*;
import com.vividsolutions.jts.geom.Envelope;
import javax.swing.event.EventListenerList;
import org.geotools.cs.CoordinateSystem;
import org.geotools.map.events.*;

public class DefaultAreaOfInterestModel implements Cloneable{
    
    private Envelope areaOfInterest;
    private CoordinateSystem coordinateSystem;
    private EventListenerList listenerList = new EventListenerList();
   
    /**
     * Initialise the model.
     * @param bbox The extent associated with this class.
     * @param coordinateSystem The coordinate system associated with this class.
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public DefaultAreaOfInterestModel(
            Envelope bbox,
            CoordinateSystem coordinateSystem) throws IllegalArgumentException
    {
        if ((bbox==null) || (coordinateSystem==null)){
            throw new IllegalArgumentException();
        }
        this.areaOfInterest = new Envelope(bbox);
        this.coordinateSystem = coordinateSystem;
    }
    
    /**
     * Register interest in receiving an AreaOfInterestChangedEvent.
     * @param ecl The object to notify when AreaOfInterest has changed.
     * @param sendEvent After registering this listener, send a changeEvent
     * to all listeners.
     */
    public void addAreaOfInterestChangedListener(
            AreaOfInterestChangedListener ecl,
            boolean sendEvent){
        listenerList.add(AreaOfInterestChangedListener.class, ecl);
        if (sendEvent){
            fireAreaOfInterestChangedListener();
        }
    }

    /**
     * Register interest in receiving an AreaOfInterestChangedEvent.
     * @param ecl The object to notify when AreaOfInterest has changed.
     */
    public void addAreaOfInterestChangedListener(
            AreaOfInterestChangedListener ecl){
        addAreaOfInterestChangedListener(ecl,false);
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
     * @param bbox The new areaOfInterest.
     * @param coordinateSystem The coordinate system being using by this model.
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public void setAreaOfInterest(
            Envelope bbox,
            CoordinateSystem coordinateSystem) throws IllegalArgumentException
    {
        if ((bbox==null) || (coordinateSystem==null) || bbox.isNull()){
            throw new IllegalArgumentException();
        }
        this.areaOfInterest = new Envelope(bbox);
        this.coordinateSystem = coordinateSystem;
        fireAreaOfInterestChangedListener();
    }
    
    /**
     * Set a new AreaOfInterest and trigger an AreaOfInterestEvent.
     * @param areaOfInterest The new areaOfInterest.
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public void setAreaOfInterest(
            Envelope areaOfInterest)
    {
        setAreaOfInterest(areaOfInterest,this.coordinateSystem);
    }
    
    /**
     * Gets the current AreaOfInterest.
     * @return Current AreaOfInterest
     */
    public Envelope getAreaOfInterest(){
        return new Envelope(areaOfInterest);
    }

    /**
     * Get the coordinateSystem.
     */
    public CoordinateSystem getCoordinateSystem() {
        return this.coordinateSystem;
    }

    /*
     * Create a copy of this class
     * @HACK Probably need to add all the eventListeners to the cloned class.
     */
    public Object clone() {
        return new DefaultAreaOfInterestModel(
            this.areaOfInterest,
            this.coordinateSystem);
    }
    
    /**
     * Show the Envelope extent.
     */
    public String toString() {
        return "("+areaOfInterest.getMinX()+","+areaOfInterest.getMinY()+"),"
            +"("+areaOfInterest.getMaxX()+","+areaOfInterest.getMaxY()+")";
    }
}
