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


/**
 * Creates the appropriate SubHandler element for a given OGC simple geometry
 * type.
 *
 * @version $Id: SubHandlerFactory.java,v 1.5 2002/07/12 16:59:55 loxnard Exp $
 * @author Rob Hranac, Vision for New York
 */
public class SubHandlerFactory {
    
    
    /** List of all valid OGC multi geometry types. */
    private static final java.util.Collection BASE_GEOMETRY_TYPES = 
        new java.util.Vector(java.util.Arrays.asList(
                             new String[] {"MultiPoint", "MultiLineString", "MultiPolygon"}) 
        );
    
    
    /**
     * Empty constructor.
     */
    public SubHandlerFactory() {
    }
    
    /**
     * Creates a new SubHandler, based on the appropriate OGC simple geometry
     * type.  Note that some types are aggregated into a generic 'multi' type.
     *
     * @param type Type of SubHandler to return.
     */
    public SubHandler create(String type) {
        
        if (type.equals("Point")){
            return new SubHandlerPoint(); 
        } else if (type.equals("LineString")){
            return new SubHandlerLineString(); 
        } else if (type.equals("LinearRing")){
            return new SubHandlerLinearRing(); 
        } else if (type.equals("Polygon")){
            return new SubHandlerPolygon(); 
        } else if (type.equals("Box")){
            return new SubHandlerBox(); 
        } else if (BASE_GEOMETRY_TYPES.contains(type)) {
            return new SubHandlerMulti(); 
        } else{
            return null; 
        }
    }
    
    
}

