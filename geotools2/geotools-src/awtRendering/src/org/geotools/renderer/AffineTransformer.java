/*
 * AffineTransformer.java
 *
 * Created on 14 January 2002, 22:38
 */

package org.geotools.renderer;
import com.vividsolutions.jts.geom.*;
import org.geotools.datasource.*;
import java.awt.Rectangle;

/**
 *
 * @author  James Macgill
 */
public class AffineTransformer implements CoordinateTransformer {
    
    double[][] matrix;
    /** Creates a new instance of AffineTransformer */
    public AffineTransformer(Envelope map,Rectangle screen) {
        System.out.println("map "+map.getWidth()+" screen "+screen.width);
        double scale = screen.width/map.getWidth();
        System.out.println("Scale is "+scale);
        double angle = 0;//-Math.PI/8d;// rotation angle
        double tx = -map.getMinX()*scale; // x translation - mod by ian
        double ty = screen.getHeight();// y translation
        System.out.println("y shift is "+ty);
        
        double sc = scale*Math.cos(angle);
        double ss = scale*Math.sin(angle);
        
        double t[][] = {{sc,ss,tx},{-ss,sc,ty}};
        t[1][1] = -t[1][1];//flip by y axis
        matrix = t;
    }
    
    public int getSourceSRID() {
        return -1;
    }
    
    public int getTargetSRID() {
        return -1;
    }
    
    public CoordinateTransformer inverse() {
        return null;
    }
    
    public Coordinate transform(Coordinate in) {
        Coordinate out = new Coordinate();
        out.x = matrix[0][0]*in.x + matrix[0][1]*in.y +matrix[0][2];
        out.y = matrix[1][0]*in.x + matrix[1][1]*in.y +matrix[1][2];
        return out;
    }
    
    public Coordinate[] transform(Coordinate[] in) {
        Coordinate[] out = new Coordinate[in.length];
        for(int i=0;i<in.length;i++){
            out[i] = transform(in[i]);
        }
        return out;
    }
    
}
