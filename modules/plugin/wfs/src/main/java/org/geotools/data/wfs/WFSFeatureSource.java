/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
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
