package org.geotools.filter;

import org.geotools.feature.Feature;


public interface MathExpression extends Expression{
    Object getValue(Feature feature);
    void addRightValue(Expression rightValue) throws IllegalFilterException;

    short getType();

    Expression getLeftValue();

    Expression getRightValue();

    boolean equals(Object obj);


    void addLeftValue(Expression leftValue) throws IllegalFilterException;


}
