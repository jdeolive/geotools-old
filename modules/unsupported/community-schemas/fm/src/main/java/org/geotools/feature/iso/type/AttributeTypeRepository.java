/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.feature.iso.type;

import java.util.Map;
import java.util.NoSuchElementException;

import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.TypeName;

/**
 * What is this for? It looks like a primitive Schema?
 * @deprecated Is this needed?
 * @author Justin
 */
interface AttributeTypeRepository {
	AttributeType getType(TypeName typeName)throws NoSuchElementException;
	Map/*<Name, AttributeType>*/ getTypes();
	void registerType(AttributeType type);
}
