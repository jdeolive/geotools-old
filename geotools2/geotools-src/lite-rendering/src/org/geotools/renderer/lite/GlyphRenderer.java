/*
 * GlyphRenderer.java
 *
 * Created on April 6, 2004, 11:06 AM
 */

package org.geotools.renderer.lite;

import java.awt.image.BufferedImage;
import java.util.List;
import org.geotools.feature.Feature;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.Graphic;

/**
 *
 * @author  jamesm
 */
public interface GlyphRenderer {
 
    public boolean canRender(String format);
    public List getFormats();
    
    public BufferedImage render(Graphic graphic, ExternalGraphic eg, Feature feature);
    
}
