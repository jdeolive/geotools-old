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
package org.geotools.data.ows;

import java.net.URL;

import org.opengis.util.InternationalString;

/**
 * Used to hold data regarding Processes. Used by the DescribeProcessResponse
 * 
 * @author gdavis
 */
public class ProcessDescription {
    private String name;
    private InternationalString title;
    private InternationalString description;
    private String owsType;
    private URL owsURL;
    
    public String getName() {
        return name;
    }
    public void setName( String name ) {
        this.name = name;
    }
    public InternationalString getTitle() {
    	return title;
    }
    public void setTitle(InternationalString t) {
    	this.title = t;
    }
    public InternationalString getDescription() {
    	return description;
    }
    public void setDescription(InternationalString d) {
    	this.description = d;
    }
    public String getOwsType() {
        return owsType;
    }
    public void setOwsType( String owsType ) {
        this.owsType = owsType;
    }
    public URL getOwsURL() {
        return owsURL;
    }
    public void setOwsURL( URL owsURL ) {
        this.owsURL = owsURL;
    }
}
