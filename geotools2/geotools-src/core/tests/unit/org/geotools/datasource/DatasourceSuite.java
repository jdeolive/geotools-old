/*
 * DatasourceSuite.java
 * NetBeans JUnit based test
 *
 * Created on 20 February 2002, 15:51
 */                

package org.geotools.datasource;
 
import junit.framework.*;
         
/**
 *
 * @author ian
 */
public class DatasourceSuite extends TestCase {

    public DatasourceSuite(java.lang.String testName) {
        super(testName);
    }        
        
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        //--JUNIT:
        //This block was automatically generated and can be regenerated again.
        //Do NOT change lines enclosed by the --JUNIT: and :JUNIT-- tags.
        
        TestSuite suite = new TestSuite("DatasourceSuite");
        suite.addTest(org.geotools.datasource.SimpleIndexTest.suite());
        suite.addTest(org.geotools.datasource.FeatureComparatorTest.suite());
        suite.addTest(org.geotools.datasource.TableChangedEventTest.suite());
        suite.addTest(org.geotools.datasource.FeatureTableTest.suite());
        //:JUNIT--
        //This value MUST ALWAYS be returned from this function.
        return suite;
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example: 
    // public void testHello() {}



}
