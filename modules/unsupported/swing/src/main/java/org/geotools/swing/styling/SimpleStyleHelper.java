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
import org.geotools.styling.AnchorPoint;
import org.geotools.styling.Displacement;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Font;
import org.geotools.styling.Graphic;
import org.geotools.styling.LabelPlacement;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
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
            return createPointStyle("Circle", color, color, 0.5f, 3.0f);
        }

        throw new UnsupportedOperationException("No style method for " + clazz.getName());
    }

    /**
     * Create a polygon style with the given colors and opacity.
     *
     * @param outlineColor color of polygon outlines
     * @param fillColor color for the fill
     * @param opacity proportional opacity (0 to 1)
     *
     * @return a new Style instance
     */
    public static Style createPolygonStyle(Color outlineColor, Color fillColor, float opacity) {
        Stroke stroke = sf.createStroke(ff.literal(outlineColor), ff.literal(1.0f));
        Fill fill = sf.createFill(ff.literal(fillColor), ff.literal(opacity));
        return wrapSymbolizers( sf.createPolygonSymbolizer(stroke, fill, null) );
    }

    /**
     * Create a polygon style with the given colors, opacity and optional labels.
     *
     * @param outlineColor color of polygon outlines
     * @param fillColor color for the fill
     * @param opacity proportional opacity (0 to 1)
     *
     * @param labelField name of the feature field (attribute) to use for labelling;
     *        mauy be {@code null} for no labels
     *
     * @param labelFont GeoTools Font object to use for labelling; if {@code null}
     *        and {@code labelField} is not {@code null} the default font will be
     *        used
     *
     * @return a new Style instance
     */
    public static Style createPolygonStyle(Color outlineColor, Color fillColor, float opacity,
            String labelField, Font labelFont) {
        Stroke stroke = sf.createStroke(ff.literal(outlineColor), ff.literal(1.0f));
        Fill fill = sf.createFill(ff.literal(fillColor), ff.literal(opacity));
        PolygonSymbolizer polySym = sf.createPolygonSymbolizer(stroke, fill, null);

        if (labelField == null) {
            return wrapSymbolizers( polySym );

        } else {
            Font font = (labelFont == null ? sf.getDefaultFont() : labelFont);
            Fill labelFill = sf.createFill(ff.literal(Color.BLACK));
            
            TextSymbolizer textSym = sf.createTextSymbolizer(
                    labelFill, new Font[]{font}, null, ff.property(labelField), null, null);

            return wrapSymbolizers( polySym, textSym );
        }
    }

    /**
     * Create a line style with given color and line width
     *
     * @param lineColor color of lines
     * @param width width of lines
     *
     * @return a new Style instance
     */
    public static Style createLineStyle(Color lineColor, float width) {
        Stroke stroke = sf.createStroke(ff.literal(lineColor), ff.literal(width));
        return wrapSymbolizers( sf.createLineSymbolizer(stroke, null) );
    }

    /**
     * Create a line style with given color, line width and optional labels
     *
     * @param lineColor color of lines
     * @param width width of lines
     *
     * @param labelField name of the feature field (attribute) to use for labelling;
     *        mauy be {@code null} for no labels
     *
     * @param labelFont GeoTools Font object to use for labelling; if {@code null}
     *        and {@code labelField} is not {@code null} the default font will be
     *        used
     *
     * @return a new Style instance
     */
    public static Style createLineStyle(Color lineColor, float width,
            String labelField, Font labelFont) {
        Stroke stroke = sf.createStroke(ff.literal(lineColor), ff.literal(width));
        LineSymbolizer lineSym = sf.createLineSymbolizer(stroke, null);

        if (labelField == null) {
            return wrapSymbolizers( lineSym );

        } else {
            Font font = (labelFont == null ? sf.getDefaultFont() : labelFont);
            Fill labelFill = sf.createFill(ff.literal(Color.BLACK));

            TextSymbolizer textSym = sf.createTextSymbolizer(
                    labelFill, new Font[]{font}, null, ff.property(labelField), null, null);

            return wrapSymbolizers( lineSym, textSym );
        }
    }

    /**
     * Create a point style without labels
     *
     * @param wellKnownName one of: Circle, Square, Cross, X, Triangle or Star
     * @param lineColor color for the point symbol outline
     * @param fillColor color for the point symbol fill
     * @param opacity a value between 0 and 1 for the opacity of the fill
     * @param size size of the point symbol
     *
     * @return a new Style instance
     */
    public static Style createPointStyle(
            String wellKnownName,
            Color lineColor,
            Color fillColor,
            float opacity,
            float size) {

        return createPointStyle(wellKnownName, lineColor, fillColor, opacity, size, null, null);
    }

    /**
     * Create a point style, optionally with text labels
     *
     * @param wellKnownName one of: Circle, Square, Cross, X, Triangle or Star
     * @param lineColor color for the point symbol outline
     * @param fillColor color for the point symbol fill
     * @param opacity a value between 0 and 1 for the opacity of the fill
     * @param size size of the point symbol
     *
     * @param labelField name of the feature field (attribute) to use for labelling;
     *        mauy be {@code null} for no labels
     *
     * @param labelFont GeoTools Font object to use for labelling; if {@code null}
     *        and {@code labelField} is not {@code null} the default font will be
     *        used
     *
     * @return a new Style instance
     */
    public static Style createPointStyle(
            String wellKnownName,
            Color lineColor,
            Color fillColor,
            float opacity,
            float size,
            String labelField,
            Font labelFont) {

        Stroke stroke = sf.createStroke(ff.literal(lineColor), ff.literal(1.0f));
        Fill fill = Fill.NULL;
        if (size > 1.0) {
            fill = sf.createFill(ff.literal(fillColor), ff.literal(opacity));
        }

        Mark mark = sf.createMark(ff.literal(wellKnownName), stroke, fill,
                ff.literal(size), ff.literal(0));

        Graphic graphic = sf.createDefaultGraphic();
        graphic.graphicalSymbols().clear();
        graphic.graphicalSymbols().add(mark);

        PointSymbolizer pointSym = sf.createPointSymbolizer(graphic, null);

        if (labelField == null) {
            return wrapSymbolizers( pointSym );

        } else {
            Font font = (labelFont == null ? sf.getDefaultFont() : labelFont);
            Fill labelFill = sf.createFill(ff.literal(Color.BLACK));
            AnchorPoint anchor = sf.createAnchorPoint(ff.literal(0.5), ff.literal(0.0));
            Displacement disp = sf.createDisplacement(ff.literal(0), ff.literal(5));
            LabelPlacement placement = sf.createPointPlacement(anchor, disp, ff.literal(0));

            TextSymbolizer textSym = sf.createTextSymbolizer(
                    labelFill, new Font[]{font}, null, ff.property(labelField), placement, null);

            return wrapSymbolizers( pointSym, textSym );
        }

    }

    /**
     * Wrap a symbolizer into a Rule / FeatureTypeStyle / Style
     *
     * @param sym the Symbolizer object
     *
     * @return a new Style instance
     */
    public static Style wrapSymbolizers(Symbolizer ...symbolizers) {
        Rule rule = sf.createRule();

        for (Symbolizer sym : symbolizers) {
            rule.symbolizers().add(sym);
        }

        FeatureTypeStyle fts = sf.createFeatureTypeStyle(new Rule[] {rule});

        Style style = sf.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }
}
