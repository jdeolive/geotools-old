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

import java.util.Arrays;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FunctionExpression;


/**
 * Tests UniqueIntervalFunction
 *
 * @author Cory Horner
 * @source $URL$
 */
public class UniqueIntervalFunctionTest extends FunctionTestSupport {
    public UniqueIntervalFunctionTest(String testName) {
        super(testName);
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(UniqueIntervalFunctionTest.class);

        return suite;
    }

    /**
     * Test of getName method, of class
     * org.geotools.filter.functions.UniqueIntervalFunction.
     */
    public void testInstance() {
        FunctionExpression equInt = FilterFactoryFinder.createFilterFactory()
                                                 .createFunctionExpression("UniqueInterval");
        assertNotNull(equInt);
    }

    /**
     * Test of getName method, of class
     * org.geotools.filter.functions.UniqueIntervalFunction.
     */
    public void testGetName() {
        FunctionExpression equInt = FilterFactoryFinder.createFilterFactory()
                .createFunctionExpression("UniqueInterval");
        assertEquals("UniqueInterval", equInt.getName());
    }

    /**
     * Test of setNumberOfClasses method, of class
     * org.geotools.filter.function.UniqueIntervalFunction.
     */
    public void testSetClasses() throws Exception {
        Expression classes = (Expression) builder.parser(dataType, "3");
        Expression exp = (Expression) builder.parser(dataType, "foo");
        UniqueIntervalFunction func = (UniqueIntervalFunction) fac
            .createFunctionExpression("UniqueInterval");
        func.setArgs(new Expression[] { exp, classes });
        assertEquals(3, func.getClasses());
        func.setClasses(12);
        assertEquals(12, func.getClasses());
    }

    /**
     * Test of getValue method, of class
     * org.geotools.filter.function.UniqueIntervalFunction.
     */
    public void testEvaluate() throws Exception {
        Expression classes = (Expression) builder.parser(dataType, "2");
        Expression exp = (Expression) builder.parser(dataType, "foo");
        FunctionExpression func = fac.createFunctionExpression("UniqueInterval");
        func.setArgs(new Expression[] { exp, classes });

        Object result = func.evaluate(featureCollection);
        assertTrue(result instanceof ExplicitClassifier);
        ExplicitClassifier classifier = (ExplicitClassifier) result;
        assertEquals(2, classifier.getSize());
        assertEquals(classifier.values[0].size(), classifier.values[1].size());
        assertFalse(classifier.values[0].removeAll(classifier.values[1]));
        
    }
}
