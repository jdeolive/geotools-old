package org.geotools.filter;

import java.util.logging.Level;
import org.geotools.feature.Feature;


public interface CompareFilter {
    String toString();
    void addLeftValue(Expression leftValue)  throws IllegalFilterException;

    Expression getLeftValue();

    Expression getRightValue();

    boolean equals(Object obj);

    boolean contains(Feature feature);


    void addRightValue(Expression rightValue) throws IllegalFilterException;


}
