/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.geotools.filter.text.ecql;

import org.geotools.filter.text.commons.Language;
import org.geotools.filter.text.cql2.CQLAttributeNameTest;
import org.geotools.filter.text.cql2.CQLException;
import org.junit.Assert;
import org.junit.Test;
import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsEqualTo;

/**
 * Test case for Attribute Name
 * <p>
 * <pre>
 * @see CQLAttributeNameTest
 * </pre>
 * </p>
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.7
 *
 * @source $URL: http://svn.osgeo.org/geotools/trunk/modules/library/cql/src/test/java/org/geotools/filter/text/ecql/ECQLAttributeNameTest $
 */
public class ECQLAttributeNameTest extends CQLAttributeNameTest{
	public ECQLAttributeNameTest(){
        super(Language.ECQL);
    }
	
	
    @Test
    public void propertyName() throws CQLException{

    	testUsingPropertyName( "NAME");
        
    }
	/**
	 * Using a CQL Keyword as property name
	 * 
	 * 
	 * @throws Exception
	 */
    @Test 
    public void keywordAsProperty() throws CQLException {

 
    	testUsingPropertyName("\"LIKE\"");
    }

    /**
     * Using different local characters as property name.
     * 
     * 
     * @throws Exception
     */
    @Test
    public void localCharactersetInProperty() throws CQLException {
        
    	testUsingPropertyName("\"población\"");

    	testUsingPropertyName("\"reconnaître\"");

    	testUsingPropertyName("\"können\"");
    }

    /**
     * Test if the returned filter  by cql has the attribute name provided in the parameter.
     * @param expectedName an attribute name
     * @throws CQLException
     */
    private void testUsingPropertyName(final String expectedName) throws CQLException{
    	
    	Filter resultFilter = ECQL.toFilter( expectedName + " = 1 ");
    	
    	final PropertyIsEqualTo eq = (PropertyIsEqualTo) resultFilter;
		final String nameProduced = eq.getExpression1().toString();
    	Assert.assertEquals( expectedName, nameProduced );

    }
	
}
