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
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.feature.FeatureCollection;
import org.geotools.filter.Filter;
import org.geotools.map.DefaultMap;
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
    private static String dataFolder;
    
    public void setUp() throws Exception {
        if(setup) return;
        setup = true;
        dataFolder = System.getProperty("dataFolder");
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData/";
        }
        ds = new ImageDataSource(dataFolder+"etopo.png");
        
        if(ds == null){
            fail("unable to build datasource " + dataFolder+"etopo.png");
        }
        System.out.println("get a datasource " + ds);
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
        org.geotools.map.Map map = new DefaultMap();
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
        map.addFeatureTable(ft,style);
        LiteRenderer renderer = new LiteRenderer();
        Frame frame = new Frame();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {e.getWindow().dispose(); }
        });
        Panel p = new Panel();
        frame.add(p);
        int w = 600, h = 300;
        frame.setSize(w,h);
        frame.setVisible(true);
        renderer.setOutput(p.getGraphics(),p.getBounds());
        map.render(renderer,ex);//and finaly try and draw it!
        
        BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0,0,w,h);
        renderer.setOutput(g,new java.awt.Rectangle(0,0,w,h));
        map.render(renderer,ex);//and finaly try and draw it!
        String dataFolder = System.getProperty("dataFolder");
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData";
        }
        File file = new File(dataFolder, "RendererStyle.jpg"); 
        FileOutputStream out = new FileOutputStream(file);
        ImageIO.write(image, "JPEG", out); 
        
        //Thread.sleep(5000);
        frame.dispose();
    }
    
}
