/*
 * StylingTest.java
 * JUnit based test
 *
 * Created on April 12, 2002, 1:18 PM
 */

package org.geotools.styling;
import org.geotools.renderer.*;
import org.geotools.data.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.datasource.extents.*;
import org.geotools.feature.*;
import org.geotools.styling.*;
import org.geotools.map.*;
import org.geotools.filter.*;
import java.util.*;
import junit.framework.*;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
/**
 *
 * @author jamesm
 */
public class TextSymbolTest extends TestCase {
    
    public TextSymbolTest(java.lang.String testName) {
        super(testName);
        BasicConfigurator.configure();
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(TextSymbolTest.class);
        return suite;
    }
    
    public void testRender()throws Exception {
        
        System.out.println("\n\nTextSymbolTest\n");
        // Request extent
        EnvelopeExtent ex = new EnvelopeExtent(0, 45, 0, 45);
        
        GeometryFactory geomFac = new GeometryFactory();
        MemoryDataSource datasource = new MemoryDataSource();
        AttributeType[] pointAttribute = new AttributeType[4];
        pointAttribute[0] = new AttributeTypeDefault("centre", com.vividsolutions.jts.geom.Point.class);
        pointAttribute[1] = new AttributeTypeDefault("size",Double.class);
        pointAttribute[2] = new AttributeTypeDefault("rotation",Double.class);
        pointAttribute[3] = new AttributeTypeDefault("symbol",String.class);
        FeatureType pointType = new FeatureTypeFlat(pointAttribute).setTypeName("testPoint");
        FeatureFactory pointFac = new FeatureFactory(pointType);
        
        Point point;
        Feature pointFeature;
        String[] symbol = {"\uF04A", "\uF04B", "\uF059", "\uF05A", "\uF06B", "\uF06C", "\uF06E"};
        int size = 16;
        double rotation = 0.0;
        int rows = 8;
        for(int j=0;j<rows;j++){
            for(int i = 0; i < symbol.length; i++){
                point = makeSamplePoint(geomFac, (double)i*5.0+5.0, 5.0+j*5);
                pointFeature = pointFac.create(new Object[]{point,new Double(size),new Double(rotation),symbol[i]});
                datasource.addFeature(pointFeature);
            }
            size+=2;
            rotation+=45;
        }
        FeatureCollection ft = new FeatureCollectionDefault(datasource);
        
        org.geotools.map.Map map = new DefaultMap();
        
        //The following is complex, and should be built from
        //an SLD document and not by hand
        org.geotools.styling.DefaultFont font = new org.geotools.styling.DefaultFont();
        font.setFontFamily(new ExpressionLiteral("MapInfo Cartographic"));
        font.setFontSize(new ExpressionAttribute(pointType, "size"));
        
        ExpressionAttribute symbExpr = new ExpressionAttribute(pointType, "symbol");
        TextMark textMark = new TextMark(font,symbExpr);
        
        DefaultGraphic graphic = new DefaultGraphic();
        graphic.addSymbol(textMark);
        DefaultPointSymbolizer pointsym = new DefaultPointSymbolizer();
        pointsym.setGeometryPropertyName("centre");
        pointsym.setGraphic(graphic);
        
        DefaultRule rule3 = new DefaultRule();
        rule3.setSymbolizers(new Symbolizer[]{pointsym});
        DefaultFeatureTypeStyle fts3 = new DefaultFeatureTypeStyle();
        fts3.setRules(new Rule[]{rule3});
        fts3.setFeatureTypeName("testPoint");
        DefaultStyle style = new DefaultStyle();
        style.setFeatureTypeStyles(new FeatureTypeStyle[]{fts3});
        
        map.addFeatureTable(ft,style);
        Java2DRenderer renderer = new org.geotools.renderer.Java2DRenderer();
        Frame frame = new Frame();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {e.getWindow().dispose(); }
        });
        Panel p = new Panel();
        frame.add(p);
        frame.setSize(300,300);
        frame.setVisible(true);
        renderer.setOutput(p.getGraphics(),p.getBounds());
        map.render(renderer,ex.getBounds());//and finaly try and draw it!
        Thread.sleep(5000);
        
       
        frame.dispose();
    }
    private Point makeSamplePoint(final GeometryFactory geomFac, double x, double y) {
        Coordinate c = new Coordinate(x, y);
        Point point = geomFac.createPoint(c);
        return point;
    }
    
    
}
