package org.geotools.data;

import com.vividsolutions.jts.geom.*;
import org.geotools.datasource.extents.*;
import org.geotools.feature.*;
import java.util.*;
import junit.framework.*;

public class DatasourceTest extends TestCase implements CollectionListener {
    FeatureCollection ft = null;
    FeatureIndex fi = null;
    public DatasourceTest(java.lang.String testName){
        super(testName);
    }
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    public static Test suite() {
        TestSuite suite = new TestSuite(DatasourceTest.class);
        return suite;
    }
    
    public void testLoad() throws java.io.IOException {
        System.out.println("testLoad() called");
        EnvelopeExtent r = new EnvelopeExtent();
        r.setBounds(new Envelope(50, 360, 0, 180.0));
        String dataFolder = System.getProperty("dataFolder");
        
        String path =new java.io.File(dataFolder,"Furizibad.csv").getCanonicalFile().toString();
        
        ft = new FeatureCollectionDefault(new VeryBasicDataSource(path));
        //ft.setLoadMode(FeatureTable.MODE_LOAD_INTERSECT);
        ft.addListener(this);
        // Request extent
        EnvelopeExtent ex = new EnvelopeExtent(0, 360, 0, 180.0);
        try{
            ft.getFeatures(ex);
        }
        catch(DataSourceException e){
            fail(e.toString());
        }
        System.out.println("Loaded: "+ft.getFeatures());
        assertEquals(5,ft.getFeatures().length);
    }
    
    public void collectionChanged(CollectionEvent tce) {
        System.out.println("collectionChanged called()");
    }
}

