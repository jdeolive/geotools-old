/*
 * RenderedObject.java
 *
 * Created on 08 January 2003, 13:48
 */

package org.geotools.renderer;

import java.awt.Graphics2D;

/**
 *
 * @author  iant
 */
public interface RenderedObject {
    
    void render(Graphics2D graphics);
    boolean isRenderable();
}
