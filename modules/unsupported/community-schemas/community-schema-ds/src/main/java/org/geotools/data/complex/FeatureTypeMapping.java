/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.geotools.data.complex.filter.XPath.StepList;
import org.geotools.data.feature.FeatureSource2;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/2.4.x/modules/unsupported/community-schemas/community-schema-ds/src/main/java/org/geotools/data/complex/FeatureTypeMapping.java $
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

    /**
     * Finds the attribute mappings for the given target location path ignoring the xpath index
     * of each step.
     * @param targetPath
     * @return
     */
    public List/* <AttributeMapping> */getAttributeMappingsIgnoreIndex(final StepList targetPath) {
        AttributeMapping attMapping;
        List mappings = Collections.EMPTY_LIST;
        for(Iterator it = attributeMappings.iterator(); it.hasNext();){
            attMapping = (AttributeMapping) it.next();
            if(targetPath.equalsIgnoreIndex(attMapping.getTargetXPath())){
                if(mappings.size() == 0){
                    mappings = new ArrayList(2);
                }
                mappings.add(attMapping);
            }
        }
        return mappings;
    }

    /**
     * Finds the attribute mapping for the target expression
     * <code>exactPath</code>
     * 
     * @param exactPath
     *            the xpath expression on the target schema to find the mapping
     *            for
     * @return the attribute mapping that match 1:1 with <code>exactPath</code>
     *         or <code>null</code> if 
     */
    public AttributeMapping getAttributeMapping(final StepList exactPath) {
        AttributeMapping attMapping;
        for(Iterator it = attributeMappings.iterator(); it.hasNext();){
            attMapping = (AttributeMapping) it.next();
            if(exactPath.equals(attMapping.getTargetXPath())){
                return attMapping;
            }
        }
        return null;
    }

    public NamespaceSupport getNamespaces() {
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
