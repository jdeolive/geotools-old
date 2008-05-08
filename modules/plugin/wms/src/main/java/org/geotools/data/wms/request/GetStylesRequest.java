/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.wms.request;

import org.geotools.data.ows.Layer;
import org.geotools.data.ows.Request;

/**
 * Represents a GetStyles Request
 * 
 * @author Richard Gould
 * @source $URL$
 */
public interface GetStylesRequest extends Request {
    public static final String LAYERS = "LAYERS";
    public static final String SLDVER = "SLDVER";
    
    /**
     * @param layers comma-separated list of named layers for which to retrieve style descriptions
     */
    public void setLayers(String layers);
    
    /**
     * @param sldVer the SLD version requested for the SLD document
     */
    public void setSLDver(String sldVer);
    
    /**
     * @return an array containing named layers
     */
    public Layer[] getLayers();
}
