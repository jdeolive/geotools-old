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
package org.geotools.styling2;

import org.opengis.filter.expression.Expression;
import org.opengis.style.GraphicFill;
import org.opengis.style.GraphicStroke;
import org.opengis.style.Stroke;

/**
 *
 * @author Johann Sorel
 */
class DefaultStroke implements Stroke{

    private final GraphicFill fill;
    private final GraphicStroke stroke;
    private final Expression color;
    private final Expression opacity;
    private final Expression width;
    private final Expression join;
    private final Expression cap;
    private final float[] dashes;
    private final Expression offset;
    
    
    DefaultStroke(GraphicFill fill, GraphicStroke stroke, Expression color, Expression opacity, Expression width, Expression join, Expression cap, float[] dashes, Expression offset){
        this.fill = fill;
        this.stroke = stroke;
        this.color = color;
        this.opacity = opacity;
        this.width = width;
        this.join = join;
        this.cap = cap;
        this.dashes = dashes;
        this.offset = offset;
    }
    
    public GraphicFill getGraphicFill() {
        return fill;
    }

    public GraphicStroke getGraphicStroke() {
        return stroke;
    }

    public Expression getColor() {
        return color;
    }

    public Expression getOpacity() {
        return opacity;
    }

    public Expression getWidth() {
        return width;
    }

    public Expression getLineJoin() {
        return join;
    }

    public Expression getLineCap() {
        return cap;
    }

    public float[] getDashArray() {
        return dashes;
    }
    
    public Expression getDashOffset() {
        return offset;
    }

    

}
