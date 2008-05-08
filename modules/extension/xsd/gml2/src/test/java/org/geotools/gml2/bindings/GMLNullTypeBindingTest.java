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
package org.geotools.gml2.bindings;

import org.geotools.gml2.GML;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;


public class GMLNullTypeBindingTest extends AbstractGMLBindingTest {
    ElementInstance nul;

    protected void setUp() throws Exception {
        super.setUp();
        nul = createElement(GML.NAMESPACE, "myNull", GML.NULLTYPE, null);
    }

    public void testAllowable() throws Exception {
        Node node = createNode(nul, null, null, null, null);

        GMLNullTypeBinding s = (GMLNullTypeBinding) getBinding(GML.NULLTYPE);

        assertEquals("inapplicable", s.parse(nul, "inapplicable"));
        assertEquals("unknown", s.parse(nul, "unknown"));
        assertEquals("unavailable", s.parse(nul, "unavailable"));
        assertEquals("missing", s.parse(nul, "missing"));
    }
}
