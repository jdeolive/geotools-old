/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.postgis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureDiff;
import org.opengis.feature.simple.SimpleFeature;

/**
 * Represents the difference between two states of the same feature.
 * 
 * @author aaime
 * @since 2.4
 * 
 * @source $URL:
 *         http://svn.osgeo.org/geotools/trunk/modules/unsupported/postgis-versioned/src/main/java
 *         /org/geotools/data/postgis/FeatureDiff.java $
 */
public class FeatureDiffImpl implements FeatureDiff {

    /**
     * Feature does not exists in fromVersion, has been created in the meantime (change map contains
     * all attributes in this case)
     */
    public static final int INSERTED = 0;

    /**
     * Feature exists in both versions, but has been modified
     */
    public static final int UPDATED = 1;

    /**
     * Feature existed in fromVersion, but has been deleted (change map is empty)
     */
    public static final int DELETED = 2;

    String ID;

    int state;

    List changedAttributes;

    SimpleFeature feature;

    SimpleFeature oldFeature;

    /**
     * Creates a new feature difference for a modified feature
     * 
     * @param ID
     * @param oldFeature
     * @param newFeature
     * @param changes
     */
    public FeatureDiffImpl(SimpleFeature oldFeature, SimpleFeature newFeature) {
        super();
        if (oldFeature == null && newFeature == null)
            throw new IllegalArgumentException("Both features are null, that's not a diff!");

        this.ID = oldFeature != null ? oldFeature.getID() : newFeature.getID();
        this.feature = newFeature;
        this.oldFeature = oldFeature;
        this.changedAttributes = Collections.EMPTY_LIST;
        if (oldFeature == null) {
            this.state = INSERTED;
        } else if (newFeature == null) {
            this.state = DELETED;
        } else {
            this.state = UPDATED;
            List changedAttributes = new ArrayList();
            for (int i = 0; i < oldFeature.getAttributeCount(); i++) {
                String attName = oldFeature.getFeatureType().getDescriptor(i).getLocalName();
                Object toAttribute = newFeature.getAttribute(attName);
                Object fromAttribute = oldFeature.getAttribute(attName);
                if (!DataUtilities.attributesEqual(fromAttribute, toAttribute)) {
                    changedAttributes.add(attName);
                }
            }
            this.changedAttributes = Collections.unmodifiableList(changedAttributes);
        }
    }

    public List getChangedAttributes() {
        return changedAttributes;
    }

    public String getID() {
        return ID;
    }

    public int getState() {
        return state;
    }

    public SimpleFeature getFeature() {
        return feature;
    }

    public SimpleFeature getOldFeature() {
        return oldFeature;
    }

}