package org.geotools.gml;

/*
 * GmlSuite.java
 * JUnit based test
 *
 * Created on 04 March 2002, 16:09
 */
import org.geotools.data.*;
import org.geotools.datasource.extents.*;
import org.geotools.feature.*;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import java.net.URL;
import java.util.*;


import junit.framework.*;

/**
 *
 * @author ian
 */
public class GmlSuite extends TestCase {
    

    
    public GmlSuite(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("All gmldatasource tests");
        suite.addTestSuite(GmlTest.class);
        return suite;
    }
    
  
    
}
