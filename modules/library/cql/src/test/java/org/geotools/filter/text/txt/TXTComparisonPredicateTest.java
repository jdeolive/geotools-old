
package org.geotools.filter.text.txt;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.filter.Filter;
import org.geotools.filter.text.cql2.CQLComparisonPredicateTest;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.cql2.CompilerFactory;


/**
 * TXT Comparison Predicate Test Case.
 * <p>
 * The implementation must parse comparison predicate using the following grammar rule:
 * <pre>
 * &lt comparison predicate &gt ::= &lt expression &gt &lt comp op &gt &lt expression &gt
 * </pre>
 * </p>
 * <p>
 * This test case extends the from CQL test in order to assure that this extension (TXT) contains
 * the base language (CQL).
 * </p>
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.5
 */
public class TXTComparisonPredicateTest extends CQLComparisonPredicateTest {
    public TXTComparisonPredicateTest() {
        // sets the language used to execute this test case
        super(CompilerFactory.Language.TXT);
    }

    /**
     * Test: Expression on the Left hand of comparison predicate
     * Sample: (1+3) > prop1
     * @throws CQLException
     */
    @Test
    public void expressionGreaterThanProperty() throws CQLException {
        Filter expected = FilterTXTSample.getSample(FilterTXTSample.EXPRESION_LESS_PROPERTY);

        Filter actual = parse(FilterTXTSample.EXPRESION_LESS_PROPERTY);

        Assert.assertNotNull("expects filter not null", actual);

        Assert.assertEquals("less than compare filter error", expected, actual);
    }

    /**
     * Test: function on the Left hand of comparison predicate
     * <pre>
     * Samples:
     *          abs(10) < aProperty
     *          area( the_geom ) < 30000
     *          area( the_geom ) < (2+3)
     *          area( the_geom ) < abs(10)
     *
     * </pre>
     * @throws CQLException
     */
    @Test
    public void functionsInComparison() throws CQLException {
        Filter expected;
        Filter actual;
        
        //abs(10) < aProperty
        assertComparasion(FilterTXTSample.ABS_FUNCTION_LESS_PROPERTY);

        // area( the_geom ) < 30000
        assertComparasion(FilterTXTSample.AREA_FUNCTION_LESS_NUMBER);
        
        //TODO area( the_geom ) < (2+3)
        actual = parse("area( the_geom ) < (2+3)");
        
        //TODO area( the_geom ) < abs(10)
        actual = parse("area( the_geom ) < abs(10)");
    }
    
    /**
     * asserts that the filter returned is the specified by the predicate 
     * 
     * @param testPredicate predicate to test
     * @throws CQLException
     */
    private void assertComparasion(final String testPredicate ) throws CQLException{
        Filter expected;
        Filter actual;
        
        expected = FilterTXTSample.getSample(testPredicate);

        actual = parse(testPredicate);

        Assert.assertNotNull("expects filter not null", actual);

        Assert.assertEquals("compare filter error", expected, actual);
        
    }
}
