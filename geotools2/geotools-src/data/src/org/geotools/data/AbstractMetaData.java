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

import java.util.Map;

/**
 * Implements MetaData interface for subclasses.
 * 
 * @author jgarnett, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: AbstractMetaData.java,v 1.1 2004/01/10 01:40:31 jive Exp $
 */
public abstract class AbstractMetaData implements MetaData {
    protected Map metaData;
    /**
     * Implement containsMetaData.
     * @see org.geotools.data.MetaData#containsMetaData(java.lang.String)
     * 
     * @param key
     * @return
     */
    public boolean containsMetaData(String key) {
        return metaData.containsKey( key );
    }

    /**
     * Implement putMetaData.
     * @see org.geotools.data.MetaData#putMetaData(java.lang.String, java.lang.Object)
     * 
     * @param key
     * @param value
     */
    public void putMetaData(String key, Object value) {
        metaData.put( key, value );
    }

    /**
     * Implement getMetaData.
     * @see org.geotools.data.MetaData#getMetaData(java.lang.String)
     * 
     * @param key
     * @return value stored
     */
    public Object getMetaData(String key) {
        return metaData.get( key );
    }
}
