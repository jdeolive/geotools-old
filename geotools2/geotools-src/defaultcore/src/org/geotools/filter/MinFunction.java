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
public class MinFunction extends FunctionExpressionImpl implements FunctionExpression {
    
    Expression a,b;
    Expression[] args;
    /** Creates a new instance of MinFunction */
    protected MinFunction() { 
    }
    
    
    
    /** Returns a value for this expression.
     *
     * @param feature Specified feature to use when returning value.
     * @return Value of the feature object.
     */
    public Object getValue(Feature feature) {
        double first = ((Number)a.getValue(feature)).doubleValue();
        double second = ((Number)b.getValue(feature)).doubleValue();
        return new Double(Math.min(first,second));
    }
    
    public int getArgCount() {
        return 2;
    }
    
    public String getName() {
        return "Min";
    }
    
    public void setArgs(Expression[] args) {
        a = args[0];
        b = args[1];
        this.args=args;
    }
    
    public Expression[] getArgs() {
        return args;
    }
    
    public String toString(){
        return "Min( " + a + ", " + b + ")";
    }
}
