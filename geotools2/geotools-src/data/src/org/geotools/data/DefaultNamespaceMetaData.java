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

import java.util.Set;

/**
 * Example NamespaceMetaData implementation.
 * <p>
 * This class may be used, or sublcassed for your own applications.
 * </p>
 * 
 * @author jgarnett, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: DefaultNamespaceMetaData.java,v 1.1 2004/01/10 01:40:31 jive Exp $
 */
public class DefaultNamespaceMetaData extends AbstractMetaData implements NamespaceMetaData {
    String prefix;
    String uri;
    Set featureTypes;
    
    /**
     * Quick constructor based on prefix.
     * @param prefix Prefix used when writing out GML
     */
    public DefaultNamespaceMetaData(String prefix) {
    }

    /**
     * URI for this namespace.
     * <p>
     * The URI should point to an XMLSchema document.
     * </p>
     * @see org.geotools.data.NamespaceMetaData#getURI()
     * 
     * @return XMLSchema URI for this namespace
     */
    public String getURI() {
        return null;
    }

    /**
     * Implement getTypeNames.
     * <p>
     * Description ...
     * </p>
     * @see org.geotools.data.NamespaceMetaData#getTypeNames()
     * 
     * @return
     */
    public Set getTypeNames() {
        return null;
    }

    /**
     * Implement getFeatureTypeMetaData.
     * <p>
     * Description ...
     * </p>
     * @see org.geotools.data.NamespaceMetaData#getFeatureTypeMetaData(java.lang.String)
     * 
     * @param typeName
     * @return
     */
    public FeatureTypeMetaData getFeatureTypeMetaData(String typeName) {
        return null;
    }



    /**
     * Implement getPrefix.
     * @see org.geotools.data.NamespaceMetaData#getPrefix()
     * 
     * @return prefix used for namespace
     */
    public String getPrefix() {
        return prefix;
    }

}
