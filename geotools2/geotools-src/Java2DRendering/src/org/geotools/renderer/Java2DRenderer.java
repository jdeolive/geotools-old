/**
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.geotools.renderer;

//geotools imports
import org.geotools.feature.*;
import org.geotools.data.*;
import org.geotools.map.Map;
import org.geotools.styling.*;

//Java Topology Suite
import com.vividsolutions.jts.geom.*;

//standard java awt imports
import java.awt.Color;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.*;
import java.awt.Shape;
import java.awt.image.*;
import java.awt.Image;
import java.awt.RenderingHints;

//util imports
import java.util.HashMap;
import java.util.HashSet;

//Logging system
import org.apache.log4j.Logger;

/**
 * @version $Id: Java2DRenderer.java,v 1.36 2002/07/04 16:46:49 ianturton Exp $
 * @author James Macgill
 */
public class Java2DRenderer implements org.geotools.renderer.Renderer {
    
    private static Logger _log = Logger.getLogger(Java2DRenderer.class);
    /**
     * Flag which determines if the renderer is interactive or not
     * an interactive renderer will return rather than waiting for time consuming
     * opperations to complete (e.g. Image Loading)
     * A non-interactive renderer (e.g. a SVG or PDF renderer) will block for these opperations
     */
    private boolean interactive = true;
    /**
     * Flag which controls behaviour for applying affine transformation
     * to the graphics object.  If true then the transform will be concatenated
     * to the existing transform.  If false it will be replaced.
     */
    private boolean concatTransforms = false;
    /**
     * where the centre of an untransormed mark is
     */
    private static com.vividsolutions.jts.geom.Point markCentrePoint;
    
    
    /**
     * Holds a lookup bewteen SLD names and java constants.
     */
    private static final java.util.HashMap joinLookup = new java.util.HashMap();
    /**
     * Holds a lookup bewteen SLD names and java constants.
     */
    private static final java.util.HashMap capLookup = new java.util.HashMap();
    /**
     * Holds a lookup bewteen SLD names and java constants.
     */
    private static final java.util.HashMap fontStyleLookup = new java.util.HashMap();
    /**
     * Holds a list of well-known marks.
     */
    static HashSet wellKnownMarks = new java.util.HashSet();
    static HashSet supportedGraphicFormats = new java.util.HashSet();
    static ImageLoader imageLoader = new ImageLoader();
    static { //static block to populate the lookups
        joinLookup.put("miter", new Integer(BasicStroke.JOIN_MITER));
        joinLookup.put("bevel", new Integer(BasicStroke.JOIN_BEVEL));
        joinLookup.put("round", new Integer(BasicStroke.JOIN_ROUND));
        
        capLookup.put("butt",   new Integer(BasicStroke.CAP_BUTT));
        capLookup.put("round",  new Integer(BasicStroke.CAP_ROUND));
        capLookup.put("square", new Integer(BasicStroke.CAP_SQUARE));
        
        fontStyleLookup.put("normal", new Integer(java.awt.Font.PLAIN));
        fontStyleLookup.put("italic", new Integer(java.awt.Font.ITALIC));
        fontStyleLookup.put("oblique", new Integer(java.awt.Font.ITALIC));
        fontStyleLookup.put("bold", new Integer(java.awt.Font.BOLD));
        /**
         * A list of wellknownshapes that we know about:
         * square, circle, triangle, star, cross, x.
         */
        wellKnownMarks.add("Square");
        wellKnownMarks.add("Triangle");
        wellKnownMarks.add("Cross");
        wellKnownMarks.add("Circle");
        wellKnownMarks.add("Star");
        wellKnownMarks.add("X");
        wellKnownMarks.add("Arrow");
        
        supportedGraphicFormats.add("image/gif");
        supportedGraphicFormats.add("image/jpg");
        supportedGraphicFormats.add("image/png");
        
        Coordinate c = new Coordinate(100,100);
        GeometryFactory fac = new GeometryFactory();
        markCentrePoint = fac.createPoint(c);
    }
    
    /**
     * Graphics object to be rendered to.
     * Controlled by set output.
     */
    private Graphics2D graphics;
    
    /**
     * The size of the output area in output units.
     */
    private Rectangle screenSize;
    
    /**
     * The ratio required to scale the features to be rendered
     * so that they fit into the output space.
     */
    private double scaleDenominator;
    
    /** Creates a new instance of Java2DRenderer */
    public Java2DRenderer() {
    }
    
    /**
     * Sets the flag which controls behaviour for applying affine
     * transformation to the graphics object.
     *
     * @param flag If true then the transform will be concatenated
     * to the existing transform.  If false it will be replaced.
     */
    public void setConcatTransforms(boolean flag){
        concatTransforms = flag;
    }
    
