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
 * StyleFactory.java
 *
 * Created on 14 October 2002, 15:50
 */
package org.geotools.styling;

import org.geotools.filter.Expression;


/**
 * Factory for creating Styles. All style elements are returned as Interfaces
 * from org.geotools.core as opposed to Implementations from
 * org.geotools.defaultcore.
 *
 * @author iant
 * @version $Id: StyleFactoryImpl.java,v 1.9 2003/07/22 15:55:04 ianturton Exp $
 */
public class StyleFactoryImpl extends StyleFactory {
    private static final org.geotools.filter.FilterFactory filterFactory = 
            org.geotools.filter.FilterFactory.createFilterFactory();

    public Style createStyle() {
        return new StyleImpl();
    }

    public PointSymbolizer createPointSymbolizer() {
        return new PointSymbolizerImpl();
    }

    public PointSymbolizer createPointSymbolizer(Graphic graphic, 
                                                 String geometryPropertyName) {
        PointSymbolizer pSymb = new PointSymbolizerImpl();
        pSymb.setGeometryPropertyName(geometryPropertyName);
        pSymb.setGraphic(graphic);

        return pSymb;
    }

    public PolygonSymbolizer createPolygonSymbolizer() {
        return new PolygonSymbolizerImpl();
    }

    public PolygonSymbolizer createPolygonSymbolizer(Stroke stroke, Fill fill, 
                                                     String geometryPropertyName) {
        PolygonSymbolizer pSymb = new PolygonSymbolizerImpl();
        pSymb.setGeometryPropertyName(geometryPropertyName);
        pSymb.setStroke(stroke);
        pSymb.setFill(fill);

        return pSymb;
    }

    public LineSymbolizer createLineSymbolizer() {
        return new LineSymbolizerImpl();
    }

    public LineSymbolizer createLineSymbolizer(Stroke stroke, 
                                               String geometryPropertyName) {
        LineSymbolizer lSymb = new LineSymbolizerImpl();
        lSymb.setGeometryPropertyName(geometryPropertyName);
        lSymb.setStroke(stroke);

        return lSymb;
    }

    public TextSymbolizer createTextSymbolizer() {
        return new TextSymbolizerImpl();
    }

    public TextSymbolizer createTextSymbolizer(Fill fill, Font[] fonts, 
                                               Halo halo, Expression label, 
                                               LabelPlacement labelPlacement, 
                                               String geometryPropertyName) {
        TextSymbolizer tSymb = new TextSymbolizerImpl();
        tSymb.setFill(fill);
        tSymb.setFonts(fonts);
        tSymb.setGeometryPropertyName(geometryPropertyName);

        tSymb.setHalo(halo);
        tSymb.setLabel(label);
        tSymb.setLabelPlacement(labelPlacement);

        return tSymb;
    }

    public FeatureTypeStyle createFeatureTypeStyle() {
        return new FeatureTypeStyleImpl();
    }

    public FeatureTypeStyle createFeatureTypeStyle(Rule[] rules) {
        return new FeatureTypeStyleImpl(rules);
    }

    public Rule createRule() {
        return new RuleImpl();
    }

    /**
     * A convienice method to make a simple stroke
     *
     * @param color the color of the line
     * @param width the width of the line
     *
     * @return the stroke object
     *
     * @see org.geotools.stroke
     */
    public Stroke createStroke(Expression color, Expression width) {
        return createStroke(color, width, 
                            filterFactory.createLiteralExpression(1.0));
    }

    /**
     * A convienice method to make a simple stroke
     *
     * @param color the color of the line
     * @param width The width of the line
     * @param opacity The opacity of the line
     *
     * @return The stroke
     *
     * @see org.geotools.stroke
     */
    public Stroke createStroke(Expression color, Expression width, 
                               Expression opacity) {
        return createStroke(color, width, opacity, 
                            filterFactory.createLiteralExpression("bevel"), 
                            filterFactory.createLiteralExpression("square"), 
                            null, filterFactory.createLiteralExpression(0.0), 
                            null, null);
    }

