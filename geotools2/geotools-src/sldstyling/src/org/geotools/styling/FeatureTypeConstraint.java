/*
 * FeatureTypeConstraint.java
 *
 * Created on November 3, 2003, 10:43 AM
 */

package org.geotools.styling;

import org.geotools.filter.Filter;

/**
 *
 * @author  jamesm
 */
public interface FeatureTypeConstraint {
    public String getFeatureTypeName();
    public void setFeatureTypeName(String name);
    
    public Filter getFilter();
    public void setFilter(Filter filter);
    
   // public 
}
