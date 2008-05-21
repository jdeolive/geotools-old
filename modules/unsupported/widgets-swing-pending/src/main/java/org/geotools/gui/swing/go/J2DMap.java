/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.gui.swing.go;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import javax.swing.JPanel;
import org.geotools.display.canvas.AWTCanvas2D;
import org.geotools.gui.swing.map.map2d.AbstractMap2D;
import org.geotools.map.event.MapLayerListEvent;

/**
 *
 * @author sorel
 */
public class J2DMap extends AbstractMap2D implements GoMap2D{

    
    private final AWTCanvas2D canvas;
    
    public J2DMap(AWTCanvas2D canvas){
        super();
        this.canvas = canvas;        
    }

    @Override
    protected void paintComponent(Graphics g) {               
        Graphics2D output = (Graphics2D) g;        
        canvas.paint(output);        
    }

    public AWTCanvas2D getCanvas() {
        return canvas;
    }

    public void dispose() {
        canvas.dispose();
    }
    
}
