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

import java.util.EventObject;


/**
 * A simple event object to represent all events triggered by FeatureCollection
 * instances (typically change events).
 *
 * @author Ray Gallagher
 * @version $Id: CollectionEvent.java,v 1.9 2003/12/04 23:17:23 aaime Exp $
 */
public class CollectionEvent extends EventObject {
    /*
     * Design Notes:
     *  - Must look at other classes for hints on how to implement nicely.
     *
     *
     */

    /** event type constant denoting the adding of a feature */
    public static final int FEATURES_ADDED = 0;

    /** event type constant denoting the removal of a feature */
    public static final int FEATURES_REMOVED = 1;

    /**
     * event type constant denoting that features in the collection has been
     * modified
     */
    public static final int FEATURES_CHANGED = 2;

    /** Indicates one of FEATURES_ADDED, FEATURES_REMOVED, FEATURES_CHANGED */
    private int type;
    
    /** Holds value of property features. */
    private Feature[] features;    
    

    /**
     * Constructs a new CollectionEvent.
     *
     * @param source the collection which triggered the event
     */
    public CollectionEvent(FeatureCollection source, Feature[] involvedFeatures, int type) {
        super(source);
        this.type = type;
        this.features = involvedFeatures;
    }

    /**
     * provides access to the featurecollection which fired the event
     *
     * @return The FeatureCollection which was the event's source.
     */
    public FeatureCollection getCollection() {
        return (FeatureCollection) source;
    }

    /**
     * Provides information on the type of change that has occured. Possible
     * types are: add, remove, change
     *
     * @return an int which must be one of FEATURES_ADDED, FEATURES_REMOVED,
     *         FEATURES_CHANGED
     */
    public int getEventType() {
        return type;
    }
    
    /** Getter for property features.
     * @return Value of property features.
     *
     */
    public Feature[] getFeatures() {
        return this.features;
    }
    
}
