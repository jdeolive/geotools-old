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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.geotools.factory.OptionalFactory;
import org.geotools.process.Process;
import org.geotools.process.impl.SingleProcessFactory;

/**
 * Factory for IDL Processes.
 */
public abstract class IDLProcessFactory extends SingleProcessFactory {

    /** The IDL folder containing IDL *.pro files */
    protected final static String IDL_LIB_FOLDER;
    
    /** The System's File separator char: / or \ depending on the OS */
    protected final static String FILE_SEPARATOR = System.getProperty("file.separator");
    
    protected final static Logger LOGGER = Logger.getLogger("org.geotools.process.idl");
    
    private final static DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");

    /** A Wrapper pool */
    private GenericObjectPool wrapperPool;

    private static boolean init = false;

    private static boolean isIDLAvailable;
    
    public static boolean isIDLAvailable() {
		return isIDLAvailable;
	}

	public abstract Process create();  

    static {
        loadIDL();
        final String IDL_HOME = System.getenv("IDL_HOME");
        if (IDL_HOME == null ||IDL_HOME.length()==0){
            LOGGER.warning("IDL_HOME variable need to be defined");
            IDL_LIB_FOLDER="";
        }
        else{
            IDL_LIB_FOLDER = new StringBuilder(IDL_HOME).append(FILE_SEPARATOR)
            .append("lib").toString();
        }
    }

    /**
     * Return true in case this {@link OptionalFactory} is available
     */
    public boolean isAvailable() {
        return isIDLAvailable();
    }

    /**
     * Load IDL Lib to check everything needed to support IDL processes, is on its place. 
     */
    private synchronized static void loadIDL() {
        if (init == false)
            init = true;
        else
            return;
        try {
            // Try loading a required IDL libraries
            System.loadLibrary("idl_ebutil");
            isIDLAvailable = true;
        } catch (UnsatisfiedLinkError e) {
            LOGGER.warning("Unable to load IDL DLLs");
            isIDLAvailable = false;
        }
    }

    /**
     * Return the wrapper pool.
     * @return the wrapper pool.
     */
    public GenericObjectPool getWrapperPool() {
        return wrapperPool;
    }

    /**
     * Set the wrapper pool for this factory.
     * @param wrapperPool the pool to be used for this factory.
     */
    protected void setWrapperPool(final GenericObjectPool wrapperPool) {
        this.wrapperPool = wrapperPool;
    }

    /**
     * Produce a timeStamp for the current time.
     * @return a String timestamp in the form yyyyMMddhhmmss.
     */
    public static String getTimeStamp() {
        return df.format(new Date(System.currentTimeMillis()));
    }

    /**
     * Call this method when the process have produced its result and it is no
     * more needed and then needs to be returned to the pool.
     * 
     * @param process
     */
    void returnWrapper(final IDLObjectWrapper wrapper) {
        try {
            wrapperPool.returnObject(wrapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Obtain a wrapper from the pool.
     * 
     * @return
     * @throws Exception
     */
    IDLObjectWrapper getWrapper() throws Exception {
            return (IDLObjectWrapper)wrapperPool.borrowObject();
    }
  
}
