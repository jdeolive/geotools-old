/*
 * StyleFactory.java
 *
 * Created on 14 October 2002, 15:50
 */

package org.geotools.styling;

import org.geotools.styling.*;
import org.geotools.filter.*;
/**
 * Factory for creating Styles. All style elements are returned as Interfaces from org.geotools.core as opposed 
 * to Implementations from org.geotools.defaultcore.
 *
 * @version $Id: StyleFactory.java,v 1.2 2002/10/16 16:57:54 ianturton Exp $
 * @author  iant
 */
public class StyleFactory {
    
    /** Creates a new instance of StyleFactory */
    private StyleFactory() {
    }
    
    /** creates an instance of a style factory
     * @return an instance of the style factory
     */    
    public static StyleFactory createStyleFactory(){
        StyleFactory sf = new StyleFactory();
        return sf;
    }
    
    /** reads an SLD Style from the named file
     * @param filename the name of the file to be read
     * @return the style
     */    
    public Style readXML(String filename){
        SLDStyle style = new SLDStyle(filename);
        return style;
    }
    
    /** reads an SLD Style from the file
     * @param file the file to read
     * @return the style
     */    
    public Style readXML(java.io.File file){
        SLDStyle style = new SLDStyle(file);
        return style;
    }
    
    /** reads an SLD Style from the url
     * @param url The url to read the style from
     * @return The style
     */    
    public Style readXML(java.net.URL url){
        SLDStyle style = new SLDStyle(url);
        return style;
    }
    
    /** reads an SLD Style from the inputstream
     * @param in The inputstream to be read
     * @return The style
     */    
    public Style readXML(java.io.InputStream in){
        SLDStyle style = new SLDStyle(in);
        return style;
    }
    
    public Style createStyle(){
        return new StyleImpl();
    }
    public  PointSymbolizer createPointSymbolizer(){
        return new PointSymbolizerImpl();
    }
    
    public  PointSymbolizer createPointSymbolizer(Graphic graphic, String geometryPropertyName){
        PointSymbolizer pSymb = new PointSymbolizerImpl();
        pSymb.setGeometryPropertyName(geometryPropertyName);
        pSymb.setGraphic(graphic);
        return pSymb;
    }
    
    public static PolygonSymbolizer createPolygonSymbolizer(){
        return new PolygonSymbolizerImpl();
    }
    
    public  PolygonSymbolizer createPolygonSymbolizer(Stroke stroke, Fill fill, String geometryPropertyName){
        PolygonSymbolizer pSymb = new PolygonSymbolizerImpl();
        pSymb.setGeometryPropertyName(geometryPropertyName);
        pSymb.setStroke(stroke);
        pSymb.setFill(fill);
        return pSymb;
    }
    public  LineSymbolizer createLineSymbolizer(){
        return new LineSymbolizerImpl();
    }
    
    public  LineSymbolizer createLineSymbolizer(Stroke stroke, String geometryPropertyName){
        LineSymbolizer lSymb = new LineSymbolizerImpl();
        lSymb.setGeometryPropertyName(geometryPropertyName);
        lSymb.setStroke(stroke);
        
        return lSymb;
    }
    public  TextSymbolizer createTextSymbolizer(){
        return new TextSymbolizerImpl();
    }
    
    public  TextSymbolizer createTextSymbolizer(Fill fill, Font[] fonts, Halo halo, Expression label, LabelPlacement labelPlacement, String geometryPropertyName){
        TextSymbolizer tSymb = new TextSymbolizerImpl();
        tSymb.setFill(fill);
        tSymb.setFonts(fonts);
        tSymb.setGeometryPropertyName(geometryPropertyName);
        
        tSymb.setHalo(halo);
        tSymb.setLabel(label);
        tSymb.setLabelPlacement(labelPlacement);
        
        return tSymb;
    }
    public  FeatureTypeStyle createFeatureTypeStyle(){
        return new FeatureTypeStyleImpl();
    }
    
    public static FeatureTypeStyle createFeatureTypeStyle(Rule[] rules){
        return new FeatureTypeStyleImpl(rules);
    }
    
    public Rule createRule(){
        return new RuleImpl();
    }
    /** A convienice method to make a simple stroke
     * @see org.geotools.stroke
     * @param color the color of the line
     * @param width the width of the line
     * @return the stroke object
     */    
    public static Stroke createStroke(Expression color, Expression width){
        return createStroke(color,width,new ExpressionLiteral(1.0));
    }
    /** A convienice method to make a simple stroke
     * @see org.geotools.stroke
     * @param color the color of the line
     * @param width The width of the line
     * @param opacity The opacity of the line
     * @return The stroke
     */    
    public static Stroke createStroke(Expression color, Expression width, Expression opacity){
        return createStroke(color, width,opacity,new ExpressionLiteral("bevel"),
            new ExpressionLiteral("square"),null,new ExpressionLiteral(0.0),null,null);
    }
    
