package org.geotools.geometry.jts;

import java.util.Random;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import com.vividsolutions.jts.geom.prep.PreparedPolygon;
import com.vividsolutions.jts.io.WKTWriter;

import junit.framework.Assert;
import junit.framework.TestCase;

public class JTSTest extends TestCase {
    static GeometryFactory factory = new GeometryFactory();
    static Random random = new Random(0); // use the same sequence each time
    static int N=10; // 93 squares 
    
    int alternative[] = new int[]{ 8, 0, 0 };
    
    /** Array of geometries to use for testing */
    static Geometry geoms[];
    static Geometry circles[];
    static {
        geoms = new Geometry[N];
        for( int i=0; i<N; i++){
            geoms[i] = createSquare();
        }
        circles = new Geometry[N];
        for( int i=0; i<N; i++){
            circles[i] = createCircle();
        }
    }
    
    /**
     * Measure how long it takes to execute the provided runnable.
     * <p>
     * We prep the system by running three times; before we start measuring.
     * The result is an averge duration of 5 runs.
     * @param run
     * @return average duration of 5 runs
     */
    public long duration( Runnable run ){
        long duration=0;
        for( int i=0; i<10; i++){
            long mark = System.currentTimeMillis();
            run.run();
            long set = System.currentTimeMillis();
            long delta = (set - mark);
            //System.out.println("Run "+i+" is "+delta+" mills");            
            if( i<3 ){
                continue;
            }
            duration += delta;
        }
        return duration / 7;
    }
    
    public void testAlternativeOne(){
        long duration = duration( new Runnable(){
            public void run() {
                for( Geometry geometry : geoms ){
                    alternative[0]=0;
                    for( Geometry geometry2 : geoms ){                
                        if( geometry.disjoint( geometry2 ) || geometry.touches( geometry2 ) ){
                            alternative[0]++;
                         }
                    }
                }
            }            
        });
        //System.out.println( "Alternative 1 "+alternative[0]+" in "+duration+" mills");
    }
    public void testAlternativeOnePrepared(){
        long duration = duration( new Runnable(){
            public void run() {
                for( Geometry geometry : geoms ){
                    PreparedGeometry prep = new PreparedPolygon( (Polygon) geometry );                    
                    alternative[0]=0;
                    for( Geometry geometry2 : geoms ){                
                        if( prep.disjoint( geometry2 ) || prep.touches( geometry2 ) ){
                            alternative[0]++;
                         }
                    }
                }
            }            
        });
        //System.out.println( "PrepairedGeomery 1 "+alternative[0]+" in "+duration+" mills");
    }
    
    public void testAlternativeA(){
        long duration = duration( new Runnable(){
            public void run() {
                for( Geometry geometry : circles ){
                    alternative[0]=0;
                    for( Geometry geometry2 : circles ){                
                        if( geometry.disjoint( geometry2 ) || geometry.touches( geometry2 ) ){
                            alternative[0]++;
                         }
                    }
                }
            }            
        });
        //System.out.println( "Circles A "+alternative[0]+" in "+duration+" mills");
    }
    public void testAlternativeDPrepaired(){
        long duration = duration( new Runnable(){
            public void run() {
                for( Geometry geometry : circles ){
                    PreparedGeometry prep = PreparedGeometryFactory.prepare( geometry );
                    alternative[0]=0;
                    for( Geometry geometry2 : circles ){                
                        if( prep.disjoint( geometry2 ) || prep.touches( geometry2 ) ){
                            alternative[0]++;
                         }
                    }
                }
            }            
        });
        //System.out.println( "Circles D "+alternative[0]+" in "+duration+" mills");
    }
    
    public void testAlternativeTwo(){
        long duration = duration( new Runnable(){
            public void run() {
                for( Geometry geometry : geoms ){
                    alternative[1]=0;
                    for( Geometry geometry2 : geoms ){  
                        IntersectionMatrix matrix = geometry.relate( geometry2 );
                        if( matrix.isDisjoint() || matrix.isTouches(2,2) ){
                            alternative[1]++;
                         }
                    }
                }
            }            
        });
        //System.out.println( "Alternative 2 "+alternative[1]+" in "+duration+" mills");
        assertEquals( "Alternative 2 correct", alternative[0], alternative[1]);
    }
    
    public void testAlternativeThree(){
        long duration = duration( new Runnable(){
            public void run() {
                for( Geometry geometry : geoms ){
                    alternative[2]=0;
                    for( Geometry geometry2 : geoms ){  
                        if( geometry.relate( geometry2, "F********" ) ){
                            alternative[2]++;
                        }
                    }
                }
            }            
        });
        //System.out.println( "Alternative 3 "+alternative[2]+" in "+duration+" mills");
        assertEquals( "Alternative 3 correct", alternative[0], alternative[2]);        
    }

    /**
     * Create a little 2x2 square in any grid location
     * in a 10 x 10 area. Used for testing.
     * 
     * @return Polygon
     */
    private static Geometry createSquare() {
        int x = random.nextInt(5);
        int y = random.nextInt(5);
        Coordinate coords[] = new Coordinate[]{
                new Coordinate((double)x,(double)y),
                new Coordinate((double)x+2,(double)y),
                new Coordinate((double)x+2,(double)y+2),
                new Coordinate((double)x,(double)y+2),
                new Coordinate((double)x,(double)y)                
        };
        LinearRing ring = factory.createLinearRing( coords );
        Polygon polygon = factory.createPolygon( ring, null );
        return polygon;
    }
    /**
     * Create a little 2x2 unit circle any grid location
     * in a 10 x 10 area. Used for testing.
     * 
     * @return Polygon
     */
    private static Geometry createCircle() {
        int x = random.nextInt(5);
        int y = random.nextInt(5);
        int S = 32;
        Coordinate coords[] = new Coordinate[S];
        for( int i = 0; i < S; i++){
            double angle = ((double) i / (double) S) * 360.0;
            double dx = Math.cos( angle );
            double dy = Math.sin( angle );
            coords[i] = new Coordinate( (double) x + dx, (double) y + dy );  
        }
        coords[coords.length-1] = coords[0];
        
        LinearRing ring = factory.createLinearRing( coords );
        Polygon polygon = factory.createPolygon( ring, null );
        
        return polygon;
    }
}
