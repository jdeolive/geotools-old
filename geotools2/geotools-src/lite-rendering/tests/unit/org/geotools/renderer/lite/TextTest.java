/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
/*
 * TextTest.java
 *
 * Created on 04 July 2002, 10:02
 */
package org.geotools.renderer.lite;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;

import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.filter.IllegalFilterException;
import org.geotools.map.DefaultMap;
import org.geotools.map.Map;
import org.geotools.renderer.Renderer;
import org.geotools.styling.Fill;
import org.geotools.styling.Font;
import org.geotools.styling.Mark;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;


/**
 * DOCUMENT ME!
 *
 * @author iant
 */
public class TextTest extends TestCase {
    /**
     * Creates a new instance of TextTest
     *
     * @param testName DOCUMENT ME!
     */
    public TextTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TextTest.class);

        return suite;
    }

    public void testTextRender() throws Exception {
        System.out.println("\n\nText Test\n\n");

        Frame frame = new Frame("text test");
        frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    e.getWindow().dispose();
                }
            });

        Envelope ex = new Envelope(0, 50, 0, 100);
        frame.setSize(300, 600);

        GeometryFactory geomFac = new GeometryFactory();
        ArrayList features = new ArrayList();
        int points = 4;
        int rows = 3;
        AttributeType[] pointAttribute = new AttributeType[3];
        pointAttribute[0] = AttributeTypeFactory.newAttributeType("centre",
                Point.class);
        pointAttribute[1] = AttributeTypeFactory.newAttributeType("size", Double.class);
        pointAttribute[2] = AttributeTypeFactory.newAttributeType("rotation", Double.class);

        FeatureType pointType = FeatureTypeFactory.newFeatureType(pointAttribute, "testPoint");

        for (int j = 0; j < rows; j++) {
            double angle = 0.0;

            for (int i = 0; i < points; i++) {
                Point point = makeSamplePoint(geomFac,
                        2.0 + ((double) i * ((ex.getWidth() - 4) / points)),
                        50.0 + ((double) j * ((50) / rows)));

                Double size = new Double(5.0 + (j * 5));
                Double rotation = new Double(angle);
                angle += 90.0;

                Feature pointFeature = pointType.create(new Object[] { point, size, rotation });

                //                System.out.println(""+pointFeature);
                features.add(pointFeature);
            }
        }

        AttributeType[] lineAttribute = new AttributeType[3];
        lineAttribute[0] = AttributeTypeFactory.newAttributeType("edge",
                LineString.class);
        lineAttribute[1] = AttributeTypeFactory.newAttributeType("size", Double.class);
        lineAttribute[2] = AttributeTypeFactory.newAttributeType("perpendicularoffset", Double.class);

        FeatureType lineType = FeatureTypeFactory.newFeatureType(lineAttribute, "testLine");
        rows = 2;
        points = 3;

        double off = 1;

        for (int j = 0; j < rows; j++) {
            double angle = 0.0;
            int sign = 1;

            for (int i = 0; i < points; i++) {
                LineString line = makeSimpleLineString(geomFac, (i * ex.getWidth()) / points,
                        j * 20, sign * 20, 20);
                Double size = new Double(12);
                Double poffset = new Double(off);
                sign--;

                Feature lineFeature = lineType.create(new Object[] { line, size, poffset });
                features.add(lineFeature);
            }

            off -= 2;
        }

        System.out.println("got " + features.size() + " features");

        FeatureCollection ft = FeatureCollections.newCollection();
        ft.addAll(features);

        Map map = new DefaultMap();



        map.addFeatureTable(ft, buildStyle());

        Renderer renderer = new LiteRenderer();

        Panel p = new Panel();
        frame.add(p);

        frame.setLocation(600, 0);
        frame.setVisible(true);
        renderer.setOutput(p.getGraphics(), p.getBounds());
        map.render(renderer, ex); //and finaly try and draw it!

        int w = 300;
        int h = 600;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, w, h);
        renderer.setOutput(g, new Rectangle(0, 0, w, h));
        map.render(renderer, ex); //and finaly try and draw it!


        // Thread.sleep(5000);
        frame.dispose();
    }

    private Point makeSamplePoint(final GeometryFactory geomFac, double x, double y) {
        Coordinate c = new Coordinate(x, y);
        Point point = geomFac.createPoint(c);

        return point;
    }

    private LineString makeSampleLineString(final GeometryFactory geomFac, double xoff, double yoff) {
        Coordinate[] linestringCoordinates = new Coordinate[4];
        linestringCoordinates[0] = new Coordinate(0.0d + xoff, 0.0d + yoff);
        linestringCoordinates[1] = new Coordinate(10.0d + xoff, 10.0d + yoff);
        linestringCoordinates[2] = new Coordinate(15.0d + xoff, 20.0d + yoff);
        linestringCoordinates[3] = new Coordinate(20.0d + xoff, 30.0d + yoff);

        LineString line = geomFac.createLineString(linestringCoordinates);

        return line;
    }

    private LineString makeSimpleLineString(final GeometryFactory geomFac, double xstart,
        double ystart, double width, double height) {
        Coordinate[] linestringCoordinates = new Coordinate[2];
        linestringCoordinates[0] = new Coordinate(xstart, ystart);

        linestringCoordinates[1] = new Coordinate(xstart + width, ystart + height);

        LineString line = geomFac.createLineString(linestringCoordinates);

        return line;
    }
    
    public Style buildStyle() throws IllegalFilterException {
        StyleBuilder sb = new StyleBuilder();
        Style style = sb.createStyle();
        
        // point style
        Mark triangle = sb.createMark(sb.MARK_TRIANGLE, Color.MAGENTA, 0.5);
        PointSymbolizer ps = sb.createPointSymbolizer(sb.createGraphic(null, triangle, null));
        Font font = sb.createFont("Dialog", 10);
        font.setFontSize(sb.attributeExpression("size"));
        PointPlacement pp = sb.createPointPlacement();
        pp.setDisplacement(sb.createDisplacement(5, 0));
        pp.setRotation(sb.attributeExpression("rotation"));
        Fill fill = sb.createFill(Color.decode("#AAAA00"));
        TextSymbolizer ts = sb.createTextSymbolizer(fill, new Font[] {font}, sb.createHalo(), sb.literalExpression("Point Label"), pp, null);
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("testPoint", 
          new Symbolizer[] {ps, ts}));
        
        // line style
        Font font2 = sb.createFont("SansSerif", 10);
        font2.setFontSize(sb.attributeExpression("size"));
        TextSymbolizer ts2 = sb.createStaticTextSymbolizer(Color.decode("#FFAA00"), font2, "Line label");
        ts2.setHalo(sb.createHalo(Color.RED, 3));
        ts2.setLabelPlacement(sb.createLinePlacement(sb.attributeExpression("perpendicularoffset")));
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("testLine", 
          new Symbolizer[] {sb.createLineSymbolizer(Color.BLUE, 1), ts2})); 
         
        
        return style;
    }
}
