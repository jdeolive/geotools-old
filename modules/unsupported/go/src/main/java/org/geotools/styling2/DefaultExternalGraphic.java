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

import java.util.Collection;
import javax.swing.Icon;
import org.opengis.metadata.citation.OnLineResource;
import org.opengis.style.ColorReplacement;
import org.opengis.style.ExternalGraphic;
import org.opengis.style.StyleVisitor;

/**
 *
 * @author Johann Sorel
 */
class DefaultExternalGraphic implements ExternalGraphic{

    private final OnLineResource resource;
    private final Icon icon;
    private String format;
    private Collection<ColorReplacement> replaces;
    
    DefaultExternalGraphic(OnLineResource resource, Icon icon, String format, Collection<ColorReplacement> replaces){
        this.resource = resource;
        this.icon = icon;
        this.format = format;
        this.replaces = replaces;
    }
    
    public OnLineResource getOnlineResource() {
        return resource;
    }

    public Icon getInlineContent() {
        return icon;
    }

    public String getFormat() {
        return format;
    }

    public Collection<ColorReplacement> getColorReplacements() {
        return replaces;
    }

    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

}
