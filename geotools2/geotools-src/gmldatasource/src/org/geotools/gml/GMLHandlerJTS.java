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
 * LEVEL3 saxGML4j GML handler: Gets JTS objects.
 *
 * <p>This handler must be implemented by the parent of a GMLFilterGeometry
 * filter in order to handle the JTS objects passed to it from the child.</p>
 *
 * @version $Id: GMLHandlerJTS.java,v 1.6 2002/07/12 17:00:42 loxnard Exp $
 * @author Rob Hranac, Vision for New York
 */
public interface GMLHandlerJTS extends org.xml.sax.ContentHandler {
    
    
    /**
     * Receives OGC simple feature type geometry from parent.
     * @param geometry the simple feature geometry
     */
    void geometry(com.vividsolutions.jts.geom.Geometry geometry);
    
    
}
