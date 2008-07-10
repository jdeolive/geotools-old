/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
import org.geotools.map.MapLayer;
import org.opengis.display.canvas.Canvas;
import org.opengis.display.primitive.Graphic;

/**
 * A graphic builder is a convinient way to build the same datas in a different
 * collection of graphic object, with different rendering and behavior.
 * 
 * @author Johann Sorel (Geomatys)
 */
public interface GraphicBuilder<T extends Graphic> {
    
    /**
     * Build a collection of graphics from the provided layer and canvas.
     * 
     * @param layer : Maplayer data source
     * @param canvas : Rendering canvas
     * @return Collection<Graphic> may not be null but can be empty.
     */
    Collection<T> createGraphics(MapLayer layer, Canvas canvas);
    
    /**
     * Returns the graphic type of this builder.
     * return class extends Graphic.
     */
    Class<T> getGraphicType();
    
}
