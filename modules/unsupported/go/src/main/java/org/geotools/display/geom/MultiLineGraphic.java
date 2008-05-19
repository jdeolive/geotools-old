/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.display.geom;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.display.primitive.FeatureGraphic;
import org.geotools.display.renderer.RenderingContext;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.LiteShape;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author sorel
 */
public abstract class MultiLineGraphic extends FeatureGraphic{

    private final MultiLineString line;
    private LiteShape j2dShape;
    
    
    public MultiLineGraphic(CoordinateReferenceSystem crs, MultiLineString line, double z){
        super(crs);
        setZOrderHint(z);
        this.line = line;
        
    }

    @Override
    public void paint(RenderingContext context) throws TransformException {
        Graphics2D g2 = context.getGraphics();
                
        AffineTransform trs = new AffineTransform();
        Geometry geom = null;
                
        try {
            MathTransform transform = CRS.findMathTransform(getEnvelope().getCoordinateReferenceSystem(), context.displayCRS);
//            System.out.println(transform);
            geom = JTS.transform(line, transform);
//            System.out.println(geom);
            j2dShape = new LiteShape(geom, trs, false);
        } catch (FactoryException ex) {
            Logger.getLogger(MultiLineGraphic.class.getName()).log(Level.SEVERE, null, ex);
        }
                
        
//        trs.scale(1, 1);
//        trs.translate(-897617, -1799350);
        
        
//        try {
//            trs = context.getAffineTransform(getEnvelope().getCoordinateReferenceSystem(), context.displayCRS);
////        System.out.println(j2dShape.getBounds2D());
//        } catch (FactoryException ex) {
//            Logger.getLogger(MultiLineGraphic.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        System.out.println(j2dShape.getBounds2D());
        
        
        g2.setStroke(new BasicStroke(3));
               
        g2.draw(j2dShape);
        
    }
    
    
    
    
    
}
