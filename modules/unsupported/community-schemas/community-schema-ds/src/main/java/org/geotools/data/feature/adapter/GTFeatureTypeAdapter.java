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
import java.rmi.server.UID;

import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.iso.AttributeBuilder;
import org.geotools.feature.iso.AttributeFactoryImpl;
import org.geotools.feature.iso.Types;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;

/**
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
public class GTFeatureTypeAdapter implements FeatureType {

    private org.opengis.feature.type.FeatureType type;

    public GTFeatureTypeAdapter(org.opengis.feature.type.FeatureType type) {
        if (type instanceof ISOFeatureTypeAdapter) {
            throw new IllegalArgumentException(
                    "No need to adapt ISOFEatureTypeAdapter, use getAdaptee() instead");
        }
        this.type = type;
    }

    public org.opengis.feature.type.FeatureType getAdaptee() {
        return type;
    }

    public Feature create(Object[] attributes) throws IllegalAttributeException {
        return create(attributes, new UID().toString());
    }

    public Feature create(Object[] attributes, String featureID) throws IllegalAttributeException {
        FeatureFactory ff = new AttributeFactoryImpl();
        AttributeBuilder builder = new AttributeBuilder(ff);
        builder.setType(this.type);
        Name[] names = Types.names(this.type);
        int count = names.length;
        for (int i = 0; i < count; i++) {
            builder.add(attributes[i], names[i]);
        }
        SimpleFeature feature = (SimpleFeature) builder.build(featureID);
        return new GTFeatureAdapter(feature, this);
    }

    public Feature duplicate(Feature feature) throws IllegalAttributeException {
        Feature duplicate = create(feature.getAttributes(null), feature.getID());
        return duplicate;
    }

    public int find(AttributeType type) {
        return find(type.getLocalName());
    }

    public int find(String attName) {
        Name[] names = Types.names(this.type);
        int index = -1;
        for (int i = 0; i < names.length; i++) {
            if (names[i].getLocalPart().equals(attName)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public FeatureType[] getAncestors() {
        // org.opengis.feature.type.AttributeType parent = type.getSuper();
        throw new UnsupportedOperationException("not implemented yet");
    }

    public int getAttributeCount() {
        if (type instanceof SimpleFeatureType) {
            return ((SimpleFeatureType) type).getNumberOfAttribtues();
        } else {
            return type.attributes().size();
        }
    }

    public AttributeType getAttributeType(String xPath) {
        if (type instanceof SimpleFeatureType) {
            SimpleFeatureType sf = (SimpleFeatureType) type;
            AttributeDescriptor descriptor = (AttributeDescriptor) Types.descriptor(sf, xPath);
            AttributeType gtAtt = new GTAttributeTypeAdapter(descriptor);
            return gtAtt;
        }
        throw new UnsupportedOperationException("not implemented for complex types");
    }

    public AttributeType getAttributeType(int position) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public AttributeType[] getAttributeTypes() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public GeometryAttributeType getDefaultGeometry() {
    	throw new UnsupportedOperationException("not implemented yet");
    }
    
    public URI getNamespace() {
        try {
            return new URI(type.getName().getNamespaceURI());
        } catch (URISyntaxException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
    }

    public String getTypeName() {
        return type.getName().getLocalPart();
    }

    public boolean hasAttributeType(String xPath) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public boolean isAbstract() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public boolean isDescendedFrom(URI nsURI, String typeName) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public boolean isDescendedFrom(FeatureType type) {
        throw new UnsupportedOperationException("not implemented yet");
    }

}
