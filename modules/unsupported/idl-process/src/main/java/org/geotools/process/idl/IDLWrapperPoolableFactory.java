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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.pool.PoolableObjectFactory;
import org.geotools.process.idl.impl.BaseIDLObjectWrapper;

/** 
 * A PoolableFactory needed to handle {@link IDLObjectWrapper} instances 
 */
public abstract class IDLWrapperPoolableFactory implements
        PoolableObjectFactory {

    protected final static Logger LOGGER = Logger.getLogger("org.geotools.process.idl");
    
    /** The IDLProcessFactory used by this PoolableFactory to create processes */
    private IDLProcessFactory factory;

    /** Process name prefix created by this factory */
    private String processNamePrefix;

    /**
     * The {@link IDLWrapperPoolableFactory} constructor.
     * @param factory
     * 			the factory used to instance IDL processes.
     * @param processNamePrefix
     * 			the name prefix of any process instanced by this factory.
     */
    public IDLWrapperPoolableFactory(final IDLProcessFactory factory,
            final String processNamePrefix) {
        this.factory = factory;
        this.processNamePrefix = processNamePrefix;
    }

    /**
     * @see org.apache.commons.pool.PoolableObjectFactory
     */
    public abstract void destroyObject(Object wrapper)  throws Exception;

    /**
     * @see org.apache.commons.pool.PoolableObjectFactory
     */
    public abstract Object makeObject() throws Exception;
    
    /**
     * @see org.apache.commons.pool.PoolableObjectFactory
     */
    public void activateObject(Object wrapper) throws Exception {
    	 final BaseIDLObjectWrapper theWrapper = (BaseIDLObjectWrapper)wrapper;
         theWrapper.setLastNotifiedError("");
    }

    /**
     * @see org.apache.commons.pool.PoolableObjectFactory
     */
    public void passivateObject(Object wrapper) throws Exception {
         final IDLObjectWrapper theWrapper = (IDLObjectWrapper)wrapper;
         theWrapper.setProgressListener(null);
    }

    /**
     * @see org.apache.commons.pool.PoolableObjectFactory
     */
    public boolean validateObject(Object wrapper) {
        IDLObjectWrapper theWrapper = (IDLObjectWrapper)wrapper;
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.log(Level.FINE, theWrapper.toString());
        return theWrapper.isObjectCreated();
    }

    /**
     * Return the process name prefix of this factory.
     * @return Return the process name prefix.
     */
    public String getProcessNamePrefix() {
        return processNamePrefix;
    }

    /**
     * Return the {@link IDLProcessFactory} used by this {@link PoolableObjectFactory}.
     * @return
     */
    public IDLProcessFactory getFactory() {
        return factory;
    }
}
