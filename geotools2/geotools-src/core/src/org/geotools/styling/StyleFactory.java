package org.geotools.styling;

import java.net.URL;
import org.geotools.filter.Expression;



public abstract class StyleFactory {
    
    /** creates an instance of a style factory
     * @return an instance of the style factory
     */   
    
    public static StyleFactory createStyleFactory() throws Exception{ //HACK - do this properly
        return (StyleFactory)Class.forName("org.geotools.styling.StyleFactoryImpl").newInstance(); 
    }
    
    public abstract TextSymbolizer createTextSymbolizer(Fill fill, Font[] fonts, Halo halo, Expression label, LabelPlacement labelPlacement, String geometryPropertyName);
    
    public abstract ExternalGraphic createExternalGraphic(URL url, String format);

    public abstract Font createFont();

    public abstract AnchorPoint createAnchorPoint(Expression x, Expression y);

    public abstract Displacement createDisplacement(Expression x, Expression y);

    public abstract LinePlacement createLinePlacement();

    public abstract PointSymbolizer createPointSymbolizer();

    public abstract PointPlacement createPointPlacement();

    public abstract Mark createMark();

    public abstract Mark createMark(Expression wellKnownName, Stroke stroke, Fill fill, Expression size, Expression rotation);

    public abstract Mark createCircleMark();    
    
    public abstract Mark createXMark();

    public abstract Mark createStarMark();

    public abstract Mark createSquareMark();

    public abstract Mark createCrossMark();

    public abstract Mark createTriangleMark();

    public abstract FeatureTypeStyle createFeatureTypeStyle(Rule[] rules);

    public abstract LinePlacement createLinePlacement(Expression offset);

    public abstract PolygonSymbolizer createPolygonSymbolizer();

    public abstract Halo createHalo(Fill fill, Expression radius);
    
    public abstract Halo createHalo();

    public abstract Graphic createGraphic();

    public abstract Fill createFill(Expression color, Expression backgroundColor, Expression opacity, Graphic graphicFill);

    public abstract LineSymbolizer createLineSymbolizer();

    public abstract PointSymbolizer createPointSymbolizer(Graphic graphic, String geometryPropertyName);

    public abstract Style createStyle();

    public abstract Fill createFill();

    public abstract Fill createFill(Expression color, Expression opacity);

    public abstract Fill createFill(Expression color);

    public abstract TextSymbolizer createTextSymbolizer();

    public abstract PointPlacement createPointPlacement(AnchorPoint anchorPoint, Displacement displacement, Expression rotation);

    public abstract Stroke createStroke();
        
    public abstract Stroke createStroke(Expression color, Expression width);

    public abstract Stroke createStroke(Expression color, Expression width, Expression opacity);

    public abstract Stroke createStroke(Expression color, Expression width, Expression opacity, Expression lineJoin, Expression lineCap, float[] dashArray, Expression dashOffset, Graphic graphicFill, Graphic graphicStroke);

    public abstract AnchorPoint createAnchorPoint();

    public abstract Displacement createDisplacement();

    public abstract Rule createRule();

    public abstract LineSymbolizer createLineSymbolizer(Stroke stroke, String geometryPropertyName);

    public abstract FeatureTypeStyle createFeatureTypeStyle();

    public abstract ExternalGraphic createExternalGraphic();

    public abstract Graphic createGraphic(ExternalGraphic[] externalGraphics, Mark[] marks, Symbol[] symbols, Expression opacity, Expression size, Expression rotation);

    public abstract Font createFont(Expression fontFamily, Expression fontStyle, Expression fontWeight, Expression fontSize);

    public abstract PolygonSymbolizer createPolygonSymbolizer(Stroke stroke, Fill fill, String geometryPropertyName);

}
