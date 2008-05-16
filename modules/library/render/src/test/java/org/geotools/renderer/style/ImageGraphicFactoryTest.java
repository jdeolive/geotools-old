package org.geotools.renderer.style;

import java.net.URL;

import javax.swing.Icon;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.renderer.lite.StreamingRenderer;
import org.opengis.filter.FilterFactory;

public class ImageGraphicFactoryTest extends TestCase {

    private ImageGraphicFactory image;
    private FilterFactory ff;

    @Override
    protected void setUp() throws Exception {
        image = new ImageGraphicFactory();
        ff = CommonFactoryFinder.getFilterFactory(null);
    }
    
    /**
     * Check that at least the well known png and jpeg formats are supported
     * @throws Exception
     */
    public void testFormats() throws Exception {
        assertTrue(image.getSupportedMimeTypes().contains("image/png"));
        assertTrue(image.getSupportedMimeTypes().contains("image/jpeg"));
    }
    
    public void testInvalidPaths() throws Exception {
        assertNull(image.getIcon(null, ff.literal("http://www.nowhere.com"), "image/not!", 20));
        try {
            image.getIcon(null, ff.literal("ThisIsNotAUrl"), "image/png", 20);
            fail("Should have throw an exception, invalid url");
        } catch(IllegalArgumentException e) {
        }
    }
    
    public void testLocalURL() throws Exception {
        URL url = StreamingRenderer.class.getResource("test-data/draw.png");
        assertNotNull(url);
        // first call, non cached path
        Icon icon = image.getIcon(null, ff.literal(url), "image/png", 80);
        assertNotNull(icon);
        assertEquals(80, icon.getIconHeight());
    }
    
    public void testNaturalSize() throws Exception {
        URL url = StreamingRenderer.class.getResource("test-data/draw.png");
        assertNotNull(url);
        Icon icon = image.getIcon(null, ff.literal(url), "image/png", -1);
        assertNotNull(icon);
        assertEquals(22, icon.getIconHeight());
    }
}
