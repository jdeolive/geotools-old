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

import org.geotools.filter.AttributeExpression;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.IllegalFilterException;
import java.awt.Color;
import java.net.URL;


/**
 * An utility class designed to ease style building by convinience methods.
 *
 * @author aaime
 */
public class StyleBuilder {
    public static final String LINE_JOIN_MITRE = "mitre";
    public static final String LINE_JOIN_ROUND = "round";
    public static final String LINE_JOIN_BEVEL = "bevel";
    public static final String LINE_CAP_BUTT = "butt";
    public static final String LINE_CAP_ROUND = "round";
    public static final String LINE_CAP_SQUARE = "square";
    public static final String MARK_SQUARE = "square";
    public static final String MARK_CIRCLE = "circle";
    public static final String MARK_TRIANGLE = "triangle";
    public static final String MARK_STAR = "star";
    public static final String MARK_CROSS = "cross";
    public static final String MARK_ARROW = "arrow";
    public static final String MARK_X = "x";
    public static final String FONT_STYLE_NORMAL = "normal";
    public static final String FONT_STYLE_ITALIC = "italic";
    public static final String FONT_STYLE_OBLIQUE = "oblique";
    public static final String FONT_WEIGHT_NORMAL = "normal";
    public static final String FONT_WEIGHT_BOLD = "bold";
    private StyleFactory sf;
    private FilterFactory ff;

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

    public StyleFactory getStyleFactory() {
        return sf;
    }

    public FilterFactory getFilterFactory() {
        return ff;
    }

    public Stroke createStroke() {
        return sf.getDefaultStroke();
    }

    public Stroke createStroke(double width) {
        return createStroke(Color.BLACK, width);
    }

    public Stroke createStroke(Color color) {
        return createStroke(color, 1);
    }

    public Stroke createStroke(Color color, double width) {
        return sf.createStroke(colorExpression(color), literalExpression(width));
    }

    public Stroke createStroke(Color color, double width, String lineJoin, String lineCap) {
        Stroke stroke = createStroke(color, width);
        stroke.setLineJoin(literalExpression(lineJoin));
        stroke.setLineCap(literalExpression(lineCap));

        return stroke;
    }

    public Stroke createStroke(Color color, double width, float[] dashArray) {
        Stroke stroke = createStroke(color, width);
        stroke.setDashArray(dashArray);

        return stroke;
    }

    public Stroke createStroke(Expression color, Expression width) {
        return sf.createStroke(color, width);
    }

    public Stroke createStroke(Color color, double width, double opacity) {
        return sf.createStroke(colorExpression(color), literalExpression(width),
            literalExpression(opacity));
    }

    public Stroke createStroke(Expression color, Expression width, Expression opacity) {
        return sf.createStroke(color, width, opacity);
    }

    public Fill createFill() {
        return sf.getDefaultFill();
    }

    public Fill createFill(Color fillColor) {
        return createFill(colorExpression(fillColor));
    }

    public Fill createFill(Expression fillColor) {
        return sf.createFill(fillColor);
    }

    public Fill createFill(Color fillColor, double opacity) {
        return sf.createFill(colorExpression(fillColor), literalExpression(opacity));
    }

    public Fill createFill(Expression color, Expression opacity) {
        return sf.createFill(color, opacity);
    }

    public Fill createFill(Color color, Color backgroundColor, double opacity, Graphic fill) {
        return sf.createFill(colorExpression(color), colorExpression(backgroundColor),
            literalExpression(opacity), fill);
    }

    public Fill createFill(Expression color, Expression backgroundColor, Expression opacity,
        Graphic fill) {
        return sf.createFill(color, backgroundColor, opacity, fill);
    }

    public Mark createMark(String wellKnownName) {
        Mark mark = sf.createMark();
        mark.setWellKnownName(literalExpression(wellKnownName));

        return mark;
    }

    public Mark createMark(String wellKnownName, Color fillColor, Color borderColor,
        double borderWidth) {
        Mark mark = sf.createMark();
        mark.setWellKnownName(literalExpression(wellKnownName));
        mark.setStroke(createStroke(borderColor, borderWidth));
        mark.setFill(createFill(fillColor));

        return mark;
    }

