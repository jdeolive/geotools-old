package org.geotools.gui.tools;

import org.geotools.gui.widget.AbstractWidget;
import org.geotools.map.Context;

/**
 * Base class for all the geotools Tools, like PanTool, ZoomTool, etc.
 * Some tools require Widget size information to convert click (x,y) points
 * into relative points (move half a map width to the left).  Consequently,
 * there can only be one widget for each tool.<br>
 * Tools can be created with null parameters at any time.  Tools should be
 * initialsed by Widgets when the Widget assigns a Tool to the Widget.
 * Tools should be destroyed when the owning Widget is destroyed.
 */
public abstract interface AbstractTool {

    /**
     * Get the MapPane from which this Tool get's MouseEvents.  If widget has
     * not been set yet, then null is returned.
     * @param The MapPane from which this Tool get's MouseEvents.
     */
    public AbstractWidget getWidget();
    
    /**
     * Set the Widget which sends MouseEvents and contains widget size
     * information.
     * @param widget The widget to get size information from.
     * @throws IllegalStateException if the widget has already been set to
     * another widget.
     */
    public void setWidget(AbstractWidget widget) throws IllegalStateException;
    
    /**
     * Get the context.  If context has not been set yet, then null is returned.
     * @param The context which stores the state data.
     */
    public Context getContext();
    
    /**
     * Set the Context for this tool to send data to.
     * @param context The context to send data to.
     */
    public void setContext(Context context);
}
