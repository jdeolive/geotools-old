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
import org.opengis.style.Graphic;
import org.opengis.style.GraphicStroke;

/**
 *
 * @author Johann Sorel
 */
class DefaultGraphicStroke implements GraphicStroke{

    private final Graphic graphic;
    private final Expression initial;
    private final Expression gap;
    
    DefaultGraphicStroke(Graphic graphic, Expression initial, Expression gap){
        this.graphic = graphic;
        this.gap = gap;
        this.initial = initial;
    }
    
    public Graphic getGraphic() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Expression getInitialGap() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Expression getGap() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
