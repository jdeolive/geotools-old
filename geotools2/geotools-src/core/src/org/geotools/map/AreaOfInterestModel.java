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
 * @version $Id: AreaOfInterestModel.java,v 1.7 2002/12/19 09:52:13 camerons Exp $
 * @author Cameron Shorter
 * 
 */

import java.lang.Cloneable;
import java.lang.IllegalArgumentException;
import java.util.Vector;
import java.util.EventObject;
import com.vividsolutions.jts.geom.Envelope;
import javax.swing.event.EventListenerList;
import org.geotools.cs.CoordinateSystem;
import org.geotools.map.events.*;

public interface AreaOfInterestModel extends Cloneable{
    
    /**
     * Register interest in receiving an AreaOfInterestChangedEvent.
     * @param ecl The object to notify when AreaOfInterest has changed.
     * @param sendEvent After registering this listener, send a changeEvent
     * to all listeners.
     */
    public void addAreaOfInterestChangedListener(
            AreaOfInterestChangedListener ecl,
            boolean sendEvent);

    /**
     * Register interest in receiving an AreaOfInterestChangedEvent.
     * @param ecl The object to notify when AreaOfInterest has changed.
     */
    public void addAreaOfInterestChangedListener(
            AreaOfInterestChangedListener ecl);

    /**
     * Remove interest in receiving an AreaOfInterestChangedEvent.
     * @param ecl The object to stop sending AreaOfInterestChanged Events.
     */
    public void removeAreaOfInterestChangedListener(
            AreaOfInterestChangedListener ecl);

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
            CoordinateSystem coordinateSystem) throws IllegalArgumentException;
    
    /**
     * Set a new AreaOfInterest and trigger an AreaOfInterestEvent.
     * @param areaOfInterest The new areaOfInterest.
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public void setAreaOfInterest(
            Envelope areaOfInterest);
    
    /**
     * Gets the current AreaOfInterest.
     * @return Current AreaOfInterest
     */
    public Envelope getAreaOfInterest();

    /**
     * Get the coordinateSystem.
     */
    public CoordinateSystem getCoordinateSystem();

    /*
     * Create a copy of this class
     */
    public Object clone();
}
