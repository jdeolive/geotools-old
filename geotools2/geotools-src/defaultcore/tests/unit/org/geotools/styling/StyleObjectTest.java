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

import junit.framework.TestCase;

import org.geotools.filter.FilterFactory;


/** Tests style cloning
 *
 * @author Sean Geoghegan
 */
public class StyleObjectTest extends TestCase {
    private StyleFactory styleFactory;
    private FilterFactory filterFactory;

    /**
     * Constructor for StyleCloneTest.
     *
     * @param arg0
     */
    public StyleObjectTest(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        styleFactory = StyleFactory.createStyleFactory();
        filterFactory = FilterFactory.createFilterFactory();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        styleFactory = null;
    }

    public void testStyle() throws Exception {
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle();
        fts.setFeatureTypeName("feature-type-1");
        FeatureTypeStyle fts2 = styleFactory.createFeatureTypeStyle();
        Rule rule = styleFactory.createRule();
        fts2.addRule(rule);
        fts2.setFeatureTypeName("feature-type-2");
        
        Style style = styleFactory.getDefaultStyle();
        style.addFeatureTypeStyle(fts);
        style.addFeatureTypeStyle(fts2);
        Style clone = (Style) style.clone();
        assertClone(style,clone);
        
        Style notEq = styleFactory.getDefaultStyle();
        notEq.addFeatureTypeStyle(fts2);
        assertEqualsContract(clone,notEq,style);
    }

    public void testFeatureTypeStyle() throws Exception {
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle();
        fts.setFeatureTypeName("feature-type");
        Rule rule1 = styleFactory.createRule();
        rule1.setName("rule1");
        rule1.setFilter(filterFactory.createFidFilter("FID"));
        Rule rule2 = styleFactory.createRule();
        rule2.setIsElseFilter(true);
        rule2.setName("rule2");
        fts.addRule(rule1);
        fts.addRule(rule2);
        
        FeatureTypeStyle clone = (FeatureTypeStyle) fts.clone();
        assertClone(fts,clone);
        
        FeatureTypeStyle notEq = styleFactory.createFeatureTypeStyle();
        notEq.setName("fts-not-equal");
        notEq.addRule(rule1);
        assertEqualsContract(clone,notEq,fts);
    }

    public void testRule() throws Exception {
        Symbolizer symb1 = styleFactory.createLineSymbolizer(
                                styleFactory.getDefaultStroke(), 
                                "geometry");
        Symbolizer symb2 = styleFactory.createPolygonSymbolizer(
                                styleFactory.getDefaultStroke(),
                                styleFactory.getDefaultFill(), 
                                "shape");
        Rule rule = styleFactory.createRule();
        rule.setSymbolizers(new Symbolizer[]{ symb1,symb2});        
        Rule clone = (Rule) rule.clone();
        assertClone(rule,clone);
        
        Rule notEq = styleFactory.createRule();
        notEq.setSymbolizers(new Symbolizer[]{symb2});
        assertEqualsContract(clone,notEq,rule);
    }
    
    public void testPointSymbolizer() throws Exception {
        PointSymbolizer pointSymb = styleFactory.createPointSymbolizer();
        PointSymbolizer clone = (PointSymbolizer) pointSymb.clone();
        assertClone(pointSymb,clone);
        
        PointSymbolizer notEq = styleFactory.getDefaultPointSymbolizer();
        notEq.setGeometryPropertyName("something_else");
        assertEqualsContract(clone, notEq, pointSymb);
    }
    
    public void testTextSymbolizer() {
//        TextSymbolizer textSymb = styleFactory.createTextSymbolizer();
//        TextSymbolizer clone = (TextSymbolizer) textSymb.clone();
//        assertClone(textSymb,clone);
//        
//        TextSymbolizer notEq = styleFactory.getDefaultTextSymbolizer();
//        notEq.setGeometryPropertyName("something_else");
//        assertEqualsContract(clone, notEq, textSymb);
    }
    
    public void testPolygonSymbolizer() {
        PolygonSymbolizer polygonSymb = styleFactory.createPolygonSymbolizer();
        PolygonSymbolizer clone = (PolygonSymbolizer) polygonSymb.clone();
        assertClone(polygonSymb,clone);
        
        PolygonSymbolizer notEq = styleFactory.getDefaultPolygonSymbolizer();
        notEq.setGeometryPropertyName("something_else");
        assertEqualsContract(clone, notEq, polygonSymb);
    }
    
