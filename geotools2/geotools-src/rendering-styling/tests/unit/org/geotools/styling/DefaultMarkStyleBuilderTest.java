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
 * RenderStyleTest.java
 *
 * Created on 27 May 2002, 15:40
 */
package org.geotools.styling;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
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
import org.geotools.filter.FilterFactory;
import org.geotools.map.DefaultMap;
import org.geotools.map.Map;
import org.geotools.renderer.Renderer;

import org.geotools.styling.AnchorPoint;
import org.geotools.styling.Font;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.geotools.renderer.lite.LiteRenderer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.TextSymbolizer;


/**
 * DOCUMENT ME!
 *
 * @author jamesm
 */
public class DefaultMarkStyleBuilderTest extends TestCase {
    public DefaultMarkStyleBuilderTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DefaultMarkTest.class);

        return suite;
    }

    public void testSimpleRender() throws Exception {
        //same as the datasource test, load in some features into a table
        System.out.println("\n\nMark Test\n");

        // Request extent
        Envelope ex = new Envelope(0, 45, 0, 45);

        //EnvelopeExtent ex = new EnvelopeExtent(0, 45, 0, 45);
        GeometryFactory geomFac = new GeometryFactory();
        ArrayList features = new ArrayList();

        AttributeType[] pointAttribute = new AttributeType[4];
        pointAttribute[0] = AttributeTypeFactory.newAttributeType("centre", Point.class);
        pointAttribute[1] = AttributeTypeFactory.newAttributeType("size", Double.class);
        pointAttribute[2] = AttributeTypeFactory.newAttributeType("rotation", Double.class);
        pointAttribute[3] = AttributeTypeFactory.newAttributeType("name", String.class);

        FeatureType pointType = FeatureTypeFactory.newFeatureType(pointAttribute, "testPoint");

        AttributeType[] labelAttribute = new AttributeType[4];
        labelAttribute[0] = AttributeTypeFactory.newAttributeType("centre", Point.class);
        labelAttribute[1] = AttributeTypeFactory.newAttributeType("name", String.class);
        labelAttribute[2] = AttributeTypeFactory.newAttributeType("X", Double.class);
        labelAttribute[3] = AttributeTypeFactory.newAttributeType("Y", Double.class);

        FeatureType labelType = FeatureTypeFactory.newFeatureType(labelAttribute, "labelPoint");
        String[] marks = { "Circle", "Triangle", "Cross", "Star", "X", "Square", "Arrow" };
        double size = 6;
        double rotation = 0;
        int rows = 7;

        for (int j = 0; j < rows; j++) {
            Point point = makeSamplePoint(geomFac, 2, 5.0 + (j * 5));
            Feature pointFeature = labelType.create(new Object[] {
                        point, "" + size + "/" + rotation, new Double(0.3), new Double(.5)
                    });
            features.add(pointFeature);

            for (int i = 0; i < marks.length; i++) {
                point = makeSamplePoint(geomFac, ((double) i * 5.0) + 10.0, 5.0 + (j * 5));
                pointFeature = pointType.create(new Object[] {
                            point, new Double(size), new Double(rotation), marks[i]
                        });
                features.add(pointFeature);
            }

            size += 2;
            rotation += 45;
        }

        for (int i = 0; i < marks.length; i++) {
            Point point = makeSamplePoint(geomFac, ((double) i * 5.0) + 10.0, 5.0 + (rows * 5));
            Feature pointFeature = labelType.create(new Object[] {
                        point, marks[i], new Double(.5), new Double(0)
                    });
            features.add(pointFeature);
        }

        //        System.out.println("got "+features.size()+" features");
        FeatureCollection ft = FeatureCollections.newCollection();
        ft.addAll(features);

        Map map = new DefaultMap();

        Style style = buildStyle();

        map.addFeatureTable(ft, style);

        Renderer renderer = new LiteRenderer();
        Frame frame = new Frame("default mark test");
        frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    e.getWindow().dispose();
                }
            });

        Panel p = new Panel();
        frame.add(p);
        frame.setSize(300, 300);
        frame.setLocation(300, 0);
        frame.setVisible(true);
        renderer.setOutput(p.getGraphics(), p.getBounds());
        map.render(renderer, ex); //and finaly try and draw it!

        int w = 400;
        int h = 400;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, w, h);
        renderer.setOutput(g, new Rectangle(0, 0, w, h));
        map.render(renderer, ex); //and finaly try and draw it!

//        URL base = getClass().getResource("testData/");
//        File file = new File(base.getPath(), "DefaultMarkTest.jpg");
//        FileOutputStream out = new FileOutputStream(file);
//        boolean fred = ImageIO.write(image, "JPEG", out);

//        if (!fred) {
//            System.out.println("Failed to write image to " + file.toString());
//        }

        // Thread.sleep(5000);
        frame.dispose();
    }

    private Point makeSamplePoint(final GeometryFactory geomFac, double x, double y) {
        Coordinate c = new Coordinate(x, y);
        Point point = geomFac.createPoint(c);

        return point;
    }

    private Style buildStyle() throws Exception {
        StyleBuilder sb = new StyleBuilder();
        FilterFactory ff = sb.getFilterFactory();
        Style style = sb.createStyle();
        style.setName("MyStyle");

        // "testPoint" feature type style
        Mark testMark = sb.createMark(sb.attributeExpression("name"),
                sb.createFill(Color.RED, 0.5), null);
        Graphic graph = sb.createGraphic(null, new Mark[] { testMark }, null,
                sb.literalExpression(1), sb.attributeExpression("size"),
                sb.attributeExpression("rotation"));
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("testPoint",
                new Symbolizer[] { sb.createPointSymbolizer(graph) }));

        // "labelPoint" feature type style
        AnchorPoint anchorPoint = sb.createAnchorPoint(sb.attributeExpression("X"),
                sb.attributeExpression("Y"));
        PointPlacement pointPlacement = sb.createPointPlacement(anchorPoint, null,
                sb.literalExpression(0));
        TextSymbolizer textSymbolizer = sb.createTextSymbolizer(sb.createFill(Color.BLACK),
                new Font[] { sb.createFont("Times New Roman", 10), sb.createFont("Arial", 10) },
                sb.createHalo(), sb.attributeExpression("name"), pointPlacement, null);
        Mark circle = sb.createMark(sb.MARK_CIRCLE, Color.RED);
        Graphic graph2 = sb.createGraphic(null, circle, null, 1, 4, 0);
        PointSymbolizer pointSymbolizer = sb.createPointSymbolizer(graph2);
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("labelPoint",
                new Symbolizer[] { textSymbolizer, pointSymbolizer }));

        return style;
    }
}
