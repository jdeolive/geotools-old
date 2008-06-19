/*
 *    GeoTools - The Open Source Java GIS Toolkit
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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import org.geotools.util.SimpleInternationalString;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.style.AnchorPoint;
import org.opengis.style.ChannelSelection;
import org.opengis.style.ColorMap;
import org.opengis.style.ColorReplacement;
import org.opengis.style.ContrastEnhancement;
import org.opengis.style.Description;
import org.opengis.style.Displacement;
import org.opengis.style.ExternalGraphic;
import org.opengis.style.ExternalMark;
import org.opengis.style.Fill;
import org.opengis.style.Font;
import org.opengis.style.Graphic;
import org.opengis.style.GraphicFill;
import org.opengis.style.GraphicStroke;
import org.opengis.style.GraphicSymbol;
import org.opengis.style.Halo;
import org.opengis.style.LabelPlacement;
import org.opengis.style.LinePlacement;
import org.opengis.style.LineSymbolizer;
import org.opengis.style.Mark;
import org.opengis.style.OnlineResource;
import org.opengis.style.OverlapBehavior;
import org.opengis.style.PointPlacement;
import org.opengis.style.PointSymbolizer;
import org.opengis.style.PolygonSymbolizer;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.SelectedChannelType;
import org.opengis.style.ShadedRelief;
import org.opengis.style.Stroke;
import org.opengis.style.Symbolizer;
import org.opengis.style.TextSymbolizer;
import org.opengis.util.InternationalString;

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
                createStroke(Color.BLACK, 1), 
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
        
    public AnchorPoint createAnchorPoint(double x, double y){
        AnchorPoint anchor = new DefaultAnchorPoint(SB.literalExpression(x), SB.literalExpression(y));
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
    
    public Stroke createStroke(Color color, double width, float[] dashes){
        Stroke stroke = new DefaultStroke(
                null, 
                null, 
                SB.colorExpression(color), 
                DEFAULT_OPACITY, 
                SB.literalExpression(width), 
                SB.literalExpression("bevel"), 
                SB.literalExpression("butt"), 
                dashes, 
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
    
    //-------------------------------------------------------------------------------------------
    //complete creation methods -----------------------------------------------------------------
    //-------------------------------------------------------------------------------------------
    
    public AnchorPoint createAnchorPoint(Expression x, Expression y){
        AnchorPoint anchor = new DefaultAnchorPoint(x,y);
        return anchor;
    }
    
    public ChannelSelection createChannelSelection(SelectedChannelType[] rgb, SelectedChannelType gray){
        ChannelSelection selection = new DefaultChannelSelection(rgb, gray);
        return selection; 
    }
    
    public ColorMap createColorMap(Function function){
        ColorMap color = new DefaultColorMap(function);
        return color; 
    }
    
    public ColorReplacement createColorReplacement(Function recode){
        ColorReplacement cr = new DefaultColorReplacement(recode);
        return cr;
    }
    
    public ContrastEnhancement createContrastEnhancement(boolean normalize, boolean histogram, double gamma){
        ContrastEnhancement ce = new DefaultContrastEnchancement(normalize, histogram, gamma);
        return ce; 
    }
    
    public Description createDescription(InternationalString title, InternationalString abs){
        Description desc = new DefaultDescription(title, abs);
        return desc; 
    }
    
    public Displacement createDisplacement(Expression x, Expression y){
        Displacement disp = new DefaultDisplacement(x, y);
        return disp; 
    }
    
    public ExternalGraphic createExternalGraphic(OnlineResource resource, Icon icon, String format, Collection<ColorReplacement> replaces){
        ExternalGraphic eg = new DefaultExternalGraphic(resource, icon, format, replaces);
        return eg;
    }
    
    public ExternalMark createExternalMark(OnlineResource online, Icon icon, String format, int index){
        ExternalMark em = new DefaultExternalMark(online, icon, format, index);
        return em;
    }
    
    public Fill createFill(GraphicFill fill, Expression color, Expression opacity){
        Fill fl = new DefaultFill(fill, color, opacity);
        return fl; 
    }
    
    public Font createFont(List<Expression> family, Expression style, Expression weight, Expression size){
        Font f = new DefaultFont(family, style, weight, size);
        return f;
    }
    
    public Graphic createGraphic(List<GraphicSymbol> symbols, 
            Expression opacity, 
            Expression size, 
            Expression rotation, 
            AnchorPoint anchor, 
            Displacement disp){
        Graphic g = new DefaultGraphic(symbols, opacity, size, rotation, anchor, disp);
        return g; 
    }
    
    public GraphicFill createGraphicFill(Graphic graphic){
        GraphicFill fill = new DefaultGraphicFill(graphic);
        return fill;
    }
    
    public GraphicStroke createGraphicStroke(Graphic graphic, Expression initial, Expression gap){
        GraphicStroke stroke = new DefaultGraphicStroke(graphic, initial, gap);
        return stroke;
    }
    
    public Halo createHalo(Fill fill, Expression radius){
        Halo halo = new DefaultHalo(fill, radius);
        return halo;
    }
    
    public LinePlacement createLinePlacement(Expression offset, 
            Expression initial, 
            Expression gap, 
            boolean repeated, 
            boolean aligned, 
            boolean generalize){
        LinePlacement lp = new DefaultLinePlacement(offset, initial, gap, repeated, aligned, generalize);
        return lp;
    }
    
    public LineSymbolizer createLineSymbolizer(Stroke stroke, Expression offset, String uom, String geom, String name, Description desc){
        LineSymbolizer ls = new DefaultLineSymbolizer(stroke, offset, uom, geom, name, desc);
        return ls;
    }
    
    public Mark createMark(Expression wkn, ExternalMark external, Fill fill, Stroke stroke){
        Mark mark = new DefaultMark(wkn, external, fill, stroke);
        return mark;
    }
    
    public OnlineResource createOnlineResource(URI uri){
        OnlineResource or = new DefaultOnlineResource(uri);
        return or;
    }
    
    public PointPlacement createPointPlacement(AnchorPoint anchor, Displacement disp, Expression rotation){
        PointPlacement pp = new DefaultPointPlacement(anchor, disp, rotation);
        return pp;
    }
    
    public PointSymbolizer createPointSymbolizer(Graphic graphic, String uom, String geom, String name, Description desc){
        PointSymbolizer ps = new DefaultPointSymbolizer(graphic, uom, geom, name, desc);
        return ps;
    }
    
    public PolygonSymbolizer createPolygonSymbolizer(Stroke stroke, 
            Fill fill, 
            Displacement disp, 
            Expression offset, 
            String uom, 
            String geom,
            String name, 
            Description desc){
        PolygonSymbolizer ps = new DefaultPolygonSymbolizer(stroke, fill, disp, offset, uom, geom, name, desc);
        return ps;
    }
    
    public RasterSymbolizer createRasterSymbolizer(Expression opacity, 
            ChannelSelection selection, 
            OverlapBehavior overlap, 
            ColorMap colorMap, 
            ContrastEnhancement enchance,
            ShadedRelief relief,
            Symbolizer outline,
            String uom,
            String geom,
            String name,
            Description desc){
        RasterSymbolizer rs = new DefaultRasterSymbolizer(opacity, selection, overlap, colorMap, enchance, relief, outline, uom, geom, name, desc);
        return rs; 
    }
    
    public SelectedChannelType createSelectedChannelType(String name, ContrastEnhancement enchance){ 
        SelectedChannelType sct = new DefaultSelectedChannelType(name, enchance);
        return sct;
    }
    
    public ShadedRelief createShadedRelief(boolean bright, double relief){
        ShadedRelief sr = new DefaultShadedRelief(bright, relief);
        return sr;
    }
    
    public Stroke createStroke(GraphicFill fill, 
            GraphicStroke stroke, 
            Expression color, 
            Expression opacity, 
            Expression width, 
            Expression join, 
            Expression cap, 
            float[] dashes, 
            Expression offset){
        Stroke str = new DefaultStroke(fill, stroke, color, opacity, width, join, cap, dashes, offset);
        return str; 
    }
    
    public TextSymbolizer createTextSymbolizer(Expression label, 
            Font font, 
            LabelPlacement placement, 
            Halo halo,
            Fill fill, 
            String uom, 
            String geom, 
            String name, 
            Description desc){
        TextSymbolizer ts = new DefaultTextSymbolizer(label, font, placement, halo, fill, uom, geom, name, desc);
        return ts;
    }
    
    
}
