package org.geotools.filter;

import java.util.Iterator;
import org.geotools.feature.Feature;


public interface FidFilter {
    boolean contains(Feature feature);
    String toString();

    boolean equals(Object filter);


    void addFid(String fid);


}