    /**
     * Flag which controls behaviour for applying affine transformation
     * to the graphics object.
     *
     * @return a boolean flag. If true then the transform will be
     * concatenated to the existing transform.  If false it will be replaced.
     */
    public boolean getConcatTransforms(){
        return concatTransforms;
    }
    
    /**
     * Called before {@link render}, this sets where any output will be sent.
     * @param g A graphics object for future rendering to be sent to.  Note:
     *        must be an instance of Graphics2D.
     * @param bounds The size of the output area, required so that scale can be
     *        calculated.
     */
    public void setOutput(Graphics g,Rectangle bounds){
        graphics = (Graphics2D)g;
        screenSize = bounds;
    }
    
    /**
     * Performs the actual rendering process to the graphics context set in
     * setOutput.<p>
     * The style parameter controls the appearance features.  Rules
     * within the style object may cause some features to be rendered
     * multiple times or not at all.
     *
     * @param features An array of features to be rendered.
     * @param map Controls the full extent of the input space.  Used in the
     *        calculation of scale.
     * @param s A style object.  Contains a set of FeatureTypeStylers that are
     *        to be applied in order to control the rendering process.
     */
    public void render(Feature features[], Envelope map,Style s){
        if(graphics==null) return;
        _log.info("renderering "+features.length+" features");
        
        //set up the affine transform and calculate scale values
        AffineTransform at = new AffineTransform();
        
        double scale = Math.min(screenSize.getHeight()/map.getHeight(),
        screenSize.getWidth()/map.getWidth());
        //TODO: angle is almost certainly not needed and should be dropped
        double angle = 0;//-Math.PI/8d;// rotation angle
        double tx = -map.getMinX()*scale; // x translation - mod by ian
        double ty = map.getMinY()*scale + screenSize.getHeight();// y translation
        
        double sc = scale*Math.cos(angle);
        double ss = scale*Math.sin(angle);
        
        at = new AffineTransform(sc,-ss,ss,-sc,tx,ty);
        
        /* If we are rendering to a component which has already set up some form
         * of transformation then we can concatenate our transformation to it.
         * An example of this is the ZoomPane component of the swinggui module.*/
        if(concatTransforms){
            graphics.getTransform().concatenate(at);
        }
        else{
            graphics.setTransform(at);
        }
        //extract the feature type stylers from the style object and process them
        FeatureTypeStyle[] featureStylers = s.getFeatureTypeStyles();
        processStylers(features, featureStylers);
    }
    
