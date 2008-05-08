package org.geotools.filter.function;

import java.util.Calendar;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;

public class DateFunctionTest extends TestCase {

    public void testDateParse() {
        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
        Literal pattern = ff.literal("yyyy");
        Literal year = ff.literal("1975");
        
        Function f = ff.function("dateParse", new Expression[]{pattern, year});
        Calendar cal = f.evaluate(null , Calendar.class);
        //System.out.println(cal);
        assertEquals(1975, cal.get(Calendar.YEAR));
    }
    
    public void testDateEncode() {
        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
        Literal pattern = ff.literal("yyyy");
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.YEAR, 2000);
        Literal date = ff.literal(cal.getTime());
        
        Function f = ff.function("dateFormat", new Expression[]{pattern, date});
        String year = f.evaluate(null , String.class);
        assertEquals("2000", year);
    }
}
