/*
 * XMLEncoder.java
 *
 * Created on 22 July 2002, 17:12
 */

package org.geotools.filter;

import org.geotools.filter;

/**
 *
 * @author  jamesm
 */
public class XMLEncoder implements org.geotools.filter.FilterVisitor {
    
    /** Creates a new instance of XMLEncoder */
    public XMLEncoder() {
    }
    
    public void visit(Filter filter) {
        //unknown filter type!
    }
    
    public void visit(LogicFilter filter){
        
    }
    public void visit(ComparisonFilter filter){
        
    }
    
    public void visit(GeometryFilter filter){
        
    }
}
