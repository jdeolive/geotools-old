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

import org.opengis.style.ContrastEnhancement;
import org.opengis.style.SelectedChannelType;
import org.opengis.style.StyleVisitor;

/**
 *
 * @author Johann Sorel
 */
class DefaultSelectedChannelType implements SelectedChannelType{

    private final String name;
    private final ContrastEnhancement enchance;
    
    DefaultSelectedChannelType(String name, ContrastEnhancement enchance){
        this.name = name;
        this.enchance = enchance;
    }
    
    public String getChannelName() {
        return name;
    }

    public ContrastEnhancement getContrastEnhancement() {
        return enchance;
    }

    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

}
