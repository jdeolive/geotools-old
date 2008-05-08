package org.geotools.feature;

import java.util.List;

import org.opengis.feature.simple.SimpleFeature;

import org.opengis.feature.simple.SimpleFeatureType;

/**
 * An extension of {@link SimpleFeatureFactoryImpl} which creates 
 * {@link DefaultFeature} instances.
 *  
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 * 
 * @deprecated use {@link SimpleFeatureFactory},  this class is only provided to 
 * maintain backwards compatability for transition to geoapi feature model and 
 * will be removed in subsequent versions.
 * 
 * @since 2.5
 *
 */
public class DefaultFeatureFactory extends FeatureFactoryImpl {

	public SimpleFeature createSimpleFeature(List properties, SimpleFeatureType type, String id) {
		return new DefaultFeature( properties, (DefaultFeatureType) type, id );
	}

}
