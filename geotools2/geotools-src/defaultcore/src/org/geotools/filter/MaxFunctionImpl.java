/*
 * MinFunction.java
 *
 * Created on 28 July 2002, 16:03
 */

package org.geotools.filter;

import org.geotools.feature.Feature;

/**
 *
 * @author  James
 */
public class MaxFunctionImpl extends MathExpressionImpl implements MaxFunction {
    
    Expression a,b;
    
    /** Creates a new instance of MinFunction */
    public MaxFunctionImpl() { 
    }
    
    
    
    /** Returns a value for this expression.
     *
     * @param feature Specified feature to use when returning value.
     * @return Value of the feature object.
     */
    public Object getValue(Feature feature) {
        double first = ((Number)a.getValue(feature)).doubleValue();
        double second = ((Number)b.getValue(feature)).doubleValue();
        return new Double(Math.max(first,second));
    }
    
    public int getArgCount() {
        return 2;
    }
    
    public String getName() {
        return "Max";
    }
    
    public void setArgs(Expression[] args) {
        a = args[0];
        b = args[1];
    }
    
}
