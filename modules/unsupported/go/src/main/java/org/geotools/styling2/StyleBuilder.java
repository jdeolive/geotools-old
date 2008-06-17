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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.opengis.style.FeatureTypeStyle;
import org.opengis.style.LineSymbolizer;
import org.opengis.style.PointSymbolizer;
import org.opengis.style.PolygonSymbolizer;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.Rule;
import org.opengis.style.Style;
import org.opengis.style.StyleVisitor;
import org.opengis.style.Symbolizer;
import org.opengis.style.TextSymbolizer;

/**
 *
 * TODO Cache and reuse symbolizers and others (they are immutable so better do it)
 *
 * @author Johann Sorel
 */
public class StyleBuilder {

    private static final org.geotools.styling.StyleBuilder STYLE_BUILDER = new org.geotools.styling.StyleBuilder();
    private static final SymbolizerBuilder SYMBOL_BUILDER = new SymbolizerBuilder();
    private static final Symbolizer DEFAULT_FALLBACK_SYMBOLIZER = SYMBOL_BUILDER.createDefaultLineSymbolizer();

    public Style createStyle(){
        Symbolizer symbol = SYMBOL_BUILDER.createDefaultLineSymbolizer();
        FeatureTypeStyle fts = createFeatureTypeStyle(symbol,FeatureTypeStyle.SEMANTIC_ID_ANY);

        List<FeatureTypeStyle> ftss =new ArrayList<FeatureTypeStyle>();
        ftss.add(fts);
        Style catalog = new MutableStyle("name",SYMBOL_BUILDER.DEFAULT_DESCRIPTION,ftss, DEFAULT_FALLBACK_SYMBOLIZER,false);

        return catalog;
    }

    public Style createPointStyle(PointSymbolizer symbol){
        FeatureTypeStyle fts = createFeatureTypeStyle(symbol,FeatureTypeStyle.SEMANTIC_ID_POINT);
        List<FeatureTypeStyle> ftss =new ArrayList<FeatureTypeStyle>();
        ftss.add(fts);
        Style catalog = new MutableStyle("name",SYMBOL_BUILDER.DEFAULT_DESCRIPTION,ftss, DEFAULT_FALLBACK_SYMBOLIZER,false);
        return catalog;
    }

    public Style createLineStyle(LineSymbolizer symbol){
        FeatureTypeStyle fts = createFeatureTypeStyle(symbol,FeatureTypeStyle.SEMANTIC_ID_LINE);
        List<FeatureTypeStyle> ftss =new ArrayList<FeatureTypeStyle>();
        ftss.add(fts);
        Style catalog = new MutableStyle("name",SYMBOL_BUILDER.DEFAULT_DESCRIPTION,ftss, DEFAULT_FALLBACK_SYMBOLIZER,false);
        return catalog;
    }

    public Style createPolygonStyle(PolygonSymbolizer symbol){
        FeatureTypeStyle fts = createFeatureTypeStyle(symbol,FeatureTypeStyle.SEMANTIC_ID_POLYGON);
        List<FeatureTypeStyle> ftss =new ArrayList<FeatureTypeStyle>();
        ftss.add(fts);
        Style catalog = new MutableStyle("name",SYMBOL_BUILDER.DEFAULT_DESCRIPTION,ftss, DEFAULT_FALLBACK_SYMBOLIZER,false);
        return catalog;
    }

    public Style createTextStyle(TextSymbolizer symbol){
        FeatureTypeStyle fts = createFeatureTypeStyle(symbol, FeatureTypeStyle.SEMANTIC_ID_TEXT);
        List<FeatureTypeStyle> ftss =new ArrayList<FeatureTypeStyle>();
        ftss.add(fts);
        Style catalog = new MutableStyle("name",SYMBOL_BUILDER.DEFAULT_DESCRIPTION,ftss, DEFAULT_FALLBACK_SYMBOLIZER,false);
        return catalog;
    }

    public Style createRasterStyle(RasterSymbolizer symbol){
        FeatureTypeStyle fts = createFeatureTypeStyle(symbol,FeatureTypeStyle.SEMANTIC_ID_RASTER);
        List<FeatureTypeStyle> ftss =new ArrayList<FeatureTypeStyle>();
        ftss.add(fts);
        Style catalog = new MutableStyle("name",SYMBOL_BUILDER.DEFAULT_DESCRIPTION,ftss, DEFAULT_FALLBACK_SYMBOLIZER,false);
        return catalog;
    }



    public FeatureTypeStyle createFeatureTypeStyle(Symbolizer symbol, String semantic){

        //TODO replace those fakenames, will disapear when merged with geotools styles

        List<Symbolizer> symbols = new ArrayList<Symbolizer>();
        symbols.add(symbol);
        Rule rule = new MutableRule("fakename", SymbolizerBuilder.DEFAULT_DESCRIPTION, null, null, false, 0, Double.MAX_VALUE, symbols);

        Collection<String> semantics = new ArrayList<String>();
        semantics.add(semantic);
        List<Rule> rules = new ArrayList<Rule>();
        rules.add(rule);
        FeatureTypeStyle fts = new MutableFeatureTypeStyle("fakename", SymbolizerBuilder.DEFAULT_DESCRIPTION, null, null, semantics , rules);
        return fts;
    }

}
