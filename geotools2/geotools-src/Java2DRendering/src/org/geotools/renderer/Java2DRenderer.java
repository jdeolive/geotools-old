/**
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Center for Computational Geography
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
 *
 * Contacts:
 *     UNITED KINDOM: James Macgill j.macgill@geog.leeds.ac.uk
 *
 *
 * @author jamesm
 */

package org.geotools.renderer;

import org.geotools.featuretable.*;
import org.geotools.datasource.*;
import org.geotools.map.Map;
import org.geotools.styling.*;

import com.vividsolutions.jts.geom.*;

import java.awt.Color;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import java.util.HashMap;
public class Java2DRenderer implements org.geotools.renderer.Renderer {
    /**
     * Flag which controls behaviour for applying affine transformation
     * to the graphics object.  If true then the transform will be concatonated
     * to the existing transform, if false it will be replaced.
     */
    private boolean concatTransforms = false;
    
    /**
     * Holds a lookup bewteen SLD names and java constants
     **/
    private static final java.util.HashMap joinLookup = new java.util.HashMap();
    /**
     * Holds a lookup bewteen SLD names and java constants
     **/
    private static final java.util.HashMap capLookup = new java.util.HashMap();
    
    static { //static block to populate the lookups
        joinLookup.put("miter", new Integer(BasicStroke.JOIN_MITER));
        joinLookup.put("bevel", new Integer(BasicStroke.JOIN_BEVEL));
        joinLookup.put("round", new Integer(BasicStroke.JOIN_ROUND));
        
        capLookup.put("butt",   new Integer(BasicStroke.CAP_BUTT));
        capLookup.put("round",  new Integer(BasicStroke.CAP_ROUND));
        capLookup.put("square", new Integer(BasicStroke.CAP_SQUARE));
    }
    
    private Graphics2D graphics;
    private Rectangle screenSize;
    private double scaleDenominator;
    
    /** Creates a new instance of AWTRenderer */
    public Java2DRenderer() {
    }
    
    /**
     * Sets the flag which controls behaviour for applying affine 
     * transformation to the graphics object.
     *  
     * @param flag If true then the transform will be concatonated
     * to the existing transform, if false it will be replaced.
     */
    public void setConcatTransforms(boolean flag){
        concatTransforms = flag;
    }
    
    /**
     * Flag which controls behaviour for applying affine transformation
     * to the graphics object.  
     *
     * @return a boolean flag. If true then the transform will be
     * concatonated to the existing transform, if false it will be replaced.
     */
    public boolean gatConcatTransforms(){
        return concatTransforms;
    }
    public void setOutput(Graphics g,Rectangle bounds){
        graphics = (Graphics2D)g;
        screenSize = bounds;
    }
    
    public void render(Feature features[], Envelope map,Style s){
        if(graphics==null) return;
        System.out.println("renderering "+features.length+" features");
        //GeometryTransformer transform = new GeometryTransformer(new AffineTransformer(e,component.getBounds()));
        
        AffineTransform at = new AffineTransform();
        
        
        double scale = Math.min(screenSize.getHeight()/map.getHeight(),
        screenSize.getWidth()/map.getWidth());
        
        double angle = 0;//-Math.PI/8d;// rotation angle
        double tx = -map.getMinX()*scale; // x translation - mod by ian
        double ty = map.getMinY()*scale + screenSize.getHeight();// y translation
        
        double sc = scale*Math.cos(angle);
        double ss = scale*Math.sin(angle);
        
        
        at = new AffineTransform(sc,-ss,ss,-sc,tx,ty);
        Point2D testPoint = new Point2D.Double();
        testPoint.setLocation(map.getMinX(),map.getMinY());
        at.transform(testPoint,testPoint);
        System.out.println("origin "+map.getMinX()+","+map.getMinY()+"\ntrans "
        +testPoint.toString());
        if(concatTransforms){
            graphics.getTransform().concatenate(at);
        }
        else{
            graphics.setTransform(at);
        }
        
        FeatureTypeStyle[] featureStylers = s.getFeatureTypeStyles();
        processStylers(features, featureStylers);
    }
    
    private void processStylers(final Feature[] features, final FeatureTypeStyle[] featureStylers) {
        for(int i=0;i<featureStylers.length;i++){
            FeatureTypeStyle fts = featureStylers[i];
            for(int j=0;j<features.length;j++){
                Feature feature = features[j];
                System.out.println("fature is "+feature.getTypeName()+" type styler is "+fts.getFeatureTypeName());
                if(feature.getTypeName().equalsIgnoreCase(fts.getFeatureTypeName())){
                    //this styler is for this type of feature
                    //now find which rule applies
                    Rule[] rules = fts.getRules();
                    for(int k=0;k<rules.length;k++){
                        //does this rule apply?
                        if(rules[k].getMinScaleDenominator()<scaleDenominator && rules[k].getMaxScaleDenominator()>scaleDenominator){
                            //yes it does
                            //this gives us a list of symbolizers
                            Symbolizer[] symbolizers = rules[k].getSymbolizers();
                            //HACK: now this gets a little tricky...
                            //HACK: each symbolizer could be a point, line, text, raster or polygon symboliser
                            //HACK: but, if need be, a line symboliser can symbolise a polygon
                            //HACK: this code ingores this potential problem for the moment
                            processSymbolizers(feature, symbolizers);
                        }
                    }
                }
            }
        }
    }
    
