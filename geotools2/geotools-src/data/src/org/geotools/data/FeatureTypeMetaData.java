/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
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
 * @version $Id: FeatureTypeMetaData.java,v 1.1 2004/01/10 00:41:46 jive Exp $
 */
public interface FeatureTypeMetaData extends MetaData {
    /**
     * Access FeatureType (schema) information for this FeatureType.
     * 
     * <p>
     * It is assumed that the interface will call through to the DataStore to
     * retrive the FeatureType.
     * </p>
     * 
     * <p>
     * This relationship should be turned around, the FeatureType should
     * provide a refernece to getFeatureTypeMetaData.
     * </p>
     *
     * @return Schema information for this DataType
     */
    public FeatureType getFeatureType(String typeName);

    /**
     * Access DataStore meta data that defines this FeatureType.
     *
     * @return DataStoreMetaData associated with FeatureType
     */
    public DataStoreMetaData getDataStoreMetaData();

    /**
     * List of atributes names.
     *
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
