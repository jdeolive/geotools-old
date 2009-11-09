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

import java.util.LinkedHashMap;
import java.util.Map;

import org.opengis.util.ProgressListener;

import com.idl.javaidl.JIDLObjectI;
import com.idl.javaidl.JIDLString;

public interface IDLObjectWrapper extends JIDLObjectI{
    
    /**
     * Invokes the underlying IDL function with the proper set of input parameters as specified
     * by the Map. Note that the Map shall be an instance of {@link LinkedHashMap} in order to 
     * preserve parameters order.
     * 
     * @param inputParameters an instance of {@link LinkedHashMap} containing input parameters
     * @return the result of the processing as a {@link JIDLString}.
     * @throws IDLExecutionException 
     * 
     */
    public JIDLString execute(final Map<String, Object> inputParameters) throws IDLExecutionException;
    
    /**
     * Set a ProgressListener for this {@link IDLObjectWrapper}
     * 
     * @param listener
     */
    public void setProgressListener(final ProgressListener listener);
    
}
