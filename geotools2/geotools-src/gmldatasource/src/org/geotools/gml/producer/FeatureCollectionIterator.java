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
/*
 * FeatureCollectionIterator.java
 *
 * Created on March 26, 2003, 1:53 PM
 */
package org.geotools.gml.producer;

import org.geotools.feature.*;


/**
 * The FeatureCollectionIterator provides a depth first traversal of a
 * FeatureCollection which will call the provided call-back Handler. The call
 * backs will be call in this order: 1) handleFeature. 2) for each attribute
 * of the Feature, handleAttribute if Attribute, or handleFeature if Feature,
 * 3) endAttribute, 4) endFeature. Because Features may be nested, the Handler
 * may receive several handleFeatures in a row, followed later by several
 * endFeatures corresponding the first calls.
 * <pre>
 * NOTES for improvement:
 *  If AttributeType used the visitor pattern, the ugly if-else blocks could
 *    be removed.
 *  If Feature had a method getAttribute(FeatureType), things could operate a 
 *    bit quicker.
 * </pre>
 *
 * @author Ian Schneider
 * @author Chris Holmes
 */
public class FeatureCollectionIterator {
    public void walk(Handler handler, FeatureCollection collection) {
        if ((handler == null) || (collection == null)) {
            throw new NullPointerException();
        }

        walker(handler, collection);
    }

    protected void walker(Handler handler, FeatureCollection collection) {
        final Feature[] features = collection.getFeatures();
        handler.handleFeatureCollection(collection);

        for (int i = 0, ii = features.length; i < ii; i++) {
            walker(handler, features[i]);
        }

        handler.endFeatureCollection(collection);
    }

    protected void walker(Handler handler, Feature feature) {
        final FeatureType schema = feature.getSchema();
        final AttributeType[] attTypes = schema.getAttributeTypes();
        handler.handleFeature(feature);

        for (int i = 0, ii = attTypes.length; i < ii; i++) {
            String attName = attTypes[i].getName();

            // recurse if attribute type is another collection
            if (FeatureCollection.class.isAssignableFrom(attTypes[i].getType())) {
                try {
                    walker(handler,
                        (FeatureCollection) feature.getAttribute(attName));
                } catch (IllegalFeatureException iae) {
                    throw new RuntimeException("This Feature is broken - " +
                        feature);
                }
            }
            // recurse if attribute type is another feature
            else if (attTypes[i].isNested()) {
                try {
                    walker(handler, (Feature) feature.getAttribute(attName));
                } catch (IllegalFeatureException iae) {
                    throw new RuntimeException("This Feature is broken - " +
                        feature);
                }
            }
            // normal handling
            else {
                handler.handleAttribute(feature, attTypes[i]);
                handler.endAttribute(feature, attTypes[i]);
            }
        }

        handler.endFeature(feature);
    }

    public interface Handler {
        void handleFeatureCollection(FeatureCollection fc);

        void endFeatureCollection(FeatureCollection fc);

        void handleFeature(Feature f);

        void endFeature(Feature f);

        void handleAttribute(Feature f, AttributeType type);

        void endAttribute(Feature f, AttributeType type);
    }
}
