/*
 * StyleFactoryImplTest.java
 * JUnit based test
 *
 * Created on 11 November 2002, 11:55
 */

package org.geotools.styling;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import junit.framework.*;
import org.geotools.feature.Feature;
import org.geotools.filter.*;
import org.geotools.filter.FilterFactory;

/**
 *
 * @author iant
 */
public class StyleFactoryImplTest extends TestCase {
    static StyleFactory styleFactory;
    static FilterFactory filterFactory = FilterFactory.createFilterFactory();
    
    static Feature feature;
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
    
    /** Test of createStyle method, of class org.geotools.styling.StyleFactoryImpl. */
    public void testCreateStyle() {
        System.out.println("testCreateStyle");
        
        styleFactory = StyleFactory.createStyleFactory();
        
        assertNotNull("Failed to build styleFactory", styleFactory);
        
    }
    
    /** Test of createPointSymbolizer method, of class org.geotools.styling.StyleFactoryImpl. */
    public void testCreatePointSymbolizer() {
        System.out.println("testCreatePointSymbolizer");
        
        PointSymbolizer ps = styleFactory.createPointSymbolizer();
        
        assertNotNull("Failed to create PointSymbolizer", ps);
        
    }
    
    /** Test of createPolygonSymbolizer method, of class org.geotools.styling.StyleFactoryImpl. */
    public void testCreatePolygonSymbolizer() {
        System.out.println("testCreatePolygonSymbolizer");
        
        PolygonSymbolizer ps = styleFactory.createPolygonSymbolizer();
        
        assertNotNull("Failed to create PolygonSymbolizer", ps);
    }
    
    /** Test of createLineSymbolizer method, of class org.geotools.styling.StyleFactoryImpl. */
    public void testCreateLineSymbolizer() {
        System.out.println("testCreateLineSymbolizer");
        
        LineSymbolizer ls = styleFactory.createLineSymbolizer();
        
        assertNotNull("Failed to create PolygonSymbolizer", ls);
    }
    
    /** Test of createTextSymbolizer method, of class org.geotools.styling.StyleFactoryImpl. */
    public void testCreateTextSymbolizer() {
        System.out.println("testCreateTextSymbolizer");
        
        TextSymbolizer ts = styleFactory.createTextSymbolizer();
        
        assertNotNull("Failed to create TextSymbolizer", ts);
        
    }
    
    /** Test of createFeatureTypeStyle method, of class org.geotools.styling.StyleFactoryImpl. */
    public void testCreateFeatureTypeStyle() {
        System.out.println("testCreateFeatureTypeStyle");
        
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle();
        
        assertNotNull("failed to create featureTypeStyle", fts);
        
    }
    
    /** Test of createRule method, of class org.geotools.styling.StyleFactoryImpl. */
    public void testCreateRule() {
        System.out.println("testCreateRule");
        
        Rule r = styleFactory.createRule();
        
        assertNotNull("failed to create Rule",r);
        
    }
    
    /** Test of createStroke method, of class org.geotools.styling.StyleFactoryImpl. */
    public void testCreateStroke() {
        System.out.println("testCreateStroke");
        
        Stroke s = styleFactory.createStroke(filterFactory.createLiteralExpression("#000000"), 
            filterFactory.createLiteralExpression(2.0));
        
        assertNotNull("Failed to build stroke ",s);
        
        s = styleFactory.createStroke(filterFactory.createLiteralExpression("#000000"), 
            filterFactory.createLiteralExpression(2.0), 
            filterFactory.createLiteralExpression(0.5));
        
        assertNotNull("Failed to build stroke ",s);
        
        s = styleFactory.createStroke(filterFactory.createLiteralExpression("#000000"),
            filterFactory.createLiteralExpression(2.0),
            filterFactory.createLiteralExpression(0.5),
            filterFactory.createLiteralExpression("bevel"), 
            filterFactory.createLiteralExpression("square"),
            new float[]{1.1f,2.1f,6f,2.1f,1.1f,5f},
            filterFactory.createLiteralExpression(3), null,null);
            
        assertNotNull("Failed to build stroke ",s);
        
        
        assertEquals("Wrong color ", "#000000", s.getColor().getValue(feature).toString());
        assertEquals("Wrong width ", "2.0", s.getWidth().getValue(feature).toString());
        assertEquals("Wrong opacity ", "0.5", s.getOpacity().getValue(feature).toString());
        assertEquals("Wrong linejoin ", "bevel", s.getLineJoin().getValue(feature).toString());
        assertEquals("Wrong linejoin ", "square", s.getLineCap().getValue(feature).toString());
        assertEquals("Broken dash array",2.1f,s.getDashArray()[1],0.001f);
        assertEquals("Wrong dash offset ","3",s.getDashOffset().getValue(feature).toString());
        
        
        
    }
    
