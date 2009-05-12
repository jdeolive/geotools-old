/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2009, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.gml3.bindings;

import javax.xml.namespace.QName;

import org.geotools.gml3.GML;

/**
 * Binding for gml:CodeType, with support for simpleContent property and XML attributes stored in
 * UserData map.
 * 
 * @author Ben Caradoc-Davies, CSIRO Exploration and Mining
 */
public class CodeTypeBinding extends AbstractSimpleContentComplexBinding {

    /**
     * @see org.geotools.xml.Binding#getTarget()
     */
    public QName getTarget() {
        return GML.CodeType;
    }

    /**
     * @see org.geotools.xml.Binding#getType()
     */
    public Class getType() {
        return String.class;
    }

}
