/*
 * RecordHeader.java
 *
 * Created on February 12, 2002, 3:33 PM
 */

package org.geotools.shapefile;


import cmp.LEDataStream.*;
import java.io.*;

/**
 *
 * @author  jamesm
 */
public class ShapeRecord implements Serializable {
    RecordHeader header;
    ShapefileShape shape;
    int mainindex = -1;
    
    public ShapeRecord(RecordHeader header,ShapefileShape shape){
        this.header=header;
        this.shape=shape;
    }
    
    public ShapeRecord(int index,ShapefileShape shape){
        this.header = new RecordHeader(index,shape);
        this.shape = shape;
    }
    
    public int getShapeType(){
        return shape.getShapeType();
    }
    
    public ShapefileShape getShape(){
        return shape;
    }
}
