package org.geotools.filter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.TopologyException;


public interface BBoxExpression extends LiteralExpression{

    void setBounds(Envelope env)  throws IllegalFilterException;

}
