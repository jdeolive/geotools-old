package org.geotools.filter;

import org.geotools.feature.Feature;


public interface GeometryFilter extends Filter{
    boolean equals(Object obj);
    void addRightGeometry(Expression rightGeometry) throws IllegalFilterException;

    String toString();

    void addLeftGeometry(Expression leftGeometry) throws IllegalFilterException;

    boolean contains(Feature feature);

    Expression getRightGeometry();


    Expression getLeftGeometry();


}
