/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.gui.tools.event;

import java.util.EventListener;
import java.util.EventObject;


/**
 * Methods to handle a change of a MapPane's selected tool.
 *
 * @author Cameron Shorter
 * @version $Id: SelectedToolListener.java,v 1.2 2003/08/20 21:32:14 cholmesny Exp $
 */
public interface SelectedToolListener extends EventListener {
    /**
     * Called when the selectedTool on a MapPane changes.
     * @param event the event that changed the MapPane
     */
    void selectedToolChanged(EventObject event);
}
