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
        System.out.println("testCreateProjection");
        try{
            Projection p = ProjectionFactory.createProjection(new String[]{"proj=tmerc"});
        }catch(ProjectionException e){
            fail(e.toString());
        }
        
        //load non existent projection
        try{
            Projection p = ProjectionFactory.createProjection(new String[]{"proj=foobar","a=6378206.4","es=.006768658"});
            fail("Projection foobar does not exist and should not have been constructed");
        }catch(ProjectionException e){
        }
           
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ProjectionFactoryTest.class);
        
        return suite;
    }
    


}
