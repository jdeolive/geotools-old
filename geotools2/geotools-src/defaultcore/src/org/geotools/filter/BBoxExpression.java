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

public class BBoxExpression extends org.geotools.filter.ExpressionLiteral {
    private GeometryFactory gfac = new GeometryFactory();
    /** Creates a new instance of BBoxExpression */
    public BBoxExpression() throws IllegalFilterException{
        this(new Envelope());
    }
    public BBoxExpression(Envelope env) throws IllegalFilterException{
        expressionType = ExpressionDefault.LITERAL_GEOMETRY;
        setBounds(env);
    }
    
    /**
     * Set the bbox for this expreson
     * @task HACK: currently sets the SRID to null, which can cause
     *             problems with JTS when it comes to doing spatial tests
     */
    public void setBounds(Envelope env) throws IllegalFilterException{
        
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
    
    /** Used by FilterVisitors to perform some action on this filter instance.
     * Typicaly used by Filter decoders, but may also be used by any thing which needs
     * infomration from filter structure.
     *
     * Implementations should always call: visitor.visit(this);
     *
     * It is importatant that this is not left to a parent class unless the parents
     * API is identical.
     *
     * @param visitor The visitor which requires access to this filter,
     *                the method must call visitor.visit(this);
     *
     */
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }
    
}
