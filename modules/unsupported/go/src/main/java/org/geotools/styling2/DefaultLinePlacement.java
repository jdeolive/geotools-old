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
import org.opengis.style.LinePlacement;

/**
 *
 * @author Johann Sorel
 */
class DefaultLinePlacement implements LinePlacement{

    private final Expression offset;
    private final Expression initial;
    private final Expression gap;
    private final boolean repeated;
    private final boolean aligned;
    private final boolean generalize;
    
    DefaultLinePlacement(Expression offset, Expression initial, Expression gap, boolean repeated, boolean aligned, boolean generalize){
        this.offset = offset;
        this.initial  = initial;
        this.gap = gap;
        this.repeated = repeated;
        this.aligned = aligned;
        this.generalize = generalize;
    }
    
    public Expression getPerpendicularOffset() {
        return offset;
    }

    public Expression getInitialGap() {
        return initial;
    }

    public Expression getGap() {
        return gap;
    }

    public boolean isRepeated() {
        return repeated;
    }

    public boolean IsAligned() {
        return aligned;
    }

    public boolean isGeneralizeLine() {
        return generalize;
    }

}
