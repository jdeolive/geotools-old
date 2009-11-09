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
package org.geotools.process.idl.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.process.Process;
import org.geotools.process.idl.IDLExecutionException;
import org.geotools.process.idl.IDLObjectWrapper;
import org.geotools.text.Text;
import org.geotools.util.logging.Logging;
import org.opengis.util.ProgressListener;

import com.idl.javaidl.JIDLArray;
import com.idl.javaidl.JIDLByte;
import com.idl.javaidl.JIDLDouble;
import com.idl.javaidl.JIDLException;
import com.idl.javaidl.JIDLFloat;
import com.idl.javaidl.JIDLInteger;
import com.idl.javaidl.JIDLLong;
import com.idl.javaidl.JIDLNotifyListener;
import com.idl.javaidl.JIDLNumber;
import com.idl.javaidl.JIDLObject;
import com.idl.javaidl.JIDLObjectI;
import com.idl.javaidl.JIDLOutputListener;
import com.idl.javaidl.JIDLShort;
import com.idl.javaidl.JIDLString;

/**
 * Base Implementation of {@link IDLObjectWrapper}. 
 * Instances of this class will be used by {@link Process}es 
 * based on IDL capabilities. 
 */
public abstract class BaseIDLObjectWrapper extends JIDLObject 
    implements IDLObjectWrapper, JIDLNotifyListener, JIDLOutputListener{

	private final static Logger LOGGER = Logging
		.getLogger("org.geotools.process.idl.impl");
	
	private static final long serialVersionUID = -3079444720037881431L;

	/** Method prefix to be found in order to use reflection */
    private final static String EXPOSED_METHOD_PREFIX = "_IDL__";
    
    private final static String ERROR_PREFIX = "FX ERROR";
    
    private String lastNotifiedError;
    
    /**
     * Return the last notified error.
     * @return
     */
    public String getLastNotifiedError() {
		return lastNotifiedError;
	}

    /**
     * Set the last notified error content.
     * @param lastNotifiedError
     */
	public void setLastNotifiedError(String lastNotifiedError) {
		this.lastNotifiedError = lastNotifiedError;
	}

	/**
	 * {@link BaseIDLObjectWrapper} constructor.
	 * @param className
	 * @param processName
	 */
	protected BaseIDLObjectWrapper(final String className, final String processName){
        super(className,processName);
    }
    
    /** A listener for this IDL Object Wrapper */
    private ProgressListener progressListener;
    
    
    /** 
     * Call the wrapper's function with the specified set of input parameters.
     * Note that the incoming map should become pre-ordered in order to assign the proper
     * input parameters to the related wrapper's argument.  
     * @throws IDLExecutionException 
     */
    public JIDLString execute(final Map<String, Object> inputParameters) throws IDLExecutionException{
        Map<String, Object>  parameters = null;
        
        // Wrap JAVA primitive types to proper IDL Objects to be used by the
        // incoming IDL's function call
        if (inputParameters != null && !inputParameters.isEmpty()){
            parameters = wrap(inputParameters);
        }
        
        final Method[] methods = this.getClass().getMethods();
        JIDLString result = null;
        
        // Variables to check conditions needed to successful method invocation
        boolean methodInvoked = false;
        boolean parameterSizeMatch = false;
        boolean parametersTypeMatch = true;

        //Find the proper method
        for (Method method: methods){
            if (method.getName().startsWith(EXPOSED_METHOD_PREFIX)){
                final Class<?>[] parameterTypes = method.getParameterTypes();
                final int size = parameterTypes.length;
                
                // //
                //
                // 1st check: Parameter size match
                //
                // //
                if (parameters.size() == size) {
                    parameterSizeMatch = true;
                    final Object[] args = new Object[size];
                    final Iterator<String> keys = parameters.keySet().iterator();

                    // Set arguments
                    for (int i = 0; i < size; i++) {
                        final Object value = ((LinkedHashMap<String, Object>) parameters)
                                .get(keys.next());
                        final Class<?> parameterClass = parameterTypes[i];
                        
                        // //
                        //
                        // 2nd check: Parameter types match
                        //
                        // //
                        if (!(parameterClass.isInstance(value))) {
                            parametersTypeMatch = false;
                            break;
                        }
                        else
                            args[i] = value;
                    }
                    if (parametersTypeMatch) {
                        try {
                            result = (JIDLString) method.invoke(this, args);
                        } catch (IllegalArgumentException e) {
                            throw new IDLExecutionException(
                                    "Invoking IDL method:" + e.getLocalizedMessage(), e);
                        } catch (IllegalAccessException e) {
                            throw new IDLExecutionException(
                                    "Invoking IDL method:" + e.getLocalizedMessage(), e);
                        } catch (InvocationTargetException e) {
                            final Throwable e1 = e.getCause();
                            if (e1 != null && e1 instanceof JIDLException) {
                                // This step is suggested in the IDL-Bridges
                                // documentation.
                                // Needs further investigation
                                this.executeString("MESSAGE, /RESET");
                                throw new IDLExecutionException(
                                        "Invoking IDL method:" + e1.getMessage(), e);
                            }
                            throw new IDLExecutionException(
                                    "Invoking IDL method:" + e.getMessage(), e);
                        } finally {
                        	if (lastNotifiedError != null && lastNotifiedError.length()>0)
                        		if (LOGGER.isLoggable(Level.WARNING))
                        			LOGGER.warning("IDL Algorithm Error Message:" + lastNotifiedError);
                        }
                        methodInvoked = true;
                        break;
                    }
                }
                else {
                    parameterSizeMatch = false;
                }
            }
        }
        // //
        //
        // Check method invocation have been done
        // 
        // //
        if (!methodInvoked){
            final StringBuilder sb = new StringBuilder("No valid IDL function have been found ");
            if (!parameterSizeMatch)
                sb.append("having the proper number of arguments");
            else
                sb.append("having the proper type of arguments");
            throw new IDLExecutionException(sb.toString());
        }
        return result;
    }

    /**
     * Catch the IDL Notify and propagate the message to the progress listener. 
     */
    public void OnIDLNotify(JIDLObjectI idlObject, final String task, final String value) {
    	
    	// Look for an IDL Error
    	if (task != null && task.length()>0 && task.equalsIgnoreCase(ERROR_PREFIX))
    		lastNotifiedError = value;
    	
    	// Notify the progress
    	if (progressListener != null){
    		final StringBuilder taskText = new StringBuilder(task);
            float progress = Float.NaN;
            try{
                progress = Float.parseFloat(value);
                
            }catch(NumberFormatException nfe){
                //TODO: Does nothing. Maybe, no progress percent have been notified.
                progress = Float.NaN;
            }
            if (Float.isNaN(progress)){
                taskText.append(":").append(value);
                progressListener.setTask(Text.text(taskText.toString()));
            }
            else{
                progressListener.progress(progress);
            }
        }
        
    }
    

    public void IDLoutput(JIDLObjectI object, String val) {
        //Default implementation actually does nothing
    }

    /**
     * Return the progress listener attached to this {@link IDLObjectWrapper}
     * @return
     */
    public ProgressListener getProgressListener() {
        return progressListener;
    }

    /**
     * Set the progress listener for this {@link IDLObjectWrapper}
     * @return
     */
    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }
    
    /**
     * Builds a Map containing proper JIDL entities to be used by the {@link IDLObjectWrapper}, 
     * by wrapping the Java Object entities contained in the input map.
     *  
     * TODO: Handle JIDLObjectI types
     */
    private static Map<String,Object> wrap(final Map<String,Object> params){
        if (params == null || params.size() == 0)
            throw new IllegalArgumentException("Provided parameters map is not valid");
        final int size = params.size();
       
        //prepare the wrapped parameters Map
        final Map<String,Object> wrapped = new LinkedHashMap<String, Object>(size);
        final Iterator<String> paramsIt = params.keySet().iterator();
        
        // Loop over the params
        while (paramsIt.hasNext()){
            final String key = paramsIt.next();
            final Object value = params.get(key);
            if (value instanceof String){
                wrapped.put(key, new JIDLString((String)value));
            }
            else if (value instanceof Number){
                wrapped.put(key, wrapNumber((Number)value));
            }
            else if (value instanceof String[] ||
                    value instanceof Number[]){
                wrapped.put(key, wrapArray((Object[])value));
            }
            else if (value instanceof byte[] ||
                value instanceof short[] ||
                value instanceof int[] ||
                value instanceof long[] ||
                value instanceof float[] ||
                value instanceof double[]){
                wrapped.put(key, wrapPrimitiveArray(value));
            } 
            else
                throw new IllegalArgumentException("Unsupported param type: " + value.getClass());
        }
        return wrapped;
    }

    /** 
     * Wrap a Java Array of primitives to a {@link JIDLArray} 
     */
    private static JIDLArray wrapPrimitiveArray(Object value) {
        final JIDLArray array = new JIDLArray(value);
        return array;
    }

    /** 
     * Wrap a Java Array of Objects to a {@link JIDLArray} 
     */
    private static JIDLArray wrapArray(Object[] value) {
        if (value == null || value.length == 0){
           throw new IllegalArgumentException("Provided Object array is null or empty");
        }
        final JIDLArray array = new JIDLArray(value);
        return array;
    }

    /**
     * Setup a proper {@link JIDLNumber} from a Java {@link Number}
     * 
     * @param value the Java {@link Number} to be transformed to a {@link JIDLNumber}
     * @return the wrapping {@link JIDLNumber}
     */
    private static JIDLNumber wrapNumber(final Number value) {
        if (value == null)
            throw new NullPointerException("Provided Number is null");
        if (value instanceof Byte)
            return new JIDLByte(value.byteValue());
        else if (value instanceof Short)
            return new JIDLShort(value.shortValue());
        else if (value instanceof Integer)
            return new JIDLInteger(value.intValue());
        else if (value instanceof Long)
            return new JIDLLong(value.longValue());
        else if (value instanceof Float)
            return new JIDLFloat(value.floatValue());
        else if (value instanceof Double)
            return new JIDLDouble(value.doubleValue());
        else 
            throw new IllegalArgumentException("Unsupported Number type");
    }

    @Override
    public String toString() {
        if (isObjectCreated()){
            final StringBuffer buffer = new StringBuffer();
            buffer.append("IDL Process Name: ").append(getProcessName()).
            append("\n IDLObject Class Name: ").append(getIDLObjectClassName()).
            append("\n IDLObject Variable Name: ").append(getIDLObjectVariableName());
            return buffer.toString();
        }
        else 
            return "No Underlying IDL Object available";
    }
}
