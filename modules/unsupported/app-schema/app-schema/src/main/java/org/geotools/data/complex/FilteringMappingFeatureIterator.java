/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.identity.FeatureId;

/**
 * An extension to {@linkplain org.geotools.data.complex.DataAccessMappingFeatureIterator} where
 * filter is present. Since join query between 2 or more tables isn't supported, the only way we can
 * query nested features is by applying the filter per simple feature (database row). This is done
 * in hasNext().
 * 
 * @author Rini Angreani, CSIRO Earth Science and Resource Engineering
 */
public class FilteringMappingFeatureIterator extends DataAccessMappingFeatureIterator {

    private List<String> filteredFeatures;

    private Filter filter;

    public FilteringMappingFeatureIterator(AppSchemaDataAccess store, FeatureTypeMapping mapping,
            Query query, Filter filter) throws IOException {
        super(store, mapping, query);
        this.filter = filter;
        query.setFilter(Filter.INCLUDE);
        filteredFeatures = new ArrayList<String>();
    }

    @Override
    protected void closeSourceFeatures() {
        super.closeSourceFeatures();
        filteredFeatures.clear();
    }

    @Override
    public boolean hasNext() {
        // check that the feature exists
        while (super.hasNext()) {
            // check that this row has already been accounted for
            if (filteredFeatures.contains(extractIdForFeature(this.curSrcFeature))) {
                // get the next one as this row would've been already added to the target
                // feature from setNextFilteredFeature
                return hasNext();
            }
            // apply filter
            if (filter.evaluate(curSrcFeature)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void setNextFeature(String fId, ArrayList<Feature> features) throws IOException {
        FeatureCollection<FeatureType, Feature> matchingFeatures;
        FeatureId featureId = namespaceAwareFilterFactory.featureId(fId);
        // Since this is filtered, it could happen that the source is a denormalised view
        // where only the last row of the same id satisfies the filter. However, we'd want
        // to get all the rows of the matching id to group as a complex feature.. so we
        // have to run an extra query to get the rows of the same id.
        Query query = new Query();
        if (reprojection != null) {
            query.setCoordinateSystemReproject(reprojection);
        }
        if (featureFidMapping instanceof PropertyName
                && ((PropertyName) featureFidMapping).getPropertyName().equals("@id")) {
            // no real feature id mapping,
            // so trying to find it when the filter's evaluated will result in exception
            Set<FeatureId> ids = new HashSet<FeatureId>();
            ids.add(featureId);
            query.setFilter(namespaceAwareFilterFactory.id(ids));
            matchingFeatures = this.mappedSource.getFeatures(query);
        } else {
            // in case the expression is wrapped in a function, eg. strConcat
            // that's why we don't always filter by id, but do a PropertyIsEqualTo
            query.setFilter(namespaceAwareFilterFactory.equals(featureFidMapping,
                    namespaceAwareFilterFactory.literal(featureId)));
            matchingFeatures = this.mappedSource.getFeatures(query);
        }

        FeatureIterator<Feature> iterator = matchingFeatures.features();

        while (iterator.hasNext()) {
            features.add(iterator.next());
        }

        if (features.size() < 1) {
            LOGGER.warning("This shouldn't have happened."
                    + "There should be at least 1 features with id='" + fId + "'.");
        }
        filteredFeatures.add(fId);
        iterator.close();
        curSrcFeature = null;
    }
}
