/*
 * EqualClassesTest.java
 * JUnit based test
 *
 * Created on 08 December 2003, 10:21
 */

package org.geotools.algorithms.classification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import junit.framework.*;

/**
 *
 * @author iant
 */
public class EqualClassesTest extends TestCase {
    
    public EqualClassesTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(EqualClassesTest.class);
        return suite;
    }
    static boolean setup = false;
    static int nClasses = 4;
    static int nData = 100;
    static double[] data = new double[nData];
    static EqualClasses classifier;
    public static void setup(){
        if(setup) return;
        setup=true;
        for(int i=0;i<nData;i++){
            data[i]=i*10.0;
        }
        classifier = new EqualClasses(nClasses, data);
    }
        
    /** Test of getNumberClasses method, of class org.geotools.algorithms.classification.EqualClasses. */
    public void testGetNumberClasses() {
        setup();
        System.out.println("testGetNumberClasses");
        assertEquals("Wrong number of classes",nClasses,classifier.getNumberClasses());
        // Add your test code below by replacing the default call to fail.
        
    }
        
    /** Test of getBreaks method, of class org.geotools.algorithms.classification.EqualClasses. */
    public void testGetBreaks() {
        setup();
        System.out.println("testGetBreaks");
        
        // Add your test code below by replacing the default call to fail.
        double[] breaks = classifier.getBreaks();
        for(int i=0;i<breaks.length;i++){
            System.out.println("break "+i+" "+breaks[i]);
        }
        
    }
    
    
}
