/*
 * TmercTest.java
 * JUnit based test
 *
 * Created on February 22, 2002, 4:24 PM
 */                

package org.geotools.proj4j.projections;

import junit.framework.*;
import org.geotools.proj4j.*;

/**
 *
 * @author jamesm
 */                                
public class TmercTest extends TestCase {
    
    public TmercTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(TmercTest.class);
        
        return suite;
    }
    
    /** Test of setParams method, of class org.geotools.proj4j.projections.Tmerc. */
    //public void testSetParams() {
    //    System.out.println("testSetParams");
   //}
    
    public void testForwards() throws ProjectionException {
        System.out.println("Testing tmerc forwards");
        Projection p = ProjectionFactory.createProjection(new String[]{"proj=tmerc"});
        System.out.println("testing tmerc forward() call on created projection");
        LP lp = new LP("12d32'12\"S 45d24'1\"E");
        XY xy;
        xy = p.forward(lp);
        System.out.println("projected "+xy.x+" "+xy.y);
        assertEquals(-981415.13,xy.x,0.01);
        assertEquals(5106494.08,xy.y,0.01);
        
        LP lp2 = p.inverse(xy);//do we get back what we put in?
        assertEquals(lp.lam,lp2.lam,0.01);
        assertEquals(lp.phi,lp2.phi,0.01);
    }
    
    
        
}
