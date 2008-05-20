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

/**
 *
 * @author sorel
 */
public class NormalMapPane extends JPanel{

    
    private final AWTCanvas2D canvas;
    
    public NormalMapPane(AWTCanvas2D canvas){
        super();
        this.canvas = canvas;        
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
                
        Graphics2D output = (Graphics2D) g;
        
        canvas.setDisplayBounds(getBounds());
        canvas.paint(output);
        
    }
    
}
