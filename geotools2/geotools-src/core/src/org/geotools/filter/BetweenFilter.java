package org.geotools.filter;

import org.geotools.feature.Feature;


public interface BetweenFilter {
    boolean equals(Object oFilter);
    String toString();

    boolean contains(Feature feature);

    Expression getMiddleValue();


    void addMiddleValue(Expression middleValue);


}
