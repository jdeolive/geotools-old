package org.geotools.data.postgis;

import junit.framework.*;
import org.apache.log4j.Category;
import org.apache.log4j.BasicConfigurator;
import com.vividsolutions.jts.geom.Envelope;
import java.util.*;
import org.geotools.data.*;
import org.geotools.feature.*;
import org.geotools.filter.*;
import org.geotools.datasource.extents.*;

public class PostgisTest extends TestCase {
    
    /** Standard logging instance */
    private static Category _log = Category.getInstance(PostgisTest.class.getName());
    
    DataSource postgis = null;
    
    FeatureCollection collection = new FeatureCollectionDefault();
    
    public PostgisTest(String testName){
        super(testName);
    }
    
    public static void main(String[] args) {
        BasicConfigurator.configure();
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        BasicConfigurator.configure();
        _log.info("starting suite...");
        TestSuite suite = new TestSuite(PostgisTest.class);
        _log.info("made suite...");
        return suite;
    }
    
    public void setUp() {
        BasicConfigurator.configure();
        _log.info("creating postgis connection...");
        PostgisConnection db = new PostgisConnection("feathers.leeds.ac.uk","5432","postgis_test");
        _log.info("created new db connection");
        db.setLogin("postgis_ro","postgis_ro");
        _log.info("set the login");
        postgis = new PostgisDataSource(db, "testset");
        _log.info("created new datasource");
    }
    
    
    public void testImport() {
        _log.info("starting type enforcement tests...");
        try {
            GeometryFilter gf =
            new GeometryFilter(AbstractFilter.GEOMETRY_BBOX);
            ExpressionLiteral right =
            new BBoxExpression(new Envelope(0,0,300,300));
            gf.addRightGeometry(right);
            
            
            postgis.getFeatures(collection,gf);
        }
        catch(DataSourceException dse) {
            _log.info("...threw data source exception",dse);
            this.fail("...threw data source exception");
        }
        catch(IllegalFilterException fe) {
            _log.info("...threw filter exception",fe);
            this.fail("...threw filter exception");
        }
        assertEquals(18,collection.getFeatures().length);
        _log.info("...ending type enforcement tests");
    }
    
    
}
