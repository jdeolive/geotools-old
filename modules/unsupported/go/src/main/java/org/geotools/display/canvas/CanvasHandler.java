/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.display.canvas;

import java.awt.Component;
import org.geotools.display.canvas.AWTCanvas2D;

/**
 *
 * @author sorel
 */
public interface CanvasHandler {

    void setCanvas(AWTCanvas2D canvas);
    
    AWTCanvas2D getCanvas();    
    
    void install(Component component);
    
    void uninstall(Component component);
        
}
