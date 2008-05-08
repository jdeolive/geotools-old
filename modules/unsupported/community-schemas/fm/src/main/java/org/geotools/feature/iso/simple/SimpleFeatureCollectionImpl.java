package org.geotools.feature.iso.simple;

import java.util.Collections;

import org.geotools.feature.iso.FeatureCollectionImpl;
import org.opengis.feature.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;

public class SimpleFeatureCollectionImpl extends FeatureCollectionImpl
	implements SimpleFeatureCollection {

	public SimpleFeatureCollectionImpl(AttributeDescriptor descriptor, String id) {
		super(Collections.EMPTY_LIST, descriptor, id);
	}
	
	public SimpleFeatureCollectionImpl(SimpleFeatureCollectionType type, String id) {
		super(Collections.EMPTY_LIST, type, id);
	}

	public FeatureType memberType() {
		return (FeatureType) memberTypes().iterator().next();
	}
	
}
