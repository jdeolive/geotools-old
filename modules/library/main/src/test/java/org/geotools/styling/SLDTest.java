/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.styling;

import java.awt.Color;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory2;

import junit.framework.TestCase;

/**
 * Test cases for the SLD utility class
 * @author Jody
 *
 */
public class SLDTest extends TestCase {
    StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
    FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    /**
     * We should have a test case for the expected default values
     * so we can be sure of correct SLD rendering.
     */
    public void testDefaults(){
        Stroke stroke = sf.getDefaultStroke();        
        assertEquals( "default stroke width is one", 1, SLD.width( stroke ));
        assertEquals( "default stroke color is black", Color.BLACK, SLD.color( stroke ));
    }
    /**
     * See http://jira.codehaus.org/browse/UDIG-1374
     */
    public void testStroke(){
        Stroke stroke = sf.createStroke( ff.literal("#FF0000"), ff.literal("3") );
        assertEquals( "width", 3, SLD.width( stroke ));
        assertEquals( "color", Color.RED, SLD.color( stroke ));
        
        stroke = sf.createStroke( ff.literal("#FF0000"), ff.literal("3.0") );
        assertEquals( "width", 3, SLD.width( stroke ));
    }
}
