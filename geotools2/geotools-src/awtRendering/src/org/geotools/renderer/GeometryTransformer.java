/*
 * FeatureTransformer.java
 *
 * Created on 13 January 2002, 20:30
 */

package org.geotools.renderer;
import com.vividsolutions.jts.geom.*;

/**
 *
 * @author  James Macgill
 */
public class GeometryTransformer {
    
    CoordinateTransformer trans;
    
    /** Creates a new instance of FeatureTransformer */
    public GeometryTransformer(CoordinateTransformer trans) {
        this.trans = trans;
    }
    
    public Geometry transformGeometry(Geometry geom){
        if(geom instanceof LinearRing) return transformLinearRing((LinearRing)geom);
        if(geom instanceof LineString) return transformLineString((LineString)geom);
        if(geom instanceof Polygon) return transformPolygon((Polygon)geom);
        return null;//HACK: is this the right thing to do?
    }
    
    private LineString transformLineString(LineString in){
        Coordinate[] points = in.getCoordinates();
        LineString out = new LineString(trans.transform(points),in.getPrecisionModel(),trans.getTargetSRID());//srid should come from trans
        return out;
    }
    
    //not happy here, this is almost an exact copy of transformLineString
    //as LinearRing is a subclass of LineString, but how would I make sure the right type was retured if I just used
    //a single method - James Macgill
    private LinearRing transformLinearRing(LineString in){
        Coordinate[] points = in.getCoordinates();
        LinearRing out = new LinearRing(trans.transform(points),in.getPrecisionModel(),trans.getTargetSRID());//srid should come from trans
        return out;
    }
    
    private Polygon transformPolygon(Polygon in){
        
        LinearRing exRing = (LinearRing)transformLinearRing(in.getExteriorRing());
        
        int ringCount = in.getNumInteriorRing();
        if (ringCount>0){
            LinearRing holes[] = new LinearRing[ringCount];
            for(int i=0;i<in.getNumInteriorRing();i++){
                holes[i] = transformLinearRing(in.getExteriorRing());
            }
           return new Polygon(exRing,holes,in.getPrecisionModel(),trans.getTargetSRID());
        }
        return  new Polygon(exRing,in.getPrecisionModel(),trans.getTargetSRID());
    }
}