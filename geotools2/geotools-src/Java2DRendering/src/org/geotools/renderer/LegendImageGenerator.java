/*
 * LegendImageGenerator.java
 *
 * Created on 18 June 2003, 12:00
 */

package org.geotools.renderer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.logging.Logger;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.FilterFactory;
import org.geotools.renderer.Java2DRenderer;
import org.geotools.renderer.RenderedObject;

import org.geotools.renderer.RenderedPoint;
import org.geotools.renderer.Renderer;

import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;

/**
 * Based on a Style object return an Image of a legend for the style.
 *
 * @author  iant
 */
public class LegendImageGenerator {
    
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.wmsserver");
    
    /** The style to use **/
    Style[] styles;
    /** the current renderer object **/
    Java2DRenderer renderer = new Java2DRenderer();
    /** the width of the returned image **/
    int width = 100;
    /** The hieght of the returned image **/
    int height = 100;
    int hpadding = 3;
    int vpadding = 3;
    int symbolWidth = 20;
    int symbolHeight = 20;
    double scale = 1.0;
    static StyleFactory sFac= StyleFactory.createStyleFactory();
    static FilterFactory filFac = FilterFactory.createFilterFactory();
    GeometryFactory gFac = new GeometryFactory();
//    FeatureFactory fFac,labelFac;
    FeatureType fFac,labelFac;
    /** Creates a new instance of LegendImageGenerator */
    public LegendImageGenerator() {
//        FeatureType type = FeatureTypeFactory.newFeatureType(new FeatureType[] {(new AttributeTypeDefault("testGeometry",Geometry.class)},"legend"); 
//        fFac = org.geotools.feature.FeatureFactoryFinder.getFeatureFactory(type);
//        AttributeTypeDefault[] attribs = {
//            new AttributeTypeDefault("geometry:text",Geometry.class),
//            new AttributeTypeDefault("label",String.class)
//        };
//        try{
//        FeatureType labeltype = new FeatureTypeFlat(attribs);
//        labelFac = org.geotools.feature.FeatureFactoryFinder.getFeatureFactory(labeltype);
//        }catch (SchemaException se){
//            throw new RuntimeException(se);
//        }
        
        AttributeType[] attribs = {
            AttributeTypeFactory.newAttributeType("geometry:text",Geometry.class),
            AttributeTypeFactory.newAttributeType("label",String.class)
        };
        try{
        fFac = FeatureTypeFactory.newFeatureType(
          new AttributeType[] {AttributeTypeFactory.newAttributeType("testGeometry",Geometry.class)},"legend"
        ); 
        labelFac = FeatureTypeFactory.newFeatureType(attribs,"attribs");
        }catch (SchemaException se){
            throw new RuntimeException(se);
        }
        
    }
    /** Creates a new instance of LegendImageGenerator */
    public LegendImageGenerator(Style style, int width, int height) {
        this();
        setStyles(new Style[]{style});
        setWidth(width);
        setHeight(height);
    }
    
    public LegendImageGenerator(Style[] styles, int width, int height) {
        this();
        setStyles(styles);
        setWidth(width);
        setHeight(height);
    }
    public BufferedImage getLegend(){
        return getLegend(null);
    }
    public BufferedImage getLegend(Color background){
        BufferedImage image = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_RGB);
        Color fgcolor=Color.black;
        Graphics2D graphics = image.createGraphics();
        LOGGER.fine("bgcolor "+background);
        if(!(background==null)){
            graphics.setColor(background);
            graphics.fillRect(0,0, getWidth(),getHeight());
            int red1 = Color.LIGHT_GRAY.getRed();
            int blue1 = Color.LIGHT_GRAY.getBlue();
            int green1 = Color.LIGHT_GRAY.getGreen();
            
            int red2 = background.getRed();
            int blue2 = background.getBlue();
            int green2 = background.getGreen();
            
            if(red2<red1||blue2<blue1||green2<green1){
                fgcolor = Color.white;
            }
                       
        }
        renderer.setScaleDenominator(getScale());
        renderer.render(graphics, new java.awt.Rectangle(0,0,getWidth(),getHeight()));
        
