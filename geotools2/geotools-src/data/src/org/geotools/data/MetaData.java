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

/**
 * Common marker interface for MetaData interfaces
 * 
 * <p>
 * Currently many MetaData classes have access to the object they are intended
 * to represent. This situation needs to be reversed as these classes are
 * migrated to core. For now this is the best we can do.
 * </p>
 * 
 * <p></p>
 * 
 * <p>
 * Note all MetaData classes are open ended, the OGC Metadata specification
 * allows for application specific extension. These classes are intended to
 * eventually represent the OGC MetaData and will need to reflect this open
 * ended nature.
 * </p>
 *
 * @author jgarnett, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: MetaData.java,v 1.1 2004/01/10 00:41:46 jive Exp $
 */
public interface MetaData {
    /**
     * Tests if key has any associated meta data content.
     *
     * @return <code>true</code> if key has associated value
     */
    public boolean containsMetaData(String key);

    /**
     * Allow application to store MetaData by key.
     * 
     * <p>
     * Allows applications to store their own information, and constraints
     * associated with out MetaData classes.
     * </p>
     *
     * @param key Key used to identify meta data content
     * @param value MetaData to be stored.
     */
    public void putMetaData(String key, Object value);

    /**
     * Retrieve application provided meta data by key.
     * 
     * <p>
     * Allows applications to store their own information, and constraints
     * associated with out MetaData classes.
     * </p>
     *
     * @param key Used to identify meta data
     *
     * @return Previously stored value
     */
    public Object getMetaData(String key);
}
