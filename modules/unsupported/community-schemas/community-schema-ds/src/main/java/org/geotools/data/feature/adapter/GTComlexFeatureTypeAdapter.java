/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.feature.adapter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.GeometryType;

/**
 * Adapts an ISO complex feature type to a GeoTools FeatureType by eliminating
 * complex properties.
 * <p>
 * This is of almost any use but to allow the geoserver Data module to deal with
 * ComplexDataStore as a normal DataStore instead of FeatureAccess, while we're
 * not allowed to merge Feature implementations
 * </p>
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
public class GTComlexFeatureTypeAdapter implements FeatureType {

    private AttributeDescriptor adaptee;

    private LinkedHashMap properties;

    private GeometryAttributeType defaultGeom;

    public GTComlexFeatureTypeAdapter(AttributeDescriptor featureDescriptor) {
        if (featureDescriptor.getType() instanceof ISOFeatureTypeAdapter) {
            throw new IllegalArgumentException(
                    "No need to adapt ISOFEatureTypeAdapter, use getAdaptee() instead");
        }
        this.adaptee = featureDescriptor;
        this.properties = new LinkedHashMap();

        org.opengis.feature.type.FeatureType type;
        type = (org.opengis.feature.type.FeatureType) adaptee.getType();
        
        List props = new LinkedList(type.getProperties());
        
        for (Iterator it = props.iterator(); it.hasNext();) {
            AttributeDescriptor att = (AttributeDescriptor) it.next();
            if (att.getType() instanceof ComplexType) {
                it.remove();
            } else {
                AttributeType gtAttType;
                gtAttType = GTAttributeTypeAdapter.adapter(att);

                if (att.getType() instanceof GeometryType) {
                    defaultGeom = (GeometryAttributeType) gtAttType;
                }
                properties.put(att.getName().getLocalPart(), gtAttType);
            }
        }

    }

    public AttributeDescriptor getAdaptee() {
        return adaptee;
    }

    public Feature create(Object[] attributes) throws IllegalAttributeException {
        throw new UnsupportedOperationException();
    }

    public Feature create(Object[] attributes, String featureID) throws IllegalAttributeException {
        throw new UnsupportedOperationException();
    }

    public Feature duplicate(Feature feature) throws IllegalAttributeException {
        throw new UnsupportedOperationException();
    }

    public int find(AttributeType type) {
        return find(type.getLocalName());
    }

    public int find(String attName) {
        int index = 0;
        for (Iterator it = properties.keySet().iterator(); it.hasNext();) {
            String name = (String) it.next();
            if (name.equals(attName)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public FeatureType[] getAncestors() {
        return new FeatureType[0];
    }

    public int getAttributeCount() {
        return properties.size();
    }

    public AttributeType getAttributeType(String xPath) {
        AttributeType att = (AttributeType) properties.get(xPath);
        return att;
    }

    public AttributeType getAttributeType(int position) {
        ArrayList arrayList = new ArrayList(properties.values());
        Object object = arrayList.get(position);
        return (AttributeType) object;
    }

    public AttributeType[] getAttributeTypes() {
        Collection collection = properties.values();
        AttributeType[] types = (AttributeType[]) collection.toArray(new AttributeType[0]);
        return types;
    }

    public GeometryAttributeType getDefaultGeometry() {
    	return defaultGeom;
    }

    public URI getNamespace() {
        try {
            URI ns = new URI(adaptee.getName().getNamespaceURI());
            return ns;
        } catch (URISyntaxException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
    }

    public String getTypeName() {
        return adaptee.getName().getLocalPart();
    }

    public boolean hasAttributeType(String xPath) {
        return properties.containsKey(xPath);
    }

    public boolean isAbstract() {
        return adaptee.getType().isAbstract();
    }

    public boolean isDescendedFrom(URI nsURI, String typeName) {
        return false;
    }

    public boolean isDescendedFrom(FeatureType type) {
        return false;
    }

}
