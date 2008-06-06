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

import javax.swing.Icon;
import org.opengis.style.ExternalMark;
import org.opengis.style.OnlineResource;

/**
 *
 * @author Johann Sorel
 */
class DefaultExternalMark implements ExternalMark{

    private final OnlineResource online;
    private final Icon icon;
    private final String format;
    private final int index;
    
    DefaultExternalMark(OnlineResource online, Icon icon, String format, int index){
        this.online = online;
        this.icon = icon;
        this.format = format;
        this.index = index;
    }
    
    public OnlineResource getOnlineResource() {
        return online;
    }

    public Icon getInlineContent() {
        return icon;
    }

    public String getFormat() {
        return format;
    }

    public int getMarkIndex() {
        return index;
    }

}
