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

import java.awt.*;

public class AWTRenderer implements org.geotools.renderer.Renderer {
    private Graphics graphics;
    private Rectangle screen;
    private double scaleDenominator;
    
    /** Creates a new instance of AWTRenderer */
    public AWTRenderer() {
    }
    
    public void setOutput(Graphics g,Rectangle bounds){
        graphics = g;
        screen = bounds;
    }
    
    public void render(Feature features[], Envelope e,Style s){
        if(graphics==null) return;
        System.out.println("renderering "+features.length+" features");
        GeometryTransformer transform = new GeometryTransformer(new AffineTransformer(e,screen));
        FeatureTypeStyle[] featureStylers = s.getFeatureTypeStyles();
        processStylers(features, transform, featureStylers);
    }
    
    private void processStylers(final Feature[] features, final GeometryTransformer transform, final FeatureTypeStyle[] featureStylers) {
        for(int i=0;i<featureStylers.length;i++){
            FeatureTypeStyle fts = featureStylers[i];
            for(int j=0;j<features.length;j++){
                Feature feature = features[j];
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
                            processSymbolizers(transform, feature, symbolizers);
                        }
                    }
                }
            }
        }
    }
    
    private void processSymbolizers(final GeometryTransformer transform, final Feature feature, final Symbolizer[] symbolizers) {
        for(int m =0;m<symbolizers.length;m++){
            System.out.println("Using symbolizer "+symbolizers[m]);
            if (symbolizers[m] instanceof PolygonSymbolizer){
                renderPolygon(feature,(PolygonSymbolizer)symbolizers[m],transform);
            }
            else if(symbolizers[m] instanceof LineSymbolizer){
                renderLine(feature,(LineSymbolizer)symbolizers[m],transform);
            }
            //else if...
        }
    }
    
    private void renderPolygon(Feature feature, PolygonSymbolizer symbolizer,GeometryTransformer transform){
        
        
        org.geotools.styling.Stroke stroke = symbolizer.getStroke();
        Fill fill = symbolizer.getFill();
        String geomName = symbolizer.geometryPropertyName();
        Geometry geom = findGeometry(feature,geomName);
        Geometry scaled = transform.transformGeometry(geom);
        renderAsArea(scaled,fill,stroke);
    }
    private void renderAsArea(Geometry scaled, Fill fill, org.geotools.styling.Stroke stroke){
        if( scaled instanceof com.vividsolutions.jts.geom.Polygon){
            com.vividsolutions.jts.geom.Polygon scaledPoly = (com.vividsolutions.jts.geom.Polygon)scaled;
            System.out.println("drawing outer ring of polygon\n"+scaledPoly.toText());
            renderAsArea(scaledPoly.getExteriorRing(),fill,stroke);
            for (int i=0;i<scaledPoly.getNumInteriorRing();i++){
                System.out.println("drawing interior ring of polygon ("+i+")\n"+scaledPoly.getInteriorRingN(i).toText());
                DefaultFill background = new DefaultFill();
                renderAsArea(scaledPoly.getInteriorRingN(i),background,stroke);
            }
            return;
        }else if (scaled instanceof com.vividsolutions.jts.geom.GeometryCollection){
            com.vividsolutions.jts.geom.GeometryCollection scaledMPoly = (com.vividsolutions.jts.geom.GeometryCollection)scaled;
            for (int i = 0;i<scaledMPoly.getNumGeometries();i++){
                System.out.println("drawing part of a feature collection");
                renderAsArea(scaledMPoly.getGeometryN(i),fill,stroke);
                
            }
            return;
        }
        System.out.println("rendering a linestring? " + scaled.getGeometryType());
        Coordinate[] coords = scaled.getCoordinates();
        int[][] points = extractPointArrays(coords);
        if(fill!=null){
            graphics.setColor(Color.decode(fill.getColor()));
            graphics.fillPolygon(points[0],points[1],points[0].length);
        }
        if(stroke!=null){
            graphics.setColor(Color.decode(stroke.getColor()));
            graphics.drawPolygon(points[0],points[1],points[0].length);
        }
        
        System.out.println("Rendering a polygon with an outline colour of "+stroke.getColor()+
        "and a fill colour of "+fill.getColor() + "\nat "+points[0][0] +","+ points[1][0]);
    }
    
    private int[][] extractPointArrays(final Coordinate[] coords) {
        int[][] points = new int[2][coords.length];
        int ypoints[] = new int[coords.length];
        for(int i=0;i<coords.length;i++){
            points[0][i]=(int)coords[i].x;
            points[1][i]=(int)coords[i].y;
        }
        
        return points;
    }
    
    private void renderLine(Feature feature, LineSymbolizer symbolizer,GeometryTransformer transform){
        org.geotools.styling.Stroke stroke = symbolizer.getStroke();
        String geomName = symbolizer.geometryPropertyName();
        Geometry geom = findGeometry(feature, geomName);
        Geometry scaled = transform.transformGeometry(geom);
        Coordinate[] coords = scaled.getCoordinates();
        int points[][] = extractPointArrays(coords);
        graphics.setColor(Color.decode(stroke.getColor()));
        graphics.drawPolyline(points[0],points[1],coords.length);
        System.out.println("Rendering a line with a colour of "+stroke.getColor());
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