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
 * Wrapper for a Shapefile arc.
 *
 * @version $Id: MultiLineHandler.java,v 1.3 2002/06/05 12:49:03 loxnard Exp $
 * @author James Macgill, CCG
 */
public class MultiLineHandler implements ShapeHandler{
    public MultiLineHandler(){};
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
        return geometryFactory.createMultiLineString(lines);
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
     * Gets the type of shape stored (Shapefile.ARC)
     */
    public int getShapeType(){
        return Shapefile.ARC;
    }
    
    public int getLength(Geometry geometry){
        
        return (44+(4*((GeometryCollection)geometry).getNumGeometries()));
    }
    
}

/*
 * $Log: MultiLineHandler.java,v $
 * Revision 1.3  2002/06/05 12:49:03  loxnard
 * Added licence statement and cvs id tag
 *
 * Revision 1.2  2002/03/05 10:23:59  jmacgill
 * made sure geometries were created using the factory methods
 *
 * Revision 1.1  2002/02/28 00:38:50  jmacgill
 * Renamed files to more intuitve names
 *
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
