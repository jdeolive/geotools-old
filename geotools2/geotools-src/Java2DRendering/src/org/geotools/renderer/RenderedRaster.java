/*
 * RenderedRaster2.java
 *
 * Created on 08 January 2003, 17:35
 */

package org.geotools.renderer;

import java.awt.Graphics2D;
import java.util.logging.Logger;
import org.geotools.feature.Feature;
import org.geotools.gc.GridCoverage;
import org.geotools.styling.RasterSymbolizer;

/**
 *
 * @author  iant
 */
public class RenderedRaster implements RenderedObject {
        /**
     * The logger for the rendering module.
     */
    private static final Logger LOGGER = Logger.getLogger(
                                                 "org.geotools.rendering");
    private boolean renderable = false;
    private GridCoverageRenderer gcr;
    /** Creates a new instance of RenderedRaster2 */
    public RenderedRaster(Feature feature, RasterSymbolizer symbolizer) {
            GridCoverage grid = (GridCoverage) feature.getAttribute("grid");
            gcr = new GridCoverageRenderer(grid);
            
        renderable = grid != null;
    }
    
    public boolean isRenderable() {
        return renderable;
    }
    
    public void render(Graphics2D graphics) {
        gcr.paint(graphics);
    }
    
}
