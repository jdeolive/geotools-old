/*
 * FilterVisitor.java
 *
 * Created on 22 July 2002, 16:50
 */

package org.geotools.filter;

/**
 *
 * @author  jamesm
 */
public interface FilterVisitor {
    
    void visit(Filter filter);
    
}
