/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data;

import org.geotools.factory.Factory;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.factory.FactoryFinder;

/**
 * This specifies the interface to create FeatureLocks.
 * <p>
 * Sample use:
 * <code><pre>
 * FeatureLock lock = FeatureLockFactory.generate( "MyLock", 3600 );
 * </pre></code>
 * @version $Id: FeatureLockFactory.java,v 1.2 2003/10/01 23:40:31 jive Exp $
 * @task REVISIT: Combine this with a factory to also make Query objects?
 * @author Chris Holmes, TOPP
 */

public abstract class FeatureLockFactory implements Factory {
    /** A cached factory to create FeatureLocks. */
    private static FeatureLockFactory factory = null;
    
    /**
     * Gets an instance of the FeatureLockFactory.
     *
     * @return A FeatureLockFactory instance.
     *
     * @throws FactoryConfigurationError If there exists a configuration error.
     */
    public static FeatureLockFactory getInstance()
        throws FactoryConfigurationError {
        if (factory == null) {
        factory = (FeatureLockFactory) FactoryFinder
            .findFactory("org.geotools.data.FeatureLockFactory",
                "org.geotools.data.DefaultFeatureLockFactory");
	}
        return factory;
    }

    /**
     * Generates a new FeatureLock for use.
     * <p>
     * The lock will be of the form:
     * </p>
     * <table border=1 width="100%" background="gray"><code><pre>
     * LockID{number}
     * Where:
     *   {number} - is unique based on time and expiration requested
     * </code></pre></table>
     * <p>
     * The resulting lock is unique.</p>
     * <p>
     * To aid in tracing your may wish to supply your own name,
     * rather than <code>LockID<code>, for use in lock generation.</p>
     * 
     * @param duration FeatureLock duration in seconds
     */
    public static FeatureLock generate(long duration) {
	   return generate("LockID", duration);
    }

    /**
     * Generates a new FeatureLock for use.
     * 
     * The lock will be of the form:
     * <table border=1 width="100%" background="gray"><code><pre>
     * {name}{number}
     * Where:
     *   {number} - is unique based on time and expiration requested
     * </code></pre></table>
     * The resulting lock is unique.
     * <p>
     * To aid in tracing your may wish to supply your own name,
     * rather than <code>LockID<code>, for use in lock generation.
     * @param name User supplied name used in lock generation.
     * @param date Date lock expires on.
     */
    public static FeatureLock generate(String name, long duration){
        return getInstance().createLock(name, duration);
    } 

    protected abstract FeatureLock createLock(String name, long duration);

}
