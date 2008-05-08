package org.geotools.data.gml;

import java.io.IOException;

import org.geotools.data.store.AbstractFeatureSource2;
import org.geotools.feature.FeatureCollection;

public class GMLFeatureSource extends AbstractFeatureSource2 {

	public GMLFeatureSource(GMLTypeEntry entry) {
		super(entry);
	}

	public FeatureCollection getFeatures() throws IOException {
		return new GMLFeatureCollection( (GMLTypeEntry) entry );
	}
}