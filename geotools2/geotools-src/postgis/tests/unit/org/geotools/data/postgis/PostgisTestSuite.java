package org.geotools.data.postgis;

import junit.framework.*;
import org.apache.log4j.Category;
import org.apache.log4j.BasicConfigurator;
import com.vividsolutions.jts.geom.*;
import java.util.*;
import org.geotools.data.*;
import org.geotools.feature.*;
import org.geotools.datasource.extents.*;

public class PostgisTestSuite extends TestCase {

    /** Standard logging instance */
    private static Category _log = Category.getInstance(PostgisTestSuite.class.getName());

    DataSource postgis = null;

    FeatureCollection collection = new FeatureCollectionDefault();

    public PostgisTestSuite(String testName){
        super(testName);
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("All postgis tests");
        suite.addTestSuite(PostgisTest.class);
        return suite;
    }
    

    
}
