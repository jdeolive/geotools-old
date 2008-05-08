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

/**
 * TXT Test Case
 *
 * @author Jody Garnett
 * @author Maria Comanescu
 * @author Mauricio Pazos (Axios Engineering)
 *
 * @version Revision: 1.9
 * @since 2.5 
 */
public final class TXTTest extends TestCase{
    

    public void testFacade() throws Exception {
        TXT.toFilter("A = 1");
        TXT.toExpression("A + 1");
        TXT.toFilterList("A=1; B<4");
    }

    /**
     * <id predicate> ::= <id> {"," <id> };
     * <id> ::= <hash> { <character> }
     * <hash> ::= "#"
     * 
     * Test: #15521.3566 
     * 
     * @throws Exception
     */
    public void testFilterId() throws Exception {

        String strId;
        Filter filter; 
        Id filterId; 
        Set<?> idSet;
        
        //sample: #15521.3566 
        strId = "15521.3566";
        filter = TXT.toFilter("#" + strId);
        assertNotNull(filter);
        assertTrue(filter instanceof Id);

        filterId = (Id) filter;
        idSet = filterId.getIDs();
        assertTrue("one id in filter Id was expected", idSet.size() == 1);
        assertTrue(strId + "was expected", idSet.contains(strId));

        //sample: #fid-_df58120_11814e5d8b3__7ffb 
        strId = "fid-_df58120_11814e5d8b3__7ffb";
        filter = TXT.toFilter("#" + strId);
        assertNotNull(filter);
        assertTrue(filter instanceof Id);

        filterId = (Id) filter;
        idSet = filterId.getIDs();
        assertTrue("one id in filter Id was expected", idSet.size() == 1);
        assertTrue(strId + "was expected", idSet.contains(strId) );
    }
    
    /**
     * <id predicate> ::= <id> {"," <id> }; <id> ::= <hash> { <character> }
     * <hash> ::= "#"
     * 
     * Test: #15521.3566, #15521.3567, #15521.3568
     * 
     * @throws Exception
     */
    public void testFilterIdList() throws Exception {
        final String strId1 = "15521.3566";
        final String strId2 = "15521.3567";
        final String strId3 = "15521.3568";
        Filter filter = TXT.toFilter("#" + strId1 + ", #" + strId2 + ", #"
                + strId3);
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

    public void testFilterIdError(){
        String strId;
        Filter filter; 
        Id filterId; 
        Set<?> idSet;
        
        //sample: 15521,3566 (without #)
        try {
            strId = "15521.3566";
            TXT.toFilter(strId);
            fail("Exception is expected");
        } catch (CQLException e) {
            
            assertTrue("Expects syntax error", e.getSyntaxError().length()>0 );
        }
    }
    
}
