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
import com.vividsolutions.jts.algorithm.*;
import java.util.ArrayList;

/**
 * Wrapper for a Shapefile polygon.
 *
 * @version $Id: PolygonHandler.java,v 1.6 2002/07/16 22:10:39 jmacgill Exp $
 * @author James Macgill, CCG
 */
public class PolygonHandler implements ShapeHandler{
    protected static CGAlgorithms cga = new RobustCGAlgorithms();
    
    public PolygonHandler(){};
    
    public Geometry read(LEDataInputStream file, GeometryFactory geometryFactory)
    throws IOException, InvalidShapefileException, TopologyException {
        
        file.setLittleEndianMode(true);
        int shapeType = file.readInt();
        if (shapeType != Shapefile.POLYGON) {
            throw new InvalidShapefileException
            ("Error: Attempt to load non polygon shape as polygon.");
        }
        double[] box = new double[4];
        for (int i = 0; i < 4; i++) {
            box[i] = file.readDouble();
        }//read and for now ignore bounds.
        
        int partOffsets[];
        
        int numParts = file.readInt();
        int numPoints = file.readInt();
        
        partOffsets = new int[numParts];
        
        for (int i = 0; i < numParts; i++){
            partOffsets[i] = file.readInt();
        }
        //LinearRing[] rings = new LinearRing[numParts];
        ArrayList shells = new ArrayList();
        ArrayList holes = new ArrayList();
        
        int start, finish, length;
        for (int part = 0; part < numParts; part++){
            start = partOffsets[part];
            if (part == numParts - 1){finish = numPoints;}
            else {
                finish = partOffsets[part + 1];
            }
            length = finish - start;
            Coordinate points[] = new Coordinate[length];
            for (int i = 0; i < length; i++){
                points[i] = new Coordinate(file.readDouble(), file.readDouble());
            }
            LinearRing ring = geometryFactory.createLinearRing(points);
            if (cga.isCCW(points)){
                holes.add(ring);
            }
            else {
                shells.add(ring);
            }
        }
        
        //now we have a list of all shells and all holes
        ArrayList holesForShells = new ArrayList(shells.size());
        for (int i = 0; i < shells.size(); i++){
            holesForShells.add(new ArrayList());
        }
        
        //find homes
        for (int i = 0; i < holes.size(); i++){
            LinearRing testRing = (LinearRing) holes.get(i);
            LinearRing minShell = null;
            Envelope minEnv = null;
            Envelope testEnv = testRing.getEnvelopeInternal();
            Coordinate testPt = testRing.getCoordinateN(0);
            LinearRing tryRing;
            for (int j = 0; j < shells.size(); j++){
                tryRing = (LinearRing) shells.get(j);
                Envelope tryEnv = tryRing.getEnvelopeInternal();
                if (minShell != null) {
                    minEnv = minShell.getEnvelopeInternal();
                }
                boolean isContained = false;
                Coordinate[] coordList = tryRing.getCoordinates() ;
                if (tryEnv.contains(testEnv)
                && (cga.isPointInPolygon(testPt,coordList ) ||
                (pointInList(testPt,coordList)))) {
                    isContained = true;
                }
                // check if this new containing ring is smaller than the
                // current minimum ring
                if (isContained) {
                    if (minShell == null
                    || minEnv.contains(tryEnv)) {
                        minShell = tryRing;
                    }
                }
            }
            
            ((ArrayList) holesForShells.get(shells.indexOf(minShell))).add(testRing);
        }
        
        Polygon[] polygons = new Polygon[shells.size()];
        for (int i = 0; i < shells.size(); i++){
            polygons[i] = geometryFactory.createPolygon((LinearRing) shells.get(i), (LinearRing[])((ArrayList) holesForShells.get(i)).toArray(new LinearRing[0]));
        }
        
        if (polygons.length == 1){
            return polygons[0];
        }
        //it's a multi part
        return geometryFactory.createMultiPolygon(polygons);
    }
    
    public void write(Geometry geometry, LEDataOutputStream file)throws IOException{
        GeometryCollection multi;
        if(geometry instanceof GeometryCollection){
            multi = (GeometryCollection) geometry;
        }
        else {
            multi = new MultiPolygon(new Polygon[]{(Polygon) geometry}, geometry.getPrecisionModel(), geometry.getSRID());
        }
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
    
    
    public int getShapeType(){
        return Shapefile.POLYGON;
    }
    public int getLength(Geometry geometry){
        int numParts;
        if (geometry instanceof MultiPolygon){
            numParts = ((MultiPolygon) geometry).getNumGeometries();
        }
        else {
            numParts = 1;
        }
        return (22 + (2 * numParts) + geometry.getNumPoints() * 8);
    }
    
    /**
     * Test if a point is in a list of coordinates
     * @param testPoint the point to test for
     * @param pointList the list of points to look through
     * @return true if testPoint is a point in the pointList list.
     */
    private boolean pointInList(Coordinate testPoint, Coordinate[] pointList) {
        int t, numpoints;
        Coordinate  p;
        
        numpoints = pointList.length;
        //TODO: check if nan case needs to be delt with
        //TODO: check if coordinate.equals3D or equals2D can be used instead.
        for (t=0;t<numpoints; t++) {
            p = pointList[t];
            if ( (testPoint.x == p.x) && (testPoint.y == p.y) &&
            ((testPoint.z == p.z) ||
            (!(testPoint.z == testPoint.z)))  //nan test; x!=x iff x is nan
            ) {
                return true;
            }
        }
        return false;
    }
}

/*
 * $Log: PolygonHandler.java,v $
 * Revision 1.6  2002/07/16 22:10:39  jmacgill
 * Applied bug fix patch supplied by Dave Blasby
 *
 * The bug occured if the first point in a hole's ring was on the boundary of
 * an exterior ring.
 *
 * Revision 1.5  2002/07/12 13:55:35  loxnard
 *
 * Updated javadocs.
 *
 * Revision 1.4  2002/06/05 12:51:20  loxnard
 *
 * Added licence statement and cvs id tag
 *
 * Revision 1.3  2002/03/05 10:51:01  andyt
 * removed use of factory from write method
 *
 * Revision 1.2  2002/03/05 10:23:59  jmacgill
 * made sure geometries were created using the factory methods
 *
 * Revision 1.1  2002/02/28 00:38:50  jmacgill
 * Renamed files to more intuitve names
 *
 * Revision 1.4  2002/02/13 00:23:53  jmacgill
 * First semi working JTS version of Shapefile code
 *
 * Revision 1.3  2002/02/11 18:44:22  jmacgill
 * replaced geometry constructions with calls to geometryFactory.createX methods
 *
 * Revision 1.2  2002/02/11 18:28:41  jmacgill
 * rewrote to have static read and write methods
 *
 * Revision 1.1  2002/02/11 16:54:43  jmacgill
 * added shapefile code and directories
 *
 */
