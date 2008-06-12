/*
 *    GeoTools - The Open Source Java GIS Tookit
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
import org.opengis.style.Font;

/**
 *
 * @author Johann Sorel
 */
class DefaultFont implements Font{

    private final List<Expression> family;
    private final Expression style;
    private final Expression weight;
    private final Expression size;
    
    DefaultFont(List<Expression> family, Expression style, Expression weight, Expression size){
        this.family = family;
        this.style = style;
        this.weight = weight;
        this.size = size;
    }
    
    public List<Expression> getFamily() {
        return family;
    }

    public Expression getStyle() {
        return style;
    }

    public Expression getWeight() {
        return weight;
    }

    public Expression getSize() {
        return size;
    }

}
