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
public abstract class FunctionExpression extends org.geotools.filter.ExpressionDefault {
    
    /** Creates a new instance of FunctionExpression */
    public FunctionExpression() {
    }
    /*
     * @task HACK: this shoud return a proper type for Functions
     */
    public short getType(){
        return -1;
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
     */
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }
    
    public abstract String getName();
    
    public abstract void setArgs(Expression[] args);
    
    public abstract int getArgCount();
    
}