    /**
     * Applies each feature type styler in turn to all of the features.
     * This perhaps needs some explanation to make it absolutely clear.
     * featureStylers[0] is applied to all features before featureStylers[1]
     * is applied.  This can have important consequences as regards the
     * painting order.<p>
     * In most cases, this is the desired effect.  For example, all line
     * features may be rendered with a fat line and then a thin line.  This
     * produces a 'cased' effect without any strange overlaps.<p>
     * This method is internal and should only be called by render.<p>
     *
     * @param features An array of features to be rendered
     * @param featureStylers An array of feature stylers to be applied
     **/
    private void processStylers(final Feature[] features, final FeatureTypeStyle[] featureStylers) {
        for(int i=0;i<featureStylers.length;i++){
            FeatureTypeStyle fts = featureStylers[i];
            for(int j=0;j<features.length;j++){
                Feature feature = features[j];
                _log.info("feature is "+feature.getSchema().getTypeName()+
                " type styler is "+fts.getFeatureTypeName());
                if(feature.getSchema().getTypeName().equalsIgnoreCase(fts.getFeatureTypeName())){
                    //this styler is for this type of feature
                    //now find which rule applies
                    Rule[] rules = fts.getRules();
                    for(int k=0;k<rules.length;k++){
                        //does this rule apply?
                        //TODO: the rule may be FAR more complex than this and code needs to
                        //TODO: be written to support this, particularly the filtering aspects.
                        if(rules[k].getMinScaleDenominator()<scaleDenominator && rules[k].getMaxScaleDenominator()>scaleDenominator){
                            _log.info("rule passed, moving on to symobolizers");
                            //yes it does
                            //this gives us a list of symbolizers
                            Symbolizer[] symbolizers = rules[k].getSymbolizers();
                            processSymbolizers(feature, symbolizers);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Applies each of a set of symbolizers in turn to a given feature.<p>
     * This is an internal method and should only be called by processStylers.
     *
     * @param feature The feature to be rendered
     * @param symbolizers An array of symbolizers which actually perform the
     * rendering.
     */
    private void processSymbolizers(final Feature feature, final Symbolizer[] symbolizers) {
        for(int m =0;m<symbolizers.length;m++){
            _log.info("applying symbolizer "+symbolizers[m]);
            if (symbolizers[m] instanceof PolygonSymbolizer){
                renderPolygon(feature,(PolygonSymbolizer)symbolizers[m]);
            }
            else if(symbolizers[m] instanceof LineSymbolizer){
                renderLine(feature,(LineSymbolizer)symbolizers[m]);
            }else if(symbolizers[m] instanceof PointSymbolizer){
                renderPoint(feature,(PointSymbolizer)symbolizers[m]);
            }else if(symbolizers[m] instanceof TextSymbolizer){
                renderText(feature,(TextSymbolizer)symbolizers[m]);
            }
            //else if...
            //TODO: support other symbolizers
        }
    }
    
    /**
     * Renders the given feature as a polygon using the specified symbolizer.
     * Geometry types other than inherently area types can be used.
     * If a line is used then the line string is closed for filling (only)
     * by connecting its end point to its start point.
     * This is an internal method that should only be called by
     * processSymbolizers.
     *
     * TODO: the properties of a symbolizer may, in part, be dependent on
     * TODO: attributes of the feature.  This is not yet supported.
     *
     * @param feature The feature to render
     * @param symbolizer The polygon symbolizer to apply
     **/
    private void renderPolygon(Feature feature, PolygonSymbolizer symbolizer){
        _log.info("rendering polygon with a scale of "+this.scaleDenominator);
        Fill fill = symbolizer.getFill();
        String geomName = symbolizer.geometryPropertyName();
        Geometry geom = findGeometry(feature,geomName);
        
        if(geom.isEmpty()) return;
        
        GeneralPath path = createGeneralPath(geom);
        
        if(fill!=null){
            applyFill(graphics,fill, feature);
            _log.debug("paint in renderPoly: "+graphics.getPaint());
            
            graphics.fill(path);
            // shouldn't we reset the graphics when we return finished?
            resetFill();
        }
        if(symbolizer.getStroke() != null) {
            Stroke stroke = symbolizer.getStroke();
            applyStroke(graphics,stroke, feature);
            _log.debug("path is "+graphics.getTransform().createTransformedShape(path).getBounds2D().toString());
            if(stroke.getGraphicStroke()==null){
                graphics.draw(path);
            }else{
                // set up the graphic stroke
                drawWithGraphicStroke(graphics,path,stroke.getGraphicStroke());
            }
            
        }
    }
    
    /**
     * Renders the given feature as a line using the specified symbolizer.
     *
     * This is an internal method that should only be called by
     * processSymbolizers
     *
     * Geometry types other than inherently linear types can be used.
     * If a point geometry is used, it should be interpreted as a line of zero
     * length and two end caps.  If a polygon is used (or other "area" type)
     * then its closed outline will be used as the line string
     * (with no end caps).
     *
     * TODO: the properties of a symbolizer may, in part, be dependent on
     * TODO: attributes of the feature.  This is not yet supported.
     *
     * @param feature The feature to render
     * @param symbolizer The polygon symbolizer to apply
     **/
    private void renderLine(Feature feature, LineSymbolizer symbolizer){
        if(symbolizer.getStroke()==null) return;
        Stroke stroke = symbolizer.getStroke();
        applyStroke(graphics,stroke,feature);
        String geomName = symbolizer.geometryPropertyName();
        Geometry geom = findGeometry(feature, geomName);
        if(geom.isEmpty()) return;
        GeneralPath path = createGeneralPath(geom);
        if(stroke.getGraphicStroke()==null){
            graphics.draw(path);
        }else{
            // set up the graphic stroke
            drawWithGraphicStroke(graphics,path,stroke.getGraphicStroke());
        }
    }
    
    private void renderPoint(Feature feature, PointSymbolizer symbolizer){
        _log.info("rendering a point from "+feature);
        org.geotools.styling.Graphic sldgraphic = symbolizer.getGraphic();
        _log.debug("sldgraphic = "+sldgraphic);
        
        String geomName =symbolizer.geometryPropertyName();
        Geometry geom = findGeometry(feature, geomName);
        if(geom.isEmpty()){
            _log.debug("empty geometry");
            return;
        }
        // TODO: consider if mark and externalgraphic should share an ancestor?
        if(null != (Object)sldgraphic.getExternalGraphics()){
            _log.debug("rendering External graphic");
            renderExternalGraphic(geom,sldgraphic);
        }else{
            _log.debug("rendering mark");
            renderMark(geom,sldgraphic);
        }
    }
    String[] fontFamilies = null;
    private void renderText(Feature feature, TextSymbolizer symbolizer) {
        try{
            
            _log.debug("rendering text");
            
            String geomName =symbolizer.geometryPropertyName();
            Geometry geom = findGeometry(feature, geomName);
            if(geom.isEmpty()){
                _log.debug("empty geometry");
                return;
            }
            Object obj = symbolizer.getLabel().getValue(feature);
            if(obj == null){
                _log.debug("Null label in render text");
                return;
            }
            String label = obj.toString();
            _log.debug("label is "+label);
            if(label==null) return;
            

            if(fontFamilies == null){
                java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
                fontFamilies = ge.getAvailableFontFamilyNames();
                //_log.debug("there are "+fontFamilies.length+" fonts available");
            }
            
            java.awt.Font javaFont = null ;
            org.geotools.styling.Font[] fonts = symbolizer.getFonts();
            int styleCode = 0;
            int size = 6;
            String requestedFont = "";
            for(int k=0;k<fonts.length;k++){
                requestedFont = fonts[k].getFontFamily().getValue(feature).toString();

                

                for(int i=0;i<fontFamilies.length;i++){
                    //_log.debug(fontFamilies[i]);
                    if(requestedFont.equalsIgnoreCase(fontFamilies[i])){
                        String reqStyle = (String)fonts[k].getFontStyle().getValue(feature);
                        
                        if(fontStyleLookup.containsKey(reqStyle)){
                            styleCode = ((Integer) fontStyleLookup.get(reqStyle)).intValue();
                        }
                        else{
                            styleCode = java.awt.Font.PLAIN;
                        }
                        String reqWeight = (String)fonts[k].getFontWeight().getValue(feature);
                        if(reqWeight.equalsIgnoreCase("Bold")){
                            styleCode = styleCode|java.awt.Font.BOLD;
                        }
                        size = ((Number)fonts[k].getFontSize().getValue(feature)).intValue();
                        _log.debug("requesting "+requestedFont+" "+styleCode+" "+size);
                        javaFont = new java.awt.Font(requestedFont,styleCode,size);
                        break;
                    }
                }
                if(javaFont!=null) break;
            }
            LabelPlacement placement = symbolizer.getLabelPlacement();
            Halo halo = symbolizer.getHalo();
            if(halo!=null){
                
                float radius = ((Number)halo.getRadius().getValue(feature)).floatValue();
                
                applyFill(graphics,halo.getFill(),feature);
                //drawText(graphics,feature,label,placement);
                resetFill();
            }
            if(javaFont!=null){
                graphics.setFont(javaFont);
            }
            applyFill(graphics,symbolizer.getFill(),feature);
            
            drawText(graphics,feature,label,placement);
            resetFill();
            
        }catch (org.geotools.filter.MalformedFilterException mfe){
            _log.fatal("Malformed Filter exception:"+mfe);
            return;
        }
    }
    private void drawText(Graphics2D graphics, Feature feature, String label,LabelPlacement placement){
        try{    
            AffineTransform temp = graphics.getTransform();
            AffineTransform labelAT = new AffineTransform();
            double x=0,y=0,rotation=0;
            double tx=0,ty=0;
            Rectangle2D rect = graphics.getFont().getStringBounds(label,graphics.getFontRenderContext());
            
            if(placement instanceof PointPlacement){
                //HACK: this will fail if the geometry of the feature isn't a point
                _log.debug("setting pointPlacement");
                tx = ((Point)feature.getDefaultGeometry()).getX();
                ty = ((Point)feature.getDefaultGeometry()).getY();
                PointPlacement p = (PointPlacement)placement;
                x = ((Number)p.getAnchorPoint().getAnchorPointX().getValue(feature)).doubleValue()*rect.getWidth();
                y = ((Number)p.getAnchorPoint().getAnchorPointY().getValue(feature)).doubleValue()*rect.getHeight();
                x += ((Number)p.getDisplacement().getDisplacementX().getValue(feature)).doubleValue();
                y += ((Number)p.getDisplacement().getDisplacementY().getValue(feature)).doubleValue();
                rotation = ((Number)p.getRotation().getValue(feature)).doubleValue();
                rotation*=Math.PI/180.0;
            }else if(placement instanceof LinePlacement){
                _log.debug("setting line placement");
                //HACK: this will fail if the geometry of the feature is not a linestring
                double offset = ((Number)((LinePlacement)placement).getPerpendicularOffset().getValue(feature)).doubleValue();
                LineString line = ((LineString)feature.getDefaultGeometry());
                Point start = line.getStartPoint();
                Point end = line.getEndPoint();
                double dx = end.getX()-start.getX();
                double dy = end.getY() - start.getY();
                rotation = Math.atan2(dx,dy)-Math.PI/2.0;
                tx = dx/2.0 + start.getX();
                ty = dy/2.0 + start.getY();
                x = -rect.getWidth()/2.0;
                
                y=0;
                if(offset >= 0.0 ){ // to the left of the line
                    y = -offset;
                }else{
                    y = y = offset+rect.getHeight();
                }
                
                _log.debug("offset = "+offset+" x = "+x+" y "+y);
            }
            
            Point2D mapCentre = new Point2D.Double(tx,ty);
            Point2D graphicCentre = new Point2D.Double();
            temp.transform(mapCentre,graphicCentre);
            labelAT.translate(graphicCentre.getX(),graphicCentre.getY());
            
            _log.debug("rotation "+rotation);
            
            labelAT.rotate(rotation);
            
            //        markAT.scale(drawSize,drawSize);
            graphics.setTransform(labelAT);
            // we move this to the centre of the image.
            _log.debug("about to draw at "+tx+","+ty);
            
            graphics.drawString(label,(float)x,(float)y);
            resetFill();
            graphics.setTransform(temp);
            return;
                    } catch (org.geotools.filter.MalformedFilterException mfe){
            _log.debug("MalformedFilterException in RenderText\n"+mfe);
            
            return;
        }
        
        }
        
        private void renderExternalGraphic(Geometry geom, Graphic graphic){
            BufferedImage img = getExternalGraphic(graphic);
            if(img!=null){
                renderImage((Point)geom,img,(int)graphic.getSize(),graphic.getRotation());
            }else{
                // if we get to here we need to render the marks;
                renderMark(geom,graphic);
            }
            return;

    }
    private BufferedImage getExternalGraphic(Graphic graphic){
        ExternalGraphic[] extgraphics = graphic.getExternalGraphics();
        if(extgraphics != null){
            for(int i=0;i<extgraphics.length;i++){
                ExternalGraphic eg = extgraphics[i];
                _log.info("got a "+eg.getFormat());
                if(supportedGraphicFormats.contains(eg.getFormat().toLowerCase())){
                    if(eg.getFormat().equalsIgnoreCase("image/gif") ||
                    eg.getFormat().equalsIgnoreCase("image/jpg") ||
                    eg.getFormat().equalsIgnoreCase("image/png")){
                        _log.debug("a java supported format");
                        BufferedImage img = imageLoader.get(eg.getLocation(),isInteractive());
                        _log.debug("Image return = "+img);
                        if(img!=null){
                            return img;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    
    private void renderMark(Geometry geom, Graphic graphic){
        int size = 6; // size in pixels
        double rotation = 0.0; // rotation in degrees
        Mark mark = getMark(graphic);
        size = (int)graphic.getSize();
        rotation = graphic.getRotation()*Math.PI/180d;
        fillDrawMark(graphics,(Point)geom,mark,size,rotation);
    }
    private Mark getMark(Graphic graphic){
        Mark marks[] = graphic.getMarks();
        Mark mark;
        
        if(marks == null || marks.length==0){
            _log.debug("choosing a default mark as no marks returned");
            mark = new DefaultMark();
            return mark;
        }
        
        for(int i = 0; i<marks.length; i++){
            if(wellKnownMarks.contains(marks[i].getWellKnownName())){
                mark = marks[i];
                return mark;
            }
        }
        _log.debug("going for a defaultMark");
        mark = new DefaultMark();
        return mark;
        
    }
    private void renderImage(com.vividsolutions.jts.geom.Point point, BufferedImage img, int size,
    double rotation){
        renderImage(point.getX(),point.getY(),img,size,rotation);
    }
    private void renderImage(double tx, double ty, BufferedImage img, int size,
    double rotation){
        _log.info("drawing Image @"+tx+","+ty);
        AffineTransform temp = graphics.getTransform();
        AffineTransform markAT = new AffineTransform();
        Point2D mapCentre = new Point2D.Double(tx,ty);
        Point2D graphicCentre = new Point2D.Double();
        temp.transform(mapCentre,graphicCentre);
        markAT.translate(graphicCentre.getX(),graphicCentre.getY());
        markAT.rotate(rotation);
        
        double unitSize = Math.max(img.getWidth(),img.getHeight());
        
        double drawSize = (double)size/unitSize;
        _log.debug("unitsize "+unitSize+" size = "+size+" -> scale "+drawSize);
        markAT.scale(drawSize,drawSize);
        graphics.setTransform(markAT);
        // we move this to the centre of the image.
        graphics.drawImage(img,-img.getWidth()/2,-img.getHeight()/2,obs);
        graphics.setTransform(temp);
        return;
    }
    
    private void fillDrawMark(Graphics2D graphic,com.vividsolutions.jts.geom.Point point,Mark mark, int size, double rotation){
        fillDrawMark(graphic,point.getX(),point.getY(),mark,size,rotation);
    }
    private void fillDrawMark(Graphics2D graphic,double tx, double ty,Mark mark, int size, double rotation){
        AffineTransform temp = graphic.getTransform();
        AffineTransform markAT = new AffineTransform();
        Shape shape = Java2DMark.getWellKnownMark(mark.getWellKnownName());
        
        Point2D mapCentre = new Point2D.Double(tx,ty);
        Point2D graphicCentre = new Point2D.Double();
        temp.transform(mapCentre,graphicCentre);
        markAT.translate(graphicCentre.getX(),graphicCentre.getY());
        markAT.rotate(rotation);
        double unitSize = 1.0; // getbounds is broken !!!
        double drawSize = (double)size/unitSize;
        markAT.scale(drawSize,-drawSize);
        
        
        graphic.setTransform(markAT);
        if(mark.getFill()!=null){
            _log.debug("applying fill to mark");
            applyFill(graphic,mark.getFill(),null);
            graphic.fill(shape);
        }
        if(mark.getStroke()!=null){
            _log.debug("applying stroke to mark");
            applyStroke(graphic,mark.getStroke(),null);
            graphic.draw(shape);
        }
        graphic.setTransform(temp);
        if(mark.getFill()!=null){
            resetFill();
        }
        return;
        
    }
    static java.awt.Canvas obs = new java.awt.Canvas();
    private void applyFill(Graphics2D graphic, Fill fill, Feature feature){
        if(fill == null ) return;
        //HACK: not happy with having to catch exceptions. getValue should
        //HACK: never be throwing any
        try{
            graphic.setColor(Color.decode((String)fill.getColor().getValue(feature)));
            _log.debug("Setting fill: "+graphic.getColor().toString());
            Number value = (Number)fill.getOpacity().getValue(feature);
            float opacity = value.floatValue();
            graphic.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,opacity));
            
        } catch (org.geotools.filter.MalformedFilterException mfe){
            //HACK: see above hack statement, not happy with having to catch these
            _log.error(mfe);
        }
        org.geotools.styling.Graphic gr=fill.getGraphicFill();
        
        if(gr!=null){
            setTexture(graphic,gr);
        }else{
            _log.debug("no graphic fill set");
        }
    }
    private void setTexture(Graphics2D graphic,Graphic gr){
        BufferedImage image = getExternalGraphic(gr);
        if(image != null){
            _log.debug("got an image in graphic fill");
        }else{
            _log.debug("going for the mark from graphic fill");
            
            Mark mark = getMark(gr);
            int size=200;
            
            image = new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g1 = image.createGraphics();
            
            fillDrawMark(g1,markCentrePoint,mark,(int)(size*.9),gr.getRotation());
            
            java.awt.MediaTracker track = new java.awt.MediaTracker(obs);
            track.addImage(image,1);
            try{
                track.waitForID(1);
            } catch (InterruptedException e){}
            
        }
        int width = image.getWidth();
        int height = image.getHeight();
        double unitSize = Math.max(width,height);
        double drawSize = (double)gr.getSize()/unitSize;
        _log.debug("size = "+gr.getSize()+" unitsize "+unitSize+" drawSize "+drawSize);
        AffineTransform at = graphics.getTransform();
        double scaleX = drawSize/at.getScaleX();
        double scaleY = drawSize/-at.getScaleY();
            /* This is needed because the image must be a fixed size in pixels
             * but when the image is used as the fill it is transformed by the
             * current transform.
             * However this causes problems as the image size can become very small
             * e.g. 1 or 2 pixels when the drawScale is large, this makes the image fill
             * look very poor - I have no idea how to fix this.
             */
        _log.debug("scale "+scaleX+" "+scaleY);
        AffineTransform at2 = new AffineTransform();
        
        at2.scale(scaleX,scaleY);
        at2.translate(width,height);
        at2.rotate(Math.PI);
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        hints.put(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        AffineTransformOp op = new AffineTransformOp(at2,hints);
        
        BufferedImage img =  op.filter(image, null);
        _log.debug("w/h "+img.getWidth()+" "+img.getHeight());
        Rectangle rect = new Rectangle(0,0,img.getWidth(obs),img.getHeight(obs));
        java.awt.TexturePaint imagePaint = new java.awt.TexturePaint(img,rect);
        graphic.setPaint(imagePaint);
        _log.debug("applied TexturePaint "+imagePaint);
    }
    private void resetFill(){
        _log.debug("reseting the graphics");
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));
        //graphics.setPaint(null);
    }
    /**
     * Convenience method for applying a geotools Stroke object
     * as a Graphics2D Stroke object.
     *
     * @param stroke the Stroke to apply.
     */
    private void applyStroke(Graphics2D graphic,org.geotools.styling.Stroke stroke, Feature feature){
        if(stroke == null) return;
        double scale = graphics.getTransform().getScaleX();
        //HACK:not happy with having to catch exceptions. getValue should
        //HACK: never be throwing any
        try{
            String joinType = (String)stroke.getLineJoin().getValue(feature);
            
            if(joinType==null) { joinType="miter"; }
            int joinCode;
            if(joinLookup.containsKey(joinType)){
                joinCode = ((Integer) joinLookup.get(joinType)).intValue();
            }
            else{
                joinCode = java.awt.BasicStroke.JOIN_MITER;
            }
            
            String capType = (String)stroke.getLineCap().getValue(feature);
            if(capType==null) { capType="square"; }
            int capCode;
            if(capLookup.containsKey(capType)){
                capCode = ((Integer) capLookup.get(capType)).intValue();
            }
            else{
                capCode = java.awt.BasicStroke.CAP_SQUARE;
            }
            float[] dashes = stroke.getDashArray();
            if(dashes!=null){
                for(int i = 0;i<dashes.length;i++){
                    dashes[i] = (float)Math.max(1,dashes[i]/(float)scale);
                }
            }
            
            Number value = (Number)stroke.getWidth().getValue(feature);
            float width = value.floatValue();
            value = (Number)stroke.getDashOffset().getValue(feature);
            float dashOffset = value.floatValue();
            value = (Number)stroke.getOpacity().getValue(feature);
            float opacity = value.floatValue();
            _log.debug("width, dashoffset, opacity "+width+" "+dashOffset+" "+opacity);
            
            BasicStroke stroke2d;
            //TODO: It should not be necessary to divide each value by scale.
            if(dashes.length > 0){
                stroke2d = new BasicStroke(
                width/(float)scale, capCode, joinCode,
                (float)(Math.max(1,10/scale)),dashes, dashOffset/(float)scale);
                
            } else {
                stroke2d = new BasicStroke(width/(float)scale, capCode, joinCode,
                (float)(Math.max(1,10/scale)));
            }
            
            
            
            graphic.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,opacity));
            graphic.setStroke(stroke2d);
            graphic.setColor(Color.decode((String)stroke.getColor().getValue(feature)));
            org.geotools.styling.Graphic gr=stroke.getGraphicFill();
            if(gr!=null){
                setTexture(graphic,gr);
            }else{
                _log.debug("no graphic fill set");
            }
            System.out.println("stroke color "+graphics.getColor());
        }
        catch(org.geotools.filter.MalformedFilterException mfe){
            //HACK: see above hack statement.  Not happy with having to catch these.
            _log.error(mfe);
        }
    }
    /**
     * a method to draw the path with a graphic stroke.
     * @param gFill the graphic fill to be used to draw the stroke
     * @param graphic the Graphics2D to draw on
     * @param path the general path to be drawn
     */
    private void drawWithGraphicStroke(Graphics2D graphic, GeneralPath path, org.geotools.styling.Graphic gFill){
        _log.debug("drawing a graphicalStroke");
        
        
        // get the image to draw
        BufferedImage image = getExternalGraphic(gFill);
        if(image != null){
            _log.debug("got an image in graphic fill");
        }else{
            _log.debug("going for the mark from graphic fill");
            
            Mark mark = getMark(gFill);
            int size=200;
            
            image = new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g1 = image.createGraphics();
            
            fillDrawMark(g1,markCentrePoint,mark,(int)(size*.9),gFill.getRotation());
            
            java.awt.MediaTracker track = new java.awt.MediaTracker(obs);
            track.addImage(image,1);
            try{
                track.waitForID(1);
            } catch (InterruptedException e){}
            
        }
        
        int size = (int)gFill.getSize();
        int imageWidth = size;//image.getWidth();
        int imageHeight = size;//image.getHeight();
        int midx = imageWidth/2;
        int midy = imageHeight/2;
        
        
        double scaleX = graphic.getTransform().getScaleX();
        double scaleY = -graphic.getTransform().getScaleY();
        _log.debug("scale X "+scaleX+" Y "+scaleY);
        PathIterator pi = path.getPathIterator(null,10.0);
        double[] coords = new double[6];
        int type;
        
        double[] first = new double[2];
        double[] previous = new double[2];
        double[] tprevious = new double[2];
        double[] tcoords = new double[2];
        double[] in = new double[2];
        double[] out = new double[2];
        type = pi.currentSegment(coords);
        first[0]=coords[0];
        first[1]=coords[1];
        previous[0]=coords[0];
        previous[1]=coords[1];
        _log.debug("starting at "+first[0]+","+first[1]);
        pi.next();
        while(!pi.isDone()){
            type = pi.currentSegment(coords);
            switch(type){
                case PathIterator.SEG_MOVETO:
                    // nothing to do?
                    _log.debug("moving to "+coords[0]+","+coords[1]);
                    break;
                case PathIterator.SEG_CLOSE:
                    // draw back to first from previous
                    coords[0]=first[0];
                    coords[1]=first[1];
                    _log.debug("closing from "+previous[0]+","+previous[1]+" to "+coords[0]+","+coords[1]);
                    // no break here - fall through to next section
                case PathIterator.SEG_LINETO:
                    // draw from previous to coords
                    _log.debug("drawing from "+previous[0]+","+previous[1]+" to "+coords[0]+","+coords[1]);
                    double dx = coords[0]-previous[0];
                    double dy = coords[1]-previous[1];
                    double len = Math.sqrt(dx*dx+dy*dy)*scaleX - imageWidth;
                    if(len<=0){
                        len=1;
                    }
                    double theta = Math.atan2(dx,dy);
                    dx = Math.sin(theta)*imageWidth/scaleX;
                    dy = Math.cos(theta)*imageHeight/scaleY;
                    //int dx2 = (int)Math.round(dy/2d);
                    //int dy2 = (int)Math.round(dx/2d);
                    _log.debug("dx = "+dx+" dy "+dy+" step = "+Math.sqrt(dx*dx+dy*dy));
                    /*
                    at2.setToRotation((3d*Math.PI/2.0)-theta,midx,midy);
                    at2.scale(scaleX,scaleY);
                    op = new AffineTransformOp(at2,hints);
                    image2 =  op.filter(image, null);
                     */
                    double rotation = theta-(Math.PI/2d);
                    double x = previous[0]+dx/2d,y=previous[1]+dy/2d;
                    
                    _log.debug("len ="+len+" imageWidth "+imageWidth);
                    for(double dist =0;dist<len;dist+=imageWidth){
                        /*graphic.drawImage(image2,(int)x-midx,(int)y-midy,null); */
                        renderImage(x,y,image,size,rotation);
                        x+= dx;
                        y+= dy;
                    }
                    break;
            }
            previous[0]=coords[0];
            previous[1]=coords[1];
            pi.next();
        }
    }
    /**
     * Convenience method.  Converts a Geometry object into a GeneralPath.
     * @param geom The Geometry object to convert
     * @return A GeneralPath that is equivalent to geom
     */
    private GeneralPath createGeneralPath(final Geometry geom) {
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        addToPath(geom,path);
        return path;
    }
    
    /**
     * Used by createGeneralPath during the conversion of a geometry into
     * a general path.
     *
     * If the Geometry is an instance of Polygon then all of its interior holes
     * are processed and the resulting path is closed.
     *
     * @param geom the geomerty to be converted
     * @param path the GeneralPath to add to
     * @return path with geom added to it
     */
    private GeneralPath addToPath(final Geometry geom,final GeneralPath path){
        if(geom instanceof GeometryCollection){
            GeometryCollection gc = (GeometryCollection) geom;
            for(int i=0;i<gc.getNumGeometries();i++){
                addToPath(gc.getGeometryN(i), path);
            }
            return path;
        }
        if(geom instanceof com.vividsolutions.jts.geom.Polygon){
            com.vividsolutions.jts.geom.Polygon poly;
            poly = (com.vividsolutions.jts.geom.Polygon)geom;
            addToPath(path,poly.getExteriorRing().getCoordinates());
            path.closePath();
            for(int i=1;i<poly.getNumInteriorRing();i++){
                addToPath(path,poly.getInteriorRingN(i).getCoordinates());
                path.closePath();
            }
        }
        else{
            Coordinate[] coords = geom.getCoordinates();
            addToPath(path, coords);
        }
        return path;
    }
    
    /**
     * Used by addToPath in the conversion of geometries into general paths.
     * A moveTo is executed for the first coordinate then lineTo for all that
     * remain. The path is not closed.
     *
     * @param path The path to add to.  It is modifed by the method.
     * @param coords An array of coordinates to add to the path.
     */
    private void addToPath(final GeneralPath path, final Coordinate[] coords) {
        path.moveTo((float)coords[0].x,(float)coords[0].y);
        for(int i=1;i<coords.length;i++){
            path.lineTo((float)coords[i].x,(float)coords[i].y);
        }
    }
    
    /**
     * Extracts the named geometry from feature.
     * If geomName is null then the feature's default geometry is used.
     * If geomName cannot be found in feature then null is returned.
     *
     * @param feature The feature to find the geometry in
     * @param geomName The name of the geometry to find: null if the default
     *        geometry should be used.
     * @return The geometry extracted from feature or null if this proved
     *         impossible.
     */
    private Geometry findGeometry(final Feature feature, final String geomName) {
        Geometry geom = null;
        if(geomName==null){
            geom = feature.getDefaultGeometry();
        }
        else{
            try{
                geom = (Geometry)feature.getAttribute(geomName);
            }
            catch(IllegalFeatureException ife){
                //hack: not sure if null is the right thing to return at this point
                geom = null;
            }
        }
        return geom;
    }
    
    /** Getter for property interactive.
     * @return Value of property interactive.
     */
    public boolean isInteractive() {
        return interactive;
    }
    public void setInteractive(boolean interactive){
        this.interactive = interactive;
    }
}