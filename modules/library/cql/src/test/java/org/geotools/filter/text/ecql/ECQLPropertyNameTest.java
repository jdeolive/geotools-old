package org.geotools.filter.text.ecql;

import org.geotools.filter.function.PropertyExistsFunction;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Expression;

public class ECQLPropertyNameTest {
	
	
    @Test
    public void existProperty() throws Exception{

        Filter resultFilter = ECQL.toFilter("NAME EXISTS");

        Assert.assertTrue(resultFilter instanceof PropertyIsEqualTo);
        
        PropertyIsEqualTo eq = (PropertyIsEqualTo) resultFilter;
        
        Expression expr = eq.getExpression1() ;

        Assert.assertTrue(expr instanceof PropertyExistsFunction);
        
    }
	
    @Test 
    public void keywordAsProperty() throws Exception {

    	Filter filter = ECQL.toFilter(" \"LIKE\" = 1 ");
        
        Assert.assertNotNull(filter );
    }

    @Ignore // TODO work in progress
    public void localCharactersetInProperty() throws Exception {

    	Filter filter = ECQL.toFilter(" \"población\" = 'Sudáfrica' ");
        
        Assert.assertNotNull(filter );
    }

}
