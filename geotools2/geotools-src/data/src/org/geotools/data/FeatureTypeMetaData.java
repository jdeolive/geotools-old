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

import org.geotools.feature.FeatureType;

import java.io.IOException;
import java.util.List;


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
 * @version $Id: FeatureTypeMetaData.java,v 1.3 2004/01/12 12:32:27 jive Exp $
 */
public interface FeatureTypeMetaData extends MetaData {
    /**
     * Access typeName of this FeatureType
     * <p>
     * TypeName must be unique across both DataStore and Namesapce.
     * </p>
     * @return Name used to identify this FeatureType
     */
    
    public String getTypeName();
    /**
     * Access FeatureType (schema) information for this FeatureType.
     * 
     * <p>
     * Opperates as a convience method for:
     * </p>
     * <pre><code>
     * <b>return</b> getDataStoreMetaData().getDataStore().getSchema( typeName );
     * </code></pre>
     * 
     * <p>
     * This relationship should be turned around, the FeatureType should
     * provide a refernece to getFeatureTypeMetaData.
     * </p>
     *
     * @return Schema information for this DataType
     */
    public FeatureType getFeatureType() throws IOException;

    /**
     * Provides access to the real FeatureSource used to work with data.
     * <p>
     * Opperates as a convience method for:
     * </p>
     * <pre><code>
     * <b>return</b> getDataStoreMetaData().getDataStore().getFeatureSource( typeName );
     * </code></pre>
     * 
     * @param typeName
     * @return
     */
    public FeatureSource getFeatureSource() throws IOException;
    
    /**
     * Access DataStore meta data that defines this FeatureType.
     *
     * @return DataStoreMetaData associated with FeatureType
     */
    public DataStoreMetaData getDataStoreMetaData();

    /**
     * List of atributes names.
     * <p>
     * When moving to jdk15 this should become a strongly typed StringList
     * </p>
     * @return List of Names ad defined by FeatureType
     */
    public List getAttributeNames();

    /**
     * Retrieve AttributeName meta data information based on  NamespaceMetaData
     * purpose.
     *
     * @param attributeName name used to look up AttributeTypeMetaData
     *
     * @return AttributeTypeMetaData associated with attributeName
     */
    public AttributeTypeMetaData AttributeTypeMetaData(String attributeName);
}
