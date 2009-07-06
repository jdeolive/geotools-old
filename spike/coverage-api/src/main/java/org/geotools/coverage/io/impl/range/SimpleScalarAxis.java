/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.coverage.io.impl.range;

import java.util.Collections;

import org.geotools.coverage.io.range.Axis;
import org.geotools.coverage.io.range.FieldType;
import org.opengis.feature.type.Name;
import org.opengis.util.InternationalString;

/**
 * Implementation of a simple {@link Axis} which can be used when modeling scalar {@link FieldType}s like
 * temperature or pressure which do need extra {@link Axis} instances to describe their codomain.
 * 
 * @author Simone Giannecchini, GeoSolutions
 *
 */
public class SimpleScalarAxis extends DimensionlessAxis {
	public static final String DEFAUL_BAND_NAME="0";

	/**
	 * 
	 * @param bandName
	 * @param name
	 * @param description
	 */
	public SimpleScalarAxis(final String bandName,final Name name, final InternationalString description){
		super(Collections.singletonList(bandName), name, description);
	}
	/**
	 * 
	 * @param name
	 * @param description
	 */
	public SimpleScalarAxis(final Name name, final InternationalString description){
		super(Collections.singletonList(DEFAUL_BAND_NAME), name, description);
	}
	
}
