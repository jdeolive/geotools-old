package org.geotools.filter;

import java.util.Iterator;
import org.geotools.feature.Feature;


public interface LogicFilter {
    boolean contains(Feature feature);
    Filter not();

    Filter and(Filter filter);

    Iterator getFilterIterator();

    String toString();

    Filter or(Filter filter);

    boolean equals(Object obj);


    void addFilter(Filter filter) throws IllegalFilterException;


}