    public void testLineSymbolizer() {
        LineSymbolizer lineSymb = styleFactory.createLineSymbolizer();
        LineSymbolizer clone = (LineSymbolizer) lineSymb.clone();
        assertClone(lineSymb,clone);
        
        LineSymbolizer notEq = styleFactory.getDefaultLineSymbolizer();
        notEq.setGeometryPropertyName("something_else");
        assertEqualsContract(clone, notEq, lineSymb);
    }
    
    public void testGraphic() {
        Graphic graphic = styleFactory.getDefaultGraphic();
        Graphic clone = (Graphic) graphic.clone();
        assertClone(graphic,clone);
        
        Graphic notEq = styleFactory.getDefaultGraphic();
        notEq.setGeometryPropertyName("geomprop");
        assertEqualsContract(clone, notEq, graphic);
    }
    
    public void testExternalGraphic() {
        ExternalGraphic exGraphic = styleFactory.createExternalGraphic("http://somewhere", "image/png");
        ExternalGraphic clone = (ExternalGraphic) exGraphic.clone();
        assertClone(exGraphic,clone);
        
        ExternalGraphic notEq = styleFactory.createExternalGraphic("http://somewhereelse", "image/jpeg");
        assertEqualsContract(clone, notEq, exGraphic);
        
        // make sure it works for different format, same url
        ExternalGraphic notEq2 = (ExternalGraphic) clone.clone();
        notEq2.setFormat("image/jpeg");
        assertEqualsContract(clone,notEq2,exGraphic);
    }
    
    public void testMark() {
        Mark mark = styleFactory.getCircleMark();
        Mark clone = (Mark) mark.clone();
        assertClone(mark,clone);
        
        Mark notEq = styleFactory.getStarMark();
        assertEqualsContract(clone, notEq, mark);
    }
    
    public void testFill() {
        Fill fill = styleFactory.getDefaultFill();
        Fill clone = (Fill) fill.clone();
        assertClone(fill,clone);
        
        Fill notEq = styleFactory.createFill(filterFactory.createLiteralExpression("#FF0000"));
        assertEqualsContract(clone,notEq,fill);
    }
    
    public void testStroke() {
        Stroke stroke = styleFactory.getDefaultStroke();
        Stroke clone = (Stroke) stroke.clone();
        assertClone(stroke,clone);
    
        Stroke notEq = styleFactory.createStroke(
                            filterFactory.createLiteralExpression("#FF0000"),
                            filterFactory.createLiteralExpression(10));
        assertEqualsContract(clone,notEq,stroke);
        
        // a stroke is a complex object with lots of properties,
        // need more extensive tests here.
    }
        
    private static void assertClone(Object real,Object clone) {
        assertNotNull("Real was null",real);
        assertNotNull("Clone was null",clone);
        assertTrue("" + real.getClass().getName() + " was not cloned",
                    real != clone);
    }
    
    private static void assertEqualsContract(Object controlEqual,Object controlNe,Object test){
        assertNotNull(controlEqual);
        assertNotNull(controlNe);
        assertNotNull(test);
        
        // check reflexivity
        assertTrue("Reflexivity test failed",test.equals(test));
        
        // check symmetric
        assertTrue("Symmetry test failed",controlEqual.equals(test));
        assertTrue("Symmetry test failed",test.equals(controlEqual));
        assertTrue("Symmetry test failed",!test.equals(controlNe));
        assertTrue("Symmetry test failed",!controlNe.equals(test));
        
        // check transitivity
        assertTrue("Transitivity test failed",!controlEqual.equals(controlNe));
        assertTrue("Transitivity test failed",!test.equals(controlNe));
        assertTrue("Transitivity test failed",!controlNe.equals(controlEqual));
        assertTrue("Transitivity test failed",!controlNe.equals(test));
        
        // check non-null
        assertTrue("Non-null test failed",!test.equals(null));
        
        // assertHashcode equality
        int controlEqHash = controlEqual.hashCode();
        int testHash = test.hashCode();
        assertTrue("Equal objects should return equal hashcodes",
                controlEqHash == testHash);
    }
}
