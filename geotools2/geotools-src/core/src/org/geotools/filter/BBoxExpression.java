package org.geotools.filter;

import com.vividsolutions.jts.geom.Envelope;


public interface BBoxExpression extends LiteralExpression {
    void setBounds(Envelope env) throws IllegalFilterException;
}
