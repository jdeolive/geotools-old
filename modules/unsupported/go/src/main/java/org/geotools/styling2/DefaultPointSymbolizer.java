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

import org.opengis.style.Description;
import org.opengis.style.Graphic;
import org.opengis.style.PointSymbolizer;

/**
 *
 * @author Johann Sorel
 */
class DefaultPointSymbolizer implements PointSymbolizer{

    private final Graphic graphic;
    private final String uom;
    private final String geom;
    private final String name;
    private final Description desc;
    
    DefaultPointSymbolizer(Graphic graphic, String uom, String geom, String name, Description desc){
        this.graphic = graphic;
        this.uom = uom;
        this.geom = geom;
        this.name = name;
        this.desc = desc;
    }
    
    public Graphic getGraphic() {
        return graphic;
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
