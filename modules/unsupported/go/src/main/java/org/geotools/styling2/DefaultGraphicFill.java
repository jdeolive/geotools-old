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

import java.util.Set;
import org.opengis.filter.expression.Expression;
import org.opengis.style.AnchorPoint;
import org.opengis.style.Displacement;
import org.opengis.style.GraphicFill;
import org.opengis.style.GraphicalSymbol;
import org.opengis.style.StyleVisitor;

/**
 *
 * @author Johann Sorel
 */
class DefaultGraphicFill extends DefaultGraphic implements GraphicFill{

    
    DefaultGraphicFill(Set<GraphicalSymbol> symbols, Expression opacity, Expression size, Expression rotation, AnchorPoint anchor, Displacement disp){
        super(symbols,opacity,size,rotation,anchor,disp);
    }

    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }


}
