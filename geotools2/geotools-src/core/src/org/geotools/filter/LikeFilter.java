package org.geotools.filter;

import java.util.regex.Pattern;
import org.geotools.feature.Feature;


public interface LikeFilter extends Filter{
    void setPattern(String pattern, String wildcardMulti, String wildcardSingle, String escape);
    String toString();

    String getWildcardMulti();

    String getEscape();

    void setPattern(Expression p, String wildcardMulti, String wildcardSingle, String escape);

    String getPattern();

    void setValue(Expression attribute) throws IllegalFilterException;

    Expression getValue();

    boolean equals(Object obj);

    String getWildcardSingle();


    boolean contains(Feature feature);


}
