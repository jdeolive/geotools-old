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
package org.geotools.styling;

import com.vividsolutions.jts.geom.LineString;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.filter.AbstractFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.IllegalFilterException;
import java.util.Set;


/**
 * Tests style cloning
 *
 * @author Sean Geoghegan
 */
public class StyleAttributeExtractorTest extends TestCase {
    private StyleFactory styleFactory;
    private FilterFactory filterFactory;
    private FeatureType testSchema = null;

    /**
     * Constructor for StyleCloneTest.
     *
     * @param arg0
     */
    public StyleAttributeExtractorTest(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        styleFactory = StyleFactory.createStyleFactory();
        filterFactory = FilterFactory.createFilterFactory();

        // Create the schema attributes
        AttributeType geometryAttribute = AttributeTypeFactory.newAttributeType("testGeometry",
                LineString.class);

        AttributeType booleanAttribute = AttributeTypeFactory.newAttributeType("testBoolean",
                Boolean.class);

        AttributeType charAttribute = AttributeTypeFactory.newAttributeType("testCharacter",
                Character.class);
        AttributeType byteAttribute = AttributeTypeFactory.newAttributeType("testByte", Byte.class);
        AttributeType shortAttribute = AttributeTypeFactory.newAttributeType("testShort",
                Short.class);
        AttributeType intAttribute = AttributeTypeFactory.newAttributeType("testInteger",
                Integer.class);
        AttributeType longAttribute = AttributeTypeFactory.newAttributeType("testLong", Long.class);
        AttributeType floatAttribute = AttributeTypeFactory.newAttributeType("testFloat",
                Float.class);
        AttributeType doubleAttribute = AttributeTypeFactory.newAttributeType("testDouble",
                Double.class);
        AttributeType stringAttribute = AttributeTypeFactory.newAttributeType("testString",
                String.class);
        AttributeType stringAttribute2 = AttributeTypeFactory.newAttributeType("testString2",
                String.class);

        // Builds the schema
        FeatureTypeFactory feaTypeFactory = FeatureTypeFactory.newInstance("test");
        feaTypeFactory.addType(geometryAttribute);

        feaTypeFactory.addType(booleanAttribute);

        feaTypeFactory.addType(charAttribute);

        feaTypeFactory.addType(byteAttribute);

        feaTypeFactory.addType(shortAttribute);

        //LOGGER.finer("added short to feature type");
        feaTypeFactory.addType(intAttribute);

        //LOGGER.finer("added int to feature type");
        feaTypeFactory.addType(longAttribute);

        //LOGGER.finer("added long to feature type");
        feaTypeFactory.addType(floatAttribute);

        //LOGGER.finer("added float to feature type");
        feaTypeFactory.addType(doubleAttribute);

        //LOGGER.finer("added double to feature type");
        feaTypeFactory.addType(stringAttribute);
        feaTypeFactory.addType(stringAttribute2);

        testSchema = feaTypeFactory.getFeatureType();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        styleFactory = null;
    }

    private void assertAttributeName(Style s, String name) {
        assertAttributeName(s, new String[] { name });
    }

    private void assertAttributeName(Style style, String[] names) {
        StyleAttributeExtractor sae = new StyleAttributeExtractor();
        style.accept(sae);

        Set attNames = sae.getAttributeNameSet();

        assertNotNull(attNames);
        assertEquals(names.length, attNames.size());

        for (int i = 0; i < names.length; i++) {
            assertTrue(attNames.contains(names[i]));
        }
    }

    private Style createStyle() {
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle();
        Rule rule1 = styleFactory.createRule();
        fts.addRule(rule1);

        Rule rule2 = styleFactory.createRule();
        fts.addRule(rule2);
        fts.setFeatureTypeName("feature-type-1");

        FeatureTypeStyle fts2 = styleFactory.createFeatureTypeStyle();
        fts2.setFeatureTypeName("feature-type-2");

        Style style = styleFactory.getDefaultStyle();
        style.addFeatureTypeStyle(fts);
        style.addFeatureTypeStyle(fts2);

        return style;
    }

    public void testStyle() throws Exception {
        Style s = createStyle();
        assertAttributeName(s, new String[0]);
    }

    public void testRule() throws Exception {
        Symbolizer symb1 = styleFactory.createLineSymbolizer(styleFactory.getDefaultStroke(),
                "geometry");
        Symbolizer symb2 = styleFactory.createPolygonSymbolizer(styleFactory.getDefaultStroke(),
                styleFactory.getDefaultFill(), "shape");
        Rule rule = styleFactory.createRule();
        rule.setSymbolizers(new Symbolizer[] { symb1, symb2 });

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].addRule(rule);
        assertAttributeName(s, new String[] { "geometry", "shape" });

