package org.geotools.filter;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.feature.Feature;


public interface LiteralExpression extends Expression{
    void setLiteral(Object literal) throws org.geotools.filter.IllegalFilterException;
    
    Object getValue(Feature feature);

    String toString();

    short getType();

    Object getLiteral();


    boolean equals(Object obj);


}
