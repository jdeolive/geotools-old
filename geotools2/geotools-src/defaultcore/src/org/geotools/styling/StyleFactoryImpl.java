/*
 * StyleFactory.java
 *
 * Created on 14 October 2002, 15:50
 */

package org.geotools.styling;


import org.geotools.filter.*;
/**
 * Factory for creating Styles. All style elements are returned as Interfaces from org.geotools.core as opposed
 * to Implementations from org.geotools.defaultcore.
 *
 * @version $Id: StyleFactoryImpl.java,v 1.3 2002/10/23 17:02:57 ianturton Exp $
 * @author  iant
 */
public class StyleFactoryImpl extends StyleFactory {
    
    /** Creates a new instance of StyleFactory */
    protected StyleFactoryImpl() {
    }
    
    public Style createStyle(){
        return new StyleImpl();
    }
    public PointSymbolizer createPointSymbolizer(){
        return new PointSymbolizerImpl();
    }
    
    public PointSymbolizer createPointSymbolizer(Graphic graphic, String geometryPropertyName){
        PointSymbolizer pSymb = new PointSymbolizerImpl();
        pSymb.setGeometryPropertyName(geometryPropertyName);
        pSymb.setGraphic(graphic);
        return pSymb;
    }
    
    public PolygonSymbolizer createPolygonSymbolizer(){
        return new PolygonSymbolizerImpl();
    }
    
    public PolygonSymbolizer createPolygonSymbolizer(Stroke stroke, Fill fill, String geometryPropertyName){
        PolygonSymbolizer pSymb = new PolygonSymbolizerImpl();
        pSymb.setGeometryPropertyName(geometryPropertyName);
        pSymb.setStroke(stroke);
        pSymb.setFill(fill);
        return pSymb;
    }
    public LineSymbolizer createLineSymbolizer(){
        return new LineSymbolizerImpl();
    }
    
    public LineSymbolizer createLineSymbolizer(Stroke stroke, String geometryPropertyName){
        LineSymbolizer lSymb = new LineSymbolizerImpl();
        lSymb.setGeometryPropertyName(geometryPropertyName);
        lSymb.setStroke(stroke);
        
        return lSymb;
    }
    public TextSymbolizer createTextSymbolizer(){
        return new TextSymbolizerImpl();
    }
    
    public TextSymbolizer createTextSymbolizer(Fill fill, Font[] fonts, Halo halo, Expression label, LabelPlacement labelPlacement, String geometryPropertyName){
        TextSymbolizer tSymb = new TextSymbolizerImpl();
        tSymb.setFill(fill);
        tSymb.setFonts(fonts);
        tSymb.setGeometryPropertyName(geometryPropertyName);
        
        tSymb.setHalo(halo);
        tSymb.setLabel(label);
        tSymb.setLabelPlacement(labelPlacement);
        
        return tSymb;
    }
    public FeatureTypeStyle createFeatureTypeStyle(){
        return new FeatureTypeStyleImpl();
    }
    
