package org.geotools.gui.tools;

import javax.swing.event.MouseInputAdapter;
import javax.swing.JComponent;
import org.geotools.map.Context;
import org.geotools.gui.widget.AbstractWidget;

/**
 * Base class for all the geotools Tools, like PanTool, ZoomTool, etc.
 * Some tools require Widget size information to convert click (x,y) points
 * into relative points (move half a map width to the left).  Consequently,
 * there can only be one widget for each tool.<br>
 * Tools can be created with null parameters at any time.  Tools should be
 * initialsed by Widgets when the Widget assigns a Tool to the Widget.
 * Tools should be destroyed when the owning Widget is destroyed.
 */
public abstract class AbstractToolImpl implements AbstractTool {
    /**
     * The widget from which this Tool gets MouseEvents.  The widget contains
     * information like widget size.
     */
     protected static AbstractWidget widget;
 
    /**
     * A tool is associated with only one context.  The context stores all data
     * about a mapping model.  This tool will change data in the context class.
     */
    protected Context context;
    
//    /**
//     * Construct a tool.
//     * @param context Where state data for this mapPane is stored.
//     * @param mapPane The mapPane from which this tool gets MouseEvents.
//     * @thows IllegalArgumentException
//     */
//    public Tool(
//        Context context,
//        JComponent mapPane) throws IllegalArgumentException
//    {
//        if ((context==null)||(mapPane==null)){
//            throw new IllegalArgumentException();
//        }else{
//            this.context=context;
//            this.widget=mapPane;
//        }
//    }

    /**
     * Get the MapPane from which this Tool get's MouseEvents.  If widget has
     * not been set yet, then null is returned.
     * @param The MapPane from which this Tool get's MouseEvents.
     */
    public AbstractWidget getWidget(){
        return widget;
    }
    
    /**
     * Set the Widget which sends MouseEvents and contains widget size
     * information.
     * @param widget The widget to get size information from.
     * @throws IllegalStateException if the widget has already been set to
     * another widget.
     */
    public void setWidget(AbstractWidget widget) throws IllegalStateException
    {
        if (this.widget==null){
            this.widget=widget;
        }else if (this.widget!=widget){
            throw new IllegalStateException();
        }
    }
    
    /**
     * Get the context.  If context has not been set yet, then null is returned.
     * @param The context which stores the state data.
     */
    public Context getContext(){
        return context;
    }
    
    /**
     * Set the Context for this tool to send data to.
     * @param context The context to send data to.
     */
    public void setContext(Context context){
        this.context=context;
    }
}