        CompareFilter f = filterFactory.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
        f.addLeftValue(filterFactory.createAttributeExpression(testSchema, "testLong"));
        f.addRightValue(filterFactory.createLiteralExpression(10.0));
        rule.setFilter(f);

        assertAttributeName(s, new String[] { "geometry", "shape", "testLong" });
    }

    public void testPointSymbolizer() throws Exception {
        PointSymbolizer pointSymb = styleFactory.createPointSymbolizer();
        ExternalGraphic eg = styleFactory.createExternalGraphic("www.test.com", "image/png");
        Mark mark = styleFactory.createMark();
        Stroke stroke = styleFactory.getDefaultStroke();
        stroke.setWidth(filterFactory.createAttributeExpression(testSchema, "testInteger"));
        mark.setStroke(stroke);

        Expression opacity = filterFactory.createAttributeExpression(testSchema, "testLong");
        Expression rotation = filterFactory.createAttributeExpression(testSchema, "testDouble");
        Expression size = filterFactory.createAttributeExpression(testSchema, "testFloat");
        Graphic g = styleFactory.createGraphic(new ExternalGraphic[] { eg }, new Mark[] { mark },
                null, opacity, rotation, size);
        pointSymb.setGraphic(g);

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].getRules()[0].setSymbolizers(new Symbolizer[] { pointSymb });
        assertAttributeName(s, new String[] { "testInteger", "testLong", "testDouble", "testFloat" });

        pointSymb.setGeometryPropertyName("testGeometry");
        assertAttributeName(s,
            new String[] { "testInteger", "testLong", "testDouble", "testFloat", "testGeometry" });
    }

    public void testTextSymbolizer() throws Exception {
        TextSymbolizer textSymb = styleFactory.createTextSymbolizer();
        Expression offset = filterFactory.createAttributeExpression(testSchema, "testInteger");
        Expression label = filterFactory.createAttributeExpression(testSchema, "testString");
        textSymb.setLabelPlacement(styleFactory.createLinePlacement(offset));
        textSymb.setLabel(label);

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].getRules()[0].setSymbolizers(new Symbolizer[] { textSymb });
        assertAttributeName(s, new String[] { "testInteger", "testString" });

        Expression ancX = filterFactory.createAttributeExpression(testSchema, "testFloat");
        Expression ancY = filterFactory.createAttributeExpression(testSchema, "testDouble");
        AnchorPoint ancPoint = styleFactory.createAnchorPoint(ancX, ancY);
        LabelPlacement placement = styleFactory.createPointPlacement(ancPoint, null, null);
        textSymb.setLabelPlacement(placement);

        assertAttributeName(s, new String[] { "testFloat", "testDouble", "testString" });
    }

    public void testFont() throws Exception {
        Font font = styleFactory.createFont(filterFactory.createAttributeExpression(testSchema,
                    "testString"),
                filterFactory.createAttributeExpression(testSchema, "testString2"),
                filterFactory.createAttributeExpression(testSchema, "testLong"),
                filterFactory.createAttributeExpression(testSchema, "testBoolean"));

        TextSymbolizer textSymb = styleFactory.createTextSymbolizer();
        Expression offset = filterFactory.createAttributeExpression(testSchema, "testFloat");
        Expression label = filterFactory.createAttributeExpression(testSchema, "testByte");
        textSymb.setLabelPlacement(styleFactory.createLinePlacement(offset));
        textSymb.setLabel(label);
        textSymb.setFonts(new Font[] { font });

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].getRules()[0].setSymbolizers(new Symbolizer[] { textSymb });
        assertAttributeName(s,
            new String[] {
                "testString", "testString2", "testLong", "testBoolean", "testFloat", "testByte"
            });
    }

    public void testHalo() throws Exception {
        Fill fill = styleFactory.getDefaultFill();
        fill.setColor(filterFactory.createAttributeExpression(testSchema, "testString"));

        Expression radius = filterFactory.createAttributeExpression(testSchema, "testLong");
        Halo halo = styleFactory.createHalo(fill, radius);
        TextSymbolizer textSymb = styleFactory.createTextSymbolizer();
        textSymb.setHalo(halo);

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].getRules()[0].setSymbolizers(new Symbolizer[] { textSymb });
        assertAttributeName(s, new String[] { "testString", "testLong" });
    }

    public void testLinePlacement() throws Exception {
        LinePlacement linePlacement = styleFactory.createLinePlacement(filterFactory
                .createAttributeExpression(testSchema, "testLong"));
        TextSymbolizer textSymb = styleFactory.createTextSymbolizer();
        textSymb.setLabelPlacement(linePlacement);

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].getRules()[0].setSymbolizers(new Symbolizer[] { textSymb });
        assertAttributeName(s, new String[] { "testLong" });
    }

    public void testPointPlacement() throws Exception {
        PointPlacement pp = styleFactory.getDefaultPointPlacement();

        Expression x = filterFactory.createAttributeExpression(testSchema, "testLong");
        Expression y = filterFactory.createAttributeExpression(testSchema, "testInteger");
        AnchorPoint ap = styleFactory.createAnchorPoint(x, y);

        Expression dx = filterFactory.createAttributeExpression(testSchema, "testFloat");
        Expression dy = filterFactory.createAttributeExpression(testSchema, "testDouble");
        Displacement displacement = styleFactory.createDisplacement(dx, dy);

        pp.setAnchorPoint(ap);
        pp.setDisplacement(displacement);
        pp.setRotation(filterFactory.createAttributeExpression(testSchema, "testFloat"));

        TextSymbolizer textSymb = styleFactory.createTextSymbolizer();
        textSymb.setLabelPlacement(pp);

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].getRules()[0].setSymbolizers(new Symbolizer[] { textSymb });
        assertAttributeName(s, new String[] { "testLong", "testInteger", "testFloat", "testDouble" });
    }

    public void testPolygonSymbolizer() throws Exception {
        PolygonSymbolizer ps = styleFactory.createPolygonSymbolizer();
        Stroke stroke = styleFactory.getDefaultStroke();
        stroke.setColor(filterFactory.createAttributeExpression(testSchema, "testString"));

        Fill fill = styleFactory.getDefaultFill();
        fill.setOpacity(filterFactory.createAttributeExpression(testSchema, "testDouble"));
        ps.setStroke(stroke);
        ps.setFill(fill);

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].getRules()[0].setSymbolizers(new Symbolizer[] { ps });
        assertAttributeName(s, new String[] { "testString", "testDouble" });
    }

    public void testLineSymbolizer() throws IllegalFilterException {
        LineSymbolizer ls = styleFactory.createLineSymbolizer();
        Stroke stroke = styleFactory.getDefaultStroke();
        stroke.setColor(filterFactory.createAttributeExpression(testSchema, "testString"));
        ls.setStroke(stroke);

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].getRules()[0].setSymbolizers(new Symbolizer[] { ls });
        assertAttributeName(s, new String[] { "testString" });
    }

    public void testFill() throws IllegalFilterException {
        Fill fill = styleFactory.getDefaultFill();
        fill.setBackgroundColor(filterFactory.createAttributeExpression(testSchema, "testString"));
        fill.setColor(filterFactory.createAttributeExpression(testSchema, "testString2"));

        Mark mark = styleFactory.createMark();
        Expression le = filterFactory.createLiteralExpression(1);
        Expression rot = filterFactory.createAttributeExpression(testSchema, "testFloat");
        Graphic graphic = styleFactory.createGraphic(null, new Mark[] { mark }, null, le, le, rot);
        fill.setGraphicFill(graphic);

        PolygonSymbolizer ps = styleFactory.getDefaultPolygonSymbolizer();
        ps.setFill(fill);

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].getRules()[0].setSymbolizers(new Symbolizer[] { ps });
        assertAttributeName(s, new String[] { "testString", "testString2", "testFloat" });
    }

    public void testStroke() throws IllegalFilterException {
        Stroke stroke = styleFactory.getDefaultStroke();
        stroke.setColor(filterFactory.createAttributeExpression(testSchema, "testString2"));
        stroke.setDashOffset(filterFactory.createAttributeExpression(testSchema, "testString"));

        Mark mark = styleFactory.createMark();
        Expression le = filterFactory.createLiteralExpression(1);
        Expression rot = filterFactory.createAttributeExpression(testSchema, "testFloat");
        Graphic graphic = styleFactory.createGraphic(null, new Mark[] { mark }, null, le, le, rot);
        stroke.setGraphicFill(graphic);

        LineSymbolizer ls = styleFactory.getDefaultLineSymbolizer();
        ls.setStroke(stroke);

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].getRules()[0].setSymbolizers(new Symbolizer[] { ls });
        assertAttributeName(s, new String[] { "testString", "testString2", "testFloat" });
    }

    /**
     * Main for test runner.
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Required suite builder.
     *
     * @return A test suite for this unit test.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(StyleAttributeExtractorTest.class);

        return suite;
    }
}
