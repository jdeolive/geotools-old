package org.geotools.gui.tools;

import javax.swing.event.MouseInputAdapter;
import javax.swing.JComponent;
import org.geotools.map.AreaOfInterestModel;

/**
 * Base interface for all the geotools Tools, like PanTool, ZoomTool, etc.
 */
public class Tool extends MouseInputAdapter {
    /**
     * The mapPane from which this Tool gets MouseEvents.
     */
     protected JComponent mapPane = null;
 
     /**
     * The AreaOfInterestModel which determines the Area being viewed by this
     * Tool's viewer.
     */
    protected AreaOfInterestModel areaOfInterestModel = null;
  
    public Tool()
    {
    }
    
    /**
     * Set the MapPane from which this Tool will get MouseEvents from, and
     * register for the events.  A Tool should only be associated with one
     * mapPane.  If you want to use the same Tool for multiple MapPanes, then
     * create multiple instances of the Tool class.
     * @param mapPane The mapPane from which this Tool gets MouseEvents.
     * @task TODO: implement logging
     */
    public void setMapPane(JComponent mapPane){
        // Make sure we don't register for events twice.  (This shouldn't
        // happen if Tool is used correctly and is only associated with
        // one mapPane.
        if (this.mapPane!=null){
            // log(ERROR,
            // "Multiple MapPanes have been associated with this Tool")
            this.mapPane.removeMouseListener(this);
            this.mapPane.removeMouseMotionListener(this);
        }
        
        this.mapPane=mapPane;
        // Register for MapPane Mouse events
        this.mapPane.addMouseListener(this);
        this.mapPane.addMouseMotionListener(this);
    }
    
    /**
     * Get the MapPane from which this Tool get's MouseEvents.
     * @param The MapPane from which this Tool get's MouseEvents.
     */
    public JComponent getMapPane(){
        return mapPane;
    }
    
    /**
     * Set the AreaOfInterest Class this Tool uses when Zooming/Panning etc.
     * @param areaOfInterestModel The AreaOfInterest Class this Tool uses when
     * Zooming/Panning etc.
     */
    public void setAreaOfInterestModel(
            AreaOfInterestModel areaOfInterestModel)
    {
        this.areaOfInterestModel=areaOfInterestModel;
    }
    
    /**
     * Get the AreaOfInterest Class this Tool uses when Zooming/Panning etc.
     * @param areaOfInterestModel The AreaOfInterest Class this Tool uses when
     * Zooming/Panning etc.
     */
    public AreaOfInterestModel getAreaOfInterestModel()
    {
        return this.areaOfInterestModel;
    }
}
