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


/**
 * Available WPS Operations are listed in a Request element.
 *
 * @author gdavis
 */
public class WPSRequest {
    private OperationType getCapabilities;
    private OperationType describeProcess;

    /**
     * DOCUMENT ME!
     *
     * @return Returns the getCapabilities.
     */
    public OperationType getGetCapabilities() {
        return getCapabilities;
    }

    /**
     * DOCUMENT ME!
     *
     * @param getCapabilities The getCapabilities to set.
     */
    public void setGetCapabilities(OperationType getCapabilities) {
        this.getCapabilities = getCapabilities;
    }


    public OperationType getDescribeProcess() {
        return describeProcess;
    }
    public void setDescribeProcess( OperationType describeProcess ) {
        this.describeProcess = describeProcess;
    }
 
}
