package org.geotools.gui.tools;

import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputAdapter;
import org.geotools.gui.tools.Tool;

public class PanTool extends Tool {


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
