/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003, Geotools Project Managment Committee (PMC)
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

import org.geotools.feature.AttributeType;

/**
 * Provides support for creating AttributeReaders.
 * 
 * @version $Id: AbstractAttributeIO.java,v 1.2 2003/11/04 00:28:49 cholmesny Exp $
 * @author  Ian Schneider
 */
public abstract class AbstractAttributeIO {
    
    protected AttributeType[] metaData;
    
    protected AbstractAttributeIO(AttributeType[] metaData) {
        this.metaData = metaData;
    }
    
    /**
     * Copy the meta-data from this reader, but don't use the reader!!
     */
    protected AbstractAttributeIO(AttributeReader defs) {
        this(copy(defs));
    }
    
    public static AttributeType[] copy(AttributeReader defs) {
        AttributeType[] d = new AttributeType[defs.getAttributeCount()];
        for (int i = 0, ii = d.length; i < ii; i++) {
            d[i] = defs.getAttributeType(i);
        }
        return d;
    }
    
    public final int getAttributeCount() {
        return metaData.length;
    }
    
    public final AttributeType getAttributeType(int position) {
        return metaData[position];
    }
    
    
    
}