    public Mark createMark(String wellKnownName, Color borderColor, double borderWidth) {
        Mark mark = sf.createMark();
        mark.setWellKnownName(literalExpression(wellKnownName));
        mark.setStroke(createStroke(borderColor, borderWidth));

        return mark;
    }

    public Mark createMark(String wellKnownName, Color fillColor) {
        Mark mark = sf.createMark();
        mark.setWellKnownName(literalExpression(wellKnownName));
        mark.setFill(createFill(fillColor));
        mark.setStroke(null);

        return mark;
    }

    public Mark createMark(String wellKnownName, Fill fill, Stroke stroke) {
        Mark mark = sf.createMark();
        mark.setWellKnownName(literalExpression(wellKnownName));
        mark.setStroke(stroke);
        mark.setFill(fill);

        return mark;
    }

    public Mark createMark(Expression wellKnownName, Fill fill, Stroke stroke) {
        Mark mark = sf.createMark();
        mark.setWellKnownName(wellKnownName);
        mark.setStroke(stroke);
        mark.setFill(fill);

        return mark;
    }

    public ExternalGraphic createExternalGraphic(String uri, String format) {
        return sf.createExternalGraphic(uri, format);
    }

    public ExternalGraphic createExternalGraphic(URL url, String format) {
        return sf.createExternalGraphic(url, format);
    }

    public Graphic createGraphic(ExternalGraphic externalGraphic, Mark mark, Symbol symbol) {
        Graphic gr = sf.getDefaultGraphic();

        if (externalGraphic != null) {
            gr.setExternalGraphics(new ExternalGraphic[] { externalGraphic });
        }

        if (mark != null) {
            gr.setMarks(new Mark[] { mark });
        }

        if (symbol != null) {
            gr.setSymbols(new Symbol[] { symbol });
        }

        return gr;
    }

    public Graphic createGraphic(ExternalGraphic externalGraphic, Mark mark, Symbol symbol,
        double opacity, double size, double rotation) {
        ExternalGraphic[] egs = null;
        Mark[] marks = null;
        Symbol[] symbols = null;

        if (externalGraphic != null) {
            egs = new ExternalGraphic[] { externalGraphic };
        }

        if (mark != null) {
            marks = new Mark[] { mark };
        }

        if (symbol != null) {
            symbols = new Symbol[] { symbol };
        }

        return createGraphic(egs, marks, symbols, literalExpression(opacity),
            literalExpression(size), literalExpression(rotation));
    }

    public Graphic createGraphic(ExternalGraphic[] externalGraphics, Mark[] marks,
        Symbol[] symbols, double opacity, double size, double rotation) {
        return createGraphic(externalGraphics, marks, symbols, literalExpression(opacity),
            literalExpression(size), literalExpression(rotation));
    }

    public Graphic createGraphic(ExternalGraphic[] externalGraphics, Mark[] marks,
        Symbol[] symbols, Expression opacity, Expression size, Expression rotation) {
        if (externalGraphics == null) {
            externalGraphics = new ExternalGraphic[0];
        }

        if (marks == null) {
            marks = new Mark[0];
        }

        if (symbols == null) {
            symbols = new Symbol[0];
        }

        return sf.createGraphic(externalGraphics, marks, symbols, opacity, size, rotation);
    }

    public AnchorPoint createAnchorPoint(double x, double y) {
        return sf.createAnchorPoint(literalExpression(x), literalExpression(y));
    }

    public AnchorPoint createAnchorPoint(Expression x, Expression y) {
        return sf.createAnchorPoint(x, y);
    }

    public Displacement createDisplacement(double x, double y) {
        return sf.createDisplacement(literalExpression(x), literalExpression(y));
    }

    public Displacement createDisplacement(Expression x, Expression y) {
        return sf.createDisplacement(x, y);
    }

    public PointPlacement createPointPlacement() {
        return sf.getDefaultPointPlacement();
    }

    public PointPlacement createPointPlacement(double anchorX, double anchorY, double rotation) {
        AnchorPoint anchorPoint = createAnchorPoint(anchorX, anchorY);

        return sf.createPointPlacement(anchorPoint, null, literalExpression(rotation));
    }