    /** creates a stroke 
     * @see org.geotools.stroke
     * @param color The color of the line
     * @param width The width of the line
     * @param opacity The opacity of the line
     * @param lineJoin - the type of Line joint
     * @param lineCap - the type of line cap
     * @param dashArray - an array of floats describing the dashes in the line
     * @param dashOffset - where in the dash array to start drawing from
     * @param graphicFill - a graphic object to fill the line with
     * @param graphicStroke - a graphic object to draw the line with
     * @return The completed stroke.
     */
    public static Stroke createStroke(Expression color, Expression width, Expression opacity, Expression lineJoin,
        Expression lineCap, float[] dashArray, Expression dashOffset, Graphic graphicFill, Graphic graphicStroke){
            Stroke stroke = new StrokeImpl();
            stroke.setColor(color);
            stroke.setWidth(width);
            stroke.setOpacity(opacity);
            stroke.setLineJoin(lineJoin);
            stroke.setLineCap(lineCap);
            stroke.setDashArray(dashArray);
            stroke.setDashOffset(dashOffset);
            stroke.setGraphicFill(graphicFill);
            stroke.setGraphicStroke(graphicStroke);
            return stroke;
    }
    
    public static Stroke createStroke(){
        return new StrokeImpl();
    }
    
    public static Fill createFill(Expression color, Expression backgroundColor, Expression opacity, Graphic graphicFill){
        Fill fill = new FillImpl();
        fill.setColor(color);
        fill.setBackgroundColor(backgroundColor);
        fill.setOpacity(opacity);
        fill.setGraphicFill(graphicFill);
        return fill;
    }
    
    public static Fill createFill(Expression color, Expression opacity){
        return createFill(color,null,opacity, null);
    }
    
    public static Fill createFill(Expression color){
        return createFill(color,null,new ExpressionLiteral(1.0),null);
    }
    
    public static Fill createFill(){
        return new FillImpl();
    }
    
    public static Mark createMark(Expression wellKnownName, Stroke stroke, Fill fill, Expression size, Expression rotation){
        Mark mark = new MarkImpl();
        mark.setWellKnownName(wellKnownName);
        mark.setStroke(stroke);
        mark.setFill(fill);
        mark.setSize(size);
        mark.setRotation(rotation);
        return mark;
    }
    
    public static Mark createSquareMark(){
        Mark mark = new MarkImpl();
        mark.setWellKnownName(new ExpressionLiteral("Square"));
        return mark;
    }
    public static Mark createCircleMark(){
        Mark mark = new MarkImpl();
        mark.setWellKnownName(new ExpressionLiteral("Circle"));
        return mark;
    }    
    public static Mark createCrossMark(){
        Mark mark = new MarkImpl();
        mark.setWellKnownName(new ExpressionLiteral("Cross"));
        return mark;
    }
    public static Mark createXMark(){
        Mark mark = new MarkImpl();
        mark.setWellKnownName(new ExpressionLiteral("X"));
        return mark;
    }
    public static Mark createTriangleMark(){
        Mark mark = new MarkImpl();
        mark.setWellKnownName(new ExpressionLiteral("Triangle"));
        return mark;
    }
    public static Mark createStarMark(){
        Mark mark = new MarkImpl();
        mark.setWellKnownName(new ExpressionLiteral("Star"));
        return mark;
    }
    public static Mark createMark(){
        Mark mark = new MarkImpl();
        return mark;
    }
    
    public static Graphic createGraphic(ExternalGraphic[] externalGraphics, Mark[] marks, Symbol[] symbols, 
        Expression opacity, Expression size, Expression rotation){
            Graphic graphic = new GraphicImpl();
            graphic.setExternalGraphics(externalGraphics);
            graphic.setMarks(marks);
            graphic.setSymbols(symbols);
            graphic.setOpacity(opacity);
            graphic.setSize(size);
            graphic.setRotation(rotation);
            return graphic;
    }
    public static Graphic createGraphic(){
        return new GraphicImpl();
    }
    
    public static Font createFont(){
        return new FontImpl();
    }
    
    public static Font createFont(Expression fontFamily, Expression fontStyle, Expression fontWeight, Expression fontSize){
        Font font = new FontImpl();
        font.setFontFamily(fontFamily);
        font.setFontSize(fontSize);
        font.setFontStyle(fontStyle);
        font.setFontWeight(fontWeight);
        return font;
    }
    
    public static PointPlacement createPointPlacement(){
        return new PointPlacementImpl();
    }
    
    public static PointPlacement createPointPlacement(AnchorPoint anchorPoint, Displacement displacement, Expression rotation){
        PointPlacement pp = new PointPlacementImpl();
        pp.setAnchorPoint(anchorPoint);
        pp.setDisplacement(displacement);
        pp.setRotation(rotation);
        return pp;
    }
}
