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

package org.geotools.data.complex.config;

import java.util.ArrayList;
import java.util.Collection;

import org.geotools.feature.NameImpl;
import org.geotools.gml3.GMLSchema;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * This class represents the fake feature type needed for feature chaining for properties that are
 * not features. When a non feature is mapped separately in app schema data access, it is regarded
 * as a feature since it would have a feature source.
 * 
 * @author Rini Angreani, Curtin University of Technology
 */
public class NonFeatureTypeProxy extends ComplexTypeProxy implements FeatureType {

    /**
     * GML:name attribute needed to link a (non) feature to another
     */
    public static final Name NAME = new NameImpl("http://www.opengis.net/gml", "name");

    /**
     * The attribute descriptors
     */
    private final Collection<PropertyDescriptor> descriptors;

    /**
     * The real type
     */
    private final ComplexType subject;

    /**
     * Sole constructor
     * 
     * @param type
     *            The underlying non feature type
     */
    public NonFeatureTypeProxy(final AttributeType type) {
        super(type.getName(), null);

        assert type instanceof ComplexType;
        subject = (ComplexType) type;

        // initiate descriptors
        descriptors = new ArrayList<PropertyDescriptor>();
        descriptors.addAll(((ComplexType) type).getDescriptors());

        // Need to add gml:name for feature chaining
        final AttributeType abstractGMLType = GMLSchema.ABSTRACTGMLTYPE_TYPE;
        descriptors.add(((ComplexType) abstractGMLType).getDescriptor(NAME));
    }

    /**
     * @see org.geotools.feature.type.ComplexTypeImpl#getDescriptors()
     */
    @Override
    public Collection<PropertyDescriptor> getDescriptors() {
        return descriptors;
    }

    /**
     * @see org.geotools.feature.type.ComplexTypeImpl#getDescriptor(Name)
     */
    @Override
    public PropertyDescriptor getDescriptor(final Name name) {
        if (name.equals(NAME)) {
            return GMLSchema.ABSTRACTGMLTYPE_TYPE.getDescriptor(NAME);
        }
        return ((ComplexType) getSubject()).getDescriptor(name);
    }

    /**
     * @see org.geotools.feature.type.ComplexTypeImpl#getDescriptor(String)
     */
    @Override
    public PropertyDescriptor getDescriptor(final String name) {
        if (new NameImpl(name).equals(NAME)) {
            return GMLSchema.ABSTRACTGMLTYPE_TYPE.getDescriptor(NAME);
        }
        return ((ComplexType) getSubject()).getDescriptor(name);
    }

    /**
     * @see org.geotools.data.complex.config.ComplexTypeProxy#getSubject()
     */
    @Override
    public AttributeType getSubject() {
        return subject;
    }

    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return null;
    }

    public GeometryDescriptor getGeometryDescriptor() {
        return null;
    }
}
