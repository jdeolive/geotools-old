package org.geotools.filter.expression;

import static org.junit.Assert.*;

import org.geotools.factory.CommonFactoryFinder;
import org.junit.Test;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

public class ExpressionBuilderTest {

    /**
     * This is the main entry point from a literant programming point of view. We will mostly test
     * using this as a starting point; and break out other test cases on an as needed basis.
     */
    @Test
    public void testExpressionBuilder() {
        FilterFactory ff = CommonFactoryFinder.getFilterFactory2(null);
        ExpressionBuilder b = new ExpressionBuilder();
        b.literal("hello world");
        Expression e = b.build();
        assertEquals( ff.literal("hello world"), e );
        
        assertEquals( ff.literal(1), b.literal().value(1).build() );
        
        b.literal().value(1);
        e = b.build(); // ensure delegate works
        assertEquals( ff.literal(1), e );
        
        assertEquals( ff.literal(null), b.literal( null ).build() );
        
        assertEquals( null, b.unset().build() );
        
        assertEquals( Expression.NIL, b.reset().build() );
        assertEquals( null, b.reset(null).build() );
        
        assertEquals( ff.literal(2), b.reset( ff.literal(2) ).build() );
        
    }
}
