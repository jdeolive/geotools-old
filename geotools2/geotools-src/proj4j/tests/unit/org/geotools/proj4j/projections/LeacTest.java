/*
 * AeaTest.java
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
public class LeacTest extends TestCase {
    
    public LeacTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AeaTest.class);
        
        return suite;
    }
    
    public void testForwards() throws ProjectionException {
        System.out.println("Testing Leac forwards");
        Projection p = ProjectionFactory.createProjection(new String[]{"proj=leac","lat_1=20","south"});
        System.out.println("testing leac forward() call on created projection");
        LP lp = new LP("12d32'12\"S 45d24'1\"E");
        XY xy;
        xy = p.forward(lp);
        System.out.println("projected leac "+xy.x+" "+xy.y);

        assertEquals(-1480560.36 ,xy.x,0.01);
        assertEquals(4772476.88,xy.y,0.01);
        
        //LP lp2 = p.inverse(xy);//do we get back what we put in?
        //System.out.println("reversing aea projection");
        //assertEquals(lp.lam,lp2.lam,0.01);
        //assertEquals(lp.phi,lp2.phi,0.01);
        System.out.println("done aea forward");
    }
    
    
    

}
