/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 * Created on 08 June 2002, 00:04
 */
package org.geotools.svg;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * DOCUMENT ME!
 *
 * @author James
 * @source $URL$
 */
public class SVGTest extends TestCase {    
    public SVGTest(java.lang.String testName) throws Exception{
        super(testName);
    }
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SVGTest.class);

        return suite;
    }

    public void testGenerateSVG() {
        String stylefile = "simple.sld";
        String gmlfile = "simple.gml";
        createSVG("simple.sld", "simple.gml", "simple.svg");
    }

    public void testBlueLake() {
        String stylefile = "bluelake.sld";
        String gmlfile = "bluelake.gml";
        //createSVG(stylefile, gmlfile, "bluelake.svg");
    }

    public void testNameFilterSVG() {
        createSVG("nameFilter.sld", "simple.gml", "nameFilter.svg");
    }

    /**
     * DOCUMENT ME!
     *
     * @param stylefile
     * @param gmlfile
     * @param outfile DOCUMENT ME!
     */
    private void createSVG(final String stylefile, final String gmlfile, final String outfile) {
        /* TODO: restore GML reading
        try {
            GenerateSVG gen = new GenerateSVG();
            File testFile = TestData.file( this, gmlfile );
            // DataSource ds = new GMLDataSource(url);            
            // FeatureCollection<SimpleFeatureType, SimpleFeature> fc = ds.getFeatures(Query.ALL);
            
            URI uri = testFile.toURI();            
            Map hints = new HashMap();
            Object obj = DocumentFactory.getInstance( uri, hints );
            System.out.println( "what is this:"+obj );

            FeatureCollection<SimpleFeatureType, SimpleFeature> fc = null;
            File f = TestData.file( this, stylefile );

            MapContext mapContext = new DefaultMapContext();
            StyleFactory sFac = StyleFactory.createStyleFactory();
            SLDStyle reader = new SLDStyle(sFac, f);
            Style[] style = reader.readXML();
            mapContext.addLayer(fc, style[0]);

            File file = TestData.temp( this, outfile );
            FileOutputStream out = new FileOutputStream( file ); 

            gen.setCanvasSize(new Dimension(500, 500));
            gen.go(mapContext, fc.getBounds(), out);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            fail("failed because of exception " + e.toString());
        }
        */
    }
}
