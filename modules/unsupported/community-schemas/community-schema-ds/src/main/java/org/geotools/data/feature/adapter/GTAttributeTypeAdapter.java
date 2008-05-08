/*
 *    Geotools2 - OpenSource mapping toolkit
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
 *
 */
package org.geotools.data.feature.adapter;

import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyType;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Adapts an ISO simple AttributeType to a GeoTools AttributeType
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
public class GTAttributeTypeAdapter implements org.geotools.feature.AttributeType {

    protected TypeName name;

    protected AttributeDescriptor adaptee;

    /**
     * Protected constructor, use
     * {@link #adapter(String, org.geotools.feature.AttributeType)} to create an
     * appropriate instance for your geotools attribute type.
     * 
     * @param nsUri
     * @param gtType
     */
    protected GTAttributeTypeAdapter(AttributeDescriptor descriptor) {
        this.adaptee = descriptor;
    }

    private static class GTGeometryTypeAdapter extends GTAttributeTypeAdapter implements
            GeometryAttributeType {

        GeometryType type;

        public GTGeometryTypeAdapter(AttributeDescriptor descriptor) {
            super(descriptor);
            type = (GeometryType) descriptor.getType();
        }

        public CoordinateReferenceSystem getCoordinateSystem() {
            return type.getCRS();
        }

        public GeometryFactory getGeometryFactory() {
            return null;
        }

        public boolean isGeometry() {
            return true;
        }

    }

    public static org.geotools.feature.AttributeType adapter(AttributeDescriptor adaptee) {
        org.geotools.feature.AttributeType attType;
        if (adaptee.getType() instanceof GeometryType) {
            attType = new GTGeometryTypeAdapter(adaptee);
        } else {
            attType = new GTAttributeTypeAdapter(adaptee);
        }
        return attType;
    }

    public Object createDefaultValue() {
        return null;
    }

    public Object duplicate(Object src) throws IllegalAttributeException {
        throw new UnsupportedOperationException("duplicate");
    }

    public int getMaxOccurs() {
        return adaptee.getMaxOccurs();
    }

    public int getMinOccurs() {
        return adaptee.getMinOccurs();
    }

    public String getLocalName() {
    	return adaptee.getName().getLocalPart();
    }
    
    public Filter getRestriction() {
        return null;
    }

    public Class getBinding() {
        return adaptee.getType().getBinding();
    }

    public boolean isNillable() {
        return adaptee.isNillable();
    }

    public Object parse(Object value) throws IllegalArgumentException {
        return value;
    }

    public void validate(Object obj) throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

	public AttributeType getType() {
		return adaptee.getType();
	}

	public Name getName() {
		return adaptee.getName();
	}

	public Object getUserData(Object key) {
		return adaptee.getUserData(key);
	}

	public void putUserData(Object key, Object data) {
		adaptee.putUserData(key, data);
	}

	public PropertyType type() {
		return adaptee.type();
	}

}
