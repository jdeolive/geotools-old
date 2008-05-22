/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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

import java.util.Set;

import junit.framework.TestCase;

import org.geotools.filter.text.cql2.CQLException;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.Not;

/**
 * TXT Test Case
 *
 * @author Jody Garnett
 * @author Mauricio Pazos (Axios Engineering)
 *
 * @version Revision: 1.9
 * @since 2.5 
 */
public final class TXTTest extends TestCase{
    
    /**
     * Facade simple test
     * 
     * @throws Exception
     */
    public void testFacade() throws Exception {
        TXT.toFilter("A = 1");
        TXT.toExpression("A + 1");
        TXT.toFilterList("A=1; B<4");
    }
    
    /**
     * <pre>
     * &lt id predicate &gt ::= ID IN &lt id &gt {,&lt id &gt };
     * 
     * &lt id &gt ::= 'string'
     * 
     * Samples:
     * <ul>
     * <li>ID IN '15521.3566' </li>
     * <li>ID IN 'fid-_df58120_11814e5d8b3__7ffb'</li>
     * <li>ID IN 'states.1'</li>
     * </ul> 
     * </pre>
     * @throws Exception
     */
    public void testFilterId() throws Exception {
        assertFilterId("15521.3566");
        assertFilterId("fid-_df58120_11814e5d8b3__7ffb");
        assertFilterId("states.1");
    }

    /**
     * Test the id Predicate 
     * @throws CQLException
     */
    private void assertFilterId(final String idValue) throws CQLException {

        String strId = "'"+ idValue + "'";
        Filter filter = TXT.toFilter("ID IN " + strId);
        assertNotNull(filter);
        assertTrue(filter instanceof Id);

        Id filterId = (Id) filter;
        Set<?>  idSet = filterId.getIDs();
        assertTrue("one id in filter Id was expected", idSet.size() == 1);
        assertTrue(idValue + "was expected", idSet.contains(idValue));
    }
    
    /**
     * <pre>
     * &lt id predicate &gt ::= ID IN &lt id &gt [ NOT ]{,&lt id &gt };
     * 
     * Sample: ID IN states.1, states.2, states.3
     * </pre>
     * @throws Exception
     */
    public void testFilterIdList() throws Exception {

        final String strId1 = "states.1";
        final String strId2 = "states.2";
        final String strId3 = "states.3";
        Filter filter = TXT.toFilter("ID IN '" + strId1 + "','" + strId2
                + "', '" + strId3 + "'");
        assertNotNull(filter);
        assertTrue(filter instanceof Id);

        Id filterId = (Id) filter;
        Set<?> resultIdentifiers = filterId.getIDs();
        assertTrue("one id in filter Id was expected",
                resultIdentifiers.size() == 3);

        assertTrue(strId1 + " was expected", resultIdentifiers.contains(strId1));

        assertTrue(strId2 + " was expected", resultIdentifiers.contains(strId2));

        assertTrue(strId3 + " was expected", resultIdentifiers.contains(strId3));
    }
    
    /**
     * <pre>
     * &lt id predicate &gt ::= ID IN &lt id &gt [ NOT ]{,&lt id &gt };
     * 
     * Sample: ID NOT IN states.1, states.2, states.3
     * </pre>
     * @throws Exception
     */
    public void testNotFilterId() throws Exception {

        Filter filter;
        
        final String strId1 = "states.1";
        final String strId2 = "states.2";
        final String strId3 = "states.3";
        filter = TXT.toFilter("NOT ID IN '" + strId1 + "','" + strId2
                + "', '" + strId3 + "'");
        
        assertNotNull(filter);
        assertTrue("Not filter was expected",  filter instanceof Not);
        
        Not notFilter = (Not) filter;
        filter = notFilter.getFilter();

        Id filterId = (Id) filter;
        Set<?> resultIdentifiers = filterId.getIDs();
        assertTrue("one id in filter Id was expected",
                resultIdentifiers.size() == 3);

        assertTrue(strId1 + " was expected", resultIdentifiers.contains(strId1));

        assertTrue(strId2 + " was expected", resultIdentifiers.contains(strId2));

        assertTrue(strId3 + " was expected", resultIdentifiers.contains(strId3));
    }

    /**
     * bad syntax in id predicate
     */
    public void testFilterIdError() {

        //sample: 15521,3566 (IN keyword is required )
        try {
            String strId = "ID 15521.3566";
            TXT.toFilter(strId);
            
            fail("Exception is expected");
            
        } catch (CQLException e) {

            assertTrue("Expects syntax error", e.getSyntaxError().length() > 0);
        }
    }
    
    
}
