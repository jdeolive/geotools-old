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

import java.util.logging.Logger;

import org.geotools.feature.FeatureIterator;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.parser.ParseException;
import org.opengis.feature.simple.SimpleFeature;

/**
 *
 * @author Cory Horner, Refractions Research
 * @source $URL$
 */
public class StandardDeviationFunctionTest extends FunctionTestSupport {
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(
    "org.geotools.filter");
    
    public StandardDeviationFunctionTest(String testName) {
        super(testName);
    }
    
    protected void tearDown() throws java.lang.Exception {
    }
    
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(StandardDeviationFunctionTest.class);
        
        return suite;
    }
    
    public void testInstance() {
        FunctionExpression stdDev = FilterFactoryFinder.createFilterFactory().createFunctionExpression("StandardDeviation");
        assertNotNull(stdDev);
    }
    
    public void testGetName() {
        FunctionExpression equInt = FilterFactoryFinder.createFilterFactory().createFunctionExpression("StandardDeviation");
        LOGGER.finer("testGetName");
        assertEquals("StandardDeviation",equInt.getName());
    }
    
    public void testSetNumberOfClasses() throws Exception{
        LOGGER.finer("testSetNumberOfClasses");
        
        Expression classes = (Expression) builder.parser(dataType, "3");
        Expression exp = (Expression) builder.parser(dataType, "foo");
        StandardDeviationFunction func = (StandardDeviationFunction) fac.createFunctionExpression("StandardDeviation");
        func.setArgs(new Expression[]{exp,classes});
        assertEquals(3, func.getClasses());
        classes = (Expression) builder.parser(dataType, "12");
        func.setArgs(new Expression[]{exp,classes});
        assertEquals(12, func.getClasses());
    }
    
    public void XtestGetValue() throws Exception{
        //doesn't work yet?
        LOGGER.finer("testGetValue");
        Expression classes = (Expression)builder.parse(dataType, "5");
        Expression exp = (Expression)builder.parse(dataType, "foo");
        FunctionExpression func = fac.createFunctionExpression("StandardDeviation");
        func.setArgs(new Expression[]{exp,classes});
        
        FeatureIterator<SimpleFeature> list = featureCollection.features();
        //feature 1
        SimpleFeature f = list.next();
        int slot = ((Number)func.getValue(f)).intValue();
        assertEquals(1, slot);
        //feature 2
        f = list.next();
        slot = ((Number)func.getValue(f)).intValue();
        assertEquals(4, slot);
        //feature 3
        f = list.next();
        slot = ((Number)func.getValue(f)).intValue();
        assertEquals(2, slot);
        //feature 4
        f = list.next();
        slot = ((Number)func.getValue(f)).intValue();
        assertEquals(2, slot);
        //feature 5
        f = list.next();
        slot = ((Number)func.getValue(f)).intValue();
        assertEquals(2, slot);
        //feature 6
        f = list.next();
        slot = ((Number)func.getValue(f)).intValue();
        assertEquals(3, slot);
        //feature 7
        f = list.next();
        slot = ((Number)func.getValue(f)).intValue();
        assertEquals(1, slot);
        //feature 8
        f = list.next();
        slot = ((Number)func.getValue(f)).intValue();
        assertEquals(1, slot);
    }
    
    
}