    /**
     * creates a stroke
     *
     * @param color The color of the line
     * @param width The width of the line
     * @param opacity The opacity of the line
     * @param lineJoin - the type of Line joint
     * @param lineCap - the type of line cap
     * @param dashArray - an array of floats describing the dashes in the line
     * @param dashOffset - where in the dash array to start drawing from
     * @param graphicFill - a graphic object to fill the line with
     * @param graphicStroke - a graphic object to draw the line with
     *
     * @return The completed stroke.
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     *
     * @see org.geotools.stroke
     */
    public Stroke createStroke(Expression color, Expression width, 
                               Expression opacity, Expression lineJoin, 
                               Expression lineCap, float[] dashArray, 
                               Expression dashOffset, Graphic graphicFill, 
                               Graphic graphicStroke) {
        Stroke stroke = new StrokeImpl();

        if (color == null) {
            throw new IllegalArgumentException(
                    "Color may not be null in a stroke");
        }

        stroke.setColor(color);

        if (width == null) {
            throw new IllegalArgumentException(
                    "Width may not be null in a stroke");
        }

        stroke.setWidth(width);

        if (opacity == null) {
            throw new IllegalArgumentException(
                    "Opacity may not be null in a stroke");
        }

        stroke.setOpacity(opacity);

        if (lineJoin == null) {
            throw new IllegalArgumentException(
                    "LineJoin may not be null in a stroke");
        }

        stroke.setLineJoin(lineJoin);

        if (lineCap == null) {
            throw new IllegalArgumentException(
                    "LineCap may not be null in a stroke");
        }

        stroke.setLineCap(lineCap);
        stroke.setDashArray(dashArray);
        stroke.setDashOffset(dashOffset);
        stroke.setGraphicFill(graphicFill);
        stroke.setGraphicStroke(graphicStroke);

        return stroke;
    }

    public Fill createFill(Expression color, Expression backgroundColor, 
                           Expression opacity, Graphic graphicFill) {
        Fill fill = new FillImpl();

        if (color == null) {
            throw new IllegalArgumentException(
                    "Color may not be null in a fill");
        }

        fill.setColor(color);
        fill.setBackgroundColor(backgroundColor);

        if (opacity == null) {
            throw new IllegalArgumentException(
                    "Opacity may not be null in a fill");
        }


        // would be nice to check if this was within bounds but we have to wait until use since it may depend on an attribute
        fill.setOpacity(opacity);
        fill.setGraphicFill(graphicFill);

        return fill;
    }

    public Fill createFill(Expression color, Expression opacity) {
        return createFill(color, null, opacity, null);
    }

    public Fill createFill(Expression color) {
        return createFill(color, null, 
                          filterFactory.createLiteralExpression(1.0), null);
    }

    public Mark createMark(Expression wellKnownName, Stroke stroke, Fill fill, 
                           Expression size, Expression rotation) {
        Mark mark = new MarkImpl();

        if (wellKnownName == null) {
            throw new IllegalArgumentException(
                    "WellKnownName can not be null in mark");
        }

        mark.setWellKnownName(wellKnownName);
        mark.setStroke(stroke);
        mark.setFill(fill);

        if (size == null) {
            throw new IllegalArgumentException("Size can not be null in mark");
        }

        mark.setSize(size);

        if (rotation == null) {
            throw new IllegalArgumentException(
                    "Rotation can not be null in mark");
        }

        mark.setRotation(rotation);

        return mark;
    }

    public Mark getSquareMark() {
        Mark mark = createMark(filterFactory.createLiteralExpression("Square"), 
                               getDefaultStroke(), getDefaultFill(), 
                               filterFactory.createLiteralExpression(6), 
                               filterFactory.createLiteralExpression(0));

        return mark;
    }

    public Mark getCircleMark() {
        Mark mark = getDefaultMark();
        mark.setWellKnownName(filterFactory.createLiteralExpression("Circle"));

        return mark;
    }

    public Mark getCrossMark() {
        Mark mark = getDefaultMark();
        mark.setWellKnownName(filterFactory.createLiteralExpression("Cross"));

        return mark;
    }

    public Mark getXMark() {
        Mark mark = getDefaultMark();
        mark.setWellKnownName(filterFactory.createLiteralExpression("X"));

        return mark;
    }

    public Mark getTriangleMark() {
        Mark mark = getDefaultMark();
        mark.setWellKnownName(filterFactory.createLiteralExpression("Triangle"));

        return mark;
    }

    public Mark getStarMark() {
        Mark mark = getDefaultMark();
        mark.setWellKnownName(filterFactory.createLiteralExpression("Star"));

        return mark;
    }

    public Mark createMark() {
        Mark mark = new MarkImpl();

        return mark;
    }

    public Graphic createGraphic(ExternalGraphic[] externalGraphics, 
                                 Mark[] marks, Symbol[] symbols, 
                                 Expression opacity, Expression size, 
                                 Expression rotation) {
        Graphic graphic = new GraphicImpl();
        graphic.setExternalGraphics(externalGraphics);
        graphic.setMarks(marks);
        graphic.setSymbols(symbols);

        if (opacity == null) {
            throw new IllegalArgumentException(
                    "Opacity can not be null in graphic");
        }

        graphic.setOpacity(opacity);

        if (size == null) {
            throw new IllegalArgumentException(
                    "Size can not be null in graphic");
        }

        graphic.setSize(size);

        if (rotation == null) {
            throw new IllegalArgumentException(
                    "Rotation can not be null in graphic");
        }

        graphic.setRotation(rotation);

        return graphic;
    }

