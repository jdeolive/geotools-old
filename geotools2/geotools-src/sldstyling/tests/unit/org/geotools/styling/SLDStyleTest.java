/*
 * SLDStyleTest.java
 * JUnit based test
 *
 * Created on November 6, 2003, 11:32 AM
 */

package org.geotools.styling;

import junit.framework.*;
import org.geotools.filter.Expression;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 *
 * @author jamesm
 */
public class SLDStyleTest extends TestCase {
    
    public SLDStyleTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SLDStyleTest.class);
        return suite;
    }
    //
    //    /** Test of setInput method, of class org.geotools.styling.SLDStyle. */
    //    public void testSetInput() {
    //        System.out.println("testSetInput");
    //
    //        // Add your test code below by replacing the default call to fail.
    //        fail("The test case is empty.");
    //    }
    //
    //    /** Test of readXML method, of class org.geotools.styling.SLDStyle. */
    //    public void testReadXML() {
    //        System.out.println("testReadXML");
    //
    //        // Add your test code below by replacing the default call to fail.
    //        fail("The test case is empty.");
    //    }
    //
    //    /** Test of readDOM method, of class org.geotools.styling.SLDStyle. */
    //    public void testReadDOM() {
    //        System.out.println("testReadDOM");
    //
    //        // Add your test code below by replacing the default call to fail.
    //        fail("The test case is empty.");
    //    }
    //
    //    /** Test of parseSLD method, of class org.geotools.styling.SLDStyle. */
    //    public void testParseSLD() {
    //        System.out.println("testParseSLD");
    //
    //        // Add your test code below by replacing the default call to fail.
    //        fail("The test case is empty.");
    //    }
    
    /** Test of parseStyle method, of class org.geotools.styling.SLDStyle. */
    public void testParseStyle() throws Exception{
        java.net.URL base = getClass().getResource("testData/");
        
        StyleFactory factory = StyleFactory.createStyleFactory();
        java.net.URL surl = new java.net.URL(base + "/test-sld.xml");
        SLDStyle stylereader = new SLDStyle(factory, surl);
        StyledLayerDescriptor sld = stylereader.parseSLD();
        assertEquals("My Layer", sld.getName());
        assertEquals("A layer by me", sld.getTitle());
        assertEquals("this is a sample layer", sld.getAbstract());
        assertEquals(1,sld.getStyledLayers().length);
        UserLayer layer = (UserLayer)sld.getStyledLayers()[0];
        assertNull(layer.getName());
        assertEquals(1,layer.getUserStyles().length);
        Style style = layer.getUserStyles()[0];
        assertEquals(1,style.getFeatureTypeStyles().length);
        assertEquals("My User Style", style.getName());
        assertEquals("A style by me", style.getTitle());
        assertEquals("this is a sample style", style.getAbstract());
        FeatureTypeStyle fts = style.getFeatureTypeStyles()[0];
        Rule rule = fts.getRules()[0];
        LineSymbolizer lineSym = (LineSymbolizer)rule.getSymbolizers()[0];
        assertEquals(4,((Number)lineSym.getStroke().getWidth().getValue(null)).intValue());
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
