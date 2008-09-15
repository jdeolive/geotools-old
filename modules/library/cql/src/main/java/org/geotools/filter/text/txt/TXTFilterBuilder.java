/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.filter.text.txt;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.cql2.CQLFilterBuilder;
import org.geotools.filter.text.cql2.IToken;
import org.geotools.filter.text.cql2.Result;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.identity.FeatureId;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Builds the filters required by the {@link TXTCompiler}.
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.5
 */
final class TXTFilterBuilder extends CQLFilterBuilder {

    private final GeometryFactory GEOM_FACTORY = new GeometryFactory();

    public TXTFilterBuilder(String cqlSource, FilterFactory filterFactory) {
        super(cqlSource, filterFactory);
    }

    /**
     * builds the filter id
     * 
     * @param token  <character>
     * @return String without the quotes
     */
    public FeatureId buildFeatureID(IToken token) {
       
        String strId = removeQuotes(token.toString());

        FeatureId id = getFilterFactory().featureId( strId);
        
        return id;
    }

    /**
     * builds the filter id
     * @param jjtfeature_id_separator_node 
     * @return Id
     * @throws CQLException
     */
    public Id buildFilterId(final int nodeFeatureId) throws CQLException {
        
        //retrieves the id from stack
        List<FeatureId> idList = new LinkedList<FeatureId>();
        while (!getResultStack().empty()) {

            Result result = getResultStack().peek();

            int node = result.getNodeType();
            if (node != nodeFeatureId) {
                break;
            }
            FeatureId id = (FeatureId) result.getBuilt();
            idList.add(id);
            getResultStack().popResult();
        }
        assert idList.size() >= 1: "must have one or more FeatureIds";
        
        // shorts the id list and builds the filter Id
        Collections.reverse(idList);
        Set<FeatureId> idSet = new LinkedHashSet<FeatureId>(idList); 
        Id filter = getFilterFactory().id(idSet);

        return filter;
    }

    /**
     * Builds a negative Number
     * @return Negative number
     * @throws CQLException
     */
    public Literal bulidNegativeNumber() throws CQLException {
        
        // retrieves the number value from stack and adds the (-) minus
        Literal literal = getResultStack().popLiteral();
        String strNumber = "-" + literal.getValue();
        Object value = literal.getValue();
        
        //builds the negative number
        @SuppressWarnings("unused")
        Number number = null;
        if(value instanceof Double){
            number = Double.parseDouble(strNumber);
        }else if (value instanceof Float){
            number = Float.parseFloat(strNumber);
        }else if(value instanceof Integer) {
            number = Integer.parseInt(strNumber);
        }else if(value instanceof Long) {
            number = Long.parseLong(strNumber);
        }else{
            assert false: "Number instnce is expected";
        }
        Literal signedNumber = getFilterFactory().literal(strNumber);
        
        return signedNumber;
    }

    /**
     * builds the or filter for the in predicate. The method 
     * retrieves the list of expressions and the property name 
     * from stack to make the Or filter.
     * <pre>
     * Thus if the stack have the following predicate 
     * propName in (expr1, expr2)
     * this method will produce:
     * (propName = expr1) or (propName = expr2)
     * </pre>
     *  
     * @param nodeExpression
     * @return
     * @throws CQLException 
     */
    public Or buildInPredicate(final int nodeExpression) throws CQLException {
        //retrieves the expressions from stack
        List<Expression> exprList = new LinkedList<Expression>();
        while (!getResultStack().empty()) {

            Result result = getResultStack().peek();

            int node = result.getNodeType();
            if (node != nodeExpression) {
                break;
            }
            getResultStack().popResult();

            Expression expr = (Expression) getResultStack().popExpression();
            exprList.add(expr);
        }
        
        assert exprList.size() >= 1: "must have one or more FeatureIds";
        
        // retrieve the attribute from stack
        final PropertyName property = getResultStack().popPropertyName();
        
        // makes one comparison for each expression in the expression list,
        // associated by the Or filter.
        List<Filter> filterList = new LinkedList<Filter>();
        for (Expression expression : exprList) {
            PropertyIsEqualTo eq = getFilterFactory().equals(property, expression);
            filterList.add(eq);
        }
        Or orFilter = getFilterFactory().or(filterList);
        
        return orFilter;
    }

    public Coordinate buildCoordinate() throws CQLException {

        double y = getResultStack().popDoubleValue();
        double x = getResultStack().popDoubleValue();

        Coordinate coordinate = new Coordinate(x, y);

        return coordinate;
    }

