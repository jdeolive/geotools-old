package org.geotools.data.ogr;

import org.geotools.data.AbstractFeatureSource;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureListener;
import org.geotools.feature.FeatureType;

public class OGRFeatureSource extends AbstractFeatureSource {
	
	private OGRDataStore store;
	private FeatureType schema;

	public OGRFeatureSource(OGRDataStore store, FeatureType schema) {
		this.store = store;
		this.schema = schema;
	}

	public void addFeatureListener(FeatureListener listener) {
		store.listenerManager.addFeatureListener(this, listener);
		
	}

	public DataStore getDataStore() {
		return store;
	}

	public FeatureType getSchema() {
		return schema;
	}

	public void removeFeatureListener(FeatureListener listener) {
		store.listenerManager.removeFeatureListener(this, listener);
	}
	
	

}
