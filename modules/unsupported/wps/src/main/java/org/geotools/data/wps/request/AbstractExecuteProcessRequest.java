/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.wps.request;


import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

import net.opengis.ows11.CodeType;
import net.opengis.ows11.Ows11Factory;
import net.opengis.wps.DataInputsType1;
import net.opengis.wps.ExecuteType;
import net.opengis.wps.WpsFactory;

import org.geotools.gml2.GMLConfiguration;
import org.geotools.wps.WPS;
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
    	
    	//http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd
    	
    	ExecuteType request = createExecuteType();
    	encoder.encode(request, WPS.Execute, outputStream);
    	
    	/*
    	Set<Object> keyset = this.properties.keySet();
    	Iterator<Object> iterator = keyset.iterator();
    	while (iterator.hasNext()) {
    		Object key = iterator.next();
    		Object object = this.properties.get(key);
    		// will a null QName work? Or do I have to know the QName this
    		// object maps to?  I hope I don't need to know...
    		encoder.encode(object, WPS.Execute, outputStream);
    	}
    	*/

	}   
    
    @SuppressWarnings("unchecked")
    private ExecuteType createExecuteType() {
        ExecuteType request = WpsFactory.eINSTANCE.createExecuteType();
        CodeType codetype = Ows11Factory.eINSTANCE.createCodeType();
        codetype.setValue((String)this.properties.get(this.IDENTIFIER));
        request.setIdentifier(codetype);
        request.setService("WPS");// TODO: un-hardcode
        request.setVersion("1.0.0");// TODO: un-hardcode
        DataInputsType1 inputs = WpsFactory.eINSTANCE.createDataInputsType1();
        //inputs.getInput().
        request.setDataInputs(inputs);
        //request.setResponseForm(value);
        return request;
    }    

}
