/*
 * PNGDataSourceTest.java
 * JUnit based test
 *
 * Created on 30 October 2002, 17:25
 */

package org.geotools.coverage;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.feature.FeatureCollection;
import org.geotools.filter.Filter;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.renderer.lite.LiteRenderer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;

import com.vividsolutions.jts.geom.Envelope;


/**
 *
 * @author iant
 */
public class TestImageDataSource extends TestCase {
    
    public TestImageDataSource(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) throws Exception {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(TestImageDataSource.class);
        
        return suite;
    }
    private static boolean setup = false;
    private static ImageDataSource ds;
    
    public void setUp() throws Exception {
        if(setup) return;
        setup = true;
        ds = new ImageDataSource(getResourcePath("/testData/etopo.png"));
        
        if(ds == null){
            fail("unable to build datasource /testData/etopo.png");
        }
        System.out.println("get a datasource " + ds);
    }

    private String getResourcePath(String resourceName) {
        URL r = getClass().getResource(resourceName);
        if (r == null) {
        	throw new RuntimeException("Could not locate resource : " + resourceName);
        }
        return r.getFile();
    }
    
    
    
    
    
    
    /** Test of getFeatures method, of class org.geotools.coverage.PNGDataSource. */
    public void testGetFeatures() throws Exception{
        System.out.println("testGetFeatures");
	//HACK: We need a Filter.ALL or a getFeatures()
        Filter filter = null;
        FeatureCollection fc = ds.getFeatures(filter);
        
        
    }
    
    /** Test of getBbox method, of class org.geotools.coverage.PNGDataSource. */
    public void testGetBbox() {
        System.out.println("testGetBbox");
        Envelope env = ds.getBbox(false);
        assertNotNull("null bounding box",env);
        System.out.println("Bounding box = " + env.toString());
        
        
    }
    
    public void testRenderImage() throws Exception{
	Filter filter = null;
        FeatureCollection ft = ds.getFeatures(filter);
        MapContext mapContext = new DefaultMapContext();
        StyleFactory sFac = StyleFactory.createStyleFactory();
        Envelope ex = ds.getBounds();
        //The following is complex, and should be built from
        //an SLD document and not by hand
        RasterSymbolizer rs = sFac.getDefaultRasterSymbolizer();
        Rule rule = sFac.createRule();
        rule.setSymbolizers(new Symbolizer[]{rs});
        FeatureTypeStyle fts = sFac.createFeatureTypeStyle(new Rule[]{rule});
        Style style = sFac.createStyle();
        style.setFeatureTypeStyles(new FeatureTypeStyle[]{fts});
        mapContext.addLayer(ft,style);
        LiteRenderer renderer = new LiteRenderer(mapContext);
        Frame frame = new Frame();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {e.getWindow().dispose(); }
        });
        Panel p = new Panel();
        frame.add(p);
        int w = 600, h = 300;
        frame.setSize(w,h);
        frame.setVisible(true);
		Rectangle screenRect = new Rectangle(w, h);
		AffineTransform at = renderer.worldToScreenTransform(ft.getBounds(), screenRect);
        renderer.paint((Graphics2D) p.getGraphics(), screenRect, at); 
        
        BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0,0,w,h);
		renderer.paint((Graphics2D) p.getGraphics(), screenRect, at);
        File file = new File(getResourcePath("/testData/etopo.png"));
        file = new File(file.getParent(), "RendererStyle.jpg"); 
        FileOutputStream out = new FileOutputStream(file);
        ImageIO.write(image, "JPEG", out); 
        
        //Thread.sleep(5000);
        frame.dispose();
    }
    
}
