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
public class AreaFunction implements org.geotools.filter.FunctionExpression { 
    
    /**
     * Holds the geometry to calculate the area of
     */
    private Expression geom;
    private Expression[] args;
    /**
     * Instance of algorithms to use when calculating the area
     */
    private RobustGeometryProperties calc;
    
    /** Creates a new instance of AreaFunction */
    public AreaFunction() {
         calc = new RobustGeometryProperties();
    }
    
    public short getType(){
        return DefaultExpression.FUNCTION;
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
        this.args = args;
    }
    
    /** Used by FilterVisitors to perform some action on this filter instance.
     * Typicaly used by Filter decoders, but may also be used by any thing which needs
     * infomration from filter structure.
     *
     * Implementations should always call: visitor.visit(this);
     *
     * It is importatant that this is not left to a parent class unless the parents
     * API is identical.
     *
     * @param visitor The visitor which requires access to this filter,
     *                the method must call visitor.visit(this);
     *
     *
     */
    public void accept(FilterVisitor visitor) {
         visitor.visit(this);
    }
    
    public Expression[] getArgs() {
        return args;
    }
    
}
