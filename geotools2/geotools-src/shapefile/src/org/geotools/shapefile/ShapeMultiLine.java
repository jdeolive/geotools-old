/*
 * $Id: ShapeMultiLine.java,v 1.1 2002/02/11 16:54:43 jmacgill Exp $
 *
 */

package org.geotools.shapefile;

import java.io.*;
import cmp.LEDataStream.*;
import com.vividsolutions.jts.geom.*;

/**
 * Wrapper for a Shapefile arc.
 */
public class ShapeMultiLine  {
    
    public static MultiLineString read( LEDataInputStream file ) throws IOException {
        file.setLittleEndianMode(true);
        int shapeType = file.readInt();//ignored
        double box[] = new double[4];
        for ( int i = 0; i<4; i++ ){
            box[i] = file.readDouble();
        }//we don't need the box....
        
        int numParts = file.readInt();
        int numPoints = file.readInt();//total number of points
        
        int[] parts = new int[numParts];
        
        //points = new Coordinate[numPoints];
        
        for ( int i = 0; i < numParts; i++ ){
            parts[i]=file.readInt();
        }
        
        LineString lines[] = new LineString[numParts];
        int start,finish,length;
        for(int part=0;part<numParts;part++){
            start = parts[part];
            if(part == numParts-1){finish = numPoints;}
            else {
                finish=parts[part+1];
            }
            length = finish-start;
            Coordinate points[] = new Coordinate[length];
            for(int i=0;i<length;i++){
                points[i]=new Coordinate(file.readDouble(),file.readDouble());
            }
            lines[part] = new LineString(points,null,-1);
            
        }
        return new MultiLineString(lines,null,-1);
    }
    
    public void write(MultiLineString multi,LEDataOutputStream file)throws IOException{
        
        file.setLittleEndianMode(true);
        file.writeInt(getShapeType());
        
        Envelope box = multi.getEnvelopeInternal();
        file.writeDouble(box.getMinX());
        file.writeDouble(box.getMinY());
        file.writeDouble(box.getMaxX());
        file.writeDouble(box.getMaxY());
        
        int numParts = multi.getNumGeometries();
        
        file.writeInt(numParts);
        file.writeInt(multi.getNumPoints());
        
        LineString[] lines = new LineString[numParts];
        
        for(int i = 0;i<numParts;i++){
            lines[i] = (LineString)multi.getGeometryN(i);
            file.writeInt(lines[i].getNumPoints());
        }
        
        for(int part = 0;part<numParts;part++){
            Coordinate[] points = lines[part].getCoordinates();
            for(int i = 0;i<points.length;i++){
                file.writeDouble(points[i].x);
                file.writeDouble(points[i].y);
            }
        }
    }
    
    /**
     * Get the type of shape stored (Shapefile.ARC)
     */
    public static int getShapeType(){
        return Shapefile.ARC;
    }
    
    public static int getLength(MultiLineString multi){
        
        return (44+(4*multi.getNumGeometries()));
    }
    
}

/*
 * $Log: ShapeMultiLine.java,v $
 * Revision 1.1  2002/02/11 16:54:43  jmacgill
 * added shapefile code and directories
 *
 */
