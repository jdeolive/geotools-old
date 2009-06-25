/*
 *    GeoTools - The Open Source Java GIS Toolkit
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

package org.geotools.filter;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.geotools.TestData;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Function;

/**
 * Test the VocabFunction making use of a sample mapping provided
 * by Alister.
 */
public class CustomFunctionsTest extends TestCase {

    public void testVocabFunction() throws IOException{
        File file = TestData.file(CustomFunctionsTest.class,"minoc_lithology_mapping.properties");
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
        Function function = ff.function("Vocab", ff.literal("1LIST"), ff.literal(file.toString()) );
        
        Object value = function.evaluate(null);
        assertEquals( "urn:cgi:classifier:CGI:SimpleLithology:2008:calcareous_carbonate_sedimentary_rock", value );
    }
    
    public void testNoVocabFunction(){
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
        Function function = ff.function("Vocab", ff.literal("a"), ff.literal("urn:1234") );
        
        try {
            Object value = function.evaluate(null);
            fail("Should not be able to get this far");
        }
        catch( Throwable expected ){
            
        }
    }

}
