/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.display.renderer;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.display.canvas.AWTCanvas2D;
import org.geotools.display.canvas.ReferencedCanvas;
import org.geotools.display.primitive.GraphicPrimitive2D;
import org.opengis.display.canvas.Canvas;
import org.opengis.display.primitive.Graphic;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author johann sorel
 */
public class AWTDirectRenderer2D extends ReferencedRenderer2D{

    protected AWTCanvas2D canvas;
    
    
    public AWTDirectRenderer2D(){
        super();
    }
    
    @Override
    public AWTCanvas2D getCanvas() {
        return canvas;
    }

    @Override
    public boolean paint(Graphics2D output, AffineTransform zoom) {
                
        output.addRenderingHints(hints);
        
        final Rectangle displayBounds = canvas.getDisplayBounds().getBounds();
        Rectangle clipBounds = output.getClipBounds();
                
        final RenderingContext context = new RenderingContext(getCanvas(), displayBounds, false);        
        context.setGraphics(output, zoom);
                
        /*
         * Draw all graphics, starting with the one with the lowest <var>z</var> value. Before
         * to start the actual drawing,  we will notify all graphics that they are about to be
         * drawn. Some graphics may spend one or two threads for pre-computing data.
         */
        final List<Graphic> graphics = getSortedGraphics();
        
        for(Graphic graphic : graphics){
            if(graphic instanceof GraphicPrimitive2D){
                try {
                    ((GraphicPrimitive2D) graphic).paint(context);
                } catch (TransformException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(AWTDirectRenderer2D.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        
        return true;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = (AWTCanvas2D) canvas;
    }

}
