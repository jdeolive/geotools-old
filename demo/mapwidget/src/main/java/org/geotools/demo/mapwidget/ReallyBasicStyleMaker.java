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

package org.geotools.demo.mapwidget;

import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import java.awt.Color;
import java.io.IOException;
import org.geotools.data.AbstractDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;

/**
 * This class illustrates one approach to generating very minimal styles
 * for point, line and polygon data.
 *
 * @author Michael Bedward
 */
public class ReallyBasicStyleMaker {

    /**
     * Create a really basic Style object to render features of type {@code typeName}
     * which are read from a data store
     *
     * @param store the data store containing the features
     * @param typeName the feature type to create the style for
     * @return a new Style instance
     * @throws java.io.IOException if the data store cannot be access
     */
    static Style createBasicStyle(AbstractDataStore store, String typeName) throws IOException {
        SimpleFeatureType type = store.getSchema(typeName);
        GeometryDescriptor desc = type.getGeometryDescriptor();
        Class<?> binding = desc.getType().getBinding();

        if (binding.isAssignableFrom(Point.class)) {
            return createBasicPointStyle(typeName);
        } else if (binding.isAssignableFrom(MultiLineString.class)) {
            return createBasicLineStyle(typeName);
        } else if (binding.isAssignableFrom(MultiPolygon.class)) {
            return createBasicPolygonStyle(typeName);
        } else {
            throw new RuntimeException(java.util.ResourceBundle.getBundle("org/geotools/gui/swing/MapWidget").getString("unrecognized_geometry_error") + binding.getSimpleName());
        }
    }

    /**
     * Create a really basic Style for line features
     */
    private static Style createBasicLineStyle(String typeName) {
        StyleFactory factory = CommonFactoryFinder.getStyleFactory(null);
        StyleBuilder sb = new StyleBuilder(factory);
        LineSymbolizer sym = sb.createLineSymbolizer(Color.BLACK, 1.0);
        Style style = sb.createStyle(typeName, sym);
        return style;
    }

    /**
     * Create a really basic Style for point features
     */
    private static Style createBasicPointStyle(String typeName) {
        StyleFactory factory = CommonFactoryFinder.getStyleFactory(null);
        StyleBuilder sb = new StyleBuilder(factory);
        PointSymbolizer sym = sb.createPointSymbolizer();
        Style style = sb.createStyle(typeName, sym);

        return style;
    }

    /**
     * Create a really basic Style for polygon features
     */
    private static Style createBasicPolygonStyle(String typeName) {
            StyleFactory factory = CommonFactoryFinder.getStyleFactory(null);
            StyleBuilder sb = new StyleBuilder(factory);
            PolygonSymbolizer sym = sb.createPolygonSymbolizer(Color.BLACK, 1.0);
            Style style = sb.createStyle(typeName, sym);
            return style;
    }

}
