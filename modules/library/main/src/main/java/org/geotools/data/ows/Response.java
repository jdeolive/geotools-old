/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.ows;

import java.io.IOException;
import java.io.InputStream;

import org.geotools.ows.ServiceException;
import org.jdom.JDOMException;


/**
 * Provides a base class for Responses from an OWS. Checks the incoming content
 * for a ServiceException and parses it if it encounters one.
 *
 * @author rgould
 * @source $URL$
 */
public abstract class Response {
    protected InputStream inputStream;
    protected String contentType;

    public Response(String contentType, InputStream inputStream) throws ServiceException, IOException {
        this.inputStream = inputStream;
        this.contentType = contentType;
        
        /*
         * Intercept XML ServiceExceptions and throw them
         */
        if (contentType.toLowerCase().equals("application/vnd.ogc.se_xml")) {
        	throw parseException(inputStream);
        }
    }

    public String getContentType() {
        return contentType;
    }

    /**
     * Returns the InputStream that contains the response from the server. 
     * The contents of this stream vary according to the type of request
     * that was made, and whether it was successful or not.
     * 
     * <B>NOTE:</B>
     * Note that clients using this code are responsible for closing the
     * InputStream when they are finished with it.
     * 
     * @return the input stream containing the response from the server
     */
    public InputStream getInputStream() {
        return inputStream;
    }
    
    protected ServiceException parseException(InputStream inputStream) throws IOException {
    	try {
			return ServiceExceptionParser.parse(inputStream);
		} catch (JDOMException e) {
			throw (IOException) new IOException().initCause(e);
		} finally {
			inputStream.close();
		}
    }
}
