/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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

import java.util.List;
import java.util.Set;

import org.geotools.feature.FeatureType;

/**
 * Captures Namespace information for Catalog.
 * <p>
 * Interface provides a geotools2 front end for Namespace information
 * required for GML writer.
 * </p>
 * <p>
 * Example Use:
 * </p>
 * <pre><code>
 * Namespace topp = catalog.getNameSpace( "topp" );
 * 
 * for( Iterator i=topp.getDataStolreList(); i.hasNext();){
 *    DataStore data = (DataStore) i.next();
 *    String typeNames[] = data.getTypeNames();
 *    for( type = 0; typeNames.length; type++){
 *       System.out.println( topp.getPrefix()+"."+typeNames[ type ] );
 *    } 
 * }
 * </code></pre>
 * <p>
 * Note all MetaData classes are open ended, the OGC Metadata specification
 * allows for application specific extension. These classes are intended to
 * eventually represent the OGC MetaData and will need to reflect this open
 * ended nature.
 * </p>
 * @author jgarnett, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: NamespaceMetaData.java,v 1.1 2004/01/10 00:41:46 jive Exp $
 */
public interface NamespaceMetaData extends MetaData {
    
    /** Prefix used to identify Namespace in GML */
    public String getPrefix();
    
    /** URI where namespace is defined as an XMLSchema */
    public String getURI();
    
    /**
     * Set of typeNames held by this Namespace.
     * <p>
     * I wish we had strongly typed Sets, but this will do for now.
     * </p>
     * @return Set of typeNames (String) held by Namespace
     */
    public Set getTypeNames();    
    
    /**
     * Access FeatureType meta data information on a type in this Namespace
     * <p>
     * <p>
     * Example Use:
     * </p>
     * <pre><code>
     * FeatureTypeMetaData x = new NamespaceMetaData(...);
     * </code></pre>
     * 
     * @author jgarnett, Refractions Research, Inc.
     * @author $Author: jive $ (last modification)
     * @version $Id: NamespaceMetaData.java,v 1.1 2004/01/10 00:41:46 jive Exp $
     */
    public FeatureTypeMetaData getFeatureTypeMetaData( String typeName );

}
