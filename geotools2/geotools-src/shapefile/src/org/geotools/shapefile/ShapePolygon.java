package org.geotools.shapefile;

import java.io.*;
import cmp.LEDataStream.*;
import com.vividsolutions.jts.geom.*;

/**
 * Wrapper for a Shapefile polygon.
 */
public class ShapePolygon {

  public static MultiPolygon read( LEDataInputStream file )
    throws IOException, InvalidShapefileException { 

    file.setLittleEndianMode(true);
    int shapeType = file.readInt();
    if ( shapeType != Shapefile.POLYGON ) {
      throw new InvalidShapefileException
        ("Error: Attempt to load non polygon shape as polygon.");
    }
  
      
  }
    
  public ShapePolygon(double[] box,int[] parts,ShapePoint[] points){
    super( box, parts, points );
  }
    
  public int getShapeType(){
    return Shapefile.POLYGON;
  }
  public int getLength(){
    return (22+(2*numParts)+numPoints*8);
  }

}

/*
 * $Log: ShapePolygon.java,v $
 * Revision 1.1  2002/02/11 16:54:43  jmacgill
 * added shapefile code and directories
 *
 */
