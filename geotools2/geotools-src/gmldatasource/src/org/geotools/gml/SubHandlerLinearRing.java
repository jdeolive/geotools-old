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

package org.geotools.gml;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.gml.*;

/** 
 * Creates a simple OGC LinearRing (a closed LineString).
 * 
 * @version $Id: SubHandlerLinearRing.java,v 1.4 2002/06/05 11:36:45 loxnard Exp $
 * @author Ian Turton, CCG
 * @author Rob Hranac, Vision for New York
 */
public class SubHandlerLinearRing extends SubHandler {


		/** Internal coordinate list. */
    private ArrayList coordinateList = new ArrayList();


    /** Creates a new instance of GMLLinearRingHandler. */
    public SubHandlerLinearRing() {
    }

    /**
     * Adds a coordinate to the LinearRing.
     *
     * @param coordinate The coordinate to add to the LinearRing.
     */    
    public void addCoordinate(Coordinate coordinate) {
        coordinateList.add(coordinate);
    }
    

    /**
     * Determine whether or not this LinearRing is ready to be created.
     *
     * @param message The current geometry type in the GML stream.
     * @return Ready for creation flag.
     */    
    public boolean isComplete(String message){

				// makes sure that this LinearRing has more than one coordinate and its first and last are identical
				if( coordinateList.size() > 1 ) {
						Coordinate firstCoordinate = (Coordinate) coordinateList.get(0);
						Coordinate lastCoordinate = (Coordinate) coordinateList.get( coordinateList.size() - 1 );
						if( lastCoordinate.equals2D(firstCoordinate) ) { 
								return true;
						} else {
								return false;
						}
				} else { 
						return false; 
				}
    }


    /** 
     * Create the LinearRing.
     *
     * @param geometryFactory The geometry factory used for the build.
     * @return LinearRing geometry created.
     */    
    public Geometry create(GeometryFactory geometryFactory) {
        try{
            return geometryFactory.createLinearRing((Coordinate[])coordinateList.toArray(new Coordinate[]{}));
        } catch(TopologyException e){
            System.err.println("Caught Topology exception in GMLLinearRingHandler");
            return null;
        }
    }
    
}