    /** Test of createFill method, of class org.geotools.styling.StyleFactoryImpl. */
    public void testCreateFill() {
        System.out.println("testCreateFill");
        
        Fill f = styleFactory.createFill(filterFactory.createLiteralExpression("#808080"));
        
        assertNotNull("Failed to build fill",f);
        
        f = styleFactory.createFill(filterFactory.createLiteralExpression("#808080"),
            filterFactory.createLiteralExpression(1.0));
        assertNotNull("Failed to build fill",f);
        try{
            f = styleFactory.createFill(null);
        } catch (IllegalArgumentException iae){
            try{
                f = styleFactory.createFill(filterFactory.createLiteralExpression("#808080"),null);
            } catch (IllegalArgumentException ae){
               return ;
            }
            fail("Failed to catch broken fill");
        }
        fail("Failed to catch broken color");
        
        
    }
    
    /** Test of createMark method, of class org.geotools.styling.StyleFactoryImpl. */
    public void testCreateMark() {
        System.out.println("testCreateMark");
        
        Mark m = styleFactory.createMark();
        
        assertNotNull("Failed to build mark ",m);
    }
    
    /** Test of getSquareMark method, of class org.geotools.styling.StyleFactoryImpl. */
    public void testGetNamedMarks() {
        System.out.println("testGetNamedMarks");
        
        Mark m;
        String[] names = {"Square", "Circle", "Triangle", "Star", "X", "Cross"};
        for(int i=0;i<names.length;i++){
            try{
                Class target = styleFactory.getClass();
//                System.out.println("About to load get"+names[i]+"Mark");
                Method method = target.getMethod("get"+names[i]+"Mark",null);
//                System.out.println("got method back " + method.toString());
                m = (Mark) method.invoke(styleFactory,null);
                assertNotNull("Failed to get " + names[i] + " mark ", m);
                Expression exp = filterFactory.createLiteralExpression(names[i]);
                assertEquals("Wrong sort of mark returned ", exp, m.getWellKnownName());
                assertEquals("Wrong size of mark returned ", "6", m.getSize().getValue(feature).toString());
            }catch (InvocationTargetException ite){
                ite.getTargetException().printStackTrace();
                fail("InvocationTargetException " + ite.getTargetException());
            
            } catch (Exception e ){
                e.printStackTrace();
                fail("Exception " + e.toString());
            }
        }
    }
    
    /** Test of createGraphic method, of class org.geotools.styling.StyleFactoryImpl. */
    public void testCreateGraphic() {
        System.out.println("testCreateGraphic");
        ExternalGraphic[] externalGraphics = new ExternalGraphic[]
            {styleFactory.createExternalGraphic("http://www.ccg.leeds.ac.uk/ian/geotools/icons/rail.gif", "image/gif")};
        Mark[] marks = new Mark[]{styleFactory.getCircleMark()};
        Mark[] symbols = new Mark[0];
        Expression opacity = filterFactory.createLiteralExpression(0.5);
        Expression size = filterFactory.createLiteralExpression(10);
        Expression rotation = filterFactory.createLiteralExpression(145.0);
        Graphic g = styleFactory.createGraphic(externalGraphics, marks, symbols, opacity, size, rotation);
        
        assertNotNull("failed to build graphic ", g);
    }
    
   
    
    /** Test of createFont method, of class org.geotools.styling.StyleFactoryImpl. */
    public void testCreateFont() {
        System.out.println("testCreateFont");
        Expression fontFamily = filterFactory.createLiteralExpression("Times");
        Expression fontStyle = filterFactory.createLiteralExpression("Italic");
        Expression fontWeight = filterFactory.createLiteralExpression("Bold");
        Expression fontSize = filterFactory.createLiteralExpression("12");
        Font f = styleFactory.createFont(fontFamily, fontStyle, fontWeight, fontSize);
        
        assertNotNull("Failed to build font", f);
        
        assertEquals("Wrong font type ", "Times" , f.getFontFamily().getValue(feature).toString());
        assertEquals("Wrong font Style ", "Italic" , f.getFontStyle().getValue(feature).toString());
        assertEquals("Wrong font weight ", "Bold" , f.getFontWeight().getValue(feature).toString());
        assertEquals("Wrong font size ", "12" , f.getFontSize().getValue(feature).toString());
    }
    
    /** Test of createLinePlacement method, of class org.geotools.styling.StyleFactoryImpl. */
    public void testCreateLinePlacement() {
        System.out.println("testCreateLinePlacement");
        
        LinePlacement lp = styleFactory.createLinePlacement(filterFactory.createLiteralExpression(10));
        
        assertNotNull("failed to create LinePlacement",lp);
        
    }
    
