/*
 * AeqdTest.java
 * JUnit based test
 *
 * Created on March 1, 2002, 2:07 PM
 */                

package org.geotools.proj4j.projections;

import junit.framework.*;
import org.geotools.proj4j.*;

/**
 *
 * @author jamesm
 */                                
public class AeqdTest extends TestCase {
    
    public AeqdTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AeqdTest.class);
        return suite;
    }

    public void testForwards() throws ProjectionException {
        System.out.println("Testing aeqd forwards");
        Projection p = ProjectionFactory.createProjection(new String[]{"proj=aeqd"});
        System.out.println("testing aeqd forward() call on created projection");
        LP lp = new LP("10 10");
        XY xy;
        xy = p.forward(lp);
        System.out.println("projected aeqd "+xy.x+" "+xy.y);

        assertEquals(1101934.77,xy.x,0.01);
        assertEquals(1111443.32,xy.y,0.01);
        
        //LP lp2 = p.inverse(xy);//do we get back what we put in?
        //System.out.println("reversing aea projection");
        //assertEquals(lp.lam,lp2.lam,0.01);
        //assertEquals(lp.phi,lp2.phi,0.01);
        System.out.println("done aedq forward");
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example: 
    // public void testHello() {}


}