    public PointPlacement createPointPlacement(double anchorX, double anchorY,
        double displacementX, double displacementY, double rotation) {
        AnchorPoint anchorPoint = createAnchorPoint(anchorX, anchorY);
        Displacement displacement = createDisplacement(displacementX, displacementY);

        return sf.createPointPlacement(anchorPoint, displacement, literalExpression(rotation));
    }

    public PointPlacement createPointPlacement(AnchorPoint anchorPoint, Displacement displacement,
        Expression rotation) {
        return sf.createPointPlacement(anchorPoint, displacement, rotation);
    }

    public LinePlacement createLinePlacement(double offset) {
        return sf.createLinePlacement(literalExpression(offset));
    }

    public LinePlacement createLinePlacement(Expression offset) {
        return sf.createLinePlacement(offset);
    }

    public Font createFont(java.awt.Font font) {
        Expression family = literalExpression(font.getFamily());
        Expression style;
        Expression weight;

        if (font.isBold()) {
            weight = literalExpression(FONT_WEIGHT_BOLD);
        } else {
            weight = literalExpression(FONT_WEIGHT_NORMAL);
        }

        if (font.isItalic()) {
            style = literalExpression(FONT_STYLE_ITALIC);
        } else {
            style = literalExpression(FONT_STYLE_NORMAL);
        }

        return sf.createFont(family, style, weight, literalExpression(font.getSize2D()));
    }

    public Font createFont(String fontFamily, double fontSize) {
        Expression family = literalExpression(fontFamily);
        Expression style = literalExpression(FONT_STYLE_NORMAL);
        Expression weight = literalExpression(FONT_WEIGHT_NORMAL);

        return sf.createFont(family, style, weight, literalExpression(fontSize));
    }

    public Font createFont(String fontFamily, boolean italic, boolean bold, double fontSize) {
        Expression family = literalExpression(fontFamily);
        Expression style;
        Expression weight;

        if (bold) {
            weight = literalExpression(FONT_WEIGHT_BOLD);
        } else {
            weight = literalExpression(FONT_WEIGHT_NORMAL);
        }

        if (italic) {
            style = literalExpression(FONT_STYLE_ITALIC);
        } else {
            style = literalExpression(FONT_STYLE_NORMAL);
        }

        return sf.createFont(family, style, weight, literalExpression(fontSize));
    }

    public Font createFont(Expression fontFamily, Expression fontStyle, Expression fontWeight,
        Expression fontSize) {
        return sf.createFont(fontFamily, fontStyle, fontWeight, fontSize);
    }

    public Halo createHalo() {
        return sf.createHalo(createFill(Color.WHITE), literalExpression(1));
    }

    public Halo createHalo(Color color, double radius) {
        return sf.createHalo(createFill(color), literalExpression(radius));
    }

    public Halo createHalo(Color color, double opacity, double radius) {
        return sf.createHalo(createFill(color, opacity), literalExpression(radius));
    }

    public Halo createHalo(Fill fill, double radius) {
        return sf.createHalo(fill, literalExpression(radius));
    }

    public Halo createHalo(Fill fill, Expression radius) {
        return sf.createHalo(fill, radius);
    }

    public LineSymbolizer createLineSymbolizer() {
        return sf.createLineSymbolizer();
    }

    public LineSymbolizer createLineSymbolizer(double width) {
        return createLineSymbolizer(createStroke(width), null);
    }

    public LineSymbolizer createLineSymbolizer(Color color) {
        return createLineSymbolizer(createStroke(color), null);
    }

    public LineSymbolizer createLineSymbolizer(Color color, double width) {
        return createLineSymbolizer(createStroke(color, width), null);
    }

    public LineSymbolizer createLineSymbolizer(Color color, double width,
        String geometryPropertyName) {
        return createLineSymbolizer(createStroke(color, width), geometryPropertyName);
    }

    public LineSymbolizer createLineSymbolizer(Stroke stroke) {
        return sf.createLineSymbolizer(stroke, null);
    }

