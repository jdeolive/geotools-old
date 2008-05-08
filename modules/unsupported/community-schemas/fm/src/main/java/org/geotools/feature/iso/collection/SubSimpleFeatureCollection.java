package org.geotools.feature.iso.collection;

import org.opengis.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeatureCollection;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

/**
 * Used as a reasonable default implementation for a subCollection of
 * simple features.
 * <p>
 * Note: to implementors, this is not optimal, please do your own thing - your
 * users will thank you.
 * </p>
 * 
 * @author Jody Garnett, Refractions Research, Inc.
 * @author Justin Deoliveira, The Open Planning Project
 */
public class SubSimpleFeatureCollection extends SubFeatureCollection implements
		SimpleFeatureCollection {

	public SubSimpleFeatureCollection(SimpleFeatureCollection collection,
			Filter filter, FilterFactory factory) {
		super(collection, filter, factory);
	}

	public FeatureCollection subCollection(org.opengis.filter.Filter filter) {
		return new SubSimpleFeatureCollection(this, filter, factory);
	}

	public FeatureType memberType() {
		return ((SimpleFeatureCollection) collection).memberType();
	}
}
