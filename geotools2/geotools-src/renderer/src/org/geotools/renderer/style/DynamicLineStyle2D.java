/*
 * DynamicLineStyle2D.java
 *
 * Created on October 6, 2003, 6:25 PM
 */

package org.geotools.renderer.style;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Paint;
import org.geotools.feature.Feature;
import org.geotools.filter.Expression;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Stroke;
import org.geotools.styling.Symbolizer;

/**
 *
 * @author  jamesm
 */
public class DynamicLineStyle2D extends org.geotools.renderer.style.LineStyle2D {
    
    protected Feature feature;
    protected LineSymbolizer ls;
    
    /** Creates a new instance of DynamicLineStyle2D */
    public DynamicLineStyle2D(Feature feature, LineSymbolizer sym) {
        this.feature = feature; 
        ls = sym;
    }
    
    public java.awt.Stroke getStroke() {
        Stroke stroke = ls.getStroke();
        if (stroke == null) {
            return null;
        } 
        
        // resolve join type into a join code
        String joinType;
        int joinCode;
        
        joinType = evaluateExpression(stroke.getLineJoin(), feature, "miter");
        
        joinCode = SLDStyleFactory.lookUpJoin(joinType);
        
        // resolve cap type into a cap code
        String capType;
        int capCode;
        
        capType = evaluateExpression(stroke.getLineCap(), feature, "square");
        capCode = SLDStyleFactory.lookUpCap(capType);
        
        
        // get the other properties needed for the stroke
        float[] dashes = stroke.getDashArray();
        float width = ((Number) stroke.getWidth().getValue(feature)).floatValue();
        float dashOffset = ((Number) stroke.getDashOffset().getValue(feature)).floatValue();
        
        // Simple optimization: let java2d use the fast drawing path if the line width
        // is small enough...
        if (width <= 1) {
            width = 0;
        }
        
        // now set up the stroke
        BasicStroke stroke2d;
        
        if ((dashes != null) && (dashes.length > 0)) {
            stroke2d = new BasicStroke(width, capCode, joinCode, 1, dashes, dashOffset);
        } else {
            stroke2d = new BasicStroke(width, capCode, joinCode, 1);
        }
        return stroke2d;
    }
    
    public java.awt.Composite getContourComposite() {
        Stroke stroke = ls.getStroke();
        if (stroke == null) {
            return null;
        } 
   
        float opacity = ((Number) stroke.getOpacity().getValue(feature)).floatValue();
        Composite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
        return composite;
    }
    
    public java.awt.Paint getContour() {
        Stroke stroke = ls.getStroke();
        if (stroke == null) {
            return null;
        } 
        // the foreground color
        Paint contourPaint = Color.decode((String) stroke.getColor().getValue(feature));
        
        // if a graphic fill is to be used, prepare the paint accordingly....
        org.geotools.styling.Graphic gr = stroke.getGraphicFill();
        SLDStyleFactory fac = new SLDStyleFactory();
        if (gr != null) {
            contourPaint = fac.getTexturePaint(gr, feature);
        }
        return contourPaint;
    }
    
    
    /**
     * Evaluates an expression over the passed feature, if the expression or the result is null,
     * the default value will be returned
     *
     * @param e DOCUMENT ME!
     * @param feature DOCUMENT ME!
     * @param defaultValue DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private String evaluateExpression(Expression e, Feature feature, String defaultValue) {
        String result = defaultValue;
        
        if (e != null) {
            result = (String) e.getValue(feature);
            
            if (result == null) {
                result = defaultValue;
            }
        }
        
        return result;
    }
}
