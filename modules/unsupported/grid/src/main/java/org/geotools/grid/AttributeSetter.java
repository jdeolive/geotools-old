/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2010, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.grid;

import java.util.Map;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Sets attribute values for grid elements when constructing a vector grid.
 * <pre><code>
 * AttributeSetter setter = new AttributeSetter(myFeatureType) {
 *     private int id = 1;
 *
 *     public setAttributes(GridElement el, Map<String, Object> attributes) {
 *         // assumes "id" and "value" are valid property names for
 *         // the feature type
 *         attributes.put("id", id++);
 *         attributes.put("value", myValueGettingFunction(el.toPolygon()));
 *     }
 * };
 * </code></pre>
 *
 * @author mbedward
 * @since 2.7
 * @source $URL$
 * @version $Id$
 */
public abstract class AttributeSetter {
    protected final SimpleFeatureType type;

    /**
     * Creates an {@code AttributeSetter} to work with the given feature type.
     *
     * @param type the feature type
     */
    public AttributeSetter(SimpleFeatureType type) {
        this.type = type;
    }

    /**
     * Gets the feature type used by this {@code AttributeSetter}.
     *
     * @return the feature type
     */
    public SimpleFeatureType getType() {
        return type;
    }

    /**
     * Sets the values of attributes for a new {@code SimpleFeature} being
     * constructed from the given {@code GridElement}.
     * <p>
     * This method must be overridden by the user. It is called by the grid
     * building classes as each new feature is constructed.
     *
     * @param el the element from which the new feature is being constructed
     *
     * @param attributes a {@code Map} with attribute names as keys and
     *        attribute values as values
     */
    public abstract void setAttributes(GridElement el, Map<String, Object> attributes);

    /**
     * Sets the {@code FeatureID} as a {@code String} for a new {@code SimpleFeature}
     * being constructed from the given {@code GridElement}.
     * <p>
     * It is optional to override this method. The base implementation returns
     * {@code null}.
     *
     * @param el the element from which the new feature is being constructed
     *
     * @return a feature ID value
     */
    public String getFeatureID(GridElement el) {
        return null;
    }

}
