/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.gui.swing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import javax.swing.JPanel;
import org.geotools.display.canvas.BufferedCanvas2D;
import org.geotools.display.renderer.BufferedRenderer2D;

/**
 *
 * @author sorel
 */
public class MapPane extends JPanel{

    
    private final BufferedCanvas2D canvas;
    private final BufferedRenderer2D renderer;
    
    public MapPane(){
        super();
        renderer = new BufferedRenderer2D();
        canvas = new BufferedCanvas2D(renderer,this);
        renderer.setCanvas(canvas);
        
        renderer.add(new LineGraphic());
        
        
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
                
        Graphics2D output = (Graphics2D) g;
        
        canvas.setDisplayBounds(getBounds());
        canvas.paint(output, new AffineTransform());
        
    }
    
    
}