        RenderedPoint rp=null;
        PointSymbolizer ps;
        Point p;
        Polygon poly;
        Feature feature=null, labelFeature=null;
        TextSymbolizer textSym = sFac.getDefaultTextSymbolizer();
        textSym.getFonts()[0].setFontSize(filFac.createLiteralExpression(12.0));
        String colorCode = "#" + Integer.toHexString(fgcolor.getRed()) +
            Integer.toHexString(fgcolor.getGreen()) +
            Integer.toHexString(fgcolor.getBlue());

        textSym.setFill(sFac.createFill(filFac.createLiteralExpression(colorCode)));
        int offset = vpadding;
        int items=0;
        int hstep = (getWidth() - 2* hpadding)/2;
        
        Set rendered = new LinkedHashSet();
        
        for(int s=0;s<styles.length;s++){
            FeatureTypeStyle[] fts = styles[s].getFeatureTypeStyles();

            for(int i=0;i<fts.length;i++){
                Rule[] rules = fts[i].getRules();

                for(int j=0;j<rules.length;j++){
                    String name = rules[j].getTitle();
                    if((name==null||name.equalsIgnoreCase("title"))&&rules[j].getFilter()!=null){
                        name = rules[j].getFilter().toString();
                    }
                    if(name.equalsIgnoreCase("title")){
                        continue;
                    }
                    Graphic[] g = rules[j].getLegendGraphic();

                    if(g != null && g.length>0){
                        //System.out.println("got a legend graphic");
                        for(int k=0;k<g.length;k++){
                            p = gFac.createPoint(new Coordinate(hpadding+symbolWidth/2,offset+symbolHeight/2));
                            Object[] attrib = {p};
                            try{
                                feature = fFac.create(attrib);
                            }catch (IllegalAttributeException ife){
                                throw new RuntimeException(ife);
                            }
                            ps = sFac.createPointSymbolizer();
                            ps.setGraphic(g[k]);
                            rp = new RenderedPoint(feature, ps);

                            if(rp.isRenderable()) break;
                        }
                        rendered.add(rp);
//                        rp.render(graphics);  
                    }else{ // no legend graphic provided 
                        Symbolizer[] syms = rules[j].getSymbolizers();
                        for(int k=0;k<syms.length;k++){
                            if (syms[k] instanceof PolygonSymbolizer) {
                                //System.out.println("building polygon");
                                Coordinate[] c = new Coordinate[5];
                                c[0] = new Coordinate(hpadding, offset);
                                c[1] = new Coordinate(hpadding+symbolWidth, offset);
                                c[2] = new Coordinate(hpadding+symbolWidth, offset+symbolHeight);
                                c[3] = new Coordinate(hpadding, offset+symbolHeight);
                                c[4] = new Coordinate(hpadding, offset);

                                com.vividsolutions.jts.geom.LinearRing r = null;

                                try {
                                    r = gFac.createLinearRing(c);
                                } catch (com.vividsolutions.jts.geom.TopologyException e) {
                                    System.err.println("Topology Exception in GMLBox");

                                    return null;
                                }

                                poly = gFac.createPolygon(r, null);
                                Object[] attrib = {poly};
                                try{
                                    feature = fFac.create(attrib);
                                }catch (IllegalAttributeException ife){
                                    throw new RuntimeException(ife);
                                }
                                //System.out.println("feature = "+feature);

                            } else if (syms[k] instanceof LineSymbolizer){
                                //System.out.println("building line");
                               Coordinate[] c = new Coordinate[5];
                                c[0] = new Coordinate(hpadding, offset);
                                c[1] = new Coordinate(hpadding+symbolWidth*.3, offset+symbolHeight*.3);
                                c[2] = new Coordinate(hpadding+symbolWidth*.3, offset+symbolHeight*.7);
                                c[3] = new Coordinate(hpadding+symbolWidth*.7, offset+symbolHeight*.7);
                                c[4] = new Coordinate(hpadding+symbolWidth, offset+symbolHeight); 
                                LineString line = gFac.createLineString(c);
                                Object[] attrib = {line};
                                try{
                                    feature = fFac.create(attrib);
                                }catch (IllegalAttributeException ife){
                                    throw new RuntimeException(ife);
                                }
                                //System.out.println("feature = "+feature);

                            } else  if(syms[k] instanceof PointSymbolizer){
                                //System.out.println("building point");
                                p = gFac.createPoint(new Coordinate(hpadding+symbolWidth/2,offset+symbolHeight/2));
                                Object[] attrib = {p};
                                try{
                                    feature = fFac.create(attrib);
                                }catch (IllegalAttributeException ife){
                                    throw new RuntimeException(ife);
                                }
                                System.out.println("feature = "+feature);
                                

                            }
                        }
                        if(feature == null) continue;
                        //System.out.println("feature "+feature);
                        renderer.processSymbolizers(rendered,feature, syms); 
                        
                    }

                if(name==null || name =="") name = "unknown";

                p = gFac.createPoint(new Coordinate(2*hpadding+symbolWidth,offset+symbolHeight/2));
                Object[] attrib = {p,name};
                try{
                    labelFeature = labelFac.create(attrib);
                }catch (IllegalAttributeException ife){
                    throw new RuntimeException(ife);
                }
                textSym.setLabel(filFac.createLiteralExpression(name));
                
                renderer.processSymbolizers(rendered,labelFeature,new Symbolizer[]{textSym}); 
    
                offset += symbolHeight+vpadding;
                }
            }
        }
        Iterator it = rendered.iterator(); 

