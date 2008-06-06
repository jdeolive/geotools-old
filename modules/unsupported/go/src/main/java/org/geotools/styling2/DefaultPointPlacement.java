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
import org.opengis.style.AnchorPoint;
import org.opengis.style.Displacement;
import org.opengis.style.PointPlacement;

/**
 *
 * @author Johann Sorel
 */
class DefaultPointPlacement implements PointPlacement{

    private final AnchorPoint anchor;
    private final Displacement disp;
    private final Expression rotation;
    
    DefaultPointPlacement(AnchorPoint anchor, Displacement disp, Expression rotation){
        this.anchor = anchor;
        this.disp = disp;
        this.rotation = rotation;
    }
    
    public AnchorPoint getAnchorPoint() {
        return anchor;
    }

    public Displacement getDisplacement() {
        return disp;
    }

    public Expression getRotation() {
        return rotation;
    }

}
