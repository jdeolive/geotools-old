/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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


package org.geotools.styling2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.geotools.util.SimpleInternationalString;
import org.opengis.filter.expression.Expression;
import org.opengis.style.AnchorPoint;
import org.opengis.style.ChannelSelection;
import org.opengis.style.ColorMap;
import org.opengis.style.ContrastEnhancement;
import org.opengis.style.Description;
import org.opengis.style.Displacement;
import org.opengis.style.Fill;
import org.opengis.style.Font;
import org.opengis.style.Graphic;
import org.opengis.style.GraphicSymbol;
import org.opengis.style.Halo;
import org.opengis.style.LabelPlacement;
import org.opengis.style.LineSymbolizer;
import org.opengis.style.Mark;
import org.opengis.style.OverlapBehavior;
import org.opengis.style.PointSymbolizer;
import org.opengis.style.PolygonSymbolizer;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.SelectedChannelType;
import org.opengis.style.ShadedRelief;
import org.opengis.style.Stroke;
import org.opengis.style.Symbolizer;
import org.opengis.style.TextSymbolizer;

/**
 *
 * TODO Cache and reuse symbolizers and others (they are immutable so better do it)
 * 
 * @author Johann Sorel
 */
public class SymbolizerBuilder {
        
    private static long id = 0;
    
    public static final ChannelSelection       DEFAULT_RASTER_CHANNEL_RGB;
    public static final ChannelSelection       DEFAULT_RASTER_CHANNEL_GRAY;
    public static final OverlapBehavior        DEFAULT_RASTER_OVERLAP;
    public static final ColorMap               DEFAULT_RASTER_COLORMAP;
    public static final ContrastEnhancement    DEFAULT_RASTER_CONTRAST_ENCHANCEMENT;
    public static final ShadedRelief           DEFAULT_RASTER_SHADED_RELIEF;
    public static final Symbolizer             DEFAULT_RASTER_OUTLINE;
    
    public static final Expression             DEFAULT_GRAPHIC_SIZE;
    
    public static final String                 DEFAULT_POINT_NAME;
    public static final String                 DEFAULT_LINE_NAME;
    public static final String                 DEFAULT_POLYGON_NAME;
    public static final String                 DEFAULT_TEXT_NAME;
    public static final String                 DEFAULT_RASTER_NAME;
    
    public static final Expression             DEFAULT_OPACITY;
    public static final String                 DEFAULT_UOM;
    public static final String                 DEFAULT_GEOM;
    public static final Description            DEFAULT_DESCRIPTION;
    public static final Displacement           DEFAULT_DISPLACEMENT; 
    public static final AnchorPoint            DEFAULT_ANCHOR_POINT;
    public static final Expression             DEFAULT_ROTATION;
    
    private static final org.geotools.styling.StyleBuilder SB = new org.geotools.styling.StyleBuilder();
    
    static{
        DEFAULT_OPACITY = SB.literalExpression(1f);
        DEFAULT_UOM = Symbolizer.UOM_DISPLAY;
        DEFAULT_GEOM = null;        
        DEFAULT_DESCRIPTION = new DefaultDescription(
                new SimpleInternationalString("Title"), 
                new SimpleInternationalString("Description"));
        DEFAULT_DISPLACEMENT = new DefaultDisplacement(SB.literalExpression(0), SB.literalExpression(0));
        DEFAULT_ANCHOR_POINT = new DefaultAnchorPoint(SB.literalExpression(0.5d), SB.literalExpression(0.5d));
        DEFAULT_ROTATION = SB.literalExpression(0);
        DEFAULT_GRAPHIC_SIZE = SB.literalExpression(16);
        
        DEFAULT_POINT_NAME = "PointSymbolizer ";
        DEFAULT_LINE_NAME = "LineSymbolizer ";
        DEFAULT_POLYGON_NAME = "PolygonSymbolizer ";
        DEFAULT_TEXT_NAME = "TextSymbolizer ";
        DEFAULT_RASTER_NAME = "RasterSymbolizer ";
        
        SelectedChannelType[] rgb = new SelectedChannelType[3];
        rgb[0] = new DefaultSelectedChannelType("1", null);
        rgb[1] = new DefaultSelectedChannelType("2", null);
        rgb[2] = new DefaultSelectedChannelType("3", null);
        DEFAULT_RASTER_CHANNEL_RGB = new DefaultChannelSelection(rgb, null);
        
        SelectedChannelType gray = new DefaultSelectedChannelType("1", null);
        DEFAULT_RASTER_CHANNEL_GRAY = new DefaultChannelSelection(null, gray);
        
        DEFAULT_RASTER_OVERLAP = OverlapBehavior.LATEST_ON_TOP;
        DEFAULT_RASTER_COLORMAP = new DefaultColorMap(null);
        DEFAULT_RASTER_CONTRAST_ENCHANCEMENT = new DefaultContrastEnchancement(false,false,1d);
        DEFAULT_RASTER_SHADED_RELIEF = new DefaultShadedRelief(false, 1d);
        DEFAULT_RASTER_OUTLINE = null;
    }
    
    
    public PointSymbolizer createDefaultPointSymbolizer(){
        PointSymbolizer symbol = new DefaultPointSymbolizer(
                createDefaultGraphic(), 
                DEFAULT_UOM, 
                null, 
                DEFAULT_POINT_NAME + id++, 
                DEFAULT_DESCRIPTION);
        
        return symbol;
    }
    
