package org.geotools.filter;

import org.geotools.feature.Feature;


public interface MaxFunction extends MathExpression{
    int getArgCount();
    String getName();

    void setArgs(Expression[] args);


    Object getValue(Feature feature);


}
