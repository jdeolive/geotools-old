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
package org.geotools.styling;

import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.styling.*;
import java.awt.Color;


/**
 * An utility class designed to speed up style building by convinience methods.
 *
 * @author aaime
 */
public class StyleBuilder {
    protected StyleFactory sf;
    protected FilterFactory ff;

    public StyleBuilder() {
        sf = StyleFactory.createStyleFactory();
        ff = FilterFactory.createFilterFactory();
    }

    public StyleBuilder(StyleFactory styleFactory) {
        this.sf = styleFactory;
        ff = FilterFactory.createFilterFactory();
    }

    public StyleBuilder(FilterFactory filterFactory) {
        this.ff = filterFactory;
        this.sf = StyleFactory.createStyleFactory();
    }

    public StyleBuilder(StyleFactory styleFactory, FilterFactory filterFactory) {
        this.sf = styleFactory;
        this.ff = filterFactory;
    }

    public Stroke createStroke() {
        return sf.getDefaultStroke();
    }

    public Stroke createStroke(Color color, float width) {
        return sf.createStroke(colorExpression(color), literalExpression(width));
    }

    public Stroke createStroke(Expression color, Expression width) {
        return sf.createStroke(color, width);
    }

    public Stroke createStroke(Color color, float width, float opacity) {
        return sf.createStroke(colorExpression(color),
            literalExpression(width), literalExpression(opacity));
    }

    public Stroke createStroke(Expression color, Expression width,
        Expression opacity) {
        return sf.createStroke(color, width, opacity);
    }

    public Fill createFill() {
        return sf.getDefaultFill();
    }

    public Fill createFill(Color color) {
        return sf.createFill(colorExpression(color));
    }

    public Fill createFill(Expression color) {
        return sf.createFill(color);
    }

    public Fill createFill(Color color, float opacity) {
        return sf.createFill(colorExpression(color), literalExpression(opacity));
    }

    public Fill createFill(Expression color, Expression opacity) {
        return sf.createFill(color, opacity);
    }

    public Fill createFill(Color color, Color backgroundColor, float opacity,
        Graphic fill) {
        return sf.createFill(colorExpression(color),
            colorExpression(backgroundColor), literalExpression(opacity), fill);
    }

    public Fill createFill(Expression color, Expression backgroundColor,
        Expression opacity, Graphic fill) {
        return sf.createFill(color, backgroundColor, opacity, fill);
    }

    public PolygonSymbolizer createPolygonSymbolizer() {
        return sf.createPolygonSymbolizer();
    }

    public PolygonSymbolizer createPolygonSymbolizer(Color fillColor,
        Color borderColor, float borderWidth) {
        return createPolygonSymbolizer(createStroke(borderColor, borderWidth),
            createFill(fillColor));
    }

    public PolygonSymbolizer createPolygonSymbolizer(Stroke stroke, Fill fill) {
        PolygonSymbolizer ps = sf.createPolygonSymbolizer();
        ps.setStroke(stroke);
        ps.setFill(fill);

        return ps;
    }

    public PolygonSymbolizer createPolygonSymbolizer(Stroke stroke, Fill fill,
        String geometryPropertyName) {
        return sf.createPolygonSymbolizer(stroke, fill, geometryPropertyName);
    }

    public Style createSimpleStyle(Symbolizer symbolizer) {
        return createSimpleStyle(null, symbolizer, Double.NaN, Double.NaN);
    }

    public Style createSimpleStyle(Symbolizer symbolizer,
        double minScaleDenominator, double maxScaleDenominator) {
        return createSimpleStyle(null, symbolizer, minScaleDenominator,
            maxScaleDenominator);
    }

    public Style createSimpleStyle(String featureTypeStyleName,
        Symbolizer symbolizer) {
        return createSimpleStyle(featureTypeStyleName, symbolizer, Double.NaN,
            Double.NaN);
    }

    public Style createSimpleStyle(String featureTypeStyleName,
        Symbolizer symbolizer, double minScaleDenominator,
        double maxScaleDenominator) {
        // setup the rule
        Rule r = sf.createRule();
        r.setSymbolizers(new Symbolizer[] { symbolizer });

        if (!Double.isNaN(maxScaleDenominator)) {
            r.setMaxScaleDenominator(maxScaleDenominator);
        } else {
            r.setMaxScaleDenominator(Double.MAX_VALUE);
        }

        if (!Double.isNaN(minScaleDenominator)) {
            r.setMinScaleDenominator(minScaleDenominator);
        } else {
            r.setMinScaleDenominator(0.0);
        }

        // setup the feature type style
        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.setRules(new Rule[] { r });

        if (featureTypeStyleName != null) {
            fts.setName(featureTypeStyleName);
        }

        // and finally create the style
        Style style = sf.createStyle();
        style.setFeatureTypeStyles(new FeatureTypeStyle[] { fts });

        return style;
    }

    private Expression colorExpression(Color color) {
        String colorCode = "#" + Integer.toHexString(color.getRed()) +
            Integer.toHexString(color.getGreen()) +
            Integer.toHexString(color.getBlue());

        return ff.createLiteralExpression(colorCode);
    }

    private Expression literalExpression(float value) {
        return ff.createLiteralExpression(value);
    }
}
