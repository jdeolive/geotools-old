package org.geotools.shapefile;

import java.io.*;
import cmp.LEDataStream.*;

/**
 * Wrapper for a Shapefile arc.
 */
public class ShapeArcM implements ShapefileShape,Serializable {
    protected double[] box = new double[4];
    protected int numParts;
    protected int numPoints;
    protected int[] parts;
    protected ShapePoint[] points;
    protected double[] mRange = new double[2];
    protected double[] mArray = null;

    protected ShapeArcM(){}//For use by ShapePolygon

    public ShapeArcM(LEDataInputStream file) throws IOException{

        file.setLittleEndianMode(true);
        int shapeType = file.readInt();

        for(int i = 0;i<4;i++){
           box[i] = file.readDouble();
        }

        numParts = file.readInt();
        numPoints = file.readInt();

        parts = new int[numParts];
        points = new ShapePoint[numPoints];
        mArray = new double[numPoints];

        for(int i = 0;i<numParts;i++){
             parts[i]=file.readInt();
        }

        for(int i = 0;i<numPoints;i++){
            double x = file.readDouble();
            double y = file.readDouble();
            points[i] = new ShapePoint(x,y);
        }

        // measured data
        for(int i = 0;i<2;i++){
           mRange[i] = file.readDouble();
        }

        for(int i = 0;i<numPoints;i++){
           mArray[i] = file.readDouble();
        }

    }
    
    public ShapeArcM(double[] box,int[] parts,ShapePoint[] points){
        this.box = box;
        this.parts = parts;
        this.numParts = parts.length;
        this.points = points;
        this.numPoints = points.length;
    }
    
    public void write(LEDataOutputStream file)throws IOException{

        file.setLittleEndianMode(true);
        file.writeInt(Shapefile.ARC_M);
        

        for(int i = 0;i<4;i++){
           file.writeDouble(box[i]);
        }

        file.writeInt(numParts);
        file.writeInt(numPoints);

        //parts = new int[numParts];
        //points = new ShapePoint[numPoints];

        for(int i = 0;i<numParts;i++){
             file.writeInt(parts[i]);
        }

        for(int i = 0;i<numPoints;i++){
            file.writeDouble(points[i].x);
            file.writeDouble(points[i].y);
        }

        // measured data

        for(int i = 0;i<2;i++){
           file.writeDouble(mRange[i]);
        }

        for(int i = 0;i<numPoints;i++){
           file.writeDouble(mArray[i]);
        }
    }

    /**
     * Find out how many parts make up this arc.
     * @return The number of parts in this arc
     */
    public int getNumParts(){
        return numParts;
    }
    
    /**
     * Find out how many points make up the entire of this arc
     * @return The number of points in this arc
     */
    public int getNumPoints(){
        return numPoints;
    }
    
    /**
     * Get a copy of ALL the points that make up this arc
     * @return Array ShapePoints
     */
    public ShapePoint[] getPoints(){
        return points;
    }
    
    /**
     * Gets an array of indexes to the start of each part in the point array
     * returned by getPoints.
     * @return array of indexs
     * @see #getPoints();
     */
    public int[] getPartOffsets(){
        return parts;
    }

    /**
     * Get all the points for a given part<p>
     * a non-existent part returns <b>null</b> (would you prefer an exception?)
     * @param part id of part,[first is 0]
     */
     public ShapePoint[] getPartPoints(int part){
        if(part>numParts-1){return null;}
        
        int start,finish,length;
        
        start = parts[part];
        if(part == numParts-1){finish = numPoints;}
        else
        {
            finish=parts[part+1];
        }
        length = finish-start;
        
        
        ShapePoint[] partPoints = new ShapePoint[length];
        for(int i =0;i<length;i++){
            partPoints[i]=points[i+start];
        }
        
        return partPoints;
     }

    /**
     * Find the bounding box for this shape
     * @return double array in form {xMin,yMin,xMax,yMax}
     */
    public double[] getBounds(){
        return box;
    }

    /**
     * Get the type of shape stored (Shapefile.ARC)
     */
    public int getShapeType(){
        return Shapefile.ARC_M;
    }
    
    public int getLength(){
        return (44+(4*numParts)+16+(16*numParts));
    }

    /**
     * Get the measured data
     */

    public double[] getRange(){
      return mRange;
    }

    public double[] getMeasures(){
      return mArray;
    }
}