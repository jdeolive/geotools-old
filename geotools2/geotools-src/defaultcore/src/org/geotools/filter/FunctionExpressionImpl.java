/*
 * FunctionExpression.java
 *
 * Created on 28 July 2002, 15:18
 */

package org.geotools.filter;


import org.geotools.data.*;
import org.geotools.feature.*;

/**
 *
 * @author  James
 */
public abstract class FunctionExpressionImpl extends org.geotools.filter.DefaultExpressionImpl {
    
    /** Creates a new instance of FunctionExpression */
    public FunctionExpressionImpl() {
    }
    /*
     * @task HACK: this shoud return a proper type for Functions
     */
    public short getType(){
        return -1;
    }
    
    public abstract String getName();
    
    public abstract void setArgs(Expression[] args);
    
    public abstract int getArgCount();
    
}
