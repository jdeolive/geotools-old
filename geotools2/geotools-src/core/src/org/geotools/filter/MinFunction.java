package org.geotools.filter;

import org.geotools.feature.Feature;


public interface MinFunction extends MathExpression{
    Object getValue(Feature feature);
    String getName();

    int getArgCount();


    void setArgs(Expression[] args);


}