    public LineSymbolizer createLineSymbolizer(Stroke stroke, String geometryPropertyName) {
        return sf.createLineSymbolizer(stroke, geometryPropertyName);
    }

    public PolygonSymbolizer createPolygonSymbolizer() {
        return sf.createPolygonSymbolizer();
    }

    public PolygonSymbolizer createPolygonSymbolizer(Color fillColor) {
        return createPolygonSymbolizer(null, createFill(fillColor));
    }

    public PolygonSymbolizer createPolygonSymbolizer(Color fillColor, Color borderColor,
        double borderWidth) {
        return createPolygonSymbolizer(createStroke(borderColor, borderWidth), createFill(fillColor));
    }

    public PolygonSymbolizer createPolygonSymbolizer(Color borderColor, double borderWidth) {
        return createPolygonSymbolizer(createStroke(borderColor, borderWidth), null);
    }

    public PolygonSymbolizer createPolygonSymbolizer(Stroke stroke, Fill fill) {
        return createPolygonSymbolizer(stroke, fill, null);
    }

    public PolygonSymbolizer createPolygonSymbolizer(Stroke stroke, Fill fill,
        String geometryPropertyName) {
        return sf.createPolygonSymbolizer(stroke, fill, geometryPropertyName);
    }

    public PointSymbolizer createPointSymbolizer() {
        return sf.createPointSymbolizer();
    }

    public PointSymbolizer createPointSymbolizer(Graphic graphic) {
        PointSymbolizer ps = sf.createPointSymbolizer();
        ps.setGraphic(graphic);

        return ps;
    }

    public PointSymbolizer createPointSymbolizer(Graphic graphic, String geometryPropertyName) {
        return sf.createPointSymbolizer(graphic, geometryPropertyName);
    }

    public TextSymbolizer createTextSymbolizer(Color color, Font font, String attributeName)
        throws IllegalFilterException {
        return createTextSymbolizer(createFill(color), new Font[] { font }, null,
            attributeExpression(attributeName), null, null);
    }

    public TextSymbolizer createTextSymbolizer(Color color, Font[] fonts, String attributeName)
        throws IllegalFilterException {
        return createTextSymbolizer(createFill(color), fonts, null,
            attributeExpression(attributeName), null, null);
    }

    public TextSymbolizer createStaticTextSymbolizer(Color color, Font font, String label) {
        return createTextSymbolizer(createFill(color), new Font[] { font }, null,
            literalExpression(label), null, null);
    }

    public TextSymbolizer createStaticTextSymbolizer(Color color, Font[] fonts, String label) {
        return createTextSymbolizer(createFill(color), fonts, null, literalExpression(label), null,
            null);
    }

    public TextSymbolizer createTextSymbolizer(Fill fill, Font[] fonts, Halo halo,
        Expression label, LabelPlacement labelPlacement, String geometryPropertyName) {
        TextSymbolizer ts = sf.createTextSymbolizer();

        if (fill != null) {
            ts.setFill(fill);
        }

        if (halo != null) {
            ts.setHalo(halo);
        }

        if (label != null) {
            ts.setLabel(label);
        }

        if (labelPlacement != null) {
            ts.setLabelPlacement(labelPlacement);
        }

        if (geometryPropertyName != null) {
            ts.setGeometryPropertyName(geometryPropertyName);
        }

        if (fonts != null) {
            ts.setFonts(fonts);
        }

        return ts;
    }

    public FeatureTypeStyle createFeatureTypeStyle(Symbolizer symbolizer) {
        return createFeatureTypeStyle(null, symbolizer, Double.NaN, Double.NaN);
    }

    public Rule createRule(Symbolizer symbolizer) {
        return createRule(symbolizer, Double.NaN, Double.NaN);
    }

    public Rule createRule(Symbolizer[] symbolizers) {
        return createRule(symbolizers, Double.NaN, Double.NaN);
    }

    public Rule createRule(Symbolizer symbolizer, double minScaleDenominator,
        double maxScaleDenominator) {
        return createRule(new Symbolizer[] { symbolizer }, Double.NaN, Double.NaN);
    }

