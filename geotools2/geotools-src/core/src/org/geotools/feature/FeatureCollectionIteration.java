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
package org.geotools.feature;

import java.util.Iterator;


/**
 * The FeatureCollectionIteration provides a depth first traversal of a
 * FeatureCollection which will call the provided call-back Handler. Because
 * of the complex nature of Features, which may have other Features (or even a
 * collection of Features) as attributes, the handler is repsonsible for
 * maintaining its own state as to where in the traversal it is recieving
 * events from. Many handlers will not need to worry about state.
 * 
 * <p>
 * <b>Implementation Notes:</b> The depth first visitation is implemented
 * through recursion. The limits to recursion depending on the settings in the
 * JVM, but some tests show a 2 argument recursive having a limit of ~50000
 * method calls with a stack size of 512k (the standard setting).
 * </p>
 *
 * @author Ian Schneider
 * @author Chris Holmes
 */
public class FeatureCollectionIteration {
    /**
     * A callback handler for the iteration of the contents of a
     * FeatureCollection.
     */
    protected final Handler handler;
    /** The collection being iterated */
    private final FeatureCollection collection;

    /** Create a new FeatureCollectionIteration with the given handler and
     * collection.
     *
     * @param handler DOCUMENT ME!
     * @param collection The collection to iterate over
     */
    public FeatureCollectionIteration(Handler handler,
        FeatureCollection collection) {
        if (handler == null) {
            throw new NullPointerException("handler");
        }

        if (collection == null) {
            throw new NullPointerException("collection");
        }

        this.handler = handler;
        this.collection = collection;
    }

    /**
     * A convienience method for obtaining a new iteration and calling iterate.
     *
     * @param handler DOCUMENT ME!
     * @param collection DOCUMENT ME!
     */
    public static void iteration(Handler handler, FeatureCollection collection) {
        FeatureCollectionIteration iteration = new FeatureCollectionIteration(handler,
                collection);
        iteration.iterate();
    }

    /**
     * Start the iteration.
     */
    public void iterate() {
        walker(collection);
    }

    protected void walker(FeatureCollection collection) {
        handler.handleFeatureCollection(collection);

        iterate(collection.iterator());

        handler.endFeatureCollection(collection);
    }

    protected void iterate(Iterator iterator) {
        while (iterator.hasNext()) {
            walker((Feature) iterator.next());
        }
    }

    protected void walker(Feature feature) {
        final FeatureType schema = feature.getFeatureType();
        final int cnt = schema.getAttributeCount();

        handler.handleFeature(feature);

        for (int i = 0; i < cnt; i++) {
            AttributeType type = schema.getAttributeType(i);

            // recurse if attribute type is another collection
            if (FeatureCollection.class.isAssignableFrom(type.getType())) {
                walker((FeatureCollection) feature.getAttribute(i));
            }
            // recurse if attribute type is another feature
            else if (type.isNested()) {
                walker((Feature) feature.getAttribute(i));
            }
            // normal handling
            else {
                handler.handleAttribute(type, feature.getAttribute(i));
            }
        }

        handler.endFeature(feature);
    }

    /**
     * A callback handler for the iteration of the contents of a
     * FeatureCollection.
     */
    public interface Handler {
        void handleFeatureCollection(FeatureCollection fc);

        void endFeatureCollection(FeatureCollection fc);

        void handleFeature(Feature f);

        void endFeature(Feature f);

        void handleAttribute(AttributeType type, Object value);
    }
}