    public LineSymbolizer createDefaultLineSymbolizer(){
        LineSymbolizer symbol = new DefaultLineSymbolizer(
                createStroke(Color.RED, 1), 
                SB.literalExpression(0), 
                DEFAULT_UOM, 
                null, 
                DEFAULT_LINE_NAME + id++, 
                DEFAULT_DESCRIPTION);
        return symbol;
        
    }
    
    public PolygonSymbolizer createDefaultPolygonSymbolizer(){
        PolygonSymbolizer symbol = new DefaultPolygonSymbolizer(
                createStroke(Color.BLUE.darker(), 1), 
                createFill(Color.BLUE), 
                DEFAULT_DISPLACEMENT, 
                SB.literalExpression(0), 
                DEFAULT_UOM, 
                null, 
                DEFAULT_POLYGON_NAME + id++, 
                DEFAULT_DESCRIPTION);
        return symbol;
    }
    
    public TextSymbolizer createDefaultTextSymbolizer(){
        TextSymbolizer symbol = new DefaultTextSymbolizer(
                SB.literalExpression("Label"), 
                createFont(12), 
                createLabelPlacement(), 
                createHalo(Color.WHITE, 0), 
                createFill(Color.BLACK), 
                DEFAULT_UOM, 
                null, 
                DEFAULT_TEXT_NAME + id++, 
                DEFAULT_DESCRIPTION);
        return symbol;
    }
    
    public RasterSymbolizer createDefaultRasterSymbolizer(){
        RasterSymbolizer symbol = new DefaultRasterSymbolizer(
                DEFAULT_OPACITY,
                DEFAULT_RASTER_CHANNEL_RGB,
                DEFAULT_RASTER_OVERLAP,
                DEFAULT_RASTER_COLORMAP,
                DEFAULT_RASTER_CONTRAST_ENCHANCEMENT,
                DEFAULT_RASTER_SHADED_RELIEF,
                DEFAULT_RASTER_OUTLINE,
                DEFAULT_UOM,
                DEFAULT_GEOM,
                DEFAULT_RASTER_NAME + id++,
                DEFAULT_DESCRIPTION);
        
        return symbol;
    }
    
    public Graphic createDefaultGraphic(){
        List<GraphicSymbol> mark = new ArrayList<GraphicSymbol>();
        mark.add(createDefaultMark());
        Graphic graphic = new DefaultGraphic(
                mark, 
                DEFAULT_OPACITY, 
                DEFAULT_GRAPHIC_SIZE, 
                DEFAULT_ROTATION, 
                DEFAULT_ANCHOR_POINT, 
                DEFAULT_DISPLACEMENT);
        
        return graphic;
    }
    
    public Displacement createDisplacement(double x, double y){
        Displacement disp = new DefaultDisplacement(SB.literalExpression(x), SB.literalExpression(y));
        return disp;
    }
    
    public Displacement createDisplacement(Expression x, Expression y){
        Displacement disp = new DefaultDisplacement(x, y);
        return disp;
    }
    
    public AnchorPoint createAnchorPoint(double x, double y){
        AnchorPoint anchor = new DefaultAnchorPoint(SB.literalExpression(x), SB.literalExpression(y));
        return anchor;
    }
    
    public AnchorPoint createAnchorPoint(Expression x, Expression y){
        AnchorPoint anchor = new DefaultAnchorPoint(x,y);
        return anchor;
    }
    
    public Mark createDefaultMark(){
        Mark mark = new DefaultMark(
                SB.literalExpression("square"), 
                null, 
                createFill(Color.GRAY), 
                createStroke(Color.DARK_GRAY, 1f));
        return mark;
    }
    
    public Fill createFill(Color color){
        Fill fill = new DefaultFill(
                null, 
                SB.colorExpression(color), 
                DEFAULT_OPACITY);
        return fill;
    }
    
    public Stroke createStroke(Color color, double width){
        Stroke stroke = new DefaultStroke(
                null, 
                null, 
                SB.colorExpression(color), 
                DEFAULT_OPACITY, 
                SB.literalExpression(width), 
                SB.literalExpression("bevel"), 
                SB.literalExpression("butt"), 
                new float[0], 
                DEFAULT_OPACITY);
        return stroke;
    }
    
    public Halo createHalo(Color color, double width){
        Halo halo = new DefaultHalo(
                createFill(color), 
                SB.literalExpression(width));
        return halo;
    }
    
    public LabelPlacement createLabelPlacement(){
        LabelPlacement placement = new DefaultPointPlacement(
                DEFAULT_ANCHOR_POINT, 
                DEFAULT_DISPLACEMENT, 
                DEFAULT_ROTATION);
        return placement;
    }
    
    public Font createFont(int size){
        Font font = new DefaultFont(
                new ArrayList<Expression>(), 
                null, 
                null, 
                SB.literalExpression(size));
        return font;
    }
    
}
