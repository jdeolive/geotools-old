package org.geotools.gui.tools;

import javax.swing.event.MouseInputAdapter;
import javax.swing.JComponent;
import org.geotools.map.Context;

/**
 * Base class for all the geotools Tools, like PanTool, ZoomTool, etc.
 */
public class Tool extends MouseInputAdapter {
    /**
     * The mapPane from which this Tool gets MouseEvents.
     */
     protected JComponent mapPane = null;
 
    /**
     * A tool is associated with only one context.  The context stores all data
     * about a mapping model.  This tool will change data in the context class.
     */
    protected Context context;

    /**
     * Construct a tool.
     * @context Where state data for this mapPane is stored.
     * @mapPane The mapPane from which this tool gets MouseEvents.
     * @thows IllegalArgumentException
     */
    public Tool(
        Context context,
        JComponent mapPane) throws IllegalArgumentException
    {
        if ((context==null)||(mapPane==null)){
            throw new IllegalArgumentException();
        }else{
            this.context=context;
            this.mapPane=mapPane;
        }
    }

    /**
     * Get the MapPane from which this Tool get's MouseEvents.
     * @param The MapPane from which this Tool get's MouseEvents.
     */
    public JComponent getMapPane(){
        return mapPane;
    }
    
        /**
     * Get the context.
     * @param The context which stores the state data.
     */
    public Context getContext(){
        return context;
    }
}
