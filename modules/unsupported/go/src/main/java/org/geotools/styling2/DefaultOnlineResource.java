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

import java.net.URI;
import org.opengis.metadata.citation.OnLineFunction;
import org.opengis.metadata.citation.OnLineResource;
import org.opengis.style.StyleVisitor;
import org.opengis.util.InternationalString;

/**
 *
 * @author Johann Sorel
 */
class DefaultOnlineResource implements OnLineResource{

    private final URI uri;
    
    DefaultOnlineResource(URI uri){
        this.uri = uri;
    }
    
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public URI getLinkage() {
        return uri;
    }

    public String getProtocol() {
        return null;
    }

    public String getApplicationProfile() {
        return null;
    }

    public String getName() {
        return null;
    }

    public InternationalString getDescription() {
       return null;
    }

    public OnLineFunction getFunction() {
        return null;
    }

}
