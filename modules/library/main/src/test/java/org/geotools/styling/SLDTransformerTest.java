/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2008, GeoTools Project Managment Committee (PMC)
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

import java.io.IOException;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory2;

import junit.framework.TestCase;

/**
 * This test case captures specific problems encountered with the SLDTransformer
 * code.
 * <p>
 * Please note that SLDTransformer is specifically targeted at SLD 1.0; for new code
 * you should be using the SLD 1.0 (or SE 1.1) xml-xsd bindings.
 * </p>  
 * @author Jody
 */
public class SLDTransformerTest extends TestCase {
    static StyleFactory2 sf = (StyleFactory2) CommonFactoryFinder.getStyleFactory(null);
    static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
    static SLDTransformer transformer;
    
    protected void setUp() throws Exception {
        transformer = new SLDTransformer();
    }
    
    /**
     * This problem is reported from uDig 1.2, we are trying to save a RasterSymbolizer
     * (used to record the opacity of a raster layer) out to an SLD file for safe keeping.
     */
    public void testEncodingRasterSymbolizer() throws Exception {
       RasterSymbolizer defaultRasterSymbolizer = sf.createRasterSymbolizer();
       String xmlFragment = transformer.transform( defaultRasterSymbolizer );
       assertNotNull( xmlFragment );
       
       RasterSymbolizer opacityRasterSymbolizer = sf.createRasterSymbolizer();
       opacityRasterSymbolizer.setOpacity( ff.literal(1.0));
       
       xmlFragment = transformer.transform( opacityRasterSymbolizer );
       assertNotNull( xmlFragment );
        
    }
}
