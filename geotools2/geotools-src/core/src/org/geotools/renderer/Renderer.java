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
 *
 * Contacts:
 *     UNITED KINDOM: James Macgill.  j.macgill@geog.leeds.ac.uk
 */
package org.geotools.renderer;
import org.geotools.styling.*;
import org.geotools.map.Map;
import org.geotools.datasource.Extent;

/**
 * this is very much work in progress
 * @author  jamesm
 */
public interface Renderer {

    //what do we need to do here
    //what makes sence
    
    //we could pass in somehting like
    
    public void render(Map map,Extent visible);
    
    //is that enough to go on? lets assume it is.
}

