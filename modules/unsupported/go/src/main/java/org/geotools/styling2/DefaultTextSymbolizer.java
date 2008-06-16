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
import org.opengis.style.Description;
import org.opengis.style.Fill;
import org.opengis.style.Font;
import org.opengis.style.Halo;
import org.opengis.style.LabelPlacement;
import org.opengis.style.StyleVisitor;
import org.opengis.style.TextSymbolizer;

/**
 *
 * @author Johann Sorel
 */
class DefaultTextSymbolizer implements TextSymbolizer{

    private final Expression label;
    private final Font font;
    private final LabelPlacement placement;
    private final Halo halo;
    private final Fill fill;
    private final String uom;
    private final String geom;
    private final String name;
    private final Description desc;
    
    DefaultTextSymbolizer(Expression label, Font font, LabelPlacement placement, Halo halo, Fill fill, String uom, String geom, String name, Description desc){
        this.label = label;
        this.font = font;
        this.placement = placement;
        this.halo = halo;
        this.fill = fill;
        this.uom = uom;
        this.geom = geom;
        this.name = name;
        this.desc = desc;
    }
    
    public Expression getLabel() {
         return label;
    }

    public Font getFont() {
        return font;
    }

    public LabelPlacement getLabelPlacement() {
        return placement;
    }

    public Halo getHalo() {
        return halo;
    }

    public Fill getFill() {
        return fill;
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
    
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

}
