
package org.geotools.filter.text.txt;

import org.geotools.filter.text.cql2.CQLComparisonPredicateTest;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.cql2.CompilerFactory;
import org.junit.Assert;
import org.junit.Test;
import org.opengis.filter.Filter;


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
     *         (1+3) > (4+5)
     * @throws CQLException
     */
    @Test
    public void expressionComparisonProperty() throws CQLException {

        testComparasion(FilterTXTSample.EXPRESION_GREATER_PROPERTY);
        
        //TODO (1+3) > (4+5)
        
        
    }

    /**
     * Test: function on the Left hand of comparison predicate
     * <pre>
     * Samples:
     *          abs(10) < aProperty
     *          area( the_geom ) < 30000
     *          area( the_geom ) < (1+3)
     *          area( the_geom ) < abs(10)
     *
     * </pre>
     * @throws CQLException
     */
    @Test
    public void functionsInComparison() throws CQLException {
        
        //abs(10) < aProperty
        testComparasion(FilterTXTSample.ABS_FUNCTION_LESS_PROPERTY);

        // area( the_geom ) < 30000
        testComparasion(FilterTXTSample.AREA_FUNCTION_LESS_NUMBER);
        
        // area( the_geom ) < (1+3)
        testComparasion(FilterTXTSample.FUNCTION_LESS_SIMPLE_ADD_EXPR);
        
        // area( the_geom ) < abs(10)
        testComparasion(FilterTXTSample.FUNC_AREA_LESS_FUNC_ABS);
    }
    
    /**
     * asserts that the filter returned is the specified by the predicate 
     * 
     * @param testPredicate predicate to test
     * @throws CQLException
     */
    private void testComparasion(final String testPredicate ) throws CQLException{
        
        Filter expected = FilterTXTSample.getSample(testPredicate);

        Filter actual = parse(testPredicate);

        Assert.assertNotNull("expects filter not null", actual);

        Assert.assertEquals("compare filter error", expected, actual);
    }
}
