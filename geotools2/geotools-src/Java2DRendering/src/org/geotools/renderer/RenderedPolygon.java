/*
 * RenderedPolygon.java
 *
 * Created on 08 January 2003, 12:58
 */

package org.geotools.renderer;

import com.vividsolutions.jts.geom.Geometry;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.feature.Feature;
import org.geotools.styling.Fill;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Stroke;

/**
 *
 * @author  iant
 */
public class RenderedPolygon implements RenderedObject{ 
    /**
     * The logger for the rendering module.
     */
    private static final Logger LOGGER = Logger.getLogger(
                                                 "org.geotools.rendering");
    private static final RendererUtilities utils = new RendererUtilities(); 
    
    private Feature feature;
    private GeneralPath path;
    private Fill fill;
    private Stroke stroke;
    /** Creates a new instance of RenderedPolygon 
     * @param feature The feature to render
     * @param symbolizer The polygon symbolizer to apply
     */
    public RenderedPolygon(Feature feature, PolygonSymbolizer symbolizer) {
        this.feature = feature;
        fill = symbolizer.getFill();
        stroke = symbolizer.getStroke();
        String geomName = symbolizer.geometryPropertyName();
        Geometry geom = utils.findGeometry(feature, geomName);

        if (geom==null||geom.isEmpty()) {
            LOGGER.warning("No geometry specified");
            return;
        }

        path = utils.createGeneralPath(geom);
    }
    /**
     * Renders the given feature as a polygon using the specified symbolizer.
     * Geometry types other than inherently area types can be used.
     * If a line is used then the line string is closed for filling (only)
     * by connecting its end point to its start point.
     * This is an internal method that should only be called by
     * processSymbolizers.
     *

     **/
    public void render(Graphics2D graphics) {
        if(path==null){
            return;
        }
        
        if (fill != null) {
            utils.applyFill(graphics, fill, feature); 

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("paint in renderPoly: " + graphics.getPaint());
            }
            
            graphics.fill(path);


            // shouldn't we reset the graphics when we return finished?
            utils.resetFill(graphics); 
        }

        if (stroke != null) {
            
            utils.applyStroke(graphics, stroke, feature); 

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("path is " + 
                             graphics.getTransform()
                                     .createTransformedShape(path)
                                     .getBounds2D().toString());
            }

            if (stroke.getGraphicStroke() == null) {
                graphics.draw(path);
            } else {
                // set up the graphic stroke
                utils.drawWithGraphicStroke(graphics, path, stroke.getGraphicStroke(), 
                                      feature); 
            }
        }
    }
    
    public boolean isRenderable(){
        return true;
    }
}
