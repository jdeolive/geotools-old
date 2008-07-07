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
import org.opengis.filter.expression.Expression;
import org.opengis.style.ChannelSelection;
import org.opengis.style.ColorMap;
import org.opengis.style.ContrastEnhancement;
import org.opengis.style.Description;
import org.opengis.style.OverlapBehavior;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.ShadedRelief;
import org.opengis.style.StyleVisitor;
import org.opengis.style.Symbolizer;

/**
 *
 * @author Johann Sorel
 */
class DefaultRasterSymbolizer implements RasterSymbolizer{

    private final Expression opacity;
    private final ChannelSelection selection;
    private final OverlapBehavior overlap;
    private final ColorMap colorMap;
    private final ContrastEnhancement enchance;
    private final ShadedRelief relief;
    private final Symbolizer outline;
    private final Unit uom;
    private final String geom;
    private final String name;
    private final Description desc;
    
    DefaultRasterSymbolizer(Expression opacity, 
            ChannelSelection selection, 
            OverlapBehavior overlap, 
            ColorMap colorMap, 
            ContrastEnhancement enchance,
            ShadedRelief relief,
            Symbolizer outline,
            Unit uom,
            String geom,
            String name,
            Description desc){
        this.opacity = opacity;
        this.selection = selection;
        this.overlap = overlap;
        this.colorMap = colorMap;
        this.enchance = enchance;
        this.relief = relief;
        this.outline = outline;
        this.uom = uom;
        this.geom = geom;
        this.name = name;
        this.desc = desc;
    }
    
    public Expression getOpacity() {
        return opacity;
    }

    public ChannelSelection getChannelSelection() {
        return selection;
    }

    public OverlapBehavior getOverlapBehavior() {
        return overlap;
    }

    public ColorMap getColorMap() {
        return colorMap;
    }

    public ContrastEnhancement getContrastEnhancement() {
        return enchance;
    }

    public ShadedRelief getShadedRelief() {
        return relief;
    }

    public Symbolizer getImageOutline() {
        return outline;
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
