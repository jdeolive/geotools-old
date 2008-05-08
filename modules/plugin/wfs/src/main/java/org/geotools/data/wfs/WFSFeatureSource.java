package org.geotools.data.wfs;

import org.geotools.data.FeatureSource;
import org.geotools.data.ResourceInfo;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

/**
 * Superinterface for FeatureSources returned by a WFSDataStore.
 * <p>
 * This interface is meant to be short-lived while waiting for the addition of a {@code getInfo()}
 * method to the core FeatureSource<SimpleFeatureType, SimpleFeature> interface.
 * </p>
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @source $URL$
 */
public interface WFSFeatureSource<T extends FeatureType, F extends Feature> extends FeatureSource<T, F> {

    ResourceInfo getInfo();
}
