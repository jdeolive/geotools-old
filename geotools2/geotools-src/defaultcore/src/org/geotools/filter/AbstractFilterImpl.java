/*
 * AbstractFilterImpl.java
 *
 * Created on 23 October 2002, 17:19
 */

package org.geotools.filter;

/**
 *
 * @author  iant
 */
public abstract class AbstractFilterImpl extends org.geotools.filter.AbstractFilter {
   
    
    /**
     * Default implementation for OR - should be sufficient for most filters.
     *
     * @param filter Parent of the filter: must implement GMLHandlerGeometry.
     * @return ORed filter.
     */
    public Filter or(Filter filter) {
        try {
            return new LogicFilterImpl(this, filter, LOGIC_OR);
        }
        catch (IllegalFilterException e) {
            return filter;
        }
    }
    
    /**
     * Default implementation for AND - should be sufficient for most filters.
     *
     * @param filter Parent of the filter: must implement GMLHandlerGeometry.
     * @return ANDed filter.
     */
    public Filter and(Filter filter) {
        try {
            return new LogicFilterImpl(this, filter, LOGIC_AND);
        }
        catch (IllegalFilterException e) {
            return filter;
        }
    }
    
    /**
     * Default implementation for NOT - should be sufficient for most filters.
     *
     * @return NOTed filter.
     */
    public Filter not() {
        try {
            return new LogicFilterImpl(this, LOGIC_NOT);
        }
        catch (IllegalFilterException e) {
            return this;
        }
    }
    
    
}
