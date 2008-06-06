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

import org.geotools.util.SimpleInternationalString;
import org.opengis.filter.expression.Expression;
import org.opengis.style.ChannelSelection;
import org.opengis.style.ColorMap;
import org.opengis.style.ContrastEnhancement;
import org.opengis.style.Description;
import org.opengis.style.OverlapBehavior;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.SelectedChannelType;
import org.opengis.style.ShadedRelief;
import org.opengis.style.Symbolizer;

/**
 *
 * @author Johann Sorel
 */
public class StyleBuilder {
        
    private static long id = 0;
    
    private static final ChannelSelection       DEFAULT_RASTER_CHANNEL_RGB;
    private static final ChannelSelection       DEFAULT_RASTER_CHANNEL_GRAY;
    private static final OverlapBehavior        DEFAULT_RASTER_OVERLAP;
    private static final ColorMap               DEFAULT_RASTER_COLORMAP;
    private static final ContrastEnhancement    DEFAULT_RASTER_CONTRAST_ENCHANCEMENT;
    private static final ShadedRelief           DEFAULT_RASTER_SHADED_RELIEF;
    private static final Symbolizer             DEFAULT_RASTER_OUTLINE;
    
    private static final String                 DEFAULT_POINT_NAME;
    private static final String                 DEFAULT_LINE_NAME;
    private static final String                 DEFAULT_POLYGON_NAME;
    private static final String                 DEFAULT_TEXT_NAME;
    private static final String                 DEFAULT_RASTER_NAME;
    
    private static final Expression             DEFAULT_OPACITY;
    private static final String                 DEFAULT_UOM;
    private static final String                 DEFAULT_GEOM;
    private static final Description            DEFAULT_DESCRIPTION;
    
    private static final org.geotools.styling.StyleBuilder SB = new org.geotools.styling.StyleBuilder();
    
    static{
        DEFAULT_OPACITY = SB.literalExpression(1f);
        DEFAULT_UOM = Symbolizer.UOM_DISPLAY;
        DEFAULT_GEOM = null;        
        DEFAULT_DESCRIPTION = new DefaultDescription(
                new SimpleInternationalString("Title"), 
                new SimpleInternationalString("Description"));
        
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
    
    
    public RasterSymbolizer createRasterSymbolizer(){
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
    
}
