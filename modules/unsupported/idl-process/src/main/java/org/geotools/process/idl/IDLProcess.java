/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2009, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.process.idl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.process.ProcessFactory;
import org.geotools.process.idl.impl.BaseIDLObjectWrapper;
import org.geotools.process.impl.AbstractProcess;
import org.geotools.util.logging.Logging;
import org.opengis.util.ProgressListener;

import com.idl.javaidl.JIDLString;

/**
 * Main IDL Generic process abstract class. 
 * It exposes the IDL Processing capabilities as a {@link Process}.
 */
public abstract class IDLProcess extends AbstractProcess{

	private final static Logger LOGGER = Logging
	.getLogger("org.geotools.process.idl");
	
    //TODO: move to upper class.
    protected ProcessFactory getProcessFactory(){
        return factory;
    }
    
    protected IDLProcess(final ProcessFactory factory/*, final BaseIDLObjectWrapper wrapper*/) {
        super(factory);
    }
    
    /**
     * Default Process execute implementation
     */
    public Map<String, Object> execute(final Map<String, Object> input,
            ProgressListener monitor) {
        
        // Get a pooled object from the factory
        Map<String,Object> result;
        IDLObjectWrapper wrapper = null;
        try{
            wrapper = (IDLObjectWrapper) ((IDLProcessFactory)getProcessFactory()).getWrapper();
            wrapper.setProgressListener(monitor);
            final JIDLString idlresult = wrapper.execute(input);
            result = new HashMap<String, Object>();
            boolean noRes = false;
            if (idlresult == null || idlresult.stringValue() == null || 
            		idlresult.stringValue().trim().length() == 0)
            	noRes = true;
            if (!noRes)
            	result.put("result", new String(idlresult.stringValue()));
            else{
            	final StringBuilder sb = new StringBuilder("No result have been provided by the IDL processing");
            	final String errorMessage = ((BaseIDLObjectWrapper)wrapper).getLastNotifiedError();
            	if (errorMessage != null && errorMessage.length()>0)
            		sb.append(" due to the error: ").append(errorMessage);
            	if (LOGGER.isLoggable(Level.SEVERE))
                	LOGGER.severe(sb.toString());
            	return Collections.emptyMap();            	
            }

        } catch (NoSuchElementException e){
        	if (LOGGER.isLoggable(Level.SEVERE))
            	LOGGER.severe("No more wrappers available to execute the process");
        	return Collections.emptyMap();
            
        } catch(IDLExecutionException e){
        	if (LOGGER.isLoggable(Level.SEVERE))
            	LOGGER.severe("IDL Execution exception occured during IDL process execution:" + e.getLocalizedMessage());
        	return Collections.emptyMap();
            
        } catch (Exception e) {
        	if (LOGGER.isLoggable(Level.SEVERE))
            	LOGGER.severe(e.getLocalizedMessage());
        	return Collections.emptyMap();
        }
        finally{
            //Return the wrapper to the pool
            if (wrapper != null)
                ((IDLProcessFactory)getProcessFactory()).returnWrapper(wrapper);
        }
        return result;
    }
}
