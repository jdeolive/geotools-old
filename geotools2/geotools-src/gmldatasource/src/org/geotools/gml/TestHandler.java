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

import java.io.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.vividsolutions.jts.geom.*;

/**
 * Simple test implementation of <code>GMLHandlerJTS</code>.
 *
 * This very simple handler just prints every JTS geometry that it gets to the
 * standard output.
 * 
 * @version $Id: TestHandler.java,v 1.4 2002/07/12 17:33:36 loxnard Exp $
 * @author Rob Hranac, Vision for New York
 */
public class TestHandler extends XMLFilterImpl implements GMLHandlerJTS {
    
    
    public void geometry(Geometry geometry) {
        System.out.println("here is the geometry: " + geometry.toString());
    }
    
}
