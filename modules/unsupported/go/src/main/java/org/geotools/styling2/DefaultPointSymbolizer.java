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

import javax.measure.unit.Unit;
import org.opengis.style.Description;
import org.opengis.style.Graphic;
import org.opengis.style.PointSymbolizer;
import org.opengis.style.StyleVisitor;

/**
 *
 * @author Johann Sorel
 */
class DefaultPointSymbolizer implements PointSymbolizer{

    private final Graphic graphic;
    private final Unit uom;
    private final String geom;
    private final String name;
    private final Description desc;
    
    DefaultPointSymbolizer(Graphic graphic, Unit uom, String geom, String name, Description desc){
        this.graphic = graphic;
        this.uom = uom;
        this.geom = geom;
        this.name = name;
        this.desc = desc;
    }
    
    public Graphic getGraphic() {
        return graphic;
    }

    public Unit getUnitOfMeasure() {
        return uom;
    }

    public String getGeometryPropertyName() {
        return geom;
    }

    public String getName() {
        return name;
    }

    public Description getDescription() {
        return desc;
    }

    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

}
