/*
 * ProjectionFactoryTest.java
 * JUnit based test
 *
 * Created on 21 February 2002, 17:32
 */

package org.geotools.proj4j;

import junit.framework.*;

/**
 *
 * @author ian
 */
public class ProjectionFactoryTest extends TestCase {
    
    public ProjectionFactoryTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Test of createProjection method, of class org.geotools.proj4j.ProjectionFactory. */
    public void testCreateProjection() {
        Projection p=null;
        System.out.println("testCreateProjection");
        try{
            p = ProjectionFactory.createProjection(new String[]{"proj=tmerc"});
            System.out.println("testing forward() call on created projection");
            LP lp = new LP();
            XY xy;
            lp.lam=Misc.dmsToR("12d32'12\"S");
            lp.phi=Misc.dmsToR("45d24'1\"E");
            xy = p.forward(lp);
            System.out.println("projected "+xy.x+" "+xy.y);
            assertEquals(-981415.13,xy.x,0.01);
            assertEquals(5106494.08,xy.y,0.01);
        }catch(ProjectionException e){
            fail(e.toString());
        }
        
        
        
        //load non existent projection
        try{
            Projection fake = ProjectionFactory.createProjection(new String[]{"proj=foobar"});
            fail("Projection foobar does not exist and should not have been constructed");
        }catch(ProjectionException e){
        }
        
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ProjectionFactoryTest.class);
        
        return suite;
    }
    
    
    
}
