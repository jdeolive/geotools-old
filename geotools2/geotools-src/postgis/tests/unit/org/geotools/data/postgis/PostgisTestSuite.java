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
        BasicConfigurator.configure();
        _log.info("starting suite...");
        TestSuite suite = new TestSuite(PostgisTestSuite.class);
        _log.info("made suite...");
        return suite;
    }
    
    public void setUp() {
        _log.info("creating postgis connection...");
        PostgisConnection db = new PostgisConnection ("localhost","5432","test"); 
        _log.info("created new db connection");
        db.setLogin("test","test");
        _log.info("set the login");
        postgis = new DataSourcePostgis(db, "water");
        _log.info("created new datasource");
    }


    public void testImport() {
        _log.info("starting type enforcement tests...");
        try {
            postgis.importFeatures(collection, new EnvelopeExtent());
        }
        catch(DataSourceException e) {
            _log.info("...threw data source exception");            
        }
        _log.info("...ending type enforcement tests");
    }

    
}
