/*
 * RenderedExternalGraphic.java
 *
 * Created on 08 January 2003, 15:39
 */

package org.geotools.renderer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import org.geotools.feature.Feature;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.Graphic;

/**
 *
 * @author  iant
 */
public class RenderedExternalGraphic implements RenderedObject {
    /**
     * The logger for the rendering module.
     */
    private static final Logger LOGGER = Logger.getLogger(
                                                 "org.geotools.rendering");
    private static final RendererUtilities utils = new RendererUtilities();
    
    private Feature feature;
    
    private boolean renderable = false;
    private Geometry geom;
    private int size;
    private double rotation;
    private BufferedImage img;
    /** Creates a new instance of RenderedExternalGraphic */
    public RenderedExternalGraphic(Geometry geom, Graphic graphic, Feature feature, ExternalGraphic symbol) {
        this.geom = geom;
        img = utils.getImage(symbol);

        if (img != null) {
            size = ((Number) graphic.getSize().getValue(feature)).intValue();
            rotation = ((Number) graphic.getRotation().getValue(feature)).doubleValue();
            renderable = true;

        } else {
            
            renderable = false;
        }
    
    }
    
    public void render(Graphics2D graphics) {
        if(!isRenderable()) return ;
        
        utils.renderImage(graphics,(Point) geom, img, size, rotation);
    }
    
    public boolean isRenderable() {
        return renderable;
    }
    
}
