package org.geotools.repository.postgis;

import org.geotools.repository.DataStoreService;
import org.geotools.repository.FeatureSourceGeoResource;

public class PostGISGeoResource extends FeatureSourceGeoResource {

	public PostGISGeoResource( DataStoreService parent, String name ) {
		super(parent, name);
	}

}
