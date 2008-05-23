/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.gui.swing.go;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;
import org.geotools.display.canvas.AbstractCanvas;
import org.geotools.display.canvas.ReferencedCanvas2D;
import org.geotools.display.primitive.GraphicPrimitive2D;
import org.geotools.display.renderer.RenderingContext;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.display.canvas.Canvas;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author sorel
 */
public class DynamicGraphic extends GraphicPrimitive2D{

    private final int DELAY = 20;
    private final int STEP = 3;
    
    private Rectangle bounds = new Rectangle(120, 200, 20, 20);
    private int extendX = 100;
    private int extendY = 100;
    private boolean upflag = false;
    private boolean rightflag = false;
    
    
    public DynamicGraphic(){
        super(DefaultGeographicCRS.WGS84);
                
        
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                Rectangle concat = new Rectangle(bounds);
                
                bounds.x += (rightflag) ? +STEP : -STEP ;
                if(bounds.getMinX() < 0 || bounds.getMaxX() > extendX) rightflag = !rightflag;
                
                
                bounds.y += (upflag) ? +STEP : -STEP ;
                if(bounds.getMinY() < 0 || bounds.getMaxY() > extendY) upflag = !upflag;
                
                
                concat.add(bounds);                
                setDisplayBounds(concat);
            }
        }, DELAY, DELAY);

        
//        
//        Timer timer = new Timer(20, new ActionListener() {
//
//            public void actionPerformed(ActionEvent arg0) {
//                Rectangle concat = new Rectangle(bounds);
//                
//                bounds.x += (rightflag) ? +5 : -5 ;
//                if(bounds.getMinX() < 0 || bounds.getMaxX() > extendX) rightflag = !rightflag;
//                
//                
//                bounds.y += (upflag) ? +5 : -5 ;
//                if(bounds.getMinY() < 0 || bounds.getMaxY() > extendY) upflag = !upflag;
//                
//                
//                concat.add(bounds);                
//                setDisplayBounds(concat);
//            }
//        });
//        timer.start();
        
    }
    
    @Override
    public void paint(RenderingContext context) throws TransformException {
        Graphics2D g2 = context.getGraphics();
        
        
        float Red = (float)bounds.getCenterX()/(float)extendX;
        if(Red < 0 || Red > 1) Red = 0;
        
        float Blue = (float)bounds.getCenterY()/(float)extendY;
        if(Blue < 0 || Blue > 1) Blue = 0;
        
        Color c = new Color( Red, 0 , Blue );
        
        g2.setColor( c );
        g2.fillOval(bounds.x, bounds.y, bounds.width-1, bounds.height-1);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1));
        g2.drawOval(bounds.x, bounds.y, bounds.width-1, bounds.height-1);
        
    }

    @Override
    public void setCanvas(Canvas canvas) {
        super.setCanvas(canvas);
        ((AbstractCanvas)canvas).addPropertyChangeListener(ReferencedCanvas2D.DISPLAY_BOUNDS_PROPERTY, new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                Shape shp = (Shape) event.getNewValue();
                extendY = shp.getBounds().height;
                extendX = shp.getBounds().width;
            }
        });
    }

    
    
    
    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public Envelope getEnvelope() {
        return super.getEnvelope();
    }
        
}
