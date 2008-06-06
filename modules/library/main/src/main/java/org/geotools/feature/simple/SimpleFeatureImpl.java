/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.feature.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.geotools.feature.FeatureImpl;
import org.geotools.feature.IllegalAttributeException;
import org.opengis.feature.Attribute;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;

/**
 * An implementation of the SimpleFeature convience methods ontop of
 * FeatureImpl.
 * 
 * @author Justin
 */
public class SimpleFeatureImpl extends FeatureImpl implements SimpleFeature {

    public SimpleFeatureImpl(List properties, AttributeDescriptor desc, String id) {
        super(properties, desc, id);
    }

    public SimpleFeatureImpl(List properties, SimpleFeatureType type, String id) {
        super(properties, type, id);
    }

    public SimpleFeatureType getType() {
        return (SimpleFeatureType) super.getType();
    }
    
    public SimpleFeatureType getFeatureType() {
        return getType();
    }
    
    public List<Attribute> getValue() {
        return (List<Attribute>) super.getValue();
    }
    
    public Object getAttribute(Name name) {
        return getAttribute(name.getLocalPart());
    }
    
    public void setAttribute(Name name, Object value) {
        setAttribute( name.getLocalPart(), value );
    }
    
    public Object getAttribute(String name) {
        for (Iterator<Attribute> itr = getValue().iterator(); itr.hasNext();) {
            Attribute att = (Attribute) itr.next();
            if ( att.getName().getLocalPart().equals( name )) {
                return att.getValue();
            }
        }
        return null;
    }
    
    public void setAttribute(String name, Object value) {
        Attribute attribute = (Attribute) getProperty(name);
        if ( attribute == null ) {
            throw new IllegalAttributeException("No such attribute: " + name);
        }
        
        attribute.setValue( value );
    }
    
    public Object getAttribute(int index) throws IndexOutOfBoundsException {
        return getValue().get(index).getValue();
    }
    
    public void setAttribute(int index, Object value)
            throws IndexOutOfBoundsException {
        Attribute attribute = getValue().get( index );
        attribute.setValue( value );
    }
    
    public List<Object> getAttributes() {
        List attributes = new ArrayList();
        for ( Iterator<Attribute> a = getValue().iterator(); a.hasNext(); ) {
            Attribute attribute = a.next();
            attributes.add( attribute.getValue() );
        }
        
        return attributes;
    }
    
    public void setAttributes(List<Object> values) {
        if ( values.size() != getValue().size() ) {
            String msg = "Expected " + getValue().size() + " attributes but " 
                + values.size() + " were specified";
            throw new IllegalArgumentException( msg );
        }
        
        for ( int i = 0; i < values.size(); i++ ) {
            getValue().get(i).setValue( values.get(i) );
        }
    }
    
    public void setAttributes(Object[] values) {
        setAttributes( Arrays.asList(values) );
    }
    
    public int getAttributeCount() {
        return getValue().size();
    }
    
    public int getNumberOfAttributes() {
        return getAttributeCount();
    }
    
    public Object getDefaultGeometry() {
        //first try the default geometry as described by the type
        if ( getDefaultGeometryProperty() != null ) {
            Object defaultGeometry = getDefaultGeometryProperty().getValue();
            if ( defaultGeometry != null ) {
                return defaultGeometry;
            }
            
            //default was null, look for another geometry property that does 
            // not have a null value
            for ( Property p : getProperties() ) {
                if ( p instanceof GeometryAttribute ) {
                    GeometryAttribute ga = (GeometryAttribute) p;
                    if ( ga.getValue() != null ) {
                        return ga.getValue();
                    }
                }
            }
        }
        return null;
    }
    
    public void setDefaultGeometry(Object geometry) {
        if ( getDefaultGeometryProperty() != null ) {
            getDefaultGeometryProperty().setValue(geometry);
        }
        else {
            throw new IllegalAttributeException("Feature has no defaultGeometry property");    
        }
        
    }
    
}
