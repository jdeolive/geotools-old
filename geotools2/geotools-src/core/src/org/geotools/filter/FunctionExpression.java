package org.geotools.filter;


public interface FunctionExpression extends Expression{
    int getArgCount();
    short getType();
    Expression[] getArgs();
    
    String getName();


    void setArgs(Expression[] args);


}
