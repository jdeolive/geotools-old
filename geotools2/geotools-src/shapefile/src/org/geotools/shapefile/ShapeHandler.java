package org.geotools.shapefile;
import cmp.LEDataStream.*;
import com.vividsolutions.jts.geom.*;

public interface ShapeHandler {
    public int getShapeType();
    public Geometry read(LEDataInputStream file,GeometryFactory geometryFactory) throws java.io.IOException,TopologyException,InvalidShapefileException;
    public void write(Geometry geometry,LEDataOutputStream file) throws java.io.IOException;
    public int getLength(Geometry geometry); //length in 16bit words
}
