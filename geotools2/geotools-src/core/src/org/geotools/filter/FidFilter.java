package org.geotools.filter;

import org.geotools.feature.Feature;


public interface FidFilter extends Filter{
    boolean contains(Feature feature);
    String toString();
    boolean equals(Object filter);
    void addFid(String fid);
	String[] getFids();
}
