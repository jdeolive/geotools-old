/*
 * RenderedMark.java
 *
 * Created on 08 January 2003, 16:02
 */

package org.geotools.renderer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.feature.Feature;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;

/**
 *
 * @author  iant
 */
public class RenderedMark implements RenderedObject {
    /**
     * The logger for the rendering module.
     */
    private static final Logger LOGGER = Logger.getLogger(
                                                 "org.geotools.rendering");
    private static final RendererUtilities utils = new RendererUtilities();
    
    boolean renderable = false;
    private Geometry geom;
    private int size;
    private double rotation;
    private Mark mark;
    private Feature feature;
    
    /** Creates a new instance of RenderedMark */
    public RenderedMark(Geometry geom, Graphic graphic, Feature feature, Mark mark) {
        this.geom = geom;
        this.mark = mark;
        this.feature = feature;
        if (mark == null) {
            renderable = false;
            return;
        }

        String name = mark.getWellKnownName().getValue(feature).toString();

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("rendering mark " + name);
        }

        if (!utils.wellKnownMarks.contains(name)) {
            renderable = false;
            return ;
        }

        size = 6; // size in pixels
        rotation = 0.0; // rotation in degrees
        size = ((Number) graphic.getSize().getValue(feature)).intValue();
        rotation = (((Number) graphic.getRotation().getValue(feature)).doubleValue() * Math.PI) / 180d;

        renderable = true;
        return;
    
    }
    
    public boolean isRenderable() {
        return renderable;
    }
    
    public void render(Graphics2D graphics) {
        if(!isRenderable()) return ;
        utils.fillDrawMark(graphics, (Point) geom, mark, size, rotation, feature);
    }
    
}
