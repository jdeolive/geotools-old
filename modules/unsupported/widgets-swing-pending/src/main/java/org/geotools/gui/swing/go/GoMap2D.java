/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.gui.swing.go;

import org.geotools.display.canvas.AWTCanvas2D;
import org.geotools.gui.swing.map.map2d.Map2D;

/**
 *
 * @author johann sorel
 */
public interface GoMap2D extends Map2D{

    AWTCanvas2D getCanvas();    
    
}