    public ExternalGraphic createExternalGraphic(String uri, String format) {
        ExternalGraphic extg = new ExternalGraphicImpl();
        extg.setURI(uri);
        extg.setFormat(format);

        return extg;
    }

    public ExternalGraphic createExternalGraphic(java.net.URL url, 
                                                 String format) {
        ExternalGraphic extg = new ExternalGraphicImpl();
        extg.setLocation(url);
        extg.setFormat(format);

        return extg;
    }

    public Font createFont(Expression fontFamily, Expression fontStyle, 
                           Expression fontWeight, Expression fontSize) {
        Font font = new FontImpl();

        if (fontFamily == null) {
            throw new IllegalArgumentException("Null font family specified");
        }

        font.setFontFamily(fontFamily);

        if (fontSize == null) {
            throw new IllegalArgumentException("Null font size specified");
        }

        font.setFontSize(fontSize);

        if (fontStyle == null) {
            throw new IllegalArgumentException("Null font Style specified");
        }

        font.setFontStyle(fontStyle);

        if (fontWeight == null) {
            throw new IllegalArgumentException("Null font weight specified");
        }

        font.setFontWeight(fontWeight);

        return font;
    }

    //    public LinePlacement createLinePlacement(){
    //        return new LinePlacementImpl();
    //    }
    public LinePlacement createLinePlacement(Expression offset) {
        LinePlacement linep = new LinePlacementImpl();
        linep.setPerpendicularOffset(offset);

        return linep;
    }

    //    public PointPlacement createPointPlacement(){
    //        return new PointPlacementImpl();
    //    }
    public PointPlacement createPointPlacement(AnchorPoint anchorPoint, 
                                               Displacement displacement, 
                                               Expression rotation) {
        PointPlacement pointp = new PointPlacementImpl();
        pointp.setAnchorPoint(anchorPoint);
        pointp.setDisplacement(displacement);
        pointp.setRotation(rotation);

        return pointp;
    }

    public AnchorPoint createAnchorPoint(Expression x, Expression y) {
        AnchorPoint anchorPoint = new AnchorPointImpl();
        anchorPoint.setAnchorPointX(x);
        anchorPoint.setAnchorPointY(y);

        return anchorPoint;
    }

    public Displacement createDisplacement(Expression x, Expression y) {
        Displacement displacement = new DisplacementImpl();
        displacement.setDisplacementX(x);
        displacement.setDisplacementY(y);

        return displacement;
    }

    public Halo createHalo(Fill fill, Expression radius) {
        Halo halo = new HaloImpl();
        halo.setFill(fill);
        halo.setRadius(radius);

        return halo;
    }

    public Fill getDefaultFill() {
        Fill fill = new FillImpl();

        try {
            fill.setColor(filterFactory.createLiteralExpression("#808080"));
            fill.setOpacity(filterFactory.createLiteralExpression(
                                    new Double(1.0)));
        } catch (org.geotools.filter.IllegalFilterException ife) {
            throw new RuntimeException("Error creating fill", ife);
        }

        return fill;
    }

    public LineSymbolizer getDefaultLineSymbolizer() {
        return createLineSymbolizer(getDefaultStroke(), "geometry:line");
    }

    public Mark getDefaultMark() {
        return getSquareMark();
    }

    public PointSymbolizer getDefaultPointSymbolizer() {
        return createPointSymbolizer(getDefaultGraphic(), "geometry:point");
    }

    public PolygonSymbolizer getDefaultPolygonSymbolizer() {
        return createPolygonSymbolizer(getDefaultStroke(), getDefaultFill(), 
                                       "geometry:polygon");
    }

    public Stroke getDefaultStroke() {
        try {
            Stroke stroke = createStroke(filterFactory.createLiteralExpression(
                                                 "#000000"), 
                                         filterFactory.createLiteralExpression(
                                                 new Integer(1)));

            stroke.setDashOffset(filterFactory.createLiteralExpression(
                                         new Integer(0)));
            stroke.setLineCap(filterFactory.createLiteralExpression("butt"));
            stroke.setLineJoin(filterFactory.createLiteralExpression("miter"));
            stroke.setOpacity(filterFactory.createLiteralExpression(
                                      new Integer(1)));

            return stroke;
        } catch (org.geotools.filter.IllegalFilterException ife) {
            //we should never be in here
            throw new RuntimeException("Error creating stroke", ife);
        }
    }

