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
        if(geom instanceof Point) return transformPoint((Point)geom);
        if(geom instanceof MultiPoint) return transformMultiPoint((MultiPoint)geom);
        if(geom instanceof LinearRing) return transformLinearRing((LinearRing)geom);
        if(geom instanceof LineString) return transformLineString((LineString)geom);
        if(geom instanceof Polygon) return transformPolygon((Polygon)geom);
        if(geom instanceof MultiPolygon) return transformMultiPolygon((MultiPolygon) geom);
        System.out.println("broken transform " + geom.toString());
        return null;//HACK: is this the right thing to do?
    }
    
    private Point transformPoint(Point in){
        Coordinate[] points = in.getCoordinates();
        Point out = new Point(trans.transform(points[0]),in.getPrecisionModel(),trans.getTargetSRID());
        return out;
    }
    private MultiPoint transformMultiPoint(MultiPoint in){
        int npoints = in.getNumPoints();
        Point[] points = new Point[npoints];
        for(int i=0;i<npoints;i++){
            points[i]=transformPoint((Point)in.getGeometryN(i));
        }
        MultiPoint out = new MultiPoint(points,in.getPrecisionModel(),trans.getTargetSRID());
        return out;
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
                holes[i] = transformLinearRing(in.getInteriorRingN(i));
            }
           return new Polygon(exRing,holes,in.getPrecisionModel(),trans.getTargetSRID());
        }
        return  new Polygon(exRing,in.getPrecisionModel(),trans.getTargetSRID());
    }
    private MultiPolygon transformMultiPolygon(MultiPolygon in){
        int npolys = in.getNumGeometries();
        Polygon[] out = new Polygon[npolys];
        for(int i=0;i<npolys;i++){
            out[i]=transformPolygon((Polygon) in.getGeometryN(i));
        }
        return new MultiPolygon(out,in.getPrecisionModel(),trans.getTargetSRID());
    }
}