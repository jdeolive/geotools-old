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

import java.util.List;
import org.opengis.filter.expression.Expression;
import org.opengis.style.AnchorPoint;
import org.opengis.style.Displacement;
import org.opengis.style.Graphic;
import org.opengis.style.GraphicSymbol;
import org.opengis.style.StyleVisitor;

/**
 *
 * @author Johann Sorel
 */
class DefaultGraphic implements Graphic{

    private final List<GraphicSymbol> symbols;
    private final Expression opacity;
    private final Expression size;
    private final Expression rotation;
    private final AnchorPoint anchor;
    private final Displacement disp;
    
    DefaultGraphic(List<GraphicSymbol> symbols, Expression opacity, Expression size, Expression rotation, AnchorPoint anchor, Displacement disp){
        this.symbols = symbols;
        this.opacity = opacity;
        this.size = size;
        this.rotation = rotation;
        this.anchor = anchor;
        this.disp = disp;
    }
    
    public List<GraphicSymbol> graphicSymbols() {
        return symbols;
    }

    public Expression getOpacity() {
        return opacity;
    }

    public Expression getSize() {
        return size;
    }

    public Expression getRotation() {
        return rotation;
    }

    public AnchorPoint getAnchorPoint() {
        return anchor;
    }

    public Displacement getDisplacement() {
        return disp;
    }

    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

}
