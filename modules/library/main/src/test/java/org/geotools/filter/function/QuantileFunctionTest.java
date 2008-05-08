/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.filter.function;

import java.util.ArrayList;
import java.util.List;

import org.geotools.data.DataUtilities;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.filter.Expression;
import org.geotools.filter.ExpressionType;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.MathExpression;
import org.geotools.filter.parser.ParseException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * 
 * @author Cory Horner, Refractions Research Inc.
 *
 * @source $URL$
 */
public class QuantileFunctionTest extends FunctionTestSupport {
   
    
    public QuantileFunctionTest(String testName) {
        super(testName);
    }
    
    protected void tearDown() throws java.lang.Exception {
    }
    
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(QuantileFunctionTest.class);
        
        return suite;
    }
    
    public void testInstance() {
        FunctionExpression equInt = FilterFactoryFinder.createFilterFactory().createFunctionExpression("Quantile");
        assertNotNull(equInt);
    }
    
    public void testGetName() {
        FunctionExpression qInt = FilterFactoryFinder.createFilterFactory().createFunctionExpression("Quantile");
        System.out.println("testGetName");
        assertEquals("Quantile",qInt.getName());
    }
    
    public void testSetParameters() throws Exception{
        Expression classes = (Expression) builder.parser(dataType, "3");
        Expression expr = (Expression) builder.parser(dataType, "foo");
        List params = new ArrayList();
        params.add(0, expr);
        params.add(1, classes);
        QuantileFunction func = (QuantileFunction) fac.createFunctionExpression("Quantile");
        func.setParameters(params);
        assertEquals(3, func.getClasses());
        classes = (Expression) builder.parser(dataType, "12");
        params.set(1, classes);
        func.setParameters(params);
        assertEquals(12,func.getClasses());
        //deprecated still works?
        classes = (Expression) builder.parser(dataType, "5");
        func.setArgs(new Expression[]{expr, classes});
        assertEquals(5, func.getClasses());
    }
    
    public void testEvaluateWithExpressions() throws Exception{
        Expression classes = (Expression) builder.parser(dataType, "2");
        Expression exp = (Expression) builder.parser(dataType, "foo");
        FunctionExpression func = fac.createFunctionExpression("Quantile");
        func.setArgs(new Expression[]{exp,classes});
        
        Object value = func.evaluate(featureCollection);
        assertTrue(value instanceof RangedClassifier);
        RangedClassifier ranged = (RangedClassifier) value;
        assertEquals(2, ranged.getSize());
        assertEquals("4..20", ranged.getTitle(0));
        assertEquals("20..90", ranged.getTitle(1));
    }
    public void testEvaluateWithStrings() throws Exception {
        org.opengis.filter.expression.Expression function = ff.function("Quantile", ff.property("group"), ff.literal(2)  );
        Classifier classifier = (Classifier) function.evaluate( featureCollection );
        assertNotNull( classifier );

        Classifier classifier2 = (Classifier) function.evaluate( featureCollection, Classifier.class );
        assertNotNull( classifier2 );
        
        Integer number = (Integer) function.evaluate( featureCollection, Integer.class );
        assertNull( number );
    }
    public void xtestNullNaNHandling() throws Exception {
    	//setup
    	FunctionExpression func = fac.createFunctionExpression("Quantile");
    	QuantileFunction qf = (QuantileFunction) func;
 
    	//create a feature collection
    	SimpleFeatureType ft = DataUtilities.createType("classification.nullnan",
        "id:0,foo:int,bar:double");
    	Integer iVal[] = new Integer[] {
    			new Integer(0),
    			new Integer(0),
    			new Integer(0),
    			new Integer(13),
    			new Integer(13),
    			new Integer(13),
    			null,
    			null,
    			null};
    	Double dVal[] = new Double[] {
    			new Double(0.0),
    			new Double(50.01),
    			null,
    			new Double(0.0),
    			new Double(50.01),
    			null,
    			new Double(0.0),
    			new Double(50.01),
    			null};

    	SimpleFeature[] testFeatures = new SimpleFeature[iVal.length];

    	for(int i=0; i< iVal.length; i++){
    		testFeatures[i] = SimpleFeatureBuilder.build(ft, new Object[] {
    				new Integer(i+1),
    				iVal[i],
    				dVal[i],
    		},"nantest.t"+(i+1));
    	}
    	MemoryDataStore store = new MemoryDataStore();
    	store.createSchema(ft);
    	store.addFeatures(testFeatures);
    	FeatureCollection<SimpleFeatureType, SimpleFeature> thisFC = store.getFeatureSource("nullnan").getFeatures();

    	//create the expression
        MathExpression divide = fac.createMathExpression(ExpressionType.MATH_DIVIDE);
        divide.addLeftValue((Expression)builder.parse(dataType, "foo"));
        divide.addRightValue((Expression)builder.parse(dataType, "bar"));
    	
    	qf.setClasses(3);    	
    	qf.setExpression(divide);
        
        RangedClassifier range = (RangedClassifier) qf.evaluate(thisFC);
        assertEquals(2, range.getSize()); //2 or 3?
        assertEquals("0..0", range.getTitle(0));
        assertEquals("0..0.25995", range.getTitle(1));
    }
}
