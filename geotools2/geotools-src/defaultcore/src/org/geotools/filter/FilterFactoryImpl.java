/*
 * FilterFactoryImpl.java
 *
 * Created on 24 October 2002, 16:16
 */
package org.geotools.filter;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.feature.FeatureType;

/**
 *
 * @author  iant
 */
public class FilterFactoryImpl extends FilterFactory { 

    /** Creates a new instance of FilterFactoryImpl */
    protected FilterFactoryImpl() {
    }

    public AttributeExpression createAttributeExpression(FeatureType schema) {
        return new AttributeExpressionImpl(schema);
    }

    public AttributeExpression createAttributeExpression(FeatureType schema, 
                                                         String path)
                                                  throws IllegalFilterException {
        return new AttributeExpressionImpl(schema, path);
    }

    public BBoxExpression createBBoxExpression(Envelope env)
                                        throws IllegalFilterException {
        return new BBoxExpressionImpl(env);
    }

    public BetweenFilter createBetweenFilter() throws IllegalFilterException {
        return new BetweenFilterImpl();
    }

    public CompareFilter createCompareFilter(short type)
                                      throws IllegalFilterException {
        return new CompareFilterImpl(type);
    }

    public FidFilter createFidFilter() {
        return new FidFilterImpl();
    }

    public FidFilter createFidFilter(String fid) {
        return new FidFilterImpl(fid);
    }

    public GeometryFilter createGeometryFilter(short filterType)
                                        throws IllegalFilterException {
        return new GeometryFilterImpl(filterType);
    }

    public LikeFilter createLikeFilter() {
        return new LikeFilterImpl();
    }

    public LiteralExpression createLiteralExpression() {
        return new LiteralExpressionImpl();
    }

    public LiteralExpression createLiteralExpression(Object o)
                                              throws IllegalFilterException {
        return new LiteralExpressionImpl(o);
    }

    public LiteralExpression createLiteralExpression(int i) {
        return new LiteralExpressionImpl(i);
    }

    public LiteralExpression createLiteralExpression(double d) {
        return new LiteralExpressionImpl(d);
    }

    public LiteralExpression createLiteralExpression(String s) {
        return new LiteralExpressionImpl(s);
    }

    public LogicFilter createLogicFilter(short filterType)
                                  throws IllegalFilterException {
        return new LogicFilterImpl(filterType);
    }

    public LogicFilter createLogicFilter(Filter filter, short filterType)
                                  throws IllegalFilterException {
        return new LogicFilterImpl(filter, filterType);
    }

    public LogicFilter createLogicFilter(Filter filter1, Filter filter2, 
                                         short filterType)
                                  throws IllegalFilterException {
        return new LogicFilterImpl(filter1, filter2, filterType);
    }

    public MathExpression createMathExpression() {
        return new MathExpressionImpl();
    }

    public MathExpression createMathExpression(short expressionType)
                                        throws IllegalFilterException {
        return new MathExpressionImpl(expressionType);
    }
     
    public FunctionExpression createFunctionExpression(String name){
        
        LOGGER.fine("trying to load name " + name);
        int index = -1;
        if((index = name.indexOf("Function")) != -1 ){
            name = name.substring(0,index);
        }
        name = name.toLowerCase().trim();
        char c = name.charAt(0);
        name = name.replaceFirst(""+c, ""+Character.toUpperCase(c));
        LOGGER.fine("now trying to load name " + name);
        try{
            return (FunctionExpression) Class.forName("org.geotools.filter." + name + "Function").newInstance();
        } catch (Exception e){
            severe("createFunctionExpression", "Unable to find "+name+"Function", e);
            return null;
        }
    }

    public NullFilter createNullFilter() {
        return new NullFilterImpl();
    }
}
