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
package org.geotools.swing.utils;

import java.util.Collection;
import java.util.Map;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.map.MapLayer;
import org.geotools.util.KVP;
import org.opengis.feature.type.PropertyDescriptor;

/**
 * Helper methods for swing module components that work with {@code MapLayer} objects.
 *
 * @todo Some (all ?) of this may be temporary
 *
 * @author Michael Bedward
 * @since 2.7
 * @source $URL$
 * @version $Id$
 */
public class MapLayerUtils {

    /**
     * Key for the boolean value in the {@code Map} returned by {@linkplain #isGridLayer}
     * that indicates whether the layer contained a grid (or grid reader)
     */
    public static final String IS_GRID_KEY = "is_grid";

    /**
     * Key for the boolean value in the {@code Map} returned by {@linkplain #isGridLayer}
     * that indicates whether the layer contained a grid reader
     */
    public static final String IS_GRID_READER_KEY = "is_grid_reader";
    
    /** 
     * Key for the String value in the {@code Map} returned by {@linkplain #isGridLayer}
     * that has the name of the grid attribute in the layer's feature collection; or an
     * empty string if there is no grid attribute
     */ 
    public static final String GRID_ATTR_KEY = "grid_attr";

    /**
     * Check if the given map layer contains a grid coverage or a grid coverage reader.
     *
     * @param layer the map layer
     * @return a {@code Map} containing:
     * <ul>
     * <li> {@linkplain #IS_GRID_KEY}: (Boolean) true if the layer has a grid coverage
     *      or grid coverage reader
     * <li> {@linkplain #IS_GRID_READER_KEY}: (Boolean) true if the layer has a grid
     *      coverage reader
     * <li> {@linkplain #GRID_ATTR_KEY}: (String) the name of the attribute in the layer's
     *      feature collection that contains the grid coverage or reader
     * </ul>
     */
    public static Map<String, Object> isGridLayer(MapLayer layer) {
        KVP info = new KVP(
                IS_GRID_KEY, Boolean.FALSE,
                IS_GRID_READER_KEY, Boolean.FALSE,
                GRID_ATTR_KEY, "");

        Collection<PropertyDescriptor> descriptors = layer.getFeatureSource().getSchema().getDescriptors();
        for (PropertyDescriptor desc : descriptors) {
            Class<?> clazz = desc.getType().getBinding();

            if (GridCoverage2D.class.isAssignableFrom(clazz)) {
                info.put(IS_GRID_KEY, Boolean.TRUE);
                info.put(GRID_ATTR_KEY, desc.getName().getLocalPart());
                break;

            } else if (AbstractGridCoverage2DReader.class.isAssignableFrom(clazz)) {
                info.put(IS_GRID_KEY, Boolean.TRUE);
                info.put(IS_GRID_READER_KEY, Boolean.TRUE);
                info.put(GRID_ATTR_KEY, desc.getName().getLocalPart());
                break;
            }
        }

        return info;
    }

}
