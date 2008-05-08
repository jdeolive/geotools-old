/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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

package org.geotools.data.complex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.geotools.data.feature.FeatureSource2;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
public class FeatureTypeMapping {
    /**
     * 
     */
    private FeatureSource2 source;

    /**
     * Encapsulates the name and type of target Features
     */
    private AttributeDescriptor target;

    private List/* <String> */groupByAttNames;

    /**
     * Map of <source expression>/<target property>, where target property is
     * an XPath expression addressing the mapped property of the target schema.
     */
    List/* <AttributeMapping> */attributeMappings;
    
    NamespaceSupport namespaces;

    /**
     * No parameters constructor for use by the digester configuration engine as
     * a JavaBean
     */
    public FeatureTypeMapping() {
        this.source = null;
        this.target = null;
        this.attributeMappings = new LinkedList();
        this.groupByAttNames = Collections.EMPTY_LIST;
        this.namespaces = new NamespaceSupport();
    }

    public FeatureTypeMapping(FeatureSource2 source, AttributeDescriptor target,
            List/* <AttributeMapping> */mappings, NamespaceSupport namespaces) {
        this.source = source;
        this.target = target;
        this.attributeMappings = new LinkedList/* <AttributeMapping> */(mappings);
        this.namespaces = namespaces;

        this.groupByAttNames = Collections.EMPTY_LIST;
    }

   
    public List/* <AttributeMapping> */getAttributeMappings() {
        return new ArrayList(attributeMappings);
    }

    public NamespaceSupport getNamespaces(){
        return namespaces;
    }
    
    /**
     * Has to be called after {@link #setTargetType(FeatureType)}
     * 
     * @param elementName
     * @param featureTypeName
     */
    public void setTargetFeature(AttributeDescriptor featureDescriptor) {
        this.target = featureDescriptor;
    }

    public void setSource(FeatureSource2 source) {
        this.source = source;
    }

    public AttributeDescriptor getTargetFeature() {
        return this.target;
    }

    public FeatureSource2 getSource() {
        return this.source;
    }

    public List/* <String> */getGroupByAttNames() {
        return groupByAttNames;
    }

    public void setGroupByAttNames(List/* <String> */groupByAttNames) {
        this.groupByAttNames = groupByAttNames == null ? Collections.EMPTY_LIST : Collections
                .unmodifiableList(groupByAttNames);
    }
}
