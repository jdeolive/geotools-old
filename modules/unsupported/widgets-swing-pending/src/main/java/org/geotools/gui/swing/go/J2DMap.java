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
import org.geotools.gui.swing.go.handler.CanvasHandler;
import org.geotools.gui.swing.map.map2d.AbstractMap2D;
import org.geotools.map.event.MapLayerListEvent;

/**
 *
 * @author sorel
 */
public class J2DMap extends AbstractMap2D implements GoMap2D{
    
    
    private CanvasHandler handler;
    
    private final AWTCanvas2D canvas;
    private final J2DRenderer renderer;
        
    public J2DMap(){
        super();
        
        renderer = new J2DRenderer();
        canvas = new AWTCanvas2D(renderer,this);
    }

    @Override
    protected void paintComponent(Graphics g) {        
        super.paintComponent(g);
        Graphics2D output = (Graphics2D) g;        
        canvas.paint(output);        
    }

    public AWTCanvas2D getCanvas() {
        return canvas;
    }

    public void dispose() {
        canvas.dispose();
    }
    
    
    public CanvasHandler getHandler(){
        return handler;
    }

    public void setHandler(CanvasHandler handler){

        if(this.handler != handler) {
            //TODO : check for possible vetos

            final CanvasHandler old = this.handler;

            if (this.handler != null){
                this.handler.uninstall(this);
                this.handler.setCanvas(null);
            }

            this.handler = handler;

            if (this.handler != null) {
                this.handler.setCanvas(canvas);
                this.handler.install(this);
            }

//            propertyListeners.firePropertyChange(HANDLER_PROPERTY, old, handler);
        }

    }
    
}
