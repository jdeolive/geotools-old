package org.geotools.filter;


public interface FunctionExpression extends Expression{
    int getArgCount();
    short getType();

    String getName();


    void setArgs(Expression[] args);


}
