package org.geotools.filter;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.expression.Expression;

public class FilterFactoryBeforeTest extends TestCase {

    public void testBefore() throws Exception {
        org.geotools.filter.FilterFactory ff = FilterFactoryFinder.createFilterFactory();
    
        CompareFilter filter = ff.createCompareFilter(org.geotools.filter.FilterType.COMPARE_GREATER_THAN);
        filter.addLeftValue( ff.createLiteralExpression(2));
        filter.addRightValue( ff.createLiteralExpression(1));
        
        assertTrue( filter.evaluate( null ) );
        assertTrue( filter.getFilterType() == FilterType.COMPARE_GREATER_THAN );
    }

    public void testQuick() throws Exception {
        org.geotools.filter.FilterFactory ff = FilterFactoryFinder.createFilterFactory();
    
        CompareFilter filter = ff.createCompareFilter(org.geotools.filter.FilterType.COMPARE_GREATER_THAN);
        filter.addLeftValue( ff.createLiteralExpression(2));
        filter.addRightValue( ff.createLiteralExpression(1));
        
        assertTrue( filter.evaluate( null ) );
        assertTrue( filter instanceof PropertyIsGreaterThan );
    }

    public void testAfter() throws Exception {
        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
        
        Expression left = ff.literal(2);
        Expression right = ff.literal(1);
        
        PropertyIsGreaterThan filter = ff.greater( left, right );
        
        assertTrue( filter.evaluate( null ) );
        assertTrue( filter instanceof PropertyIsGreaterThan );
    }
}
