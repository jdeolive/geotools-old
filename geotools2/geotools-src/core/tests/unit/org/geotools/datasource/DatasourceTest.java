package org.geotools.datasource;

import com.vividsolutions.jts.geom.*;
import java.util.*;
import junit.framework.*;

public class DatasourceTest extends TestCase implements TableChangedListener {
    FeatureTable ft = null;
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
        
        ft = new FeatureTable(new VeryBasicDataSource(path));
        ft.setLoadMode(FeatureTable.MODE_LOAD_INTERSECT);
        ft.addTableChangedListener(this);
        // Request extent
        try {
            fi = new SimpleIndex(ft, "LONGITUDE");
            ft.requestExtent(r);
        }
        catch(Exception exp) {
            System.out.println("Exception requesting Extent : "+exp.getClass().getName()+" : "+exp.getMessage());
        }
        // Request another extent - should get in ahead of the first extent completing, stop the load, and load this extent instead
        try {
            r.setBounds(new Envelope(0, 50, 0, 180.0));
            ft.requestExtent(r);
        }
        catch(Exception exp) {
            fail("Exception requesting Extent : "+exp.getClass().getName()+" : "+exp.getMessage());
        }
    }
    
    public void tableChanged(TableChangedEvent tce) {
        System.out.println("tableChanged called()");
        System.out.println("tableChanged() : Return code : "+tce.getCode());
        if (tce.getCode()!=tce.TABLE_OK) {
            System.out.println("tableChanged() : Exception :"+tce.getException().getClass().getName());
            tce.getException().printStackTrace();
        }
        else {
            System.out.println("Load code ok - Reading Index");
            Iterator it = fi.getFeatures().iterator();
            while (it.hasNext()) {
                Feature f = (Feature)it.next();
                System.out.println("Feature  : "+f.row[0].toString());
            }
        }
    }
}

