/*
 * DynamicPolygonStyle2D.java
 *
 * Created on October 4, 2003, 5:30 PM
 */

package org.geotools.renderer.style;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Paint;
import org.geotools.feature.Feature;
import org.geotools.styling.Fill;
import org.geotools.styling.PolygonSymbolizer;

/**
 *
 * @author  jamesm
 */
public class DynamicPolygonStyle2D extends org.geotools.renderer.style.PolygonStyle2D {
    
    Feature feature;
    PolygonSymbolizer ps;
    
    /** Creates a new instance of DynamicPolygonStyle2D */
    public DynamicPolygonStyle2D(Feature f, PolygonSymbolizer sym) {
        feature = f;
        ps = sym;
    }
    
    public java.awt.Paint getFill() {
        Fill fill = ps.getFill();
        if (fill == null) {
            return null;
        }
        Paint fillPaint = Color.decode((String) fill.getColor().getValue(feature));
        
        // if a graphic fill is to be used, prepare the paint accordingly....
        org.geotools.styling.Graphic gr = fill.getGraphicFill();
        
        if (gr != null) {
            SLDStyleFactory fac = new SLDStyleFactory();
            fillPaint = fac.getTexturePaint(gr, feature);
        }
        
        return fillPaint;
    }
    
    public Composite getFillComposite(){
        Fill fill = ps.getFill();
        if (fill == null) {
            return null;
        }
        
        // get the opacity and prepare the composite
        float opacity = ((Number) fill.getOpacity().getValue(feature)).floatValue();
        if(opacity == 1) return null;
        
        return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
    }
    
}
