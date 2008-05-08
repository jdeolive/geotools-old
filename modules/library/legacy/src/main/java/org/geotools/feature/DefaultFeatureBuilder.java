package org.geotools.feature;

import org.geotools.feature.simple.SimpleFeatureBuilder;

public class DefaultFeatureBuilder extends SimpleFeatureBuilder {
	
	public DefaultFeatureBuilder(Feature feature) {
		super(feature.getType());
		init(feature);
	}
	
	public DefaultFeatureBuilder(FeatureType featureType) {
		super(featureType);
	}
}