    public FeatureTypeStyle createFeatureTypeStyle(Rule[] rules){
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
    public Stroke createStroke(Expression color, Expression width){
        return createStroke(color,width,new LiteralExpression(1.0));
    }
    /** A convienice method to make a simple stroke
     * @see org.geotools.stroke
     * @param color the color of the line
     * @param width The width of the line
     * @param opacity The opacity of the line
     * @return The stroke
     */
    public Stroke createStroke(Expression color, Expression width, Expression opacity){
        return createStroke(color, width,opacity,new LiteralExpression("bevel"),
        new LiteralExpression("square"),null,new LiteralExpression(0.0),null,null);
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
    public Stroke createStroke(Expression color, Expression width, Expression opacity, Expression lineJoin,
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
    
    public Stroke createStroke(){
        return new StrokeImpl();
    }
    
    public Fill createFill(Expression color, Expression backgroundColor, Expression opacity, Graphic graphicFill){
        Fill fill = new FillImpl();
        fill.setColor(color);
        fill.setBackgroundColor(backgroundColor);
        fill.setOpacity(opacity);
        fill.setGraphicFill(graphicFill);
        return fill;
    }
    
    public Fill createFill(Expression color, Expression opacity){
        return createFill(color,null,opacity, null);
    }
    
    public Fill createFill(Expression color){
        return createFill(color,null,new LiteralExpression(1.0),null);
    }
    
    
    public Mark createMark(Expression wellKnownName, Stroke stroke, Fill fill, Expression size, Expression rotation){
        Mark mark = new MarkImpl();
        mark.setWellKnownName(wellKnownName);
        mark.setStroke(stroke);
        mark.setFill(fill);
        mark.setSize(size);
        mark.setRotation(rotation);
        return mark;
    }
    
    public Mark getSquareMark(){
        Mark mark = getDefaultMark();
        return mark;
    }
    public Mark getCircleMark(){
        Mark mark = getDefaultMark();
        mark.setWellKnownName(new LiteralExpression("Circle"));
        return mark;
    }
    public Mark getCrossMark(){
        Mark mark = getDefaultMark();
        mark.setWellKnownName(new LiteralExpression("Cross"));
        return mark;
    }
    public Mark getXMark(){
        Mark mark = getDefaultMark();
        mark.setWellKnownName(new LiteralExpression("X"));
        return mark;
    }
    public Mark getTriangleMark(){
        Mark mark = getDefaultMark();
        mark.setWellKnownName(new LiteralExpression("Triangle"));
        return mark;
    }
    public Mark getStarMark(){
        Mark mark = getDefaultMark();
        mark.setWellKnownName(new LiteralExpression("Star"));
        return mark;
    }
    public Mark createMark(){
        Mark mark = new MarkImpl();
        return mark;
    }
    
    public Graphic createGraphic(ExternalGraphic[] externalGraphics, Mark[] marks, Symbol[] symbols,
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
    
    
    public ExternalGraphic createExternalGraphic(String uri, String format){
        ExternalGraphic eg = new ExternalGraphicImpl();
        eg.setURI(uri);
        eg.setFormat(format);
        return eg;
    }
    public ExternalGraphic createExternalGraphic(java.net.URL url, String format){
        ExternalGraphic eg = new ExternalGraphicImpl();
        eg.setLocation(url);
        eg.setFormat(format);
        return eg;
    }
    
    
    public Font createFont(Expression fontFamily, Expression fontStyle, Expression fontWeight, Expression fontSize){
        Font font = new FontImpl();
        font.setFontFamily(fontFamily);
        font.setFontSize(fontSize);
        font.setFontStyle(fontStyle);
        font.setFontWeight(fontWeight);
        return font;
    }
    
    public LinePlacement createLinePlacement(){
        return new LinePlacementImpl();
    }
    
    public LinePlacement createLinePlacement(Expression offset){
        LinePlacement lp = new LinePlacementImpl();
        lp.setPerpendicularOffset(offset);
        return lp;
    }
    
    public PointPlacement createPointPlacement(){
        return new PointPlacementImpl();
    }
    
    public PointPlacement createPointPlacement(AnchorPoint anchorPoint, Displacement displacement, Expression rotation){
        PointPlacement pp = new PointPlacementImpl();
        pp.setAnchorPoint(anchorPoint);
        pp.setDisplacement(displacement);
        pp.setRotation(rotation);
        return pp;
    }
    
    public AnchorPoint createAnchorPoint(Expression x, Expression y){
        AnchorPoint anchorPoint = new AnchorPointImpl();
        anchorPoint.setAnchorPointX(x);
        anchorPoint.setAnchorPointY(y);
        return anchorPoint;
    }
    
    public Displacement createDisplacement(Expression x, Expression y){
        Displacement displacement = new DisplacementImpl();
        displacement.setDisplacementX(x);
        displacement.setDisplacementY(y);
        return displacement;
    }
    
    public Halo createHalo(Fill fill, Expression radius){
        Halo halo = new HaloImpl();
        halo.setFill(fill);
        halo.setRadius(radius);
        return halo;
    }
    
    public Fill getDefaultFill() {
        Fill fill = new FillImpl();
        try {
            fill.setColor(new org.geotools.filter.LiteralExpression("#808080"));
            fill.setOpacity(new org.geotools.filter.LiteralExpression(new Double(1.0)));
        } catch (org.geotools.filter.IllegalFilterException ife){
            severe("getDefaultFill", "Failed to build default fill:",ife);
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
        return createPolygonSymbolizer(getDefaultStroke(), getDefaultFill(), "geometry:polygon");
    }
    
    public Stroke getDefaultStroke() {
        try{
            Stroke stroke = createStroke(new LiteralExpression("#000000"), new LiteralExpression(new Integer(1)));
            
            stroke.setDashOffset(new LiteralExpression(new Integer(0)));
            stroke.setLineCap(new LiteralExpression("butt"));
            stroke.setLineJoin(new LiteralExpression("miter"));
            stroke.setOpacity(new LiteralExpression(new Integer(1)));
            
            return stroke;
        } catch (IllegalFilterException ife){
            //we should never be in here
            severe("getDefaultStroke", "DefaultStroke constructor failed ", ife);
            
            return null;
        }
    }
    
    public Style getDefaultStyle() {
        Style style = createStyle();
        
        return style;
    }
    
    public TextSymbolizer getDefaultTextSymbolizer() {
        return createTextSymbolizer(getDefaultFill(), new Font[]{getDefaultFont()}, null, null, null, "gemoerty:text");
    }
    
    public Font getDefaultFont(){
        Font font = new FontImpl();
        try {
            font.setFontSize(new org.geotools.filter.LiteralExpression(new Integer(10)));
            font.setFontStyle(new org.geotools.filter.LiteralExpression("normal"));
            font.setFontWeight(new org.geotools.filter.LiteralExpression("normal"));
            font.setFontFamily(new org.geotools.filter.LiteralExpression("Courier"));
        } catch (org.geotools.filter.IllegalFilterException ife){
            severe("getDefaultFont","Failed to build defaultFont:", ife);
        }
        return font;
    }
    
    public Graphic getDefaultGraphic(){
        Graphic gr = new GraphicImpl();
        try {
            gr.setSize(new LiteralExpression(new Integer(6)));
            gr.setOpacity(new LiteralExpression(new Double(1.0)));
            gr.setRotation(new LiteralExpression(new Double(0.0)));
        } catch (IllegalFilterException ife){
            severe("getDefaultGraphic", "Failed to build default graphic", ife);
        }
        
        return gr;
    }
}
