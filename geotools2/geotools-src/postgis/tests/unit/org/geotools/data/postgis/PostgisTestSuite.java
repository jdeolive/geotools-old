package org.geotools.data.postgis;

import java.util.*;
import junit.framework.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import com.vividsolutions.jts.geom.*;
import org.geotools.data.*;
import org.geotools.feature.*;
import org.geotools.datasource.extents.*;

public class PostgisTestSuite extends TestCase {

    /** Standard logging instance */
    private static Logger _log = Logger.getLogger(PostgisTestSuite.class);

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

    /*    
    public void setUp() {
        _log.info("creating postgis connection...");
        PostgisConnection db = new PostgisConnection ("feathers.leeds.ac.uk","5432","postgis_test"); 
        _log.info("created new db connection");
        db.setLogin("postgis_ro","postgis_ro");
        _log.info("set the login");
        postgis = new DataSourcePostgis(db, "testset");
        _log.info("created new datasource");
    }


    public void testImport() {
        _log.info("starting type enforcement tests...");
        try {
            postgis.importFeatures(collection, new EnvelopeExtent());
        }
        catch(DataSourceException e) {
            _log.info("...threw data source exception");      
            this.fail("...threw data source exception");  
        }
        assertEquals(18, collection.getFeatures().length);
        _log.info("...ending type enforcement tests");
    }
    */

}
