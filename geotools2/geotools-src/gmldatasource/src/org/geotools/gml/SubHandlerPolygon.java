/*
 * GMLPolygonHandler.java
 *
 * Created on 06 March 2002, 10:36
 */

package org.geotools.gml;

import java.util.ArrayList;
import com.vividsolutions.jts.geom.*;


/**
 * Creates a Polygon geometry.
 *
 * @author Ian Turton, CCG Leeds
 * @author Rob Hranac, Vision for New York
 * @version $Id: SubHandlerPolygon.java,v 1.3 2002/05/03 16:44:58 ianturton Exp $
 */

public class SubHandlerPolygon extends SubHandler {
    
    protected static com.vividsolutions.jts.algorithm.CGAlgorithms cga = 
        new com.vividsolutions.jts.algorithm.RobustCGAlgorithms();
    /** Factory for creating the Polygon geometry. */
    private GeometryFactory geometryFactory = new GeometryFactory();
    
    /** Handler for the LinearRings that comprise the Polygon. */
    private SubHandlerLinearRing currentHandler = new SubHandlerLinearRing();
    
    /** Stores Polygon's outer boundary (shell) */
    private LinearRing outerBoundary = null;
    
    /** Stores Polygon's inner boundaries (holes) */
    private ArrayList innerBoundaries = new ArrayList();
    
    /** Remember the current location in the parsing stream (inner or outer boundary) */
    private int location = 0;
    /** Indicates that we are inside the inner boundary of the Polygon. */
    private int INNER_BOUNDARY = 1;
    /** Indicates that we are inside the outer boundary of the Polygon. */
    private int OUTER_BOUNDARY = 2;
    
    
    /** Creates a new instance of GMLPolygonHandler */
    public SubHandlerPolygon() {}
    
    
    /**
     * Catch inner and outer LinearRings messages and handle them appropriately.
     *
     * @param message Name of sub geometry located.
     * @param type Type of sub geometry located.
     */
    public void subGeometry(String message, int type) {
        
        // if we have found a linear ring, either
        //  add it to the list of inner boundaries if we are reading them
        //  and at the end of the LinearRing
        //  add it to the outer boundary if we are reading it and at the end of
        //  the LinearRing
        //  create a new linear ring, if we are at the start of a new linear ring
        if( message.equals("LinearRing") ) {
            if( type == GEOMETRY_END ) {
                if( location == INNER_BOUNDARY ) {
                    LinearRing ring = (LinearRing) currentHandler.create(geometryFactory);
                    Coordinate[] points = ring.getCoordinates();
                    /* it is important later that internal rings (holes) are
                     * anticlockwise (counter clockwise) - so we reverse the
                     * points if necessary
                     */
                    if (cga.isCCW(points)){
                        System.out.println("good hole found");
                        innerBoundaries.add(ring);
                    } else {
                        System.out.println("bad hole found - fixing");
                        Coordinate[] newPoints = new Coordinate[points.length];
                        for(int i=0,j=points.length-1;i<points.length;i++,j--){
                            newPoints[i]=points[j];
                        }
                        try{
                            ring = geometryFactory.createLinearRing(newPoints);
                            innerBoundaries.add(ring);
                        } catch (TopologyException e){
                            System.err.println("Caught Topology exception in GMLPolygonHandler");
                            ring=null;
                        }
                    }
                    
                } else if( location == OUTER_BOUNDARY ) {
                    /* it is important later that the outerboundary is
                     * clockwise  - so we reverse the
                     * points if necessary
                     */
                    outerBoundary = (LinearRing) currentHandler.create(
                    geometryFactory);
                    Coordinate[] points = outerBoundary.getCoordinates();
                    if (cga.isCCW(points)){
                        System.out.println("bad outer ring - rebuilding");
                        Coordinate[] newPoints = new Coordinate[points.length];
                        for(int i=0,j=points.length-1;i<points.length;i++,j--){
                            newPoints[i]=points[j];
                        }
                        try{
                            outerBoundary =
                            geometryFactory.createLinearRing(newPoints);
                        } catch (TopologyException e){
                            System.err.println("Caught Topology exception in "+
                            "GMLPolygonHandler");
                            outerBoundary = null;
                        }
                    }
                }
            } else if ( type == GEOMETRY_START ) {
                currentHandler = new SubHandlerLinearRing();
            }
        } else if( message.equals("outerBoundaryIs") ) {
            //  or, if we are getting notice of an inner/outer boundary marker,
            // set current location appropriately
            System.out.println("new outer Boundary");
            location = OUTER_BOUNDARY;
        } else if( message.equals("innerBoundaryIs") ) {
            System.out.println("new InnerBoundary");
            location = INNER_BOUNDARY;
        }
        
    }
    
    /**
     * Add a coordinate to the current LinearRing.
     *
     * @param coordinate Name of sub geometry located.
     */
    public void addCoordinate(Coordinate coordinate) {
        currentHandler.addCoordinate(coordinate);
    }
    
    
    /**
     * Determines whether or not the geometry is ready to be returned.
     *
     * @param message Name of GML element that prompted this check.
     * @return Flag indicating whether or not the geometry is ready to be returned.
     */
    public boolean isComplete(String message) {
        
        // the conditions checked here are that the endGeometry message that prompted this check is a Polygon
        //  and that this Polygon has an outer boundary; if true,
        //  then return the all go signal
        if( message.equals("Polygon") ) {
            if( outerBoundary != null ) {
                return true;
            }
            else {
                return false;
            }
        }
        
        // otherwise, send this message to the subGeometry method for further processing
        else {
            this.subGeometry(message, GEOMETRY_END);
            return false;
        }
    }
    
    
    /**
     * Return the completed OGC Polygon.
     *
     * @param geometryFactory Geometry factory to be used in Polygon creation.
     * @return Completed OGC Polygon.
     */
    public Geometry create(GeometryFactory geometryFactory) {
        for (int i=0; i < innerBoundaries.size();i++){
            LinearRing hole = (LinearRing)innerBoundaries.get(i);
            if(hole.crosses(outerBoundary)){
                System.err.println("Topology Error building polygon");
                return null;
            }
        }
        return geometryFactory.createPolygon(outerBoundary,(LinearRing[])innerBoundaries.toArray(new LinearRing[0]));
    }
    
    
}
