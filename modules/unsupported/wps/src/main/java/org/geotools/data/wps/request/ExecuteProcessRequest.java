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
package org.geotools.data.wps.request;

import org.geotools.data.ows.Request;

/**
 * Executes a process. 
 * 
 * @author gdavis
 */
public interface ExecuteProcessRequest extends Request {
    /** Represents the PROCESS parameter */
    public static final String IDENTIFIER = "IDENTIFIER"; //$NON-NLS-1$
    
    /**
     * Sets the name of the process to execute
     * 
     * @param processname a unique process name
     */
    public void setIdentifier(String processname);
}
