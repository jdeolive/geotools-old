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

import java.io.IOException;


// J2SE dependencies
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;


/**
 * Exports a filter as a OGC XML Filter document.  This class is does not
 * generate namespace compliant xml, even though it does print gml prefixes.
 * It was also written before the 1.0 filter spec, so some of it may be not up
 * to date.
 *
 * @author James Macgill, PSU
 *
 * @task HACK: Logging errors, very bad!  We need a filter visitor exception,
 *       or have visit methods throw illegal filter exceptions, or io
 *       exceptions.
 * @task TODO: Support full header information for new XML file
 * @task REVISIT: make filter utils class so that other encoders (like sql). It
 *       could also be nice to refactor common code from gml producer, as
 *       there is basically a GeometryProducer there.
 * @task REVISIT: make namespace aware.
 */
public class XMLEncoder implements org.geotools.styling.StyleVisitor {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.style");
    private org.geotools.filter.XMLEncoder filterEncoder;
    
    
    
    /** To write the xml representations of filters to */
    private Writer out;
    
    /**
     * Constructor with writer to write filters to.
     *
     * @param out where to write the xml representation of filters.
     */
    public XMLEncoder(Writer out) {
        this.out = out;
        filterEncoder = new org.geotools.filter.XMLEncoder(out);
    }
    
    /**
     * Creates a new instance of XMLEncoder
     *
     * @param out The writer to write to.
     * @param filter the filter to encode.
     */
    public XMLEncoder(Writer out, Style style) throws java.io.IOException{
        this.out = out;
        filterEncoder = new org.geotools.filter.XMLEncoder(out);
        encode(style);
    }
    
    /**
     * Encodes the filter to the current writer.
     *
     * @param filter the filter to encode.
     *
     * @throws java.io.IOException if there are problems writing to out.
     */
    public void encode(Style style) throws java.io.IOException{
        out.write("<StyledLayerDescriptor version=\"1.0.0\">\n");
        style.accept(this);
        out.write("</StyledLayerDescriptor>\n");
    }
    
    /**
     * Encodes the expression to the current writer.
     *
     * @param expression the expression to encode.
     */
    private void encode(Expression expression) {
        if(expression!=null) expression.accept(filterEncoder);
    }
    
