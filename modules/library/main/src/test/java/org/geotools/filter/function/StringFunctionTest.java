package org.geotools.filter.function;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;

public class StringFunctionTest extends TestCase {

    public void testStrReplace() {
        
        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
        Literal foo = ff.literal("foo");
        Literal o = ff.literal("o");
        Literal bar = ff.literal("bar");
        
        Function f = ff.function("strReplace", new Expression[]{foo,o,bar,ff.literal(true)});
        String s = (String) f.evaluate(null,String.class);
        assertEquals( "fbarbar", s );
        
        f = ff.function("strReplace", new Expression[]{foo,o,bar,ff.literal(false)});
        s = (String) f.evaluate(null,String.class);
        assertEquals( "fbaro", s );
    }
}
