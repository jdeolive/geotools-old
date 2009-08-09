/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
 */

package org.geotools.gui.swing.event;

import org.geotools.gui.swing.*;
import java.util.EventObject;

/**
 * Base class for map pane events
 * 
 * @author Michael Bedward
 * @since 2.6
 * @source $URL$
 * @version $Id$
 */
public abstract class MapPaneEvent extends EventObject {
    private static final long serialVersionUID = 3379688994962024035L;
    
    /**
     * Type of MapPane event
     */
    public static enum Type {
        /**
         * The map pane has set a new context.
         */
        NEW_CONTEXT,

        /**
         * The map pane has set a new renderer.
         */
        NEW_RENDERER;
        
        @Override
        public String toString() {
            return "map pane: " + name();
        }
    }
    /** Type of mappane event */
    private Type type;

    /**
     * Constructor
     *
     * @param source the map pane issuing this event
     * @param type the type of event
     */
    public MapPaneEvent(JMapPane source, Type type) {
        super(source);
        this.type = type;
    }

    /**
     * Get the type of this event
     */
    public Type getType() {
        return type;
    }

    /**
     * Return a description of this event.
     * @return "map pane: " plus a brief description of the event type
     */
    @Override
    public String toString() {
        return type.toString();
    }
}
