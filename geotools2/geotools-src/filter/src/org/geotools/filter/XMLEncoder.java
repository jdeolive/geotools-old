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

package org.geotools.filter;

import org.apache.log4j.Logger;
/**
 *
 * @author  jamesm
 */
public class XMLEncoder implements org.geotools.filter.FilterVisitor {
    
     private static Logger log = Logger.getLogger("filter");

    
    /** Creates a new instance of XMLEncoder */
    public XMLEncoder() {
    }
    
    public void visit(Filter filter) {
        log.warn("exporting unknown filter type");
    }
    
    public void visit(LogicFilter filter){
        log.debug("exporting LogicFilter");
    }
    
    public void visit(CompareFilter filter){
        log.debug("exporting ComparisonFilter");
    }
    
    public void visit(GeometryFilter filter){
        log.debug("exporting GeometryFilter");
    }
}