    public void encodeCssParam(String name, Expression expression){
        try{
            out.write("<CssParameter name='"+name+"'>\n");
            encode(expression);
            out.write("</CssParameter>\n");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
    
    public void visit(Style style){
        try{
            out.write("<namedlayer>");
            String title = style.getTitle();
            String abs = style.getAbstract();
            String name = style.getName();
            out.write("<title>"+title+"</title>\n");
            out.write("<name>"+name+"</name>\n");
            out.write("<abstract>"+abs+"</abstract>\n");
            out.write("<userlayer>");
            FeatureTypeStyle[] fts = style.getFeatureTypeStyles();
            for(int i=0;i<fts.length;i++){
                visit(fts[i]);
            }
            out.write("</userlayer>\n");
            out.write("</namedlayer>\n");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
    public void visit(FeatureTypeStyle fts){
        try{
            out.write("<FeatureTypeStyle>\n");
            out.write("<FeatureTypeName>");
            out.write(fts.getName());
            out.write("</FeatureTypeName>\n");
            Rule rules[] = fts.getRules();
            for(int i=0;i<rules.length;i++){
                rules[i].accept(this);
            }
            out.write("</FeatureTypeStyle>\n");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
    
    public void visit(Rule rule){
        try{
            out.write("<Rule>\n");
            out.write("<name>"+rule.getName()+"</name>\n");
            out.write("<abstract>"+rule.getAbstract()+"</abstract>\n");
            out.write("<title>"+rule.getTitle()+"</title>\n");
            if(rule.getMaxScaleDenominator()!=Double.POSITIVE_INFINITY)out.write("<MaxScaleDenominator>"+rule.getMaxScaleDenominator()+"</MaxScaleDenominator>");
            if(rule.getMinScaleDenominator()!=0.0)out.write("<MinScaleDenominator>"+rule.getMinScaleDenominator()+"</MinScaleDenominator>");
            Filter filter = rule.getFilter();
            if(filter!=null){
                out.write("<Filter>");
                filter.accept(filterEncoder);
                out.write("</Filter>");
            }
            if(rule.hasElseFilter()){
                out.write("<ElseFilter/>");
            }
            Graphic[] gr = rule.getLegendGraphic();
            for(int i=0;i<gr.length;i++){
                gr[i].accept(this);
            }
            Symbolizer[] sym = rule.getSymbolizers();
            for(int i=0;i<sym.length;i++){
                sym[i].accept(this);
            }
            out.write("</Rule>\n");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
    public void visit(Graphic gr){
        try{
            out.write("<Graphic>\n");
            out.write("<geometryproperty>"+gr.getGeometryPropertyName()+"</geometryproperty>\n");
            out.write("<Size>");
            System.out.println("size "+gr.getSize());
            encode(gr.getSize());
            out.write("</Size>");
            
            out.write("<Opacity>\n");
            System.out.println("opacity "+gr.getOpacity());
            encode(gr.getOpacity());
            out.write("</Opacity>\n");
            out.write("<Rotation>");
            encode(gr.getRotation());
            out.write("</Rotation>");
            Symbol[] symbols = gr.getSymbols();
            for(int i=0;i<symbols.length;i++){
                symbols[i].accept(this);
            }
            out.write("</Graphic>\n");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
    public void visit(PointSymbolizer sym){
        try{
            out.write("<PointSymbolizer>\n");
            out.write("<Geometry>\n\t<ogc:PropertyName>"+sym.geometryPropertyName()+
            "</ogc:PropertyName>\n</Geometry>\n");
            sym.getGraphic().accept(this);
            out.write("</PointSymbolizer>\n");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    public void visit(LineSymbolizer sym){
        try{
            out.write("<LineSymbolizer>\n");
            out.write("<Geometry>\n\t<ogc:PropertyName>"+sym.geometryPropertyName()+
            "</ogc:PropertyName>\n</Geometry>\n");
            sym.getStroke().accept(this);
            out.write("</LineSymbolizer>\n");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
    public void visit(Symbolizer sym) {
        throw new IllegalStateException("visiting an unknown symbolizer");
    }
    
    
    
    public void visit(Fill fill){
        try{
            out.write("<fill>\n");
            if(fill.getGraphicFill()!=null)fill.getGraphicFill().accept(this);
            encodeCssParam("fill",fill.getColor());
            encodeCssParam("fill-opacity",fill.getOpacity());
            out.write("</fill>\n");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
    public void visit(Stroke stroke){
        try{
            out.write("<stroke>\n");
            if(stroke.getGraphicFill()!=null)stroke.getGraphicFill().accept(this);
            if(stroke.getGraphicStroke()!=null)stroke.getGraphicStroke().accept(this);
            
            encodeCssParam("stroke",stroke.getColor());
            encodeCssParam("stroke-linecap",stroke.getLineCap());
            encodeCssParam("stroke-linejoin",stroke.getLineJoin());
            encodeCssParam("stroke-opacity",stroke.getOpacity());
            encodeCssParam("stroke-width",stroke.getWidth());
            encodeCssParam("stroke-dashoffset",stroke.getDashOffset());
            float[] dash = stroke.getDashArray();
            StringBuffer sb = new StringBuffer();
            for(int i=0;i<dash.length;i++){
                sb.append(dash[i]+" ");
            }
            out.write("<CssParameter name='stroke-dasharray'>"+sb.toString()+"</CssParameter>\n");
            out.write("</stroke>\n");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
    public void visit(TextSymbolizer sym){
        try{
            out.write("<TextSymbolizer>\n");
            out.write("<Geometry>\n\t<ogc:PropertyName>"+sym.getGeometryPropertyName()+
            "</ogc:PropertyName>\n</Geometry>\n");
            out.write("<Label>\n");
            encode(sym.getLabel());
            out.write("</Label>\n");
            out.write("<Font>\n");
            Font[] fonts = sym.getFonts();
            for(int i=0;i<fonts.length;i++){
                encodeCssParam("font-family",fonts[i].getFontFamily());
            }
            encodeCssParam("font-size",fonts[0].getFontSize());
            encodeCssParam("font-style",fonts[0].getFontStyle());
            encodeCssParam("font-weight",fonts[0].getFontWeight());
            out.write("</Font>\n");
            out.write("<Label>\n");
            sym.getLabelPlacement().accept(this);
            out.write("</Label>\n");
            sym.getHalo().accept(this);
            sym.getFill().accept(this);
            out.write("<TextSymbolizer>\n");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
    public void visit(PolygonSymbolizer sym){
        try{
            out.write("<PolygonSymbolizer>\n");
            out.write("<Geometry>\n\t<ogc:PropertyName>"+sym.geometryPropertyName()+
            "</ogc:PropertyName>\n</Geometry>\n");
            if(sym.getFill()!=null)sym.getFill().accept(this);
            if(sym.getStroke()!=null)sym.getStroke().accept(this);
            out.write("</PolygonSymbolizer>");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
    public void visit(Mark mark){
        try{
            out.write("<mark>\n");
            out.write("<WellKnownName>\n");
            mark.getWellKnownName().accept(filterEncoder);
            out.write("</WellKnownName>\n");
            if(mark.getFill()!=null) mark.getFill().accept(this);
            if(mark.getStroke()!=null) mark.getStroke().accept(this);
            out.write("</mark>\n");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
    public void visit(ExternalGraphic exgr){
        try{
            out.write("<ExternalGraphic>\n");
            out.write("<Format>"+exgr.getFormat()+"</Format>\n");
            out.write("<OnlineResource xmlns:xlink='http://www.w3.org/1999/xlink"
            +"xlink:type='simple' xlink='");
            out.write(exgr.getLocation().toString()+"'/>");
            out.write("</ExternalGraphic>\n");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
    public void visit(PointPlacement pp){
        try{
            out.write("<LabelPlacement>\n");
            out.write("<PointPlacement>\n");
            pp.getAnchorPoint().accept(this);
            pp.getDisplacement().accept(this);
            out.write("<Rotation>\n");
            encode(pp.getRotation());
            out.write("</Rotation>\n");
            out.write("</PointPlacement>\n");
            out.write("</LabelPlacement>\n");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
    public void visit(AnchorPoint ap){
        try{
            out.write("<AnchorPoint>\n");
            out.write("<AnchorPointX>\n");
            encode(ap.getAnchorPointX());
            out.write("</AnchorPointX>\n");
            out.write("<AnchorPointY>\n");
            encode(ap.getAnchorPointY());
            out.write("</AnchorPointY>\n");
            out.write("</AnchorPoint>\n");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
    public void visit(Displacement dis){
        try{
            out.write("<Displacement>\n");
            out.write("<DisplacementX>\n");
            encode(dis.getDisplacementX());
            out.write("</DisplacementX>\n");
            out.write("<DisplacementY>\n");
            encode(dis.getDisplacementY());
            out.write("</DisplacementY>\n");
            out.write("</Displacement>\n");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    public void visit(LinePlacement lp){
        try{
            out.write("<LabelPlacement>\n");
            out.write("<LinePlacement>\n");
            out.write("<PerpendicularOffset>\n");
            encode(lp.getPerpendicularOffset());
            out.write("</PerpendicularOffset>\n");
            out.write("</LinePlacement>\n");
            out.write("</LabelPlacement>\n");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
    public void visit(Halo halo){
        try{
            out.write("<Halo>\n");
            out.write("<Radius>\n");
            halo.getFill().accept(this);
            out.write("</Radius>\n");
            out.write("</Halo>\n");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
}
