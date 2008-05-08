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

import org.geotools.data.ows.Request;


/**
 * Represents a PutStyles request
 * 
 * @author Richard Gould
 * @source $URL$
 */
public interface PutStylesRequest extends Request {
    public static final String MODE = "MODE";
    public static final String SLD = "SLD";
    public static final String SLD_BODY = "SLD_BODY";
    
    public static final String INSERT_AND_REPLACE = "InsertAndReplace";
    public static final String REPLACE_ALL = "ReplaceAll";

    
    /**
     * @param mode one of INSERT_AND_REPLACE or REPLACE_ALL
     */
    public void setMode(String mode);
    
    public void setSLD(String sld);
    
    public void setSLDBody(String sldBody);
}
