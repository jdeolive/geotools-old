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
                
                
        Geometry geom = null;
        
//        context.setGraphicsCRS(context.objectiveCRS);
//        try {
//            MathTransform transform = CRS.findMathTransform(getEnvelope().getCoordinateReferenceSystem(), context.objectiveCRS);
//            geom = JTS.transform(line, transform);
//        } catch (FactoryException ex) {
//            ex.printStackTrace();
//        }
//                
//        System.out.println("ObjectiveCRS => " + geom);
        
        
        context.setGraphicsCRS(context.displayCRS);
        try {
            MathTransform transform = CRS.findMathTransform(getEnvelope().getCoordinateReferenceSystem(), context.displayCRS);
            geom = JTS.transform(line, transform);
        } catch (FactoryException ex) {
            ex.printStackTrace();
        }
        
//        System.out.println("DisplayCRS => " + geom);
        
        j2dShape = new LiteShape(geom, new AffineTransform(), false);
        g2.setStroke(new BasicStroke(2));               
        g2.draw(j2dShape);
        
        
        
    }
    
    
    
    
    
}
