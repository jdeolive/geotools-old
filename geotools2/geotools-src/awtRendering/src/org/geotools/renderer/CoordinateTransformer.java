/*
 * CoordinateTransformer.java
 *
 * Created on 13 January 2002, 19:58
 */

package org.geotools.renderer;
import com.vividsolutions.jts.geom.*;

/**
 *
 * @author  James Macgill
 */
public interface CoordinateTransformer {

   public Coordinate transform(Coordinate in); 
   public Coordinate[] transform(Coordinate in[]);
   public CoordinateTransformer inverse();
   public int getTargetSRID();
   public int getSourceSRID();

}
