/*
 * RenderedLine.java
 *
 * Created on 08 January 2003, 15:02
 */

package org.geotools.renderer;

import java.awt.geom.GeneralPath;
import com.vividsolutions.jts.geom.Geometry;
import java.util.logging.Logger;
import java.awt.Graphics2D;
import org.geotools.feature.Feature;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Stroke;

/**
 *
 * @author  iant
 */
public class RenderedLine implements RenderedObject {
    /**
     * The logger for the rendering module.
     */
    private static final Logger LOGGER = Logger.getLogger(
                                                 "org.geotools.rendering");
    private static final RendererUtilities utils = new RendererUtilities();
    private Stroke stroke;
    private Geometry geom;
    private Feature feature;
    private GeneralPath path;
    private boolean renderable = false;
    /** Creates a new instance of RenderedLine */
    public RenderedLine(Feature feature, LineSymbolizer symbolizer) {
        if (symbolizer.getStroke() == null) {
            return;
        }
        this.feature = feature;
        stroke = symbolizer.getStroke();
        String geomName = symbolizer.geometryPropertyName();
        geom = RendererUtilities.findGeometry(feature, geomName);
        if (geom.isEmpty()) {
            return;
        }

        path = utils.createGeneralPath(geom);
        renderable = true;
    }
    /**
     * Renders the given feature as a line using the specified symbolizer.
     *
     * This is an internal method that should only be called by
     * processSymbolizers
     *
     * Geometry types other than inherently linear types can be used.
     * If a point geometry is used, it should be interpreted as a line of zero
     * length and two end caps.  If a polygon is used (or other "area" type)
     * then its closed outline will be used as the line string
     * (with no end caps).
     *
     * TODO: the properties of a symbolizer may, in part, be dependent on
     * TODO: attributes of the feature.  This is not yet supported.
     *
     * @param feature The feature to render
     * @param symbolizer The polygon symbolizer to apply
     **/
    public void render(Graphics2D graphics) {
        if(!isRenderable()) return;
    
        utils.applyStroke(graphics, stroke, feature);

        

        

        if (stroke.getGraphicStroke() == null) {
            graphics.draw(path);
        } else {
            // set up the graphic stroke
            utils.drawWithGraphicStroke(graphics, path, stroke.getGraphicStroke(), 
                                  feature);
        }
    }

    public boolean isRenderable() {
        return renderable;
    }    
    
}
