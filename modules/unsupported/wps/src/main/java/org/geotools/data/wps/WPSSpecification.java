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
package org.geotools.data.wps;

import java.net.URL;

import org.geotools.data.ows.Specification;
import org.geotools.data.wps.request.DescribeProcessRequest;


public abstract class WPSSpecification extends Specification {

    /**
     * Creates a DescribeProcess request which can be used to retrieve
     * information about a specific process on the WPS Server.
     * 
     * @param onlineResource the location where the request can be made
     * @return a DescribeProcessRequest to be configured and then passed to the WPS Server
     * @throws UnsupportedOperationException if the version of the specification doesn't support this request
     */
    public abstract DescribeProcessRequest createDescribeProcessRequest( URL onlineResource ) throws UnsupportedOperationException;

 }
