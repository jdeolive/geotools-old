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

import cmp.LEDataStream.LEDataInputStream;
import cmp.LEDataStream.LEDataOutputStream;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryCollection;
import java.io.IOException;



/**
 * Wrapper for a Shapefile arc.
 *
 * @version $Id: MultiLineHandler.java,v 1.5 2002/07/11 18:10:23 jmacgill Exp $
 * @author James Macgill, CCG
 */
public class MultiLineHandler implements ShapeHandler{
    /**
     * default constructor, currently empty.
     */
    public MultiLineHandler(){};
    
    /**
     * Reads and constructs a MultiLineString from the inputstream.
     * The input stream should be in the right position before this method
     * is called.
     * @param file An inputstream attached to a shapefile
     * @param geometryFactory The factory to use when constructing
     *        the MultiLineString
     * @throws IOException if anything goes wrong whilst reading from the file
     * @throws TopologyException if the MultiLineString in the shapefile
     *         cannot be represented within the Simple Feature Specifications
     * @throws InvalidShapefileException if unexpected content is encountered
     *         for example, if the type of the next shape in the Shapefile is
     *         not actualy a MultiLineString
     * @return The construced MultiLineString as a Geometry
     */
    public Geometry read(LEDataInputStream file, 
                         GeometryFactory geometryFactory)
    throws IOException, TopologyException, InvalidShapefileException {
        file.setLittleEndianMode(true);
        int shapeType = file.readInt();//ignored
        double box[] = new double[4];
        for (int i = 0; i < 4; i++){
            box[i] = file.readDouble();
        }//we don't need the box....
        
        int numParts = file.readInt();
        int numPoints = file.readInt();//total number of points
        
        int[] partOffsets = new int[numParts];
        
        //points = new Coordinate[numPoints];
        
        for (int i = 0; i < numParts; i++){
            partOffsets[i] = file.readInt();
        }
        
        LineString lines[] = new LineString[numParts];
        int start, finish, length;
        for (int part = 0; part < numParts; part++){
            start = partOffsets[part];
            if (part == numParts - 1){
                finish = numPoints;
            }
            else {
                finish = partOffsets[part + 1];
            }
            length = finish - start;
            Coordinate points[] = new Coordinate[length];
            for (int i = 0; i < length; i++){
                points[i] = new Coordinate(file.readDouble(),
                                           file.readDouble());
            }
            lines[part] = geometryFactory.createLineString(points);
            
        }
        return geometryFactory.createMultiLineString(lines);
    }
    
    /**
     * Writes a MultiLineString to the outputstream.
     * The output stream should be in the right position before this method
     * is called.
     * @param geometry the MultiLineString to write out.
     * @param file An ledataoutputstream attached to a shapefile
     * @throws IOException if anything goes wrong whilst writing to the file
     */
    public void write(Geometry geometry, LEDataOutputStream file)
    throws IOException{
        MultiLineString multi = (MultiLineString) geometry;
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
        
        for (int i = 0; i < numParts; i++){
            lines[i] = (LineString) multi.getGeometryN(i);
            file.writeInt(lines[i].getNumPoints());
        }
        
        for (int part = 0; part < numParts; part++){
            Coordinate[] points = lines[part].getCoordinates();
            for (int i = 0; i < points.length; i++){
                file.writeDouble(points[i].x);
                file.writeDouble(points[i].y);
            }
        }
    }
    
    /**
     * Gets the type of shape stored (Shapefile.ARC)
     * @return int The constant Shapefile.ARC
     */
    public int getShapeType(){
        return Shapefile.ARC;
    }
    
    /**
     * Gets the length (in terms of file length) of the record entry
     * @param geometry The MultiLineString to calculate the record length of
     * @return int The length of the header entry.
     */
    public int getLength(Geometry geometry){
        return (44 + (4 * ((GeometryCollection) geometry).getNumGeometries()));
    }
}

/*
 * $Log: MultiLineHandler.java,v $
 * Revision 1.5  2002/07/11 18:10:23  jmacgill
 * fixed javadoc errors
 *
 * Revision 1.4  2002/07/11 17:25:55  jmacgill
 *
 * updated javadocs
 *
 * Revision 1.3  2002/06/05 12:49:03  loxnard
 *
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
