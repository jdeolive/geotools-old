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
import java.awt.Shape;
import java.awt.image.BufferedImage;

//util imports
import java.util.HashMap;
import java.util.HashSet;

//Logging system
import org.apache.log4j.Logger;

/**
 * @version $Id: Java2DRenderer.java,v 1.21 2002/06/07 16:42:23 ianturton Exp $
 * @author James Macgill
 */
public class Java2DRenderer implements org.geotools.renderer.Renderer {
    
    private static Logger _log = Logger.getLogger(Java2DRenderer.class);
    
    /**
     * Flag which controls behaviour for applying affine transformation
     * to the graphics object.  If true then the transform will be concatenated
     * to the existing transform.  If false it will be replaced.
     */
    private boolean concatTransforms = false;
    
    /**
     * Holds a lookup bewteen SLD names and java constants.
     */
    private static final java.util.HashMap joinLookup = new java.util.HashMap();
    /**
     * Holds a lookup bewteen SLD names and java constants.
     */
    private static final java.util.HashMap capLookup = new java.util.HashMap();
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
        
        supportedGraphicFormats.add("image/gif");
        supportedGraphicFormats.add("image/jpg");
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
                _log.info("fature is "+feature.getSchema().getTypeName()+
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
            applyFill(fill, feature);
            graphics.fill(path);
            // shouldn't we reset the graphics when we return finished?
            resetFill();
        }
        if(symbolizer.getStroke() != null) {
            applyStroke(symbolizer.getStroke(), feature);
            _log.debug("path is "+graphics.getTransform().createTransformedShape(path).getBounds2D().toString());
            graphics.draw(path);
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
        applyStroke(symbolizer.getStroke(),feature);
        String geomName = symbolizer.geometryPropertyName();
        Geometry geom = findGeometry(feature, geomName);
        if(geom.isEmpty()) return;
        GeneralPath path = createGeneralPath(geom);
        graphics.draw(path);
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
    
    private void renderExternalGraphic(Geometry geom, Graphic graphic){
        
        ExternalGraphic[] extgraphics = graphic.getExternalGraphics();
        for(int i=0;i<extgraphics.length;i++){
            ExternalGraphic eg = extgraphics[i];
            _log.info("got a "+eg.getFormat());
            if(supportedGraphicFormats.contains(eg.getFormat().toLowerCase())){
                if(eg.getFormat().equalsIgnoreCase("image/gif") ||
                eg.getFormat().equalsIgnoreCase("image/jpg")){
                    BufferedImage img = imageLoader.get(eg.getLocation());
                    if(img!=null){
                        renderImage((Point)geom,img,(int)graphic.getSize(),graphic.getRotation());
                        return;
                    }
                }
            }
        }
        // if we get to here we need to render the marks;
        renderMark(geom,graphic);
    }
    
    
    private void renderMark(Geometry geom, Graphic graphic){
        Mark marks[] = graphic.getMarks();
        Mark mark;
        int size = 6; // size in pixels
        double rotation = 0.0; // rotation in degrees
        
        if(marks == null || marks.length==0){
            _log.debug("choosing a default mark as no marks returned");
            marks = new Mark[1];
            marks[0] = new DefaultMark();
        }
        
        for(int i = 0; i<marks.length; i++){
            if(wellKnownMarks.contains(marks[i].getWellKnownName())){
                mark = marks[i];
                size = (int)graphic.getSize();
                rotation = graphic.getRotation()*Math.PI/180d;
                if(mark.getFill()!=null){
                    fillMark((Point)geom,mark,size,rotation);
                }
                if(mark.getStroke()!=null){
                    drawMark((Point)geom,mark,size,rotation);
                }
                return;
            }
        }
        _log.debug("going for a defaultMark");
        mark = new DefaultMark();
        fillMark((Point)geom,mark,size,rotation);
        drawMark((Point)geom,mark,size,rotation);
    }
    private void renderImage(com.vividsolutions.jts.geom.Point point, BufferedImage img, int size,
    double rotation){
        _log.info("drawing Image");
        AffineTransform temp = graphics.getTransform();
        AffineTransform markAT = new AffineTransform();
        double tx = point.getX();
        double ty = point.getY();
        Point2D mapCentre = new Point2D.Double(tx,ty);
        Point2D graphicCentre = new Point2D.Double();
        temp.transform(mapCentre,graphicCentre);
        markAT.translate(graphicCentre.getX(),graphicCentre.getY());
        markAT.rotate(rotation);

        double unitSize = Math.max(img.getWidth(),img.getHeight());
        
        double drawSize = (double)size/unitSize;
        _log.debug("unitsize "+unitSize+" size = "+size+" -> scale "+drawSize);
        markAT.scale(drawSize,-drawSize);
        graphics.setTransform(markAT);
        graphics.drawImage(img,-img.getWidth()/2,-img.getHeight()/2,null);
        graphics.setTransform(temp);
        return;
    }
    private void drawMark(com.vividsolutions.jts.geom.Point point,Mark mark, int size, double rotation){
        if(mark.getStroke()== null){
            return;
        }
        _log.info("drawing Mark");
        applyStroke(mark.getStroke(),null);
        AffineTransform temp = graphics.getTransform();
        AffineTransform markAT = new AffineTransform();
        Shape shape = Java2DMark.getWellKnownMark(mark.getWellKnownName());
        double tx = point.getX();
        double ty = point.getY();
        Point2D mapCentre = new Point2D.Double(tx,ty);
        Point2D graphicCentre = new Point2D.Double();
        temp.transform(mapCentre,graphicCentre);
        markAT.translate(graphicCentre.getX(),graphicCentre.getY());
        markAT.rotate(rotation);
        //_log.debug("width + height "+shape.getBounds().getWidth()+" "+shape.getBounds().getHeight());
        //double unitSize = Math.max(shape.getBounds().getWidth(),shape.getBounds().getHeight());
        double unitSize = 1.0; // getbounds is broken !!!
        double drawSize = (double)size/unitSize;
        _log.debug("unitsize "+unitSize+" size = "+size+" -> scale "+drawSize);
        markAT.scale(drawSize,-drawSize);
        graphics.setTransform(markAT);
        graphics.draw(shape);
        graphics.setTransform(temp);
        return;
    }
    private void fillMark(com.vividsolutions.jts.geom.Point point,Mark mark, int size, double rotation){
        if(mark.getFill()== null){
            return;
        }
        _log.info("filling mark");
        applyFill(mark.getFill(),null);
        AffineTransform temp = graphics.getTransform();
        AffineTransform markAT = new AffineTransform();
        Shape shape = Java2DMark.getWellKnownMark(mark.getWellKnownName());
        double tx = point.getX();
        double ty = point.getY();
        Point2D mapCentre = new Point2D.Double(tx,ty);
        Point2D graphicCentre = new Point2D.Double();
        temp.transform(mapCentre,graphicCentre);
        markAT.translate(graphicCentre.getX(),graphicCentre.getY());
        markAT.rotate(rotation);
        //_log.debug("width + height "+shape.getBounds().getWidth()+" "+shape.getBounds().getHeight());
        //double unitSize = Math.max(shape.getBounds().getWidth(),shape.getBounds().getHeight());
        double unitSize = 1.0; // getbounds is broken !!!
        double drawSize = (double)size/unitSize;
        markAT.scale(drawSize,-drawSize);
        
        
        graphics.setTransform(markAT);
        graphics.fill(shape);
        graphics.setTransform(temp);
        resetFill();
        return;
        
    }
    private void applyFill(Fill fill, Feature feature){
        if(fill == null ) return;
        //HACK: not happy with having to catch exceptions. getValue should
        //HACK: never be throwing any
        try{
            graphics.setColor(Color.decode((String)fill.getColor().getValue(feature)));
            _log.debug("Setting fill: "+graphics.getColor().toString());
            Number value = (Number)fill.getOpacity().getValue(feature);
            float opacity = value.floatValue();
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,opacity));
        } catch (org.geotools.filter.MalformedFilterException mfe){
            //HACK: see above hack statement, not happy with having to catch these
            _log.error(mfe);
        }
    }
    private void resetFill(){
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));
    }
    /**
     * Convenience method for applying a geotools Stroke object
     * as a Graphics2D Stroke object.
     *
     * @param stroke the Stroke to apply.
     */
    private void applyStroke(org.geotools.styling.Stroke stroke, Feature feature){
        if(stroke == null) return;
        double scale = graphics.getTransform().getScaleX();
        //HACK: not happy with having to catch exceptions. getValue should
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
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,opacity));
            graphics.setStroke(stroke2d);
            graphics.setColor(Color.decode((String)stroke.getColor().getValue(feature)));
            System.out.println("stroke color "+graphics.getColor());
        }
        catch(org.geotools.filter.MalformedFilterException mfe){
            //HACK: see above hack statement.  Not happy with having to catch these.
            _log.error(mfe);
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
}