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
 */
package org.geotools.data;

import java.io.IOException;

/**
 * Captures additional information (and constraints) for FeatureType.
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
 * @version $Id: DataStoreMetaData.java,v 1.2 2004/01/12 13:38:41 jive Exp $
 */
public interface DataStoreMetaData extends MetaData {
    /**
     * Retrive the DataStore that reflects this MetaData.
     * 
     * <p>
     * When the MetaData classes move core, we can reverse this dependency.
     * </p>
     *
     * @return DataStore that reflects this Meta Data.
     */
    DataStore getDataStore() throws IOException;
}
