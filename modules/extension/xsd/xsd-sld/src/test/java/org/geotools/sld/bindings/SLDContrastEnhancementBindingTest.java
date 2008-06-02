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
package org.geotools.sld.bindings;

import org.geotools.styling.ContrastEnhancement;


public class SLDContrastEnhancementBindingTest extends SLDTestSupport {
    public void testType() throws Exception {
        assertEquals(ContrastEnhancement.class,
            new SLDContrastEnhancementBinding(null, null).getType());
    }

    public void testHistogram() throws Exception {
        SLDMockData.contrastEnhancement(document, document);

        ContrastEnhancement ce = (ContrastEnhancement) parse();
        assertNotNull(ce);

        assertNotNull(ce.getGammaValue());
        assertEquals(1.23, org.geotools.styling.SLD.doubleValue(ce.getGammaValue()), 0d);

        assertNotNull(ce.getType());
        assertEquals("HISTOGRAM", org.geotools.styling.SLD.stringValue(ce.getType()));
    }
}