    public Point buildPointText() throws CQLException {

        Result result = getResultStack().popResult();
        IToken token = result.getToken();
        try {
            Coordinate coordinate = (Coordinate) result.getBuilt();

            Point point = this.GEOM_FACTORY.createPoint(coordinate);

            return point;
            
        } catch (ClassCastException e) {
            throw new CQLException(e.getMessage(), token,this.cqlSource);
        }
    }

    public LineString buildLineString(final int pointNode) throws CQLException {

        // Retrieve the linestirng points
        Stack<Coordinate> pointStack = popCoordinatesOf(pointNode);
        // now pointStack has the coordinate in the correct order
        // the next code create the coordinate array used to create
        // the lineString
        Coordinate[] coordinates = asCoordinate(pointStack);
        LineString line= this.GEOM_FACTORY.createLineString(coordinates);
        
        return line;
    }

    public Polygon buildPolygon(final int linestringNode) throws CQLException {
        
        // Retrieve the liner ring for shell and holes
        final List<Geometry> geometryList= popGeometry(linestringNode);
        
        assert geometryList.size() >= 1;
        
        // retrieves the shell
        LineString line = (LineString)geometryList.get(0);
        final LinearRing shell = this.GEOM_FACTORY.createLinearRing(line.getCoordinates());

        // if it has holes, creates a ring for each linestring
        LinearRing[] holes = new LinearRing[0]; 
        if(geometryList.size() > 1){
            
            List<LinearRing> holeList = new LinkedList<LinearRing>();
            for( int i = 1;i < geometryList.size(); i++) {
                
                LineString holeLines = (LineString) geometryList.get(i);
                LinearRing ring = this.GEOM_FACTORY.createLinearRing(holeLines.getCoordinates());
                holeList.add(ring);
            }
            int holesSize = holeList.size();
            holes = holeList.toArray(new LinearRing[holesSize]) ;
        } 
        // creates the polygon
        Polygon polygon= this.GEOM_FACTORY.createPolygon(shell, holes);
        
        return polygon;
    }
    
    public MultiPoint buildMultyPoint(int pointNode) throws CQLException {

        // retrieves all points from stack and create the multipoint geometry

        List<Geometry> pointList = popGeometry(pointNode);
        
        int pointListSize = pointList.size();
        Point[] arrayOfPoint = pointList.toArray(new Point[pointListSize]) ;
        
        MultiPoint multiPoint= this.GEOM_FACTORY.createMultiPoint(arrayOfPoint);
        return multiPoint;
    }
    /**
     * Pop the indeed geometry and order the result before delivery the list
     * 
     * @param geometryNode geometry required
     * @return a list of indeed geometries 
     * @throws CQLException
     */
    private List<Geometry> popGeometry(final int geometryNode) throws CQLException{

        List<Geometry> geomList = new LinkedList<Geometry>();
        while( !getResultStack().empty() ) {
            
            Result result = getResultStack().peek();
            if(result.getNodeType() != geometryNode){
                break;
            }
            getResultStack().popResult();
            
            Geometry geometry = (Geometry)result.getBuilt();
            geomList.add(geometry);
        }
        Collections.reverse(geomList);

        return geomList;
    }

    private Coordinate[] asCoordinate(Stack<Coordinate> stack) {
        
        int size = stack.size();
        Coordinate[] coordinates = new Coordinate[ size ];
        int i = 0;
        while( !stack.empty()) {
            coordinates[i++]= (Coordinate) stack.pop();
        }
        return coordinates;
    }

    /**
     * Makes an stack with the geometries indeed by the typeGeom
     * @param geomNode
     * @return an Stack with the required geometries
     * @throws CQLException
     */
    private Stack<Coordinate> popCoordinatesOf(int geomNode) throws CQLException {
        Stack<Coordinate> stack = new Stack<Coordinate>();
        while (!getResultStack().empty()) {

            Result result = getResultStack().peek();

            int node = result.getNodeType();
            if (node != geomNode) {
                break;
            }
            getResultStack().popResult();
            Coordinate coordinate = (Coordinate)result.getBuilt();

            stack.push(coordinate);
        }
        return stack;
    }
    /**
     * Builds literal geometry
     * 
     * @param geometry
     * @return a Literal Geometry
     * @throws CQLException
     */
    public Literal buildGeometry() throws CQLException {

        Geometry geometry = getResultStack().popGeometry();

        Literal literal = getFilterFactory().literal(geometry);
        
        return literal;
    }


    
}
