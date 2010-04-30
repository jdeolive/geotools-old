package org.geotools.data.simple;

import java.io.IOException;

import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

public interface SimpleFeatureStore extends FeatureStore<SimpleFeatureType,SimpleFeature>, SimpleFeatureSource {
    
    public SimpleFeatureCollection getFeatures() throws IOException;

    public SimpleFeatureCollection getFeatures(Filter filter)
            throws IOException;

    public SimpleFeatureCollection getFeatures(Query query)
            throws IOException;
}
