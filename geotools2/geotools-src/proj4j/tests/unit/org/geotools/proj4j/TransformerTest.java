/*
 * TransformerTest.java
 * JUnit based test
 *
 * Created on February 28, 2002, 1:40 PM
 */                

package org.geotools.proj4j;

import junit.framework.*;

/**
 *
 * @author jamesm
 */                                
public class TransformerTest extends TestCase {
    
    public TransformerTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(TransformerTest.class);
        return suite;
    }

    /** Test of transform method, of class org.geotools.proj4j.Transformer. */
    public void testTransform() throws ProjectionException{
        System.out.println("testTransform");
        
        Projection from = ProjectionFactory.createProjection(new String[]{"proj=utm"});
        Projection to = ProjectionFactory.createProjection(new String[]{"proj=aea"});
        
        Transformer cs2cs = new Transformer();
        double x[]={10,20,30,40};
        double y[]={10,20,30,40};
        double z[]={0,0,0,0};
        double a[]={-967298.27,-967286.05,-967273.82,-967261.60};
        double b[]={38140.07,38147.71,38155.36,38163.01};
        double c[]={0,0,0,0};
        cs2cs.transform(from,to,x.length,0,x,y,z);
        for(int i=0;i<x.length;i++){
             System.out.println("answer is "+x[i]+" "+y[i]);
            assertEquals(a[i],x[i],0.01);
            assertEquals(b[i],y[i],0.01);
            assertEquals(c[i],z[i],0.01);

        }
        //reverse and see if we are where we started
        cs2cs.transform(to,from,x.length,0,x,y,z);
        for(int i=0;i<x.length;i++){
           assertEquals(10*(i+1),x[i],0.01);
           assertEquals(10*(i+1),y[i],0.01);
           assertEquals(0,z[i],0.01);
        }
    }
    
   
    // Add test methods here, they have to start with 'test' name.
    // for example: 
    // public void testHello() {}


}
