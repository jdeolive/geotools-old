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

package org.geotools.filter;

import org.opengis.filter.expression.ExpressionVisitor;

/**
 * special Expression for raster
 * 
 * @author Johann Sorel
 */
public class RasterBandExpression implements org.opengis.filter.expression.Expression{

    private final int band;
    
    public RasterBandExpression(int band){
        this.band = band;
    }
    
    
    public Object evaluate(Object object) {
        return evaluate(object, Object.class);
    }

    public <T> T evaluate(Object object, Class<T> context) {
        
        
        
        if(context.equals(Integer.class)){
            if(object instanceof int[]){
                Integer v = ((int[])object)[band];
                return (T)v;
            }else{
                Integer i = band;
                return (T)i;
            }
        }
        
        return null;
    }

    public Object accept(ExpressionVisitor visitor, Object extraData) {
        return null;
    }

}
