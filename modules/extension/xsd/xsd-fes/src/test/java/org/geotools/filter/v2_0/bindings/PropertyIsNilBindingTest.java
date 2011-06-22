/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2011, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.filter.v2_0.bindings;

import org.geotools.filter.v2_0.FESTestSupport;
import org.opengis.filter.PropertyIsNil;

public class PropertyIsNilBindingTest extends FESTestSupport {

    public void testParse() throws Exception {
        String xml = 
        "<fes:Filter " + 
        "   xmlns:fes='http://www.opengis.net/fes/2.0'>" + 
        "   <fes:PropertyIsNil nilReason='-1'> " +
        "     <fes:ValueReference>FOO</fes:ValueReference>"+
        "   </fes:PropertyIsNil> " +
        "</fes:Filter>";
        buildDocument(xml);
        
        PropertyIsNil isNil = (PropertyIsNil) parse();
        assertNotNull(isNil);
    }
}
