/*
 * LegendImageGeneratorTest.java
 *
 * Created on 19 June 2003, 10:14
 */
package org.geotools.renderer;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileOutputStream;

import java.util.logging.Logger;

import javax.imageio.ImageIO;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureFactory;
import org.geotools.feature.FeatureType;

import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLDStyle;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.wms.servlet.ImageView;


/**
 *
 * @author  iant
 */
public class LegendImageGeneratorTest extends TestCase {
    /**
     * The logger for the rendering module.
     */
    private static final Logger LOGGER = Logger.getLogger(
                                                 "org.geotools.rendering");
    private static final org.geotools.filter.FilterFactory filterFactory = 
            org.geotools.filter.FilterFactory.createFilterFactory();

    /** Creates a new instance of LegendImageGeneratorTest */
    public LegendImageGeneratorTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
//      Logger.getLogger(
//  "org.geotools.renderer.Java2DRenderer").setUseParentHandlers(false);
//      Logger.getLogger(
//  "org.geotools.renderer.Java2DRenderer").getParent().setLevel(java.util.logging.Level.ALL);
//      java.util.logging.Handler h = new java.util.logging.ConsoleHandler();
//      h.setLevel(java.util.logging.Level.ALL);
//      Logger.getLogger(
//  "org.geotools.renderer.Java2DRenderer").addHandler(h);
//      LOGGER.setUseParentHandlers(false);
//      LOGGER.getParent().setLevel(java.util.logging.Level.ALL);
//      LOGGER.addHandler(h);

      
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(LegendImageGeneratorTest.class);

        return suite;
    }

    public void testSimpleRender() throws Exception {
        //same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");

        // Request extent
        Envelope ex = new Envelope(5, 15, 5, 15);

        StyleFactory sFac = StyleFactory.createStyleFactory();

        //The following is complex, and should be built from
        //an SLD document and not by hand
        PointSymbolizer pointsym = sFac.createPointSymbolizer();
        pointsym.setGraphic(sFac.getDefaultGraphic());

        LineSymbolizer linesym = sFac.createLineSymbolizer();
        Stroke myStroke = sFac.getDefaultStroke();
        myStroke.setColor(filterFactory.createLiteralExpression("#0000ff"));
        myStroke.setWidth(filterFactory.createLiteralExpression(new Integer(5)));
        LOGGER.info("got new Stroke " + myStroke);
        linesym.setStroke(myStroke);

        PolygonSymbolizer polysym = sFac.createPolygonSymbolizer();
        Fill myFill = sFac.getDefaultFill();
        myFill.setColor(filterFactory.createLiteralExpression("#ff0000"));
        polysym.setFill(myFill);
        polysym.setStroke(sFac.getDefaultStroke());

        Rule rule = sFac.createRule();
        rule.setSymbolizers(new Symbolizer[] { polysym });

        FeatureTypeStyle fts = sFac.createFeatureTypeStyle(new Rule[] { rule });


        //fts.setRules(new Rule[]{rule});
        rule.setTitle("Polygon");

        Rule rule2 = sFac.createRule();
        rule2.setSymbolizers(new Symbolizer[] { linesym });

        FeatureTypeStyle fts2 = sFac.createFeatureTypeStyle();
        rule2.setTitle("line");
        fts2.setRules(new Rule[] { rule2 });
        fts2.setFeatureTypeName("linefeature");

        Rule rule3 = sFac.createRule();
        rule3.setSymbolizers(new Symbolizer[] { pointsym });

        FeatureTypeStyle fts3 = sFac.createFeatureTypeStyle();
        fts3.setRules(new Rule[] { rule3 });
        fts3.setFeatureTypeName("pointfeature");
        rule3.setTitle("point");

        Style style = sFac.createStyle();
        style.setFeatureTypeStyles(new FeatureTypeStyle[] { fts, fts2, fts3 });

        LegendImageGenerator lig = new LegendImageGenerator(style, 300, 300);
        BufferedImage image = lig.getLegend(Color.white);
        
        ImageView view = new ImageView(image,"Simple Test");
        view.setSize(300,300);
        view.createFrame();
        
        
//        String dataFolder = System.getProperty("dataFolder");
//
//        if (dataFolder == null) {
//            //then we are being run by maven
//            dataFolder = System.getProperty("basedir");
//            dataFolder += "/tests/unit/testData";
//        }

        //File file = new File(dataFolder, "LegendGraphicTest.jpg");
        File file = new File(getClass().getResource("/testData/").getPath(),"LegendGraphicTest.jpg");
        FileOutputStream out = new FileOutputStream(file);
        ImageIO.write(image, "JPEG", out);
        out.close();

        try{
            Thread.sleep(2000);
        }catch (InterruptedException e){}
        view.close();

        System.out.println("-------------------------------------------------------------");
    }
    
    public void testComplexStyle() throws Exception{
//        String dataFolder = System.getProperty("dataFolder");
//
//        if (dataFolder == null) {
//            //then we are being run by maven
//            dataFolder = System.getProperty("basedir");
//            dataFolder += "/tests/unit/testData";
//        }
//        
//        File f = new File(dataFolder,"popshade.sld");
        File f = new File(getClass().getResource("/testData/popshade.sld").getPath());
        
        System.out.println("testing reader using "+f.toString());
        StyleFactory factory = StyleFactory.createStyleFactory();
        SLDStyle stylereader = new SLDStyle(factory,f);
        Style[] style = stylereader.readXML();
        LegendImageGenerator lig = new LegendImageGenerator(style[0], 300, 100);
        
        BufferedImage image = lig.getLegend(Color.white);
        ImageView view = new ImageView(image,"Complex Test");
        view.setSize(300,100);
        view.createFrame();
        File file = new File(getClass().getResource("/testData/").getPath(), "LegendGraphicTest2.jpg");
        FileOutputStream out = new FileOutputStream(file);
        ImageIO.write(image, "JPEG", out);
        out.close();
        try{
            Thread.sleep(2000);
        }catch (InterruptedException e){}
        view.close();
        
        
        
    }
        
}