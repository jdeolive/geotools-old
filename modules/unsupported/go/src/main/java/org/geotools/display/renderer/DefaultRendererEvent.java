/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.display.renderer;

import java.util.Collection;
import java.util.Collections;
import org.opengis.display.primitive.Graphic;
import org.opengis.display.renderer.Renderer;
import org.opengis.display.renderer.RendererEvent;

/**
 * Default implementation of RendererEvent.
 * 
 * @author Johann Sorel
 */
class DefaultRendererEvent extends RendererEvent{

    /**
     * graphics concerned by the renderer event.
     */
    private Collection<Graphic> graphics = null;
    
    /**
     * Create a Renderer Event with a collection of graphic objects.
     * 
     * @param renderer : the renderer who generate this event
     * @param graphics : graphics concerned by this event
     */
    DefaultRendererEvent(Renderer renderer, Collection<Graphic> graphics){
        super(renderer);
        this.graphics = graphics;
    }
    
    /**
     * Create a Renderer Event with a single graphic object.
     * 
     * @param renderer : the renderer who generate this event
     * @param graphic : graphics concerned by this event
     */
    DefaultRendererEvent(Renderer renderer, Graphic graphic){
        super(renderer);
        this.graphics = Collections.singleton(graphic);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Graphic> getGraphics() {
        return graphics;
    }

}
