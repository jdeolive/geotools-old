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

package org.geotools.data.complex;

import java.io.IOException;

import org.geotools.data.Query;
import org.opengis.filter.Filter;

/**
 * @author Russell Petty, GSV
 * 
 * @source $URL:
 *         http://svn.osgeo.org/geotools/trunk/modules/unsupported/app-schema/app-schema/src/main
 *         /java/org/geotools/data/complex/MappingFeatureIteratorFactory.java $
 */
public class MappingFeatureIteratorFactory {

    public static IMappingFeatureIterator getInstance(AppSchemaDataAccess store,
            FeatureTypeMapping mapping, Query query) throws IOException {

        query = store.unrollQuery(query, mapping);

        if (mapping instanceof XmlFeatureTypeMapping) {
            return new XmlMappingFeatureIterator(store, mapping, query);
        }

        if (query.getFilter() != null) {
            Filter filter = query.getFilter();
            if (!filter.equals(Filter.INCLUDE) && !filter.equals(Filter.EXCLUDE)) {
                query.setFilter(Filter.INCLUDE);
                return new FilteringMappingFeatureIterator(store, mapping, query, filter);
            }
        }
        return new DataAccessMappingFeatureIterator(store, mapping, query);
    }
}
