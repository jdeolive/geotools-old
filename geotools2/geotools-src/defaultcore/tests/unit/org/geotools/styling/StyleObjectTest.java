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

import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.util.Cloneable;


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
        Style clone = (Style) ((Cloneable)style).clone();
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
        
        FeatureTypeStyle clone = (FeatureTypeStyle) ((Cloneable)fts).clone();
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
        Rule clone = (Rule) ((Cloneable)rule).clone();
        assertClone(rule,clone);
        
        
        Rule notEq = styleFactory.createRule();
        notEq.setSymbolizers(new Symbolizer[]{symb2});
        assertEqualsContract(clone,notEq,rule);
        
        clone.setSymbolizers(new Symbolizer[]{symb1});
        assertTrue(!rule.equals(clone));   
    }
    
    public void testPointSymbolizer() throws Exception {
        PointSymbolizer pointSymb = styleFactory.createPointSymbolizer();
        PointSymbolizer clone = (PointSymbolizer) ((Cloneable)pointSymb).clone();
        assertClone(pointSymb,clone);
        
        PointSymbolizer notEq = styleFactory.getDefaultPointSymbolizer();
        notEq.setGeometryPropertyName("something_else");
        assertEqualsContract(clone, notEq, pointSymb);
    }
    
    public void testTextSymbolizer() {
        TextSymbolizer textSymb = styleFactory.createTextSymbolizer();
        Expression offset = filterFactory.createLiteralExpression(10);
        textSymb.setLabelPlacement(styleFactory.createLinePlacement(offset));
        TextSymbolizer clone = (TextSymbolizer) ((Cloneable)textSymb).clone();
        assertClone(textSymb,clone);
        
        TextSymbolizer notEq = styleFactory.getDefaultTextSymbolizer();
        Expression ancX = filterFactory.createLiteralExpression(10);
        Expression ancY = filterFactory.createLiteralExpression(10);
        AnchorPoint ancPoint = styleFactory.createAnchorPoint(ancX, ancY);
        LabelPlacement placement = styleFactory.createPointPlacement(ancPoint, null, null);
        notEq.setLabelPlacement(placement);
        assertEqualsContract(clone, notEq, textSymb);
    }
    
    public void testFont() {
        Font font = styleFactory.getDefaultFont();
        Font clone = (Font) ((Cloneable)font).clone();
        assertClone(font,clone);
        
        Font other = styleFactory.createFont(filterFactory.createLiteralExpression("other"), 
                        filterFactory.createLiteralExpression("normal"), 
                        filterFactory.createLiteralExpression("BOLD"), 
                        filterFactory.createLiteralExpression(12));
        
        assertEqualsContract(clone, other, font);
    }
    
    public void testHalo() {
        Halo halo = styleFactory.createHalo(styleFactory.getDefaultFill(), 
                        filterFactory.createLiteralExpression(10));
        Halo clone = (Halo) ((Cloneable) halo).clone();
        assertClone(halo,clone);
        
        Halo other = styleFactory.createHalo(styleFactory.getDefaultFill(),
                        filterFactory.createLiteralExpression(12));
        assertEqualsContract(clone,other,halo);       
    }
    
    public void testLinePlacement() throws Exception {
        LinePlacement linePlacement = styleFactory.createLinePlacement(filterFactory.createLiteralExpression(12));
        LinePlacement clone = (LinePlacement) ((Cloneable) linePlacement).clone();
        assertClone(linePlacement,clone);
        
        LinePlacement other = styleFactory.createLinePlacement(filterFactory.createAttributeExpression(null, "NAME"));
        assertEqualsContract(clone,other,linePlacement);    
    }
    
    public void testAnchorPoint() {
        AnchorPoint anchorPoint = styleFactory.createAnchorPoint(filterFactory.createLiteralExpression(1),
                                    filterFactory.createLiteralExpression(2));
        AnchorPoint clone = (AnchorPoint) ((Cloneable) anchorPoint).clone();
        assertClone(anchorPoint,clone);
        
        AnchorPoint other = styleFactory.createAnchorPoint(filterFactory.createLiteralExpression(3),
                                    filterFactory.createLiteralExpression(4));
        assertEqualsContract(clone, other, anchorPoint);
    }
    
    public void testDisplacement() {
        Displacement displacement = styleFactory.createDisplacement(filterFactory.createLiteralExpression(1),
                                    filterFactory.createLiteralExpression(2));
        Displacement clone = (Displacement) ((Cloneable) displacement).clone();
        assertClone(displacement,clone);
    
        Displacement other = styleFactory.createDisplacement(filterFactory.createLiteralExpression(3),
                                    filterFactory.createLiteralExpression(4));
        assertEqualsContract(clone, other, displacement);
    }
    
    public void testPointPlacement() {
        PointPlacement pointPl = styleFactory.getDefaultPointPlacement();
        PointPlacement clone = (PointPlacement) ((Cloneable) pointPl).clone();
        assertClone(pointPl,clone);
        
        PointPlacement other = (PointPlacement) ((Cloneable) pointPl).clone();
        other.setRotation(filterFactory.createLiteralExpression(274.0));
        assertEqualsContract(clone, other, pointPl);
    }
    
    public void testPolygonSymbolizer() {
        PolygonSymbolizer polygonSymb = styleFactory.createPolygonSymbolizer();
        PolygonSymbolizer clone = (PolygonSymbolizer) ((Cloneable)polygonSymb).clone();
        assertClone(polygonSymb,clone);
        
        PolygonSymbolizer notEq = styleFactory.getDefaultPolygonSymbolizer();
        notEq.setGeometryPropertyName("something_else");
        assertEqualsContract(clone, notEq, polygonSymb);
    }
    
    public void testLineSymbolizer() {
        LineSymbolizer lineSymb = styleFactory.createLineSymbolizer();
        LineSymbolizer clone = (LineSymbolizer) ((Cloneable)lineSymb).clone();
        assertClone(lineSymb,clone);
        
        LineSymbolizer notEq = styleFactory.getDefaultLineSymbolizer();
        notEq.setGeometryPropertyName("something_else");
        assertEqualsContract(clone, notEq, lineSymb);
    }
    
    public void testGraphic() {
        Graphic graphic = styleFactory.getDefaultGraphic();
        graphic.addMark(styleFactory.getDefaultMark());
        Graphic clone = (Graphic) ((Cloneable)graphic).clone();
        assertClone(graphic,clone);
        assertEqualsContract(clone,graphic);
        assertEquals(clone.getSymbols().length,graphic.getSymbols().length);
        
        Graphic notEq = styleFactory.getDefaultGraphic();
        notEq.setGeometryPropertyName("geomprop");
        assertEqualsContract(clone, notEq, graphic);
    }
    
    public void testExternalGraphic() {
        ExternalGraphic exGraphic = styleFactory.createExternalGraphic("http://somewhere", "image/png");
        ExternalGraphic clone = (ExternalGraphic) ((Cloneable)exGraphic).clone();
        assertClone(exGraphic,clone);
        
        ExternalGraphic notEq = styleFactory.createExternalGraphic("http://somewhereelse", "image/jpeg");
        assertEqualsContract(clone, notEq, exGraphic);
        
        // make sure it works for different format, same url
        ExternalGraphic notEq2 = (ExternalGraphic) ((Cloneable)clone).clone();
        notEq2.setFormat("image/jpeg");
        assertEqualsContract(clone,notEq2,exGraphic);
    }
    
    public void testMark() {
        Mark mark = styleFactory.getCircleMark();
        Mark clone = (Mark) ((Cloneable)mark).clone();
        assertClone(mark,clone);
        
        Mark notEq = styleFactory.getStarMark();
        assertEqualsContract(clone, notEq, mark);
    }
    
    public void testFill() {
        Fill fill = styleFactory.getDefaultFill();
        Fill clone = (Fill) ((Cloneable)fill).clone();
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
        Stroke dashArray = styleFactory.getDefaultStroke();
        dashArray.setDashArray(new float[]{1.0f,2.0f,3.0f});
        Stroke dashArray2 = (Stroke) dashArray.clone();
        assertEqualsContract(dashArray,dashArray2);
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
    
    private static void assertEqualsContract(Object controlEqual,Object test){
        assertNotNull(controlEqual);
        assertNotNull(test);
    
        // check reflexivity
        assertTrue("Reflexivity test failed",test.equals(test));
    
        // check symmetric
        assertTrue("Symmetry test failed",controlEqual.equals(test));
        assertTrue("Symmetry test failed",test.equals(controlEqual));
        
        // check non-null
        assertTrue("Non-null test failed",!test.equals(null));
    
        // assertHashcode equality
        int controlEqHash = controlEqual.hashCode();
        int testHash = test.hashCode();
        assertTrue("Equal objects should return equal hashcodes",
                controlEqHash == testHash);
    }
}
