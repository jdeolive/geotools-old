package org.geotools.filter;


import org.geotools.feature.Feature;


public interface CompareFilter extends Filter{
    String toString();
    void addLeftValue(Expression leftValue)  throws IllegalFilterException;

    Expression getLeftValue();

    Expression getRightValue();

    boolean equals(Object obj);

    boolean contains(Feature feature);


    void addRightValue(Expression rightValue) throws IllegalFilterException;


}
