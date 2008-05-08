package org.geotools.data.postgis;

import java.io.IOException;
import java.util.Set;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.ResourceInfo;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

/**
 * Really delegates everything to a wrapped feature source, but allows to
 * advertise a data store other than the original one
 * 
 * @author aaime
 * @since 2.4
 * 
 */
public class WrappingPostgisFeatureSource implements FeatureSource<SimpleFeatureType, SimpleFeature> {
    FeatureSource<SimpleFeatureType, SimpleFeature> wrapped;

    VersionedPostgisDataStore store;

    public WrappingPostgisFeatureSource(FeatureSource<SimpleFeatureType, SimpleFeature> wrapped,
            VersionedPostgisDataStore store) {
        this.wrapped = wrapped;
        this.store = store;
    }
    
    public ResourceInfo getInfo() {
        return wrapped.getInfo();
    }

    /**
     * @since 2.5
     * @see FeatureSource#getName()
     */
    public Name getName() {
        return wrapped.getName();
    }
    
    public DataStore getDataStore() {
        return store;
    }

    public void addFeatureListener(FeatureListener listener) {
        wrapped.addFeatureListener(listener);
    }

    public void removeFeatureListener(FeatureListener listener) {
        wrapped.removeFeatureListener(listener);
    }

    public ReferencedEnvelope getBounds() throws IOException {
        return wrapped.getBounds();
    }

    public ReferencedEnvelope getBounds(Query query) throws IOException {
        return wrapped.getBounds(query);
    }

    public int getCount(Query query) throws IOException {
        return wrapped.getCount(query);
    }

    public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures() throws IOException {
        return wrapped.getFeatures();
    }

    public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures(Filter filter) throws IOException {
        return wrapped.getFeatures(filter);
    }

    public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures(Query query) throws IOException {
        return wrapped.getFeatures(query);
    }

    public SimpleFeatureType getSchema() {
        return wrapped.getSchema();
    }
    
    public Set getSupportedHints() {
        return wrapped.getSupportedHints();
    }

    public QueryCapabilities getQueryCapabilities() {
        return wrapped.getQueryCapabilities();
    }
}
