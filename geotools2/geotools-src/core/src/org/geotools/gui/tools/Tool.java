package org.geotools.gui.tools;

import java.awt.Component;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import org.geotools.gui.widget.Widget;
import org.geotools.map.Context;

/**
 * Base class for all the geotools Tools, like PanTool, ZoomTool, etc.
 * Tools process mouse events on behalf of widgets like MapPane and change
 * data in the Context (like the AreaOfInterest).
 * @version $Id: Tool.java,v 1.1 2003/03/30 19:25:05 camerons Exp $
 * @author Cameron Shorter
 */
public abstract interface Tool {

    /**
     * Register this tool to receive Mouse Events from <code>component<code>.
     * The events may be MouseEvents or MouseMotionEvents or both depending on
     * the Tool which implements this interface.
     * @param component The tool will process mouseEvents from this component.
     * @param context The Context that will be changed by this Tool.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     * or the tool is being assigned a different context to before.
     */
    public void addMouseListener(
        Component component,
        Context context);
    
    /**
     * Remove all Mouse Listeners from this tool.  This method should be called
     * when this tool is deselected from a MapPane.
     */
    public void removeMouseListeners();

    /**
     * Get the context.  If context has not been set yet, then null is returned.
     * @param The context which stores the state data.
     */
    public Context getContext();
    
    /**
     * Set the name for the tool, eg "Zoom In", "Zoom Out", "Pan".
     */
    public void setName(String name);
    
    /**
     * Get the name of the tool.
     */
    public String getName();
    
    /**
     * Clean up this class.
     */
    public void destroy();
}