    public Rule createRule(Symbolizer[] symbolizers, double minScaleDenominator,
        double maxScaleDenominator) {
        Rule r = sf.createRule();
        r.setSymbolizers(symbolizers);

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

        return r;
    }

    public FeatureTypeStyle createFeatureTypeStyle(Symbolizer symbolizer,
        double minScaleDenominator, double maxScaleDenominator) {
        return createFeatureTypeStyle(null, symbolizer, minScaleDenominator, maxScaleDenominator);
    }

    public FeatureTypeStyle createFeatureTypeStyle(Symbolizer[] symbolizers,
        double minScaleDenominator, double maxScaleDenominator) {
        return createFeatureTypeStyle(null, symbolizers, minScaleDenominator, maxScaleDenominator);
    }

    public FeatureTypeStyle createFeatureTypeStyle(String featureTypeStyleName,
        Symbolizer symbolizer) {
        return createFeatureTypeStyle(featureTypeStyleName, symbolizer, Double.NaN, Double.NaN);
    }

    public FeatureTypeStyle createFeatureTypeStyle(String featureTypeStyleName,
        Symbolizer[] symbolizers) {
        return createFeatureTypeStyle(featureTypeStyleName, symbolizers, Double.NaN, Double.NaN);
    }

    public FeatureTypeStyle createFeatureTypeStyle(String featureTypeStyleName,
        Symbolizer symbolizer, double minScaleDenominator, double maxScaleDenominator) {
        return createFeatureTypeStyle(featureTypeStyleName, new Symbolizer[] { symbolizer },
            minScaleDenominator, maxScaleDenominator);
    }

    public FeatureTypeStyle createFeatureTypeStyle(String featureTypeStyleName,
        Symbolizer[] symbolizers, double minScaleDenominator, double maxScaleDenominator) {
        Rule r = createRule(symbolizers, minScaleDenominator, maxScaleDenominator);

        // setup the feature type style
        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.setRules(new Rule[] { r });

        if (featureTypeStyleName != null) {
            fts.setFeatureTypeName(featureTypeStyleName);
        }

        return fts;
    }

    public Style createStyle(Symbolizer symbolizer) {
        return createStyle(null, symbolizer, Double.NaN, Double.NaN);
    }

    public Style createStyle(Symbolizer symbolizer, double minScaleDenominator,
        double maxScaleDenominator) {
        return createStyle(null, symbolizer, minScaleDenominator, maxScaleDenominator);
    }

    public Style createStyle(String featureTypeStyleName, Symbolizer symbolizer) {
        return createStyle(featureTypeStyleName, symbolizer, Double.NaN, Double.NaN);
    }

    public Style createStyle(String featureTypeStyleName, Symbolizer symbolizer,
        double minScaleDenominator, double maxScaleDenominator) {
        // create the feature type style
        FeatureTypeStyle fts = createFeatureTypeStyle(featureTypeStyleName, symbolizer,
                minScaleDenominator, maxScaleDenominator);

        // and finally create the style
        Style style = sf.createStyle();
        style.addFeatureTypeStyle(fts);

        return style;
    }

    public Style createStyle() {
        return sf.createStyle();
    }

    public Expression colorExpression(Color color) {
        String redCode = Integer.toHexString(color.getRed());
        String greenCode = Integer.toHexString(color.getGreen());
        String blueCode = Integer.toHexString(color.getBlue());

        if (redCode.length() == 1) {
            redCode = "0" + redCode;
        }

        if (greenCode.length() == 1) {
            greenCode = "0" + greenCode;
        }

        if (blueCode.length() == 1) {
            blueCode = "0" + blueCode;
        }

        String colorCode = "#" + redCode + greenCode + blueCode;

        return ff.createLiteralExpression(colorCode);
    }

    public Expression literalExpression(double value) {
        return ff.createLiteralExpression(value);
    }

    public Expression literalExpression(String value) {
        Expression result = null;

        if (value != null) {
            result = ff.createLiteralExpression(value);
        }

        return result;
    }

    public Expression attributeExpression(String attributeName)
        throws IllegalFilterException {
        AttributeExpression attribute = ff.createAttributeExpression(null);
        attribute.setAttributePath(attributeName);

        return attribute;
    }
}
