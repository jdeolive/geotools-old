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
import java.net.URL;
import org.geotools.factory.*;


public abstract class StyleFactory implements Factory {

    private static StyleFactory factory = null;

    /**
     * Create an instance of the factory.
     *
     * @return An instance of the Factory, or null if the Factory could not be
     *         created.
     */
    public static StyleFactory createStyleFactory() 
    throws FactoryConfigurationError {

        if (factory == null) {
            factory = (StyleFactory) FactoryFinder.findFactory(
              "org.geotools.styling.StyleFactory", 
              "org.geotools.styling.StyleFactoryImpl"
            );
        }

        return factory;
    }

    

    public abstract TextSymbolizer createTextSymbolizer(Fill fill,
        Font[] fonts, Halo halo, Expression label,
        LabelPlacement labelPlacement, String geometryPropertyName);

    public abstract ExternalGraphic createExternalGraphic(URL url, String format);

    public abstract ExternalGraphic createExternalGraphic(String uri,
        String format);

    public abstract AnchorPoint createAnchorPoint(Expression x, Expression y);

    public abstract Displacement createDisplacement(Expression x, Expression y);

    //    public abstract LinePlacement createLinePlacement();
    public abstract PointSymbolizer createPointSymbolizer();

    //    public abstract PointPlacement createPointPlacement();
    public abstract Mark createMark(Expression wellKnownName, Stroke stroke,
        Fill fill, Expression size, Expression rotation);

    public abstract Mark getCircleMark();

    public abstract Mark getXMark();

    public abstract Mark getStarMark();

    public abstract Mark getSquareMark();

    public abstract Mark getCrossMark();

    public abstract Mark getTriangleMark();

    public abstract FeatureTypeStyle createFeatureTypeStyle(Rule[] rules);

    public abstract LinePlacement createLinePlacement(Expression offset);

    public abstract PolygonSymbolizer createPolygonSymbolizer();

    public abstract Halo createHalo(Fill fill, Expression radius);

    public abstract Fill createFill(Expression color,
        Expression backgroundColor, Expression opacity, Graphic graphicFill);

    public abstract LineSymbolizer createLineSymbolizer();

    public abstract PointSymbolizer createPointSymbolizer(Graphic graphic,
        String geometryPropertyName);

    public abstract Style createStyle();

    public abstract Fill createFill(Expression color, Expression opacity);

    public abstract Fill createFill(Expression color);

    public abstract TextSymbolizer createTextSymbolizer();

    public abstract PointPlacement createPointPlacement(
        AnchorPoint anchorPoint, Displacement displacement, Expression rotation);

    public abstract Stroke createStroke(Expression color, Expression width);

    public abstract Stroke createStroke(Expression color, Expression width,
        Expression opacity);

    public abstract Stroke createStroke(Expression color, Expression width,
        Expression opacity, Expression lineJoin, Expression lineCap,
        float[] dashArray, Expression dashOffset, Graphic graphicFill,
        Graphic graphicStroke);

    public abstract Rule createRule();

    public abstract LineSymbolizer createLineSymbolizer(Stroke stroke,
        String geometryPropertyName);

    public abstract FeatureTypeStyle createFeatureTypeStyle();

    public abstract Graphic createGraphic(ExternalGraphic[] externalGraphics,
        Mark[] marks, Symbol[] symbols, Expression opacity, Expression size,
        Expression rotation);

    public abstract Font createFont(Expression fontFamily,
        Expression fontStyle, Expression fontWeight, Expression fontSize);

    public abstract Mark createMark();

    public abstract PolygonSymbolizer createPolygonSymbolizer(Stroke stroke,
        Fill fill, String geometryPropertyName);

    public abstract RasterSymbolizer createRasterSymbolizer(
        String geometryPropertyName, Expression opacity,
        ChannelSelection channel, Expression overlap, ColorMap colorMap,
        ContrastEnhancement ce, ShadedRelief relief, Symbolizer outline);

    public abstract RasterSymbolizer getDefaultRasterSymbolizer();

    public abstract ChannelSelection createChannelSelection(
        SelectedChannelType[] channels);

    public abstract SelectedChannelType createSelectedChannelType(String name,
        Expression enhancement);

    public abstract ColorMap createColorMap();

    public abstract Style getDefaultStyle();

    public abstract Stroke getDefaultStroke();

    public abstract Fill getDefaultFill();

    public abstract Mark getDefaultMark();

    public abstract PointSymbolizer getDefaultPointSymbolizer();

    public abstract PolygonSymbolizer getDefaultPolygonSymbolizer();

    public abstract LineSymbolizer getDefaultLineSymbolizer();

    public abstract TextSymbolizer getDefaultTextSymbolizer();

    public abstract Graphic getDefaultGraphic();

    public abstract Font getDefaultFont();
    
    public abstract PointPlacement getDefaultPointPlacement();

    
}