    private void processSymbolizers(final Feature feature, final Symbolizer[] symbolizers) {
        for(int m =0;m<symbolizers.length;m++){
            //System.out.println("Using symbolizer "+symbolizers[m]);
            if (symbolizers[m] instanceof PolygonSymbolizer){
                renderPolygon(feature,(PolygonSymbolizer)symbolizers[m]);
            }
            else if(symbolizers[m] instanceof LineSymbolizer){
                renderLine(feature,(LineSymbolizer)symbolizers[m]);
            }
            //else if...
        }
    }
    
    private void renderPolygon(Feature feature, PolygonSymbolizer symbolizer){
        Fill fill = symbolizer.getFill();
        String geomName = symbolizer.geometryPropertyName();
        Geometry geom = findGeometry(feature,geomName);
        // Geometry scaled = transform.transformGeometry(geom);
        GeneralPath path = createGeneralPath(geom);
        //path.closePath();
        if(fill!=null){
            graphics.setColor(Color.decode(fill.getColor()));
            graphics.fill(path);
        }
        if(symbolizer.getStroke() != null) {
            applyStroke(symbolizer.getStroke());
            
            //path = createGeneralPath(geom.getCoordinates());
            graphics.draw(path);
        }
        //        System.out.println("Rendering a polygon with an outline colour of "+stroke.getColor()+
        //            "and a fill colour of "+fill.getColor()+"\n at "+path.getCurrentPoint().toString());
    }
    
    private void applyStroke(org.geotools.styling.Stroke stroke){
        // I'm not sure if this is right
        // TODO: check this out
        double scale = graphics.getTransform().getScaleX();
        String joinType = stroke.getLineJoin();
        
        if(joinType==null) { joinType="miter"; }
        int joinCode;
        if(joinLookup.containsKey(joinType)){
            joinCode = ((Integer) joinLookup.get(joinType)).intValue();
        }
        else{
            joinCode = java.awt.BasicStroke.JOIN_MITER;
        }
        
        String capType = stroke.getLineCap();
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
        BasicStroke stroke2d;
        if(dashes.length > 0){
            stroke2d = new BasicStroke(
            (float)stroke.getWidth()/(float)scale, capCode, joinCode,
            (float)(Math.max(1,10/scale)),dashes, (float)stroke.getDashOffset()/(float)scale);
            
        } else {
            stroke2d = new BasicStroke((float)stroke.getWidth()/(float)scale, capCode, joinCode,
            (float)(Math.max(1,10/scale)));
        }
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)stroke.getOpacity()));
        graphics.setStroke(stroke2d);
        graphics.setColor(Color.decode(stroke.getColor()));
    }
    
    private GeneralPath createGeneralPath(final Geometry geom) {
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        addToPath(geom,path);
        return path;
    }
    
    private GeneralPath addToPath(final Geometry geom,final GeneralPath path){
        System.out.println("Geom type is "+geom.getGeometryType()+" class is "+geom.getClass().getName());
        if(geom instanceof GeometryCollection){
            GeometryCollection gc = (GeometryCollection) geom;
            for(int i=0;i<gc.getNumGeometries();i++){
                addToPath(gc.getGeometryN(i), path);
            }
            return path;
        }
        if(geom instanceof com.vividsolutions.jts.geom.Polygon){
            System.out.println("generating path for polygon");
            com.vividsolutions.jts.geom.Polygon poly;
            poly = (com.vividsolutions.jts.geom.Polygon)geom;
            addToPath(path,poly.getExteriorRing().getCoordinates());
            path.closePath();
            for(int i=1;i<poly.getNumInteriorRing();i++){
                System.out.println("adding inner ring to polygon");
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
    
    private void addToPath(final GeneralPath path, final Coordinate[] coords) {
        path.moveTo((float)coords[0].x,(float)coords[0].y);
        for(int i=1;i<coords.length;i++){
            path.lineTo((float)coords[i].x,(float)coords[i].y);
        }
    }
    
    
    
    
    private void renderLine(Feature feature, LineSymbolizer symbolizer){
        applyStroke(symbolizer.getStroke());
        String geomName = symbolizer.geometryPropertyName();
        Geometry geom = findGeometry(feature, geomName);
        //Geometry scaled = transform.transformGeometry(geom);
        
        GeneralPath path = createGeneralPath(geom);
        //System.out.println(path.getBounds());
        
        graphics.draw(path);
        // System.out.println("Rendering a line with a colour of "+stroke.getColor());
    }
    
    private Geometry findGeometry(final Feature feature, final String geomName) {
        Geometry geom = null;
        if(geomName==null){
            geom = feature.getGeometry();
        }
        else{
            String names[] =  feature.getAttributeNames();
            for(int i=0;i<names.length;i++){
                if(names[i].equalsIgnoreCase(geomName)){
                    geom=(Geometry)feature.getAttributes()[i];
                }
            }
        }
        return geom;
    }
}