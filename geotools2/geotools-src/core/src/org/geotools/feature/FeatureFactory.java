/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.feature;

/**
 * An interface for the construction of Features.  As Features always require a
 * FeatureType the best place to implement this is in the FeatureType itself,
 * thus the FeatureType interface extends this interface.  Other
 * FeatureFactories may be implemented, but they  should probably be
 * constructed with a FeatureType.
 *
 * @version $Id: FeatureFactory.java,v 1.3 2003/08/05 22:48:23 cholmesny Exp $
 *
 * @task REVISIT: consider a static create(Object[] attributes,  String
 *       FeatureID, FeatureType type) method.
 * @task REVISIT: move these methods directly to FeatureType?  This would not
 *       allow independent FeatureFactories, but I'm not sure if those are
 *       useful at all.
 */
public interface FeatureFactory {
    /**
     * Creates a new feature, with a generated unique featureID.  This is less
     * than ideal, as a FeatureID should be persistant over time, generally
     * created by a datasource.  This method is more for testing that doesn't
     * need featureID.
     *
     * @param attributes the array of attribute values
     *
     * @return The created feature
     *
     * @throws IllegalAttributeException if the FeatureType does not validate
     *         the attributes.
     */
    Feature create(Object[] attributes) throws IllegalAttributeException;

    /**
     * Creates a new feature, with the proper featureID.
     *
     * @param attributes the array of attribute values.
     * @param featureID the feature ID.
     *
     * @return the created feature.
     *
     * @throws IllegalAttributeException if the FeatureType does not validate
     *         the attributes.
     */
    Feature create(Object[] attributes, String featureID)
        throws IllegalAttributeException;
}
