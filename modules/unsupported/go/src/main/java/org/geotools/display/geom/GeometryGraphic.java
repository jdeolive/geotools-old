/*
 *    GeoTools - An Open Source Java GIS Tookit
 *    http://geotools.org
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.display.geom;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.display.canvas.AbstractCanvas;
import org.geotools.display.primitive.FeatureGraphic;
import org.geotools.display.renderer.RenderingContext;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.LiteShape;
import org.geotools.referencing.CRS;
import org.opengis.display.canvas.Canvas;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class GeometryGraphic extends FeatureGraphic implements PropertyChangeListener{

    private final Geometry geometry;
    private LiteShape j2dShape;
    
    public GeometryGraphic(CoordinateReferenceSystem crs, SimpleFeature feature, double z){
        super(crs);
        setZOrderHint(z);
        
        BoundingBox box = feature.getBounds();
                
        this.geometry = (Geometry) feature.getDefaultGeometry();
               
        
        try {
            setEnvelope(box);
        } catch (TransformException ex) {
            ex.printStackTrace();
//            Logger.getLogger(MultiLineGraphic.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    @Override
    public void setCanvas(Canvas canvas) {
        super.setCanvas(canvas);
        
        ((AbstractCanvas)canvas).addPropertyChangeListener(AbstractCanvas.OBJECTIVE_TO_DISPLAY_PROPERTY, this);
    }

    
    
    
    
    @Override
    public void paint(RenderingContext context) throws TransformException {
        Graphics2D g2 = context.getGraphics();
        
        Rectangle clip = g2.getClipBounds();
        
        
        //optimise : only repaint in graphic is in the clip
        if(getDisplayBounds().intersects(clip)) {
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

            //        if(j2dShape == null){
            try {
                MathTransform transform = CRS.findMathTransform(getEnvelope().getCoordinateReferenceSystem(), context.displayCRS);
                geom = JTS.transform(this.geometry, transform);
                j2dShape = new LiteShape(geom, new AffineTransform(), false);
            } catch (FactoryException ex) {
                ex.printStackTrace();
            }

            Shape bounds = j2dShape.getBounds2D();
            setDisplayBounds(j2dShape.getBounds2D());

            if(bounds.intersects(clip)){
                g2.setStroke( new BasicStroke(1) );
                g2.setPaint( Color.BLUE );
                g2.draw(j2dShape);
            }
        }
           
    }

    public void propertyChange(PropertyChangeEvent prop) {
        j2dShape = null;
        setDisplayBounds(null);
    }
    
}
