/*
 * AiryTest.java
 * JUnit based test
 *
 * Created on 07 March 2002, 23:21
 */                

package org.geotools.proj4j.projections;

import junit.framework.*;
import org.geotools.proj4j.*;

/**
 *
 * @author Linda
 */                                
public class AiryTest extends TestCase {
    
    public AiryTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AiryTest.class);
        
        return suite;
    }
    
    public void testForwards() throws ProjectionException {
        System.out.println("Testing Airy forwards");
        Projection p = ProjectionFactory.createProjection(new String[]{"proj=airy","no_cut"});
        System.out.println("testing airy forward() call on created projection");
        LP lp = new LP("91 20");
        XY xy;
        xy = p.forward(lp);
        System.out.println("projected leac "+xy.x+" "+xy.y);

        assertEquals(8407202.02,xy.x,0.01);
        assertEquals(3060437.41,xy.y,0.01);
        System.out.println("Trying illegal values with no_cut switched off");
        try{
            p = ProjectionFactory.createProjection(new String[]{"proj=airy"});
            xy = p.forward(lp);
            fail("Error should have been thrown by projection");
        }
        catch(ProjectionException e){};
    }
    
    /** Test of getDescription method, of class org.geotools.proj4j.projections.Airy. */
    public void testGetDescription() throws Exception {
        System.out.println("testGetDescription");
        //can't test what should be returned as this will be local sepcific
        //but we can check that something is returned
        Projection p = ProjectionFactory.createProjection(new String[]{"proj=airy"});
        assertNotNull(p.getDescription());
    }
    
    
    
    // Add test methods here, they have to start with 'test' name.
    // for example: 
    // public void testHello() {}


}
