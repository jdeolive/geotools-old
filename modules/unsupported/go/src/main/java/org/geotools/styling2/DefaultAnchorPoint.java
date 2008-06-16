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
import org.opengis.style.AnchorPoint;
import org.opengis.style.StyleVisitor;

/**
 *
 * @author Johann Sorel
 */
class DefaultAnchorPoint implements AnchorPoint{

    private final Expression anchorX;
    private final Expression anchorY;
    
    DefaultAnchorPoint(Expression anchorX, Expression anchorY){
        this.anchorX = anchorX;
        this.anchorY = anchorY;
    }
    
    public Expression getAnchorPointX() {
        return anchorX;
    }

    public Expression getAnchorPointY() {
        return anchorY;
    }

    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

}
