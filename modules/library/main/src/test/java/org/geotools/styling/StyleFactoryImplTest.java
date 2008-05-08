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
 */
package org.geotools.styling;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author iant
 * @source $URL$
 */
public class StyleFactoryImplTest extends TestCase {
    static StyleFactory styleFactory;
    static FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);        
    static SimpleFeature feature;
    protected static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.styling");

    public StyleFactoryImplTest(java.lang.String testName) {
        super(testName);

        feature = null;
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(StyleFactoryImplTest.class);

        return suite;
    }

    /**
     * Test of createStyle method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateStyle() {
        LOGGER.finer("testCreateStyle");

        styleFactory = CommonFactoryFinder.getStyleFactory(null);

        assertNotNull("Failed to build styleFactory", styleFactory);
    }

    /**
     * Test of createPointSymbolizer method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreatePointSymbolizer() {
        LOGGER.finer("testCreatePointSymbolizer");

        PointSymbolizer ps = styleFactory.createPointSymbolizer();

        assertNotNull("Failed to create PointSymbolizer", ps);
    }

    /**
     * Test of createPolygonSymbolizer method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreatePolygonSymbolizer() {
        LOGGER.finer("testCreatePolygonSymbolizer");

        PolygonSymbolizer ps = styleFactory.createPolygonSymbolizer();

        assertNotNull("Failed to create PolygonSymbolizer", ps);
    }

    /**
     * Test of createLineSymbolizer method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateLineSymbolizer() {
        LOGGER.finer("testCreateLineSymbolizer");

        LineSymbolizer ls = styleFactory.createLineSymbolizer();

        assertNotNull("Failed to create PolygonSymbolizer", ls);
    }

    /**
     * Test of createTextSymbolizer method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateTextSymbolizer() {
        LOGGER.finer("testCreateTextSymbolizer");

        TextSymbolizer ts = styleFactory.createTextSymbolizer();

        assertNotNull("Failed to create TextSymbolizer", ts);
    }

    /**
     * Test of createFeatureTypeStyle method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateFeatureTypeStyle() {
        LOGGER.finer("testCreateFeatureTypeStyle");

        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle();

        assertNotNull("failed to create featureTypeStyle", fts);
    }

    /**
     * Test of createRule method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateRule() {
        LOGGER.finer("testCreateRule");

        Rule r = styleFactory.createRule();

        assertNotNull("failed to create Rule", r);
    }

    /**
     * Test of createStroke method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateStroke() {
        LOGGER.finer("testCreateStroke");

        Stroke s = styleFactory.createStroke(filterFactory
                .literal("#000000"),
                filterFactory.literal(2.0));

        assertNotNull("Failed to build stroke ", s);

        s = styleFactory.createStroke(filterFactory.literal(
                    "#000000"), filterFactory.literal(2.0),
                filterFactory.literal(0.5));

        assertNotNull("Failed to build stroke ", s);

        s = styleFactory.createStroke(filterFactory.literal(
                    "#000000"), filterFactory.literal(2.0),
                filterFactory.literal(0.5),
                filterFactory.literal("bevel"),
                filterFactory.literal("square"),
                new float[] { 1.1f, 2.1f, 6f, 2.1f, 1.1f, 5f },
                filterFactory.literal(3), null, null);

        assertNotNull("Failed to build stroke ", s);

        assertEquals("Wrong color ", "#000000",
            s.getColor().evaluate(feature).toString());
        assertEquals("Wrong width ", "2.0",
            s.getWidth().evaluate(feature).toString());
        assertEquals("Wrong opacity ", "0.5",
            s.getOpacity().evaluate(feature).toString());
        assertEquals("Wrong linejoin ", "bevel",
            s.getLineJoin().evaluate(feature).toString());
        assertEquals("Wrong linejoin ", "square",
            s.getLineCap().evaluate(feature).toString());
        assertEquals("Broken dash array", 2.1f, s.getDashArray()[1], 0.001f);
        assertEquals("Wrong dash offset ", "3",
            s.getDashOffset().evaluate(feature).toString());
    }

    /**
     * Test of createFill method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateFill() {
        LOGGER.finer("testCreateFill");

        Fill f = styleFactory.createFill(filterFactory.literal(
                    "#808080"));

        assertNotNull("Failed to build fill", f);

        f = styleFactory.createFill(filterFactory.literal(
                    "#808080"), filterFactory.literal(1.0));
        assertNotNull("Failed to build fill", f);

        f = styleFactory.createFill(null);
        assertEquals( f.getColor(), Fill.DEFAULT.getColor() );
        assertSame( f.getColor(), Fill.DEFAULT.getColor() );
        
        assertEquals( f.getBackgroundColor(), Fill.DEFAULT.getBackgroundColor() );
        assertSame( f.getBackgroundColor(), Fill.DEFAULT.getBackgroundColor() );        
    }

    /**
     * Test of createMark method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateMark() {
        LOGGER.finer("testCreateMark");

        Mark m = styleFactory.createMark();

        assertNotNull("Failed to build mark ", m);
    }

    /**
     * Test of getSquareMark method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testGetNamedMarks() {
        LOGGER.finer("testGetNamedMarks");

        Mark m;
        String[] names = { "Square", "Circle", "Triangle", "Star", "X", "Cross" };

        for (int i = 0; i < names.length; i++) {
            try {
                Class target = styleFactory.getClass();

                //                LOGGER.finer("About to load get"+names[i]+"Mark");
                Method method = target.getMethod("get" + names[i] + "Mark",
                        (Class[]) null);

                //                LOGGER.finer("got method back " + method.toString());
                m = (Mark) method.invoke(styleFactory, (Object[]) null);
                assertNotNull("Failed to get " + names[i] + " mark ", m);

                Expression exp = filterFactory.literal(names[i]);
                assertEquals("Wrong sort of mark returned ", exp,
                    m.getWellKnownName());
                assertEquals("Wrong size of mark returned ", "6",
                    m.getSize().evaluate(feature).toString());
            } catch (InvocationTargetException ite) {
                ite.getTargetException().printStackTrace();
                fail("InvocationTargetException " + ite.getTargetException());
            } catch (Exception e) {
                e.printStackTrace();
                fail("Exception " + e.toString());
            }
        }
    }

    /**
     * Test of createGraphic method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateGraphic() {
        LOGGER.finer("testCreateGraphic");

        ExternalGraphic[] externalGraphics = new ExternalGraphic[] {
                styleFactory.createExternalGraphic("http://www.ccg.leeds.ac.uk/ian/geotools/icons/rail.gif",
                    "image/gif")
            };
        Mark[] marks = new Mark[] { styleFactory.getCircleMark() };
        Mark[] symbols = new Mark[0];
        Expression opacity = filterFactory.literal(0.5);
        Expression size = filterFactory.literal(10);
        Expression rotation = filterFactory.literal(145.0);
        Graphic g = styleFactory.createGraphic(externalGraphics, marks,
                symbols, opacity, size, rotation);

        assertNotNull("failed to build graphic ", g);
    }

    /**
     * Test of createFont method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateFont() {
        LOGGER.finer("testCreateFont");

        Expression fontFamily = filterFactory.literal("Times");
        Expression fontStyle = filterFactory.literal("Italic");
        Expression fontWeight = filterFactory.literal("Bold");
        Expression fontSize = filterFactory.literal("12");
        Font f = styleFactory.createFont(fontFamily, fontStyle, fontWeight,
                fontSize);

        assertNotNull("Failed to build font", f);

        assertEquals("Wrong font type ", "Times",
            f.getFontFamily().evaluate(feature).toString());
        assertEquals("Wrong font Style ", "Italic",
            f.getFontStyle().evaluate(feature).toString());
        assertEquals("Wrong font weight ", "Bold",
            f.getFontWeight().evaluate(feature).toString());
        assertEquals("Wrong font size ", "12",
            f.getFontSize().evaluate(feature).toString());
    }

    /**
     * Test of createLinePlacement method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateLinePlacement() {
        LOGGER.finer("testCreateLinePlacement");

        LinePlacement lp = styleFactory.createLinePlacement(filterFactory
                .literal(10));

        assertNotNull("failed to create LinePlacement", lp);
    }

    /**
     * Test of createPointPlacement method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreatePointPlacement() {
        LOGGER.finer("testCreatePointPlacement");

        AnchorPoint anchorPoint = styleFactory.createAnchorPoint(filterFactory
                .literal(1.0),
                filterFactory.literal(0.5));
        Displacement displacement = styleFactory.createDisplacement(filterFactory
                .literal(10.0),
                filterFactory.literal(5.0));
        Expression rotation = filterFactory.literal(90.0);
        PointPlacement pp = styleFactory.createPointPlacement(anchorPoint,
                displacement, rotation);

        assertNotNull("failed to create PointPlacement", pp);

        assertEquals("Wrong X anchorPoint ", "1.0",
            pp.getAnchorPoint().getAnchorPointX().evaluate(feature).toString());
        assertEquals("Wrong Y anchorPoint ", "0.5",
            pp.getAnchorPoint().getAnchorPointY().evaluate(feature).toString());
        assertEquals("Wrong X displacement ", "10.0",
            pp.getDisplacement().getDisplacementX().evaluate(feature).toString());
        assertEquals("Wrong Y displacement ", "5.0",
            pp.getDisplacement().getDisplacementY().evaluate(feature).toString());
        assertEquals("Wrong Rotation ", "90.0",
            pp.getRotation().evaluate(feature).toString());
    }

    /**
     * Test of createHalo method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateHalo() {
        LOGGER.finer("testCreateHalo");

        Halo h = styleFactory.createHalo(styleFactory.getDefaultFill(),
                filterFactory.literal(4));

        assertNotNull("Failed to build halo", h);

        assertEquals("Wrong radius", 4,
            ((Number) h.getRadius().evaluate(feature)).intValue());
    }

    //    
    //    /** Test of getDefaultFill method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testGetDefaultFill() {
    //        LOGGER.finer("testGetDefaultFill");
    //        
    //    }
    //    
    //    /** Test of getDefaultLineSymbolizer method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testGetDefaultLineSymbolizer() {
    //        LOGGER.finer("testGetDefaultLineSymbolizer");
    //        
    //        
    //    }
    //    
    //    /** Test of getDefaultMark method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testGetDefaultMark() {
    //        LOGGER.finer("testGetDefaultMark");
    //        
    //        
    //    }
    //    
    //    /** Test of getDefaultPointSymbolizer method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testGetDefaultPointSymbolizer() {
    //        LOGGER.finer("testGetDefaultPointSymbolizer");
    //        
    //        
    //    }
    //    
    //    /** Test of getDefaultPolygonSymbolizer method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testGetDefaultPolygonSymbolizer() {
    //        LOGGER.finer("testGetDefaultPolygonSymbolizer");
    //        
    //    }
    //    
    //    /** Test of getDefaultStroke method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testGetDefaultStroke() {
    //        LOGGER.finer("testGetDefaultStroke");
    //        
    //        
    //    }
    //    
    //    /** Test of getDefaultStyle method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testGetDefaultStyle() {
    //        LOGGER.finer("testGetDefaultStyle");
    //        
    //        
    //    }
    //    
    //    /** Test of getDefaultTextSymbolizer method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testGetDefaultTextSymbolizer() {
    //        LOGGER.finer("testGetDefaultTextSymbolizer");
    //        
    //        
    //    }
    //    
    //    /** Test of getDefaultFont method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testGetDefaultFont() {
    //        LOGGER.finer("testGetDefaultFont");
    //        
    //        
    //    }
    //    
    //    /** Test of getDefaultGraphic method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testGetDefaultGraphic() {
    //        LOGGER.finer("testGetDefaultGraphic");
    //        
    //        
    //    }
    //    
    //    /** Test of createRasterSymbolizer method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testCreateRasterSymbolizer() {
    //        LOGGER.finer("testCreateRasterSymbolizer");
    //        
    //        
    //    }
}
