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
import cmp.LEDataStream.*;
import com.vividsolutions.jts.geom.*;

/**
 * @version $Id: ShapeHandler.java,v 1.3 2002/07/12 13:57:59 loxnard Exp $
 * @author James Macgill, CCG
 */
public interface ShapeHandler {
    int getShapeType();
    Geometry read(LEDataInputStream file, GeometryFactory geometryFactory) throws java.io.IOException, TopologyException, InvalidShapefileException;
    void write(Geometry geometry, LEDataOutputStream file) throws java.io.IOException;
    int getLength(Geometry geometry); //length in 16bit words
}
