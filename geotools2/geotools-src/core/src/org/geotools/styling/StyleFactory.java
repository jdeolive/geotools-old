package org.geotools.styling;

import java.net.URL;
import org.geotools.filter.Expression;
// J2SE dependencies
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;


public abstract class StyleFactory {
    /**
     * The logger 
     */
    protected static final Logger LOGGER = Logger.getLogger("org.geotools.styling"); 
    private static StyleFactory factory = null;
    /** creates an instance of a style factory
     * @return an instance of the style factory
     */   
    
    public static StyleFactory createStyleFactory(){ // throws StyleFactoryCreationException{ 
        if(factory != null ){
            return factory;
        }
        String factoryClass = System.getProperty("StyleFactoryImpl");
        LOGGER.fine("loaded property = " + factoryClass);
        StyleFactory sf = null;
        if(factoryClass != null && factoryClass != ""){
            //try{
                sf = createStyleFactory(factoryClass); 
//            } catch (StyleFactoryCreationException e){
//                // do nothing yet or should we give up now
//                LOGGER.info("Failed to create " + factoryClass + " because \n" + e);
//            }
        }else{
            sf = createStyleFactory("org.geotools.styling.StyleFactoryImpl");
        }
        factory = sf;
        return sf;
    }
    
    public static StyleFactory createStyleFactory(String factoryClass) { //throws StyleFactoryCreationException{
        try{
            return factory = (StyleFactory)Class.forName(factoryClass).newInstance();
        } catch (ClassNotFoundException cnfe){
            severe("createStyleFactory", "failed to find implementation " + factoryClass, cnfe);
            //throw new StyleFactoryCreationException("Failed to find implementation " + factoryClass, cnfe); 
        } catch (InstantiationException ie){
            severe("createStyleFactory", "failed to insantiate implementation " + factoryClass, ie);
            //throw new StyleFactoryCreationException("Failed to insantiate implementation " + factoryClass, ie);
        } catch (IllegalAccessException iae){
            severe("createStyleFactory", "failed to access implementation " + factoryClass, iae);
            //throw new StyleFactoryCreationException("Failed to access implementation " + factoryClass, iae);
        }
        return null;
    }
        
    public abstract TextSymbolizer createTextSymbolizer(Fill fill, Font[] fonts, Halo halo, Expression label, LabelPlacement labelPlacement, String geometryPropertyName);
    
    public abstract ExternalGraphic createExternalGraphic(URL url, String format);
    
    public abstract ExternalGraphic createExternalGraphic(String uri, String format);

    public abstract AnchorPoint createAnchorPoint(Expression x, Expression y);

    public abstract Displacement createDisplacement(Expression x, Expression y);

//    public abstract LinePlacement createLinePlacement();

    public abstract PointSymbolizer createPointSymbolizer();

//    public abstract PointPlacement createPointPlacement();

    public abstract Mark createMark(Expression wellKnownName, Stroke stroke, Fill fill, Expression size, Expression rotation);

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

    public abstract Fill createFill(Expression color, Expression backgroundColor, Expression opacity, Graphic graphicFill);

    public abstract LineSymbolizer createLineSymbolizer();

    public abstract PointSymbolizer createPointSymbolizer(Graphic graphic, String geometryPropertyName);

    public abstract Style createStyle();

    public abstract Fill createFill(Expression color, Expression opacity);

    public abstract Fill createFill(Expression color);

    public abstract TextSymbolizer createTextSymbolizer();

    public abstract PointPlacement createPointPlacement(AnchorPoint anchorPoint, Displacement displacement, Expression rotation);

    public abstract Stroke createStroke(Expression color, Expression width);

    public abstract Stroke createStroke(Expression color, Expression width, Expression opacity);

    public abstract Stroke createStroke(Expression color, Expression width, Expression opacity, Expression lineJoin, Expression lineCap, float[] dashArray, Expression dashOffset, Graphic graphicFill, Graphic graphicStroke);

    public abstract Rule createRule();

    public abstract LineSymbolizer createLineSymbolizer(Stroke stroke, String geometryPropertyName);
    
    public abstract FeatureTypeStyle createFeatureTypeStyle();

    public abstract Graphic createGraphic(ExternalGraphic[] externalGraphics, Mark[] marks, Symbol[] symbols, Expression opacity, Expression size, Expression rotation);

    public abstract Font createFont(Expression fontFamily, Expression fontStyle, Expression fontWeight, Expression fontSize);

    public abstract Mark createMark();
    
    public abstract PolygonSymbolizer createPolygonSymbolizer(Stroke stroke, Fill fill, String geometryPropertyName);

    public abstract RasterSymbolizer createRasterSymbolizer(String geometryPropertyName, Expression opacity, ChannelSelection channel, Expression overlap, ColorMap colorMap, ContrastEnhancement ce, ShadedRelief relief, Symbolizer outline);

    public abstract RasterSymbolizer getDefaultRasterSymbolizer();
    
    public abstract ChannelSelection createChannelSelection(SelectedChannelType[] channels);
    
    public abstract SelectedChannelType createSelectedChannelType(String name, Expression enhancement);
    
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
    
    
    
    
    /**
     * Convenience method for logging a message with an exception.
     */
    protected static void severe(final String method, final String message, final Exception exception) {
        final LogRecord record = new LogRecord(Level.SEVERE, message);
        record.setSourceMethodName(method);
        record.setThrown(exception);
        LOGGER.log(record);
    }
}
