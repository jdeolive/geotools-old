/*
 * BBoxExpression.java
 *
 * Created on 19 July 2002, 11:39
 */

package org.geotools.filter;

/**
 *
 * @author  iant
 */

import com.vividsolutions.jts.geom.*;

public class BBoxExpressionImpl extends org.geotools.filter.LiteralExpressionImpl implements BBoxExpression { 
    private GeometryFactory gfac = new GeometryFactory();
    /** Creates a new instance of BBoxExpression */
    public BBoxExpressionImpl() throws IllegalFilterException{
        this(new Envelope());
    }
    public BBoxExpressionImpl(Envelope env) throws IllegalFilterException{
        expressionType = DefaultExpression.LITERAL_GEOMETRY;
        setBounds(env);
    }
    
    /**
     * Set the bbox for this expreson
     * @task HACK: currently sets the SRID to null, which can cause
     *             problems with JTS when it comes to doing spatial tests
     */
    public void setBounds(Envelope env)  throws IllegalFilterException{
        
        Coordinate[] c = new Coordinate[5];
        c[0] = new Coordinate(env.getMinX(), env.getMinY());
        c[1] = new Coordinate(env.getMinX(), env.getMaxY());
        c[2] = new Coordinate(env.getMaxX(), env.getMaxY());
        c[3] = new Coordinate(env.getMaxX(), env.getMinY());
        c[4] = new Coordinate(env.getMinX(), env.getMinY());
        LinearRing r = null;
        try{
            r = gfac.createLinearRing(c);
        } catch (TopologyException e){
            throw new IllegalFilterException(e.toString());
        }
        
        super.setLiteral(gfac.createPolygon(r, null));
        
    }
    
    
    
}
