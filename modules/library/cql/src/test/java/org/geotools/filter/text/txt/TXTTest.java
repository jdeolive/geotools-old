/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.filter.text.txt;

import org.geotools.filter.text.cql2.CQLException;
import org.junit.Test;

/**
 * TXT Test Case
 * <p>
 * Test the implementation of {@link TXT} facade
 * </p>
 *
 * @author Jody Garnett
 * @author Mauricio Pazos (Axios Engineering)
 *
 * @version Revision: 1.9
 * @since 2.5 
 */
public final class TXTTest  {
    
    /**
     * Simple test
     * 
     * @throws Exception
     */
    @Test 
    public void txtFacade() throws Exception {
        TXT.toFilter("A = 1");
        
        TXT.toExpression("A + 1");
        
        TXT.toFilterList("A=1; B<4");

        
        TXT.toFilter("ID IN 'river.1', 'river.2'");
    }
    
    /**
     * Test for Syntax error exception
     * @throws CQLException 
     */
    @Test(expected = CQLException.class)
    public void filterIdSyntaxError() throws CQLException {
        String strId = "ID 15521.3566"; // should be ID IN '15521.3566'
        TXT.toFilter(strId);
    }
    
}
