package org.geotools.filter;

import org.geotools.feature.Feature;


public interface NullFilter extends Filter{
    boolean equals(Object obj);
    void nullCheckValue(Expression nullCheck) throws IllegalFilterException;

    String toString();

    Expression getNullCheckValue();


    boolean contains(Feature feature);


}
