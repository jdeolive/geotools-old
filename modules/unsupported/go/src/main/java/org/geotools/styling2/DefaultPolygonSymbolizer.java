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
import org.opengis.style.Description;
import org.opengis.style.Displacement;
import org.opengis.style.Fill;
import org.opengis.style.PolygonSymbolizer;
import org.opengis.style.Stroke;

/**
 *
 * @author Johann Sorel
 */
class DefaultPolygonSymbolizer implements PolygonSymbolizer{

    private final Stroke stroke;
    private final Fill fill;
    private final Displacement disp;
    private final Expression offset;
    private final String uom;
    private final String geom;
    private final String name;
    private final Description desc;
    
    DefaultPolygonSymbolizer(Stroke stroke, Fill fill, Displacement disp, Expression offset, String uom, String geom, String name, Description desc){
        this.stroke = stroke;
        this.fill = fill;
        this.disp = disp;
        this.offset = offset;
        this.uom = uom;
        this.geom = geom;
        this.name = name;
        this.desc = desc;
    }
    
    public Stroke getStroke() {
        return stroke;
    }

    public Fill getFill() {
        return fill;
    }

    public Displacement getDisplacement() {
        return disp;
    }

    public Expression getPerpendicularOffset() {
        return offset;
    }

    public String getUnitOfMeasure() {
        return uom;
    }

    public String getGeometryAttribute() {
        return geom;
    }

    public String getName() {
        return name;
    }

    public Description getDescription() {
        return desc;
    }

}
