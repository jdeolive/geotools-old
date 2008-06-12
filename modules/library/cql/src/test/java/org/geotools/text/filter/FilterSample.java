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
package org.geotools.text.filter;

import org.opengis.filter.Filter;


/**
 *
 * Filter Sample Factory
 *
 * <p>
 * Provide samples of filters
 * </p>
 *
 * @author Mauricio Pazos - Axios Engineering
 * @author Gabriel Roldan - Axios Engineering
 * @deprecated was replaced by {@link  org.geotools.filter.text.cql2.FilterSample}
 */
public final class FilterSample extends org.geotools.filter.text.cql2.FilterSample{
    

    public static Filter getSample(final String sampleRequested) {
        return org.geotools.filter.text.cql2.FilterSample.getSample(sampleRequested);
    }
}