    public Style getDefaultStyle() {
        Style style = createStyle();

        return style;
    }

    /**
     * Creates a default Text Symbolizer, using the defaultFill, defaultFont and defaultPointPlacement, 
     * Sets the geometry attribute name to be geometry:text. No Halo is set.
     * <b>The label is not set</b>
     *
     * @return A default TextSymbolizer
     *
     */
    public TextSymbolizer getDefaultTextSymbolizer() {
        return createTextSymbolizer(getDefaultFill(), 
                                    new Font[] { getDefaultFont() }, null, null, 
                                    getDefaultPointPlacement(), "geometry:text");
    }

    /** Creates a defaultFont which is valid on all machines.
     *  The font is of size 10, Style and Weight normal and uses a serif font.
     *
     * @return the default Font
     */
    public Font getDefaultFont() {
        Font font = new FontImpl();

        try {
            font.setFontSize(filterFactory.createLiteralExpression(
                                     new Integer(10)));
            font.setFontStyle(filterFactory.createLiteralExpression("normal"));
            font.setFontWeight(filterFactory.createLiteralExpression("normal"));
            font.setFontFamily(filterFactory.createLiteralExpression("serif"));
        } catch (org.geotools.filter.IllegalFilterException ife) {
            throw new RuntimeException("Error creating font", ife);
        }

        return font;
    }

    public Graphic getDefaultGraphic() {
        Graphic graphic = new GraphicImpl();

        try {
            graphic.setSize(filterFactory.createLiteralExpression(new Integer(6)));
            graphic.setOpacity(filterFactory.createLiteralExpression(new Double(1.0)));
            graphic.setRotation(filterFactory.createLiteralExpression(
                                   new Double(0.0)));
        } catch (org.geotools.filter.IllegalFilterException ife) {
            throw new RuntimeException("Error creating graphic", ife);
        }

        return graphic;
    }

    /** returns a default PointPlacement with a 0,0 anchorPoint and a displacement of 0,0 and a rotation of 0
     *
     *  @return a default PointPlacement.
     */
    public PointPlacement getDefaultPointPlacement() {
        return this.createPointPlacement(this.createAnchorPoint(
                                                 filterFactory.createLiteralExpression(0), 
                                                 filterFactory.createLiteralExpression(0)), 
                                         this.createDisplacement(
                                                 filterFactory.createLiteralExpression(0), 
                                                 filterFactory.createLiteralExpression(0)), 
                                         filterFactory.createLiteralExpression(0));
    }

    public RasterSymbolizer createRasterSymbolizer(String geometryPropertyName, 
                                                   Expression opacity, 
                                                   ChannelSelection channel, 
                                                   Expression overlap, 
                                                   ColorMap colorMap, 
                                                   ContrastEnhancement cenhancement, 
                                                   ShadedRelief relief, 
                                                   Symbolizer outline) {
        RasterSymbolizer rastersym = new RasterSymbolizerImpl();

        if (geometryPropertyName != null) {
            rastersym.setGeometryPropertyName(geometryPropertyName);
        }

        if (opacity != null) {
            rastersym.setOpacity(opacity);
        }

        if (channel != null) {
            rastersym.setChannelSelection(channel);
        }

        if (overlap != null) {
            rastersym.setOverlap(overlap);
        }

        if (colorMap != null) {
            rastersym.setColorMap(colorMap);
        }

        if (cenhancement != null) {
            rastersym.setContrastEnhancement(cenhancement);
        }

        if (relief != null) {
            rastersym.setShadedRelief(relief);
        }

        if (outline != null) {
            rastersym.setImageOutline(outline);
        }

        return rastersym;
    }

    public RasterSymbolizer getDefaultRasterSymbolizer() {
        return createRasterSymbolizer("geom", 
                                      filterFactory.createLiteralExpression(1.0), 
                                      null, null, null, null, null, null);
    }

    public ChannelSelection createChannelSelection(SelectedChannelType[] channels) {
        ChannelSelection channelSel = new ChannelSelectionImpl();

        if ((channels != null) && (channels.length > 0)) {
            channelSel.setSelectedChannels(channels);
        }

        return channelSel;
    }

    public ColorMap createColorMap() {
        return new ColorMapImpl();
    }

    public SelectedChannelType createSelectedChannelType(String name, 
                                                         Expression enhancement) {
        SelectedChannelType sct = new SelectedChannelTypeImpl();
        sct.setChannelName(name);
        sct.setContrastEnhancement(enhancement);

        return sct;
    }
}