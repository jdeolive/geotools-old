package org.geotools.shapefile;

import java.io.*;
import cmp.LEDataStream.*;
import com.vividsolutions.jts.geom.*;

/**
 * Wrapper for a Shapefile point.
 */
public class ShapePoint  implements ShapefileShape{
    
    public Geometry read(LEDataInputStream file,GeometryFactory geometryFactory) throws IOException,TopologyException,InvalidShapefileException{
        file.setLittleEndianMode(true);
        int shapeType = file.readInt();
        double x = file.readDouble();
        double y = file.readDouble();
        return geometryFactory.createPoint(new Coordinate(x,y));
    }
    
    public void write(Geometry geometry,LEDataOutputStream file)throws IOException{
        file.setLittleEndianMode(true);
        file.writeInt(getShapeType());
        Coordinate c = geometry.getCoordinates()[0];
        file.writeDouble(c.x);
        file.writeDouble(c.y);
    }
    
    /**
     * Returns the shapefile shape type value for a point
     * @return int Shapefile.POINT
     */
    public  int getShapeType(){
        return Shapefile.POINT;
    }
    
    /**
     * Calcuates the record length of this object.
     * @return int The length of the record that this shapepoint will take up in a shapefile
     **/
    public int getLength(Geometry geometry){
        return 10;//the length of two doubles in 16bit words + the shapeType
    }
}