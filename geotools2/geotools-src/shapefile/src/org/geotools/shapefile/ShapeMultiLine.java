/*
 * $Id: ShapeMultiLine.java,v 1.3 2002/02/13 00:23:53 jmacgill Exp $
 *
 */

package org.geotools.shapefile;

import java.io.*;
import cmp.LEDataStream.*;
import com.vividsolutions.jts.geom.*;

/**
 * Wrapper for a Shapefile arc.
 */
public class ShapeMultiLine implements ShapefileShape{
    public ShapeMultiLine(){};
    public Geometry read( LEDataInputStream file , GeometryFactory geometryFactory) throws IOException,TopologyException,InvalidShapefileException {
        file.setLittleEndianMode(true);
        int shapeType = file.readInt();//ignored
        double box[] = new double[4];
        for ( int i = 0; i<4; i++ ){
            box[i] = file.readDouble();
        }//we don't need the box....
        
        int numParts = file.readInt();
        int numPoints = file.readInt();//total number of points
        
        int[] partOffsets = new int[numParts];
        
        //points = new Coordinate[numPoints];
        
        for ( int i = 0; i < numParts; i++ ){
            partOffsets[i]=file.readInt();
        }
        
        LineString lines[] = new LineString[numParts];
        int start,finish,length;
        for(int part=0;part<numParts;part++){
            start = partOffsets[part];
            if(part == numParts-1){finish = numPoints;}
            else {
                finish=partOffsets[part+1];
            }
            length = finish-start;
            Coordinate points[] = new Coordinate[length];
            for(int i=0;i<length;i++){
                points[i]=new Coordinate(file.readDouble(),file.readDouble());
            }
            lines[part] = geometryFactory.createLineString(points);
            
        }
        return new MultiLineString(lines,null,-1);
    }
    
    public void write(Geometry geometry,LEDataOutputStream file)throws IOException{
        MultiLineString multi = (MultiLineString)geometry;
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
    public int getShapeType(){
        return Shapefile.ARC;
    }
    
    public int getLength(Geometry geometry){
        
        return (44+(4*((GeometryCollection)geometry).getNumGeometries()));
    }
    
}

/*
 * $Log: ShapeMultiLine.java,v $
 * Revision 1.3  2002/02/13 00:23:53  jmacgill
 * First semi working JTS version of Shapefile code
 *
 * Revision 1.2  2002/02/11 18:42:45  jmacgill
 * changed read and write statements so that they produce and take Geometry objects instead of specific MultiLine objects
 * changed parts[] array name to partOffsets[] for clarity and consistency with ShapePolygon
 *
 * Revision 1.1  2002/02/11 16:54:43  jmacgill
 * added shapefile code and directories
 *
 */
