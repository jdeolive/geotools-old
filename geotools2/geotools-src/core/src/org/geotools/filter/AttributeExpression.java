package org.geotools.filter;

import org.geotools.feature.Feature;
import org.geotools.feature.IllegalFeatureException;


public interface AttributeExpression extends Expression{
    void setAttributePath(String attributePath) throws IllegalFilterException;
    Object getValue(Feature feature);

    boolean equals(Object obj);

    String toString();


    String getAttributePath();


}
