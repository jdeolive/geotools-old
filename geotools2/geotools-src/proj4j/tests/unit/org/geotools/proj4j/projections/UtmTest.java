/*
 * UtmTest.java
 * JUnit based test
 *
 * Created on 23 February 2002, 02:30
 */                

package org.geotools.proj4j.projections;

import junit.framework.*;
import org.geotools.proj4j.*;

/**
 *
 * @author James Macgill
 */                                
public class UtmTest extends TestCase {
    
    public UtmTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(UtmTest.class);
        
        return suite;
    }
    
     public void testForwards() throws ProjectionException {
        System.out.println("Testing utm forwards");
        Projection p = ProjectionFactory.createProjection(new String[]{"proj=utm"});
        System.out.println("testing utm forward() call on created projection");
        LP lp = new LP("12d32'12\"S 45d24'1\"E");
        XY xy;
        xy = p.forward(lp);
        System.out.println("projected "+xy.x+" "+xy.y);

        assertEquals(-246326.15,xy.x,0.01);
        assertEquals(5071847.92,xy.y,0.01);
        
        //LP lp2 = p.inverse(xy);//do we get back what we put in?
        //assertEquals(lp.lam,lp2.lam,0.01);
        //assertEquals(lp.phi,lp2.phi,0.01);
    }


}
