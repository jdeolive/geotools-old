/*
 * StyledRenderedMark.java
 *
 * Created on October 17, 2003, 2:17 PM
 */

package org.geotools.renderer.j2d;

import com.vividsolutions.jts.geom.Geometry;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D; 
import org.geotools.feature.Feature;
import org.geotools.renderer.style.Java2DMark;

/**
 *
 * @author  jamesm
 */
public class StyledMark extends org.geotools.renderer.j2d.RenderedMarks {
    Feature f;
    /** Creates a new instance of StyledRenderedMark */
    public StyledMark(Feature f) {
        this.f = f;
    }
    
    public MarkIterator getMarkIterator() {
        return new SingleMark(f);
    }
    
    
    
    class SingleMark extends org.geotools.renderer.j2d.MarkIterator{
        Feature feature;
        int pos = 0;
        public SingleMark(Feature f){
            feature = f;
            
        }
        
        public int getIteratorPosition() {
            return pos;
        }
        
        public boolean next() {
            return pos++<1;
           
        }
        
        public java.awt.geom.Point2D position() throws org.geotools.ct.TransformException {
          Geometry g = feature.getDefaultGeometry();
          return new Point2D.Double(g.getCoordinate().x, g.getCoordinate().y);
        }
        
        public void setIteratorPosition(int index) throws IllegalArgumentException {
            pos = index;
        }
        
        public Shape markShape(){
            GeneralPath shape = (GeneralPath)Java2DMark.getWellKnownMark("star"); 
            AffineTransform at = new AffineTransform();
            at.scale(10,10);
            return shape.createTransformedShape(at);
        }
    }
    
}
