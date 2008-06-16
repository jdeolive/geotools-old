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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.geotools.feature.GeometryAttributeType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.TypeName;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

/**
 * Adapts a geotools AttributeType to an ISO simple AttributeType
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
public class ISOAttributeTypeAdapter implements AttributeType {

    protected TypeName name;

    protected org.geotools.feature.AttributeType adaptee;

    /**
     * Protected constructor, use
     * {@link #adapter(String, org.geotools.feature.AttributeType)} to create an
     * appropriate instance for your geotools attribute type.
     * 
     * @param nsUri
     * @param gtType
     */
    protected ISOAttributeTypeAdapter(String nsUri,
            org.geotools.feature.AttributeType gtType) {
        this.name = new org.geotools.feature.type.TypeName(nsUri, gtType
                .getLocalName());
        this.adaptee = gtType;
    }

    public Class getBinding() {
        return adaptee.getBinding();
    }

    public Collection getOperations() {
        return Collections.EMPTY_SET;
    }

    public Set getRestrictions() {
        /*
        Filter restriction = adaptee.getRestriction();
        Set restrictions = restriction == null ? Collections.EMPTY_SET
                : Collections.singleton(restriction);
        return restrictions;
        */
        //TODO: REVISIT: the types reused from GMLSchema, which are created
        //with the old DefaultAttributeTypeFactory contains _always_ a length
        //restriction with Integer.MAX_VALUE. This causes a lot of problems
        //when evaluating the restriction, for example, over a GeometryType.
        //This happens when AttributeBuilder.build() creates the attribu
        return Collections.EMPTY_SET;
    }

    public AttributeType getSuper() {
        return null;
    }

    public boolean isAbstract() {
        return false;
    }

    public boolean isIdentified() {
        return false;
    }

    public InternationalString getDescription() {
        return null;
    }

    public TypeName getName() {
        return name;
    }

    public Object getUserData(Object key) {
        return null;
    }

    public void putUserData(Object key, Object data) {
        // do nothing
    }

    private static class ISOGeometryTypeAdapter extends ISOAttributeTypeAdapter
            implements GeometryType {

        public ISOGeometryTypeAdapter(String nsUri,
                GeometryAttributeType adaptee) {
            super(nsUri, adaptee);
        }

        public CoordinateReferenceSystem getCRS() {
            return ((GeometryAttributeType) adaptee).getCoordinateSystem();
        }

    }

    public static AttributeType adapter(String nsUri,
            org.geotools.feature.AttributeType gtType) {
        AttributeType attType;
        if (gtType instanceof GeometryAttributeType) {
            attType = new ISOGeometryTypeAdapter(nsUri,
                    (GeometryAttributeType) gtType);
        } else {
            attType = new ISOAttributeTypeAdapter(nsUri, gtType);
        }
        return attType;
    }

}
