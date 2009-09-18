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

package org.geotools.swing.styling;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Color;
import java.io.IOException;
import org.geotools.data.AbstractDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.FilterFactory2;

/**
 * A helper class for {@code JSimpleStyleDialog} to create basic Style
 * objects. It can also be used by any application that wishes to create
 * very basic display styles for features.
 *
 * @author Michael Bedward
 * @since 2.6
 * @source $URL$
 * @version $Id$
 */
public class SimpleStyleHelper {
    private static StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
    private static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

    /**
     * Create a Style object to render features of type {@code typeName}
     * which are read from a data store
     *
     * @param store the data store containing the features
     *
     * @param typeName the feature type to create the style for
     * 
     * @param color single color to use for all components of the Style
     *
     * @return a new Style instance
     * 
     * @throws java.io.IOException if the data store cannot be accessed
     */
    static Style createDefaultStyle(AbstractDataStore store, String typeName, Color color) throws IOException {
        SimpleFeatureType type = store.getSchema(typeName);
        GeometryDescriptor desc = type.getGeometryDescriptor();
        Class<?> clazz = desc.getType().getBinding();

        if (Polygon.class.isAssignableFrom(clazz) ||
                MultiPolygon.class.isAssignableFrom(clazz)) {
            return createPolygonStyle(color, color, 0.5f);

        } else if (LineString.class.isAssignableFrom(clazz) ||
                MultiLineString.class.isAssignableFrom(clazz)) {
            return createLineStyle(color, 1.0f);

        } else if (Point.class.isAssignableFrom(clazz) ||
                MultiPoint.class.isAssignableFrom(clazz)) {
            return createPointStyle(color, color, 0.5f, 3.0f);
        }

        throw new UnsupportedOperationException("No style method for " + clazz.getName());
    }

    /**
     * Create a polygon style with give colors and opacity
     * @return a new Style instance
     */
    public static Style createPolygonStyle(Color outlineColor, Color fillColor, float opacity) {
        Stroke stroke = sf.createStroke(ff.literal(outlineColor), ff.literal(1.0f));
        Fill fill = sf.createFill(ff.literal(fillColor), ff.literal(opacity));
        return wrapSymbolizer( sf.createPolygonSymbolizer(stroke, fill, null) );
    }

    /**
     * Create a line style with given color and line width
     * @return a new Style instance
     */
    public static Style createLineStyle(Color lineColor, float width) {
        Stroke stroke = sf.createStroke(ff.literal(lineColor), ff.literal(width));
        return wrapSymbolizer( sf.createLineSymbolizer(stroke, null) );
    }

    /**
     * Create a point style with circles of a given line and fill color, opacity and size
     * @return a new Style instance
     */
    public static Style createPointStyle(Color lineColor, Color fillColor, float opacity, float size) {
        Mark mark = sf.getCircleMark();
        mark.setSize(ff.literal(size));
        mark.setStroke(sf.createStroke(ff.literal(lineColor), ff.literal(1.0f)));
        if (size > 1.0f) {
            mark.setFill(sf.createFill(ff.literal(fillColor), ff.literal(opacity)));
        }

        Graphic graphic = sf.createDefaultGraphic();
        graphic.graphicalSymbols().clear();
        graphic.graphicalSymbols().add(mark);

        return wrapSymbolizer( sf.createPointSymbolizer(graphic, null) );
    }

    /**
     * Wrap a symbolizer into a Rule / FeatureTypeStyle / Style
     *
     * @param sym the Symbolizer object
     *
     * @return a new Style instance
     */
    public static Style wrapSymbolizer(Symbolizer sym) {
        Rule rule = sf.createRule();
        rule.symbolizers().add(sym);

        FeatureTypeStyle fts = sf.createFeatureTypeStyle(new Rule[] {rule});

        Style style = sf.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }
}
