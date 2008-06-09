package org.geotools.data.wps.request;

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

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.geotools.gml2.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;


/**
 * Describes an abstract ExecuteProcess request. Provides everything except
 * the versioning info, which subclasses must implement.
 * 
 * @author gdavis
 */
public abstract class AbstractExecuteProcessRequest extends AbstractWPSRequest implements ExecuteProcessRequest {

	/** only support POST for execute requests right now (in the future this
	 * could be dynamically set based on what properties are set
	 * for this request).
	 */
	private boolean usePost = true;
	
    /**
     * Constructs a basic ExecuteProcessRequest, without versioning info.
     * 
     * @param onlineResource the location of the request
     * @param properties a set of properties to use. Can be null.
     */
    public AbstractExecuteProcessRequest( URL onlineResource, Properties properties ) {
        super(onlineResource, properties);
    }
    
    protected void initRequest() {
        setProperty(REQUEST, "ExecuteProcess");
	}

	/**
     * @see org.geotools.data.wps.request.ExecuteProcessRequest#setIdentifier(java.lang.String)
     */
    public void setIdentifier( String identifier ) {
        setProperty(IDENTIFIER, identifier);
    }

    protected abstract void initVersion();
    
    @Override
	public boolean requiresPost() {
		return usePost;
	}
    
    @Override
	public void performPostOutput(OutputStream outputStream) throws IOException {
    	// Encode the request into GML2 with the schema provided in the
    	// describeprocess
    	Configuration config = new GMLConfiguration();
    	Encoder encoder = new Encoder(config);
    	
    	Set<Object> keyset = this.properties.keySet();
    	Iterator<Object> iterator = keyset.iterator();
    	while (iterator.hasNext()) {
    		Object key = iterator.next();
    		Object object = this.properties.get(key);
    		// will a null QName work? Or do I have to know the QName this
    		// object maps to?  I hope I don't need to know...
    		encoder.encode(object, null, outputStream);
    	}

	}      
}
