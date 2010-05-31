/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2010, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.grid;

import java.util.Map;
import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * A basic implementation of {@code AttributeSetter} which will create a
 * {@code SimpleFeatureType} having two properties:
 * <ul>
 * <li>element - type Polygon
 * <li>id - type Integer
 * </ul>
 *
 * Grid elements will be assigned sequential id values starting with 1.
 *
 * @author mbedward
 * @since 2.7
 * @source $URL$
 * @version $Id$
 */
public class IdAttributeSetter extends AttributeSetter {
    
    public static final String DEFAULT_TYPE_NAME = "grid";
    
    private int id;

    protected static SimpleFeatureType createType(String typeName) {
        final String finalName;
        if (typeName != null && typeName.trim().length() > 0) {
            finalName = typeName;
        } else {
            finalName = DEFAULT_TYPE_NAME;
        }

        try {
            return DataUtilities.createType(finalName, "element:Polygon,id:Integer");
        } catch (SchemaException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public IdAttributeSetter() {
        this(DEFAULT_TYPE_NAME);
    }

    public IdAttributeSetter(String typeName) {
        super(createType(typeName));
        id = 0;
    }

    @Override
    public void setAttributes(GridElement el, Map<String, Object> attributes) {
        attributes.put("id", ++id);
    }

}
