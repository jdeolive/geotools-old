package org.geotools.gui.tools;

import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.event.MouseInputAdapter;
import org.geotools.gui.swing.MapPane;
import org.geotools.gui.tools.Tool;
import org.geotools.map.AreaOfInterestModel;

public class PanTool extends Tool {

    // Internal variables
    private JComponent mapPane;
    private AreaOfInterestModel areaOfInterestModel;
    
    public PanTool(MapPane mapPane, AreaOfInterestModel areaOfInterestModel){
        this.mapPane=mapPane;
        this.areaOfInterestModel=areaOfInterestModel;

        // Register for MapPane Mouse events
        this.mapPane.addMouseListener(this);
        this.mapPane.addMouseMotionListener(this);
    }

    /**
     * Set up Click/Pan.
     * Pan the map so that the new extent has the click point in the middle
     * of the map.
     */
    public void MouseClicked(MouseEvent e) {
        areaOfInterestModel.changeRelativeAreaOfInterest(
            (float)(e.getX()-mapPane.getWidth())/mapPane.getWidth(),
            (float)(e.getX()-mapPane.getWidth())/mapPane.getWidth(),
            (float)(e.getY()-mapPane.getHeight())/mapPane.getHeight(),
            (float)(e.getY()-mapPane.getHeight())/mapPane.getHeight());
    }

    /*
     * Set up Click and Drap Pan.
     */
    //public void ...
}