        while (it.hasNext()) {
          
            RenderedObject r = (RenderedObject) it.next();
//            System.out.println("DRAWING : " + r); 
            r.render(graphics);
        }
        LOGGER.fine("Image = "+image);
        return image;
    }
    /** Getter for property style.
     * @return Value of property style.
     *
     */
    public org.geotools.styling.Style[] getStyle() {
        return styles;
    }
    
    /** Setter for property style.
     * @param style New value of property style.
     *
     */
    public void setStyles(org.geotools.styling.Style[] style) {
        this.styles = style;
    }
    
    /** Getter for property height.
     * @return Value of property height.
     *
     */
    public int getHeight() {
        return height;
    }
    
    /** Setter for property height.
     * @param height New value of property height.
     *
     */
    public void setHeight(int height) {
        this.height = height;
    }
    
    /** Getter for property width.
     * @return Value of property width.
     *
     */
    public int getWidth() {
        return width;
    }
    
    /** Setter for property width.
     * @param width New value of property width.
     *
     */
    public void setWidth(int width) {
        this.width = width;
    }
    
    /** Getter for property hpadding.
     * @return Value of property hpadding.
     *
     */
    public int getHpadding() {
        return hpadding;
    }
    
    /** Setter for property hpadding.
     * @param hpadding New value of property hpadding.
     *
     */
    public void setHpadding(int hpadding) {
        this.hpadding = hpadding;
    }
    
    /** Getter for property vpadding.
     * @return Value of property vpadding.
     *
     */
    public int getVpadding() {
        return vpadding;
    }
    
    /** Setter for property vpadding.
     * @param vpadding New value of property vpadding.
     *
     */
    public void setVpadding(int vpadding) {
        this.vpadding = vpadding;
    }
    
    /** Getter for property symbolWidth.
     * @return Value of property symbolWidth.
     *
     */
    public int getSymbolWidth() {
        return symbolWidth;
    }
    
    /** Setter for property symbolWidth.
     * @param symbolWidth New value of property symbolWidth.
     *
     */
    public void setSymbolWidth(int symbolWidth) {
        this.symbolWidth = symbolWidth;
    }
    
    /** Getter for property symbolHeight.
     * @return Value of property symbolHeight.
     *
     */
    public int getSymbolHeight() {
        return symbolHeight;
    }
    
    /** Setter for property symbolHeight.
     * @param symbolHeight New value of property symbolHeight.
     *
     */
    public void setSymbolHeight(int symbolHeight) {
        this.symbolHeight = symbolHeight;
    }
    
    /** Getter for property scale.
     * @return Value of property scale.
     *
     */
    public double getScale() {
        return scale;
    }
    
    /** Setter for property scale.
     * @param scale New value of property scale.
     *
     */
    public void setScale(double scale) {
        this.scale = scale;
    }
    
}
