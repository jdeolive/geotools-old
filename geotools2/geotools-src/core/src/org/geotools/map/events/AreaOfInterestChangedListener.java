package org.geotools.map.events;

import java.util.EventListener;

/**
 * Methods to handle a change in AreaOfInterest
 * @author <a href="mailto:cameron@shorter.net">Cameron Shorter</a>
 * @version $Id: AreaOfInterestChangedListener.java,v 1.1 2002/07/10 21:54:48 camerons Exp $
 */
public interface AreaOfInterestChangedListener extends EventListener {

    /**
     * Process an AreaOfInterestChangedEvent, probably involves a redraw.
     * @param areaOfInterestChangedEvent The new extent.
     */
    void areaOfInterestChanged(
            AreaOfInterestChangedEvent areaOfInterestChangedEvent);
}
