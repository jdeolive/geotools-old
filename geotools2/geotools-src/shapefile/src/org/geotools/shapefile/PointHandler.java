/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; 
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 */

package org.geotools.shapefile;

import java.io.*;
import cmp.LEDataStream.*;
import com.vividsolutions.jts.geom.*;

/**
 * Wrapper for a Shapefile point.
 *
 * @version $Id: PointHandler.java,v 1.2 2002/06/05 12:50:16 loxnard Exp $
 * @author James Macgill, CCG
 */
public class PointHandler implements ShapeHandler{
    
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
     * Returns the shapefile shape type value for a point.
     * @return int Shapefile.POINT
     */
    public  int getShapeType(){
        return Shapefile.POINT;
    }
    
    /**
     * Calculates the record length of this object.
     * @return int The length of the record that this shapepoint will take up
     * in a shapefile.
     */
    public int getLength(Geometry geometry){
        return 10;//the length of two doubles in 16bit words + the shapeType
    }
}