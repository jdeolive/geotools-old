/*
 * RenderedPoint.java
 *
 * Created on 08 January 2003, 15:11
 */

package org.geotools.renderer;

import com.vividsolutions.jts.geom.Geometry;
import java.awt.Graphics2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.feature.Feature;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Symbol;
import org.geotools.styling.TextMark;

/**
 *
 * @author  iant
 */
public class RenderedPoint implements RenderedObject {
    /**
     * The logger for the rendering module.
     */
    private static final Logger LOGGER = Logger.getLogger(
                                                 "org.geotools.rendering");
    private static final RendererUtilities utils = new RendererUtilities();
    
    private Geometry geom;
    private Feature feature;    
    private org.geotools.styling.Graphic sldgraphic;
    
    private RenderedObject rSymbol;
    
    /** Creates a new instance of RenderedPoint */
    public RenderedPoint(Feature feature, PointSymbolizer symbolizer) {
        String geomName = symbolizer.geometryPropertyName();
        geom = RendererUtilities.findGeometry(feature, geomName);
        this.feature = feature;
        sldgraphic = symbolizer.getGraphic();
        Symbol[] symbols = sldgraphic.getSymbols();
        

        for (int i = 0; i < symbols.length; i++) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("trying to render symbol " + i);
            }

            if (symbols[i] instanceof ExternalGraphic) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("rendering External graphic");
                }
                
                
                rSymbol = new RenderedExternalGraphic(geom, sldgraphic, feature, (ExternalGraphic) symbols[i]);

                if (rSymbol.isRenderable()) {
                    return;
                }
            }

            if (symbols[i] instanceof Mark) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("rendering mark @ PointRenderer " + 
                                 symbols[i].toString());
                }

                rSymbol = new RenderedMark(geom, sldgraphic, feature, (Mark) symbols[i]); 

                if (rSymbol.isRenderable()) {
                    return;
                }
            }

            if (symbols[i] instanceof TextMark) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("rendering text symbol");
                }

                rSymbol = new RenderedTextSymbol(geom, sldgraphic, feature,
                                        (TextMark) symbols[i]);

                if (rSymbol.isRenderable()) {
                    return;
                }
            }
        }
    }
    
    public void render(Graphics2D graphics) {
            
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("rendering a point from " + feature);
        }

        

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("sldgraphic = " + sldgraphic);
        }

        
        if (geom.isEmpty()) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("empty geometry");
            }

            return;
        }
        rSymbol.render(graphics);
    }
    
    public boolean isRenderable() {
        if(rSymbol != null ){
            return rSymbol.isRenderable();
        }else{
            return false;
        }
    }
    
}
