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


import java.util.logging.Logger;


/**
 * Creates the appropriate SubHandler element for a given OGC simple geometry
 * type.
 *
 * @version $Id: SubHandlerFactory.java,v 1.6 2003/08/05 16:56:31 dledmonds Exp $
 * @author Rob Hranac, Vision for New York
 */
public class SubHandlerFactory {

    /** The logger for the GML module */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.gml");
    
    /** List of all valid OGC multi geometry types. */
    private static final java.util.Collection BASE_GEOMETRY_TYPES = 
        new java.util.Vector(java.util.Arrays.asList(
                             new String[] {"MultiPoint", "MultiLineString", "MultiPolygon"}) 
        );
    
    
    /**
     * Empty constructor.
     */
    public SubHandlerFactory() {
        LOGGER.entering("SubHandlerFactory", "new" );
        LOGGER.exiting("SubHandlerFactory", "new" );
    }
    
    /**
     * Creates a new SubHandler, based on the appropriate OGC simple geometry
     * type.  Note that some types are aggregated into a generic 'multi' type.
     *
     * @param type Type of SubHandler to return.
     * @TODO throw an exception, not return a null
     */
    public SubHandler create(String type) {
        LOGGER.entering("SubHandlerFactory", "create", type );
        
        SubHandler returnValue = null;
        
        if (type.equals("Point")){
            returnValue = new SubHandlerPoint(); 
        } else if (type.equals("LineString")){
            returnValue = new SubHandlerLineString(); 
        } else if (type.equals("LinearRing")){
            returnValue = new SubHandlerLinearRing(); 
        } else if (type.equals("Polygon")){
            returnValue = new SubHandlerPolygon(); 
        } else if (type.equals("Box")){
            returnValue = new SubHandlerBox(); 
        } else if (BASE_GEOMETRY_TYPES.contains(type)) {
            returnValue = new SubHandlerMulti(); 
        } else{
            returnValue = null; // should be throwing an exception here!
        }
        
        LOGGER.exiting("SubHandlerFactory", "create", returnValue );
        return returnValue;
    }
    
    
}
