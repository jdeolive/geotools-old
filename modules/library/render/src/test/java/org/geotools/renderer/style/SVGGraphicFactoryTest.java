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
package org.geotools.renderer.style;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.renderer.lite.StreamingRenderer;
import org.opengis.filter.FilterFactory;

public class SVGGraphicFactoryTest extends TestCase {

    private SVGGraphicFactory svg;
    private FilterFactory ff;

    @Override
    protected void setUp() throws Exception {
        svg = new SVGGraphicFactory();
        ff = CommonFactoryFinder.getFilterFactory(null);
    }
    
    public void testInvalidPaths() throws Exception {
        assertNull(svg.getIcon(null, ff.literal("http://www.nowhere.com"), "image/svg+not!", 20));
        try {
            svg.getIcon(null, ff.literal("ThisIsNotAUrl"), "image/svg", 20);
            fail("Should have throw an exception, invalid url");
        } catch(IllegalArgumentException e) {
        }
    }
    
    public void testLocalURL() throws Exception {
        URL url = StreamingRenderer.class.getResource("test-data/gradient.svg");
        assertNotNull(url);
        // first call, non cached path
        Icon icon = svg.getIcon(null, ff.literal(url), "image/svg", 20);
        assertNotNull(icon);
        assertEquals(20, icon.getIconHeight());
        // check caching is working
        assertTrue(svg.glyphCache.containsKey(url));
        
        // second call, hopefully using the cached path
        icon = svg.getIcon(null, ff.literal(url), "image/svg", 20);
        assertNotNull(icon);
        assertEquals(20, icon.getIconHeight());
    }
    
    public void testNaturalSize() throws Exception {
        URL url = StreamingRenderer.class.getResource("test-data/gradient.svg");
        assertNotNull(url);
        // first call, non cached path
        Icon icon = svg.getIcon(null, ff.literal(url), "image/svg", -1);
        assertNotNull(icon);
        assertEquals(500, icon.getIconHeight());
    }
}
