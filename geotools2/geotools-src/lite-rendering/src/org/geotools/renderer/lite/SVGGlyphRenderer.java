/*
 * SVGGlyphRenderer.java
 *
 * Created on April 17, 2004, 1:52 PM
 */

package org.geotools.renderer.lite;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.logging.Logger;
import org.apache.batik.transcoder.TranscoderInput;

/**
 *
 * @author  James
 */
public class SVGGlyphRenderer implements GlyphRenderer {
    
    private static final java.util.List formats =  java.util.Collections.unmodifiableList(java.util.Arrays.asList(new String[]{"image/svg"}));
    /** The logger for the rendering module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.rendering");
    
    /** Creates a new instance of SVGGlyphRenderer */
    public SVGGlyphRenderer() {
    }
    
    public boolean canRender(String format) {
        return (format.toLowerCase() == "image/svg"); 
    }
    
    public java.util.List getFormats() {
        return formats;
    }
    
    public java.awt.image.BufferedImage render(org.geotools.styling.Graphic graphic, org.geotools.styling.ExternalGraphic eg, org.geotools.feature.Feature feature) {
        try {
            BufferedImage img;
            URL svgfile = eg.getLocation();
            InternalTranscoder magic = new InternalTranscoder();
            TranscoderInput in = new TranscoderInput(svgfile
            .openStream());
            magic.transcode(in, null);
            img = magic.getImage();
            return img;
        } catch (java.io.IOException mue) {
            LOGGER.warning("Unable to load external svg file, " + mue.getMessage());   
            return null;
        } catch (Exception te) {
            LOGGER.warning("Unable to render external svg file, " + te.getMessage());
            return null;
        }
    }
    
}
