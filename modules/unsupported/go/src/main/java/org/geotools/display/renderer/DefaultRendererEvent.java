/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.display.renderer;

import java.util.Collection;
import java.util.Collections;
import org.opengis.display.primitive.Graphic;
import org.opengis.display.renderer.Renderer;
import org.opengis.display.renderer.RendererEvent;

/**
 *
 * @author johann sorel
 */
class DefaultRendererEvent extends RendererEvent{

    private Collection<Graphic> graphics = null;
    
    
    DefaultRendererEvent(Renderer renderer, Collection<Graphic> graphics){
        super(renderer);
        this.graphics = graphics;
    }
    
    DefaultRendererEvent(Renderer renderer, Graphic graphic){
        super(renderer);
        graphics = Collections.singleton(graphic);
    }
    
    @Override
    public Collection<Graphic> getGraphics() {
        return graphics;
    }

}
