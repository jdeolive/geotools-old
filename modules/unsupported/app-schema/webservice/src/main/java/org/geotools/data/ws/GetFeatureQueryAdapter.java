/*
 *    GeoTools - The Open Source Java GIS Toolkit
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
package org.geotools.data.ws;

import org.geotools.data.Query;
import org.geotools.data.ws.protocol.ws.GetFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

public class GetFeatureQueryAdapter implements GetFeature {

    private Query query;

    private String outputFormat;

    public GetFeatureQueryAdapter(Query query, String outputFormat) {
        this.query = query;
        this.outputFormat = outputFormat;        
    }

    public Filter getFilter() {
        return query.getFilter();
    }

    public Integer getMaxFeatures() {
        return Integer.MAX_VALUE == query.getMaxFeatures() ? null : Integer.valueOf(query
                .getMaxFeatures());
    }

    public String getOutputFormat() {
        return this.outputFormat;
    }

    public String[] getPropertyNames() {
        return query.getPropertyNames();
    }

    public String getTypeName() {
        return query.getTypeName();
    }

    public SortBy[] getSortBy() {
        return query.getSortBy();
    }

}