    /** Test of createPointPlacement method, of class org.geotools.styling.StyleFactoryImpl. */
    public void testCreatePointPlacement() {
        System.out.println("testCreatePointPlacement");
        AnchorPoint anchorPoint = styleFactory.createAnchorPoint(
            filterFactory.createLiteralExpression(1.0), filterFactory.createLiteralExpression(0.5));
        Displacement displacement = styleFactory.createDisplacement(
            filterFactory.createLiteralExpression(10.0), filterFactory.createLiteralExpression(5.0));
        Expression rotation = filterFactory.createLiteralExpression(90.0);
        PointPlacement pp = styleFactory.createPointPlacement(anchorPoint, displacement, rotation);
        
        assertNotNull("failed to create PointPlacement",pp);
        
        assertEquals("Wrong X anchorPoint ","1.0",pp.getAnchorPoint().getAnchorPointX().getValue(feature).toString());
        assertEquals("Wrong Y anchorPoint ","0.5",pp.getAnchorPoint().getAnchorPointY().getValue(feature).toString());
        assertEquals("Wrong X displacement ","10.0",pp.getDisplacement().getDisplacementX().getValue(feature).toString());
        assertEquals("Wrong Y displacement ","5.0",pp.getDisplacement().getDisplacementY().getValue(feature).toString());
        assertEquals("Wrong Rotation ","90.0",pp.getRotation().getValue(feature).toString());
    }
    
    
    /** Test of createHalo method, of class org.geotools.styling.StyleFactoryImpl. */
    public void testCreateHalo() {
        System.out.println("testCreateHalo");
        
        Halo h = styleFactory.createHalo(styleFactory.getDefaultFill(), filterFactory.createLiteralExpression(4));
        
        assertNotNull("Failed to build halo",h);
        
        assertEquals("Wrong radius", 4, ((Number)h.getRadius().getValue(feature)).intValue());
    }
//    
//    /** Test of getDefaultFill method, of class org.geotools.styling.StyleFactoryImpl. */
//    public void testGetDefaultFill() {
//        System.out.println("testGetDefaultFill");
//        
//    }
//    
//    /** Test of getDefaultLineSymbolizer method, of class org.geotools.styling.StyleFactoryImpl. */
//    public void testGetDefaultLineSymbolizer() {
//        System.out.println("testGetDefaultLineSymbolizer");
//        
//        
//    }
//    
//    /** Test of getDefaultMark method, of class org.geotools.styling.StyleFactoryImpl. */
//    public void testGetDefaultMark() {
//        System.out.println("testGetDefaultMark");
//        
//        
//    }
//    
//    /** Test of getDefaultPointSymbolizer method, of class org.geotools.styling.StyleFactoryImpl. */
//    public void testGetDefaultPointSymbolizer() {
//        System.out.println("testGetDefaultPointSymbolizer");
//        
//        
//    }
//    
//    /** Test of getDefaultPolygonSymbolizer method, of class org.geotools.styling.StyleFactoryImpl. */
//    public void testGetDefaultPolygonSymbolizer() {
//        System.out.println("testGetDefaultPolygonSymbolizer");
//        
//    }
//    
//    /** Test of getDefaultStroke method, of class org.geotools.styling.StyleFactoryImpl. */
//    public void testGetDefaultStroke() {
//        System.out.println("testGetDefaultStroke");
//        
//        
//    }
//    
//    /** Test of getDefaultStyle method, of class org.geotools.styling.StyleFactoryImpl. */
//    public void testGetDefaultStyle() {
//        System.out.println("testGetDefaultStyle");
//        
//        
//    }
//    
//    /** Test of getDefaultTextSymbolizer method, of class org.geotools.styling.StyleFactoryImpl. */
//    public void testGetDefaultTextSymbolizer() {
//        System.out.println("testGetDefaultTextSymbolizer");
//        
//        
//    }
//    
//    /** Test of getDefaultFont method, of class org.geotools.styling.StyleFactoryImpl. */
//    public void testGetDefaultFont() {
//        System.out.println("testGetDefaultFont");
//        
//        
//    }
//    
//    /** Test of getDefaultGraphic method, of class org.geotools.styling.StyleFactoryImpl. */
//    public void testGetDefaultGraphic() {
//        System.out.println("testGetDefaultGraphic");
//        
//        
//    }
//    
//    /** Test of createRasterSymbolizer method, of class org.geotools.styling.StyleFactoryImpl. */
//    public void testCreateRasterSymbolizer() {
//        System.out.println("testCreateRasterSymbolizer");
//        
//        
//    }

}
