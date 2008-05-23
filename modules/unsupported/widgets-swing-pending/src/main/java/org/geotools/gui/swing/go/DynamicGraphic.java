/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.gui.swing.go;

import java.awt.Graphics2D;
import org.geotools.display.primitive.GraphicPrimitive2D;
import org.geotools.display.renderer.RenderingContext;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author sorel
 */
public class DynamicGraphic extends GraphicPrimitive2D{

    
    public DynamicGraphic(){
        super(DefaultGeographicCRS.WGS84);
        
    }
    
    @Override
    public void paint(RenderingContext context) throws TransformException {
        Graphics2D g2 = context.getGraphics();
        
        g2.drawLine(0, 0, 50, 50);
        g2.drawLine(50,50,120,30);
        g2.drawLine(120,30,70,10);
        g2.drawLine(70,10,35,59);
        g2.drawLine(35,59,45,85);
    }

    public boolean isVisible() {
        return true;
    }

    @Override
    public Envelope getEnvelope() {
        return super.getEnvelope();
    }

    

}
