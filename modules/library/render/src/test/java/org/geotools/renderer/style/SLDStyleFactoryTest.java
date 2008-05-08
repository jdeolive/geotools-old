/*
 * SLDStyleFactoryTest.java
 * JUnit based test
 *
 * Created on September 29, 2005, 3:10 PM
 */

package org.geotools.renderer.style;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.filter.FilterFactoryFinder;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryFinder;
import org.geotools.util.NumberRange;

/**
 *
 * @author jamesm
 * @source $URL$
 */
public class SLDStyleFactoryTest extends TestCase {
    
    public SLDStyleFactoryTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static class SymbolizerKeyTest extends TestCase {
        
        public SymbolizerKeyTest(java.lang.String testName) {
            
            super(testName);
        }
        
        protected void setUp() throws Exception {
        }
        
        protected void tearDown() throws Exception {
        }
        
        public static Test suite() {
            TestSuite suite = new TestSuite(SymbolizerKeyTest.class);
            
            return suite;
        }
        
        /**
         * Test of equals method, of class org.geotools.renderer.style.SLDStyleFactory.SymbolizerKey.
         */
        public void testEquals() {
            
            // TODO add your test code below by replacing the default call to fail.
        }
        
        /**
         * Test of hashCode method, of class org.geotools.renderer.style.SLDStyleFactory.SymbolizerKey.
         */
        public void testHashCode() {
            
            // TODO add your test code below by replacing the default call to fail.
        }
    }
    
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SLDStyleFactoryTest.class);
        
        return suite;
    }
    
    /**
     * Test of getHitRatio method, of class org.geotools.renderer.style.SLDStyleFactory.
     */
    public void testGetHitRatio() {
        
        // TODO add your test code below by replacing the default call to fail.
    }
    
    /**
     * Test of getHits method, of class org.geotools.renderer.style.SLDStyleFactory.
     */
    public void testGetHits() {
        
        // TODO add your test code below by replacing the default call to fail.
    }
    
    /**
     * Test of getRequests method, of class org.geotools.renderer.style.SLDStyleFactory.
     */
    public void testGetRequests() {
        
        // TODO add your test code below by replacing the default call to fail.
    }
    
    /**
     * Test of createStyle method, of class org.geotools.renderer.style.SLDStyleFactory.
     */
    public void testCreateStyle() {
        
        // TODO add your test code below by replacing the default call to fail.
    }
    
    /**
     * Test of createDynamicStyle method, of class org.geotools.renderer.style.SLDStyleFactory.
     */
    public void testCreateDynamicStyle() {
        
        // TODO add your test code below by replacing the default call to fail.
    }
    
    /**
     * Test of createPolygonStyle method, of class org.geotools.renderer.style.SLDStyleFactory.
     */
    public void testCreateIncompletePolygonStyle() {
        SLDStyleFactory sFac = new SLDStyleFactory();
        NumberRange range = new NumberRange(1,1);

        
        PolygonSymbolizer symb;
    
        //full symbolizer
        symb = StyleFactoryFinder.createStyleFactory().createPolygonSymbolizer();
        //symb.setFill(fac.createFill(FilterFactoryFinder.createFilterFactory().createLiteralExpression("#ffff00")));
        
        sFac.createPolygonStyle(null, symb,range);
    }
    
    /**
     * Test of createDynamicPolygonStyle method, of class org.geotools.renderer.style.SLDStyleFactory.
     */
    public void testCreateDynamicPolygonStyle() {
        
        // TODO add your test code below by replacing the default call to fail.
    }
    
    /**
     * Test of createLineStyle method, of class org.geotools.renderer.style.SLDStyleFactory.
     */
    public void testCreateLineStyle() {
        
        // TODO add your test code below by replacing the default call to fail.
    }
    
    /**
     * Test of createDynamicLineStyle method, of class org.geotools.renderer.style.SLDStyleFactory.
     */
    public void testCreateDynamicLineStyle() {
        
        // TODO add your test code below by replacing the default call to fail.
    }
    
    /**
     * Test of createPointStyle method, of class org.geotools.renderer.style.SLDStyleFactory.
     */
    public void testCreateCompletePointStyle() {
        SLDStyleFactory sFac = new SLDStyleFactory();
        NumberRange range = new NumberRange(1,1);
        StyleFactory fac = StyleFactoryFinder.createStyleFactory();
        
        PointSymbolizer symb;
        Mark myMark;
        //full symbolizer
        symb = StyleFactoryFinder.createStyleFactory().createPointSymbolizer();
        myMark = fac.createMark();
        myMark.setFill(fac.createFill(FilterFactoryFinder.createFilterFactory().createLiteralExpression("#ffff00")));
        symb.getGraphic().setSize(FilterFactoryFinder.createFilterFactory().createLiteralExpression(10));
        symb.getGraphic().addMark(myMark);
        symb.getGraphic().setOpacity(FilterFactoryFinder.createFilterFactory().createLiteralExpression(1));
        symb.getGraphic().setRotation(FilterFactoryFinder.createFilterFactory().createLiteralExpression(0));
        sFac.createPointStyle(null, symb,range);
        
    }
    
    public void testCreateIncompletePointStyle() {
        SLDStyleFactory sFac = new SLDStyleFactory();
        NumberRange range = new NumberRange(1,1);
        StyleFactory fac = StyleFactoryFinder.createStyleFactory();
        
        PointSymbolizer symb;
        Mark myMark;
        //full symbolizer
        symb = StyleFactoryFinder.createStyleFactory().createPointSymbolizer();
        myMark = fac.createMark();
        
        symb.getGraphic().addMark(myMark);
        
        sFac.createPointStyle(null, symb,range);
        
    }
    
  
    
 
    
    /**
     * Test of createTextStyle method, of class org.geotools.renderer.style.SLDStyleFactory.
     */
    public void testCreateTextStyle() {
        
        // TODO add your test code below by replacing the default call to fail.
    }
    
    /**
     * Test of setScaleRange method, of class org.geotools.renderer.style.SLDStyleFactory.
     */
    public void testSetScaleRange() {
        
        // TODO add your test code below by replacing the default call to fail.
    }
    
    /**
     * Test of getPaint method, of class org.geotools.renderer.style.SLDStyleFactory.
     */
    public void testGetPaint() {
        
        // TODO add your test code below by replacing the default call to fail.
    }
    
    /**
     * Test of getComposite method, of class org.geotools.renderer.style.SLDStyleFactory.
     */
    public void testGetComposite() {
        
        // TODO add your test code below by replacing the default call to fail.
    }
    
    /**
     * Test of getTexturePaint method, of class org.geotools.renderer.style.SLDStyleFactory.
     */
    public void testGetTexturePaint() {
        
        // TODO add your test code below by replacing the default call to fail.
    }
    
    /**
     * Test of lookUpJoin method, of class org.geotools.renderer.style.SLDStyleFactory.
     */
    public void testLookUpJoin() {
        
        // TODO add your test code below by replacing the default call to fail.
    }
    
    /**
     * Test of lookUpCap method, of class org.geotools.renderer.style.SLDStyleFactory.
     */
    public void testLookUpCap() {
        
        // TODO add your test code below by replacing the default call to fail.
    }
    
    /**
     * Test of getMapScaleDenominator method, of class org.geotools.renderer.style.SLDStyleFactory.
     */
    public void testGetMapScaleDenominator() {
        
        // TODO add your test code below by replacing the default call to fail.
    }
    
    /**
     * Test of setMapScaleDenominator method, of class org.geotools.renderer.style.SLDStyleFactory.
     */
    public void testSetMapScaleDenominator() {
        
        // TODO add your test code below by replacing the default call to fail.
    }
    
}
