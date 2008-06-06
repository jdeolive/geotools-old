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

import org.opengis.filter.expression.Expression;
import org.opengis.style.ExternalMark;
import org.opengis.style.Fill;
import org.opengis.style.Mark;
import org.opengis.style.Stroke;

/**
 *
 * @author johann Sorel
 */
class DefaultMark implements Mark{

    private final Expression wkn;
    private final ExternalMark external;
    private final Fill fill;
    private final Stroke stroke;
    
    DefaultMark(Expression wkn, ExternalMark external, Fill fill, Stroke stroke){
        this.wkn = wkn;
        this.external = external;
        this.fill = fill;
        this.stroke = stroke;
    }
    
    public Expression getWellKnownName() {
        return wkn;
    }

    public ExternalMark getExternalMark() {
        return external;
    }

    public Fill getFill() {
        return fill;
    }

    public Stroke getStroke() {
        return stroke;
    }

}
