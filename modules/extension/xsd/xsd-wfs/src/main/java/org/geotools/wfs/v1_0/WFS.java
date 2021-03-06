/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.wfs.v1_0;

import java.util.Set;

import org.geotools.filter.v1_0.OGC;

/**
 * XSD for wfs 1.0.
 * 
 * @author Justin Deoliveira, OpenGEO
 *
 *
 * @source $URL$
 */
public final class WFS extends org.geotools.wfs.WFS {

    /** singleton instance */
    private static final WFS instance = new WFS();

    /**
     * Returns the singleton instance.
     */
    public static final WFS getInstance() {
        return instance;
    }

    @Override
    protected void addDependencies(Set dependencies) {
        dependencies.add( OGC.getInstance() );
    }
    
    /**
     * Returns the location of 'WFS-transaction.xsd.'.
     */
    public String getSchemaLocation() {
        return getClass().getResource("WFS-transaction.xsd").toString();
    }
    
    /**
     * Returns '1.0.0'.
     */
    @Override
    public String getVersion() {
        return "1.0.0";
    }

}
