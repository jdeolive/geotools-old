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
 * <p>
 * Represents most of a xs:element for an XMLSchema.
 * </p>
 * 
 * <p>
 * we have three types of information to store, Schema defined types,
 * references and extentions on types. If the type represented is either  a
 * reference or a Schema defined type  then isRef should be true.
 * </p>
 * 
 * <p>
 * Please note that this breakdown by getType/getFragment/getName is not recomended by
 * David Zweiers for an alternat breakdown by isComplex/getType/getName please
 * see the GeoServer AttributeTypeInfoDTO class.
 * </p>
 * 
 * <p>
 * Non-complex types are of the form:
 * </p>
 * 
 * <ul>
 * <li>
 * <code>{element name='test' type='xs:string'/}</code>
 * </li>
 * <li>
 * <code>{element name='test' type='gml:PointType'/}</code>
 * </li>
 * </ul>
 * 
 * <p>
 * These cases have their type name stored in this.type
 * </p>
 * 
 * <p>
 * For complex types such as:<pre><code>
 * {element name='test'
 *   {xs:complexContent}
 *     {xs:extension base="gml:AbstractFeatureType"}
 *       {xs:sequence}
 *         {xs:element name="id"
 *                     type="xs:string"
 *                     minOccurs="0"/}
 *         {xs:element ref="gml:pointProperty"
 *                     minOccurs="0"/}
 *       {/xs:sequence}
 *     {/xs:extension}
 *  {/xs:complexContent}
 * {/element}
 * </code></pre>
 * The type will be equals to "(xml fragment)" and
 * fragment contains a similar to above.
 * </p>
 * 
 * <p>
 * minOccurs, maxOccurs and nillable are all attributes for all cases. There is
 * more stuff in the XMLSchema spec but we don't care to parse it out right now.
 * </p>
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
 * @version $Id: AttributeTypeMetaData.java,v 1.2 2004/02/07 00:38:17 jive Exp $
 */
public interface AttributeTypeMetaData extends MetaData {
    /** Value of getType() used to indicate that fragement is in use */
    public static final String TYPE_FRAGMENT = "(xml fragment)";
    
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
    
    /**
     * Element type, a well-known gml or xs type or <code>TYPE_FRAGMENT</code>.
     * 
     * <p>
     * If getType is equals to <code>TYPE_FRAGMENT</code> please consult
     * getFragment() to examine the actual user's definition.
     * </p>
     * 
     * <p>
     * Other than that getType should be one of the constants defined by
     * GMLUtils.
     * </p>
     *
     * @return The element, or <code>TYPE_FRAGMENT</code>
     */    
    String getType();
    
    /**
     * XML Fragment used to define stuff.
     * 
     * <p>
     * This property is only used with getType() is equals to "(xml fragment)".
     * </p>
     * 
     * <p>
     * baseGMLTypes can only be used in your XML fragment.
     * </p>
     *
     * @param fragment The fragment to set.
     */    
    String getFragment();
}
