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

import org.geotools.feature.AttributeType;


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
 * @version $Id: AttributeTypeMetaData.java,v 1.1 2004/01/10 00:41:46 jive Exp $
 */
public interface AttributeTypeMetaData extends MetaData {
    /**
     * Retrive AttributeType associated with the MetaData.
     * 
     * <p>
     * This relationship should be reversed when MetaData migrates to core
     * </p>
     *
     * @return AttributeType for this MetaData
     */
    AttributeType getAttributeType();

    /**
     * Access AttributeName
     *
     * @return name of attribute
     */
    String getAttributeName();
}
