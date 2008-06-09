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
package org.geotools.data.wps.response;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.data.ows.Response;
import org.geotools.data.ows.ProcessDescription;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.geotools.ows.ServiceException;
import org.geotools.wps.WPSConfiguration;
import org.xml.sax.SAXException;

/**
 * Represents the response from a server after an ExecuteProcess request
 * has been issued.
 * 
 * @author gdavis
 */
public class ExecuteProcessResponse extends Response {

    private ProcessDescription processDesc;

    /**
     * @param contentType
     * @param inputStream
     * @throws ServiceException 
     * @throws SAXException
     */
    public ExecuteProcessResponse( String contentType, InputStream inputStream ) throws IOException, ServiceException {
        super(contentType, inputStream);
        
        try {
	        //Map hints = new HashMap();
	        //hints.put(DocumentHandler.DEFAULT_NAMESPACE_HINT_KEY, WPSSchema.getInstance());
        	Configuration config = new WPSConfiguration();
        	Parser parser = new Parser(config);
	
	        Object object;
			try {
				//object = DocumentFactory.getInstance(inputStream, hints, Level.WARNING);
				object =  parser.parse(inputStream);
			} catch (SAXException e) {
				throw (IOException) new IOException().
				initCause(e);
			} catch (ParserConfigurationException e) {
				throw (IOException) new IOException().initCause(e);
			}
	        
	        processDesc = (ProcessDescription) object;
        } finally {
        	inputStream.close();
        }
    }

    public ProcessDescription getProcessDesc() {
        return processDesc;
    }

}
