/*
 * MinFunction.java
 *
 * Created on 28 July 2002, 16:03
 */

package org.geotools.filter;

import org.geotools.feature.Feature;
import org.geotools.algorithms.RobustGeometryProperties;

import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author  James
 */
public class AreaFunction extends org.geotools.filter.FunctionExpression {
    
    /**
     * Holds the geometry to calculate the area of
     */
    private Expression geom;
    
    /**
     * Instance of algorithms to use when calculating the area
     */
    private RobustGeometryProperties calc;
    
    /** Creates a new instance of AreaFunction */
    public AreaFunction() {
         calc = new RobustGeometryProperties();
    }
    
    
    
    /** Returns a value for this expression.
     *
     * @param feature Specified feature to use when returning value.
     * @return Value of the feature object.
     */
    public Object getValue(Feature feature) {
        Geometry g = (Geometry)geom.getValue(feature);
       
        return new Double(calc.getArea(g));
    }
    
    public int getArgCount() {
        return 1;
    }
    
    public String getName() {
        return "Area";
    }
    
    public void setArgs(Expression[] args) {
        geom = args[0];
    }
    
}
