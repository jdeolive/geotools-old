/*
 * RenderedTextSymbol.java
 *
 * Created on 08 January 2003, 16:20
 */

package org.geotools.renderer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.feature.Feature;
import org.geotools.styling.Graphic;
import org.geotools.styling.TextMark;

/**
 *
 * @author  iant
 */
public class RenderedTextSymbol implements RenderedObject {
    /**
     * The logger for the rendering module.
     */
    private static final Logger LOGGER = Logger.getLogger(
                                                 "org.geotools.rendering");
    private static final RendererUtilities utils = new RendererUtilities();
    
    private boolean renderable = false;
    
    private int size;
    private double rotation;
    private Feature feature;
    private TextMark mark;
    private Geometry geom;
    private java.awt.Font javaFont;
    /** Creates a new instance of RenderedTextSymbol */
    public RenderedTextSymbol(Geometry geom, Graphic graphic, Feature feature, TextMark mark ) {
        this.mark = mark;
        this.feature = feature;
        this.geom = geom;
        size = 6; // size in pixels
        rotation = 0.0; // rotation in degrees
        size = ((Number) graphic.getSize().getValue(feature)).intValue();
        rotation = (((Number) graphic.getRotation().getValue(feature)).doubleValue() * Math.PI) / 180d;
        javaFont = utils.getFont(feature, mark.getFonts()); 

        if (javaFont != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("found font " + javaFont.getFamily());
            }
        } else {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("failed to find font ");
            }
            renderable = false;
            return;
        }
        renderable = true;    
        return; 
    
    }
    
    public boolean isRenderable() {
        return renderable;
    }
    
    public void render(Graphics2D graphics) {
        if(!isRenderable()) return;
        
        double tx = ((Point) geom).getX();
        double ty = ((Point) geom).getY();
        
        graphics.setFont(javaFont);
        

        String symbol = mark.getSymbol().getValue(feature).toString();
        TextLayout tl = new TextLayout(symbol, javaFont, 
                                       graphics.getFontRenderContext());
        Rectangle2D textBounds = tl.getBounds();

        // TODO: consider if symbols should carry an offset
        double dx = textBounds.getWidth() / 2.0;
        double dy = textBounds.getHeight() / 2.0;
        utils.renderString(graphics, tx, ty, dx, dy, tl, feature, mark.getFill(),
                     rotation);

        return;
    }    
}
