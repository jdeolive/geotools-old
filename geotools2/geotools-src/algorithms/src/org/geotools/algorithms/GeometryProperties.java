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

package org.geotools.algorithms;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @version $Id: GeometryProperties.java,v 1.6 2004/02/24 10:50:37 aaime Exp $
 * @author James Macgill, CCG
 */
public interface GeometryProperties {
    
    /**
     * Calculates and returns the area of the specified geometry.<br>
     * For Polygons, this is the total area inside the external ring less
     * the total of any contained by interior rings.  GeometryCollections
     * (including MultiPolygons) are iterated through so the result is the
     * sum of all polygons anywhere within the collection.
     * Any geometry other than Polgyon or a collection returns 0;
     *
     * @param g The Geometry to calculate the area of.
     * @return The total area of the Geometry.
     */
    double getArea(Geometry g);
    
    /**
     * Calculates and returns the perimeter of the specified geometry.<br>
     * For Polygons, this is the total length of the exterior ring and all
     * internal rings.  For LineStrings, the total line length is returned.
     * GeometryCollections are iterated through so the result is the
     * sum of all Polygon and Line geometries anywhere within the collection.
     * Any point geometries return a value of 0;
     *
     * @param g The Geometry to calculate the area of.
     * @return The total area of the Geometry.
     */
    double getPerimeter(Geometry g);
}

