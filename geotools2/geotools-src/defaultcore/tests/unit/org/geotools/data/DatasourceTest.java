package org.geotools.data;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.feature.CollectionEvent;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIndex;
import org.geotools.filter.AbstractFilter;
import org.geotools.filter.LiteralExpression;

import com.vividsolutions.jts.geom.Envelope;

public class DatasourceTest extends TestCase implements CollectionListener {

    private static final org.geotools.filter.FilterFactory filterFactory = org.geotools.filter.FilterFactory.createFilterFactory();
    FeatureCollection ft = null;
    FeatureIndex fi = null;
    public DatasourceTest(java.lang.String testName){
        super(testName);
    }
    public static void main(java.lang.String[] args) {
      System.setProperty("dataFolder", "../../../testData");
      junit.textui.TestRunner.run(suite());
    }
    public static Test suite() {
        TestSuite suite = new TestSuite(DatasourceTest.class);
        return suite;
    }
    
    public void testLoad() throws java.lang.Exception {
        System.out.println("testLoad() called");
        java.net.URL testData = getClass().getResource("/testData/Furizibad.csv");
//        String dataFolder = System.getProperty("dataFolder");
//        if(dataFolder==null){
//            //then we are being run by maven
//            dataFolder = System.getProperty("basedir");
//            dataFolder+="/tests/unit/testData";
//        }
//        dataFolder = new java.io.File(dataFolder).getAbsolutePath();
//        String path =new java.io.File(dataFolder,"Furizibad.csv").getCanonicalFile().toString();
        
        //DataSource ds = new VeryBasicDataSource(path);
        DataSource ds = new VeryBasicDataSource(testData);
        Envelope ex = new Envelope(0, 360, 0, 180.0);
        
        org.geotools.filter.GeometryFilter gf = filterFactory.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
        LiteralExpression right = filterFactory.createBBoxExpression(ex);
        gf.addRightGeometry(right);
        try{
            ft = ds.getFeatures(gf);
        }
        catch(DataSourceException e){
            e.printStackTrace();
            fail(e.toString());
        }
        
        assertEquals(5,ft.size());
    }
    
    public void collectionChanged(CollectionEvent tce) {
        System.out.println("collectionChanged called()");
    }
}

