/*
 * GMLDataSourceTest.java
 * JUnit based test
 *
 * Created on 04 March 2002, 16:09
 */

package org.geotools.gml;

import org.geotools.datasource.*;
import org.geotools.datasource.extents.*;
import org.geotools.featuretable.*;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import java.net.URL;
import java.util.*;

/**
 *
 * @author ian
 */
public class GMLDataSourceTest extends junit.framework.TestCase {
    FeatureTable table = null;
    FeatureIndex fi = null;
    public GMLDataSourceTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(GMLDataSourceTest.class);
        
        return suite;
    }
    static int NTests = 6;
    public void testRead() throws Exception{
        System.out.println("testRead");
        String[] results = {"Point","LineString","LinearRing",
        "Polygon","Polygon","Point","Point","Point"};
        int r=0;
        for(int j=1;j<=NTests;j++){
            //System.out.println("******* TEST NUMBER "+j+" ***********");
            String dataFolder = System.getProperty("dataFolder");
            URL url = new URL("file:///"+dataFolder+"/testGML"+j+".gml");
            //System.out.println("Testing ability to read "+url);
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream()));
            GMLReader gmlr = new GMLReader(in);
            
            com.vividsolutions.jts.geom.GeometryCollection gc = gmlr.read();
            com.vividsolutions.jts.geom.GeometryCollectionIterator gci = new com.vividsolutions.jts.geom.GeometryCollectionIterator(gc);
            int i=0;
            Geometry g = (Geometry)gci.next(); // eat the first geomcollection
            while(gci.hasNext()){
                g=(Geometry)gci.next();
                assertEquals(results[r++],g.getGeometryType());
                //System.out.println("Geometry["+(i++)+"] = "+g.getGeometryType());
                if(!g.getGeometryType().equalsIgnoreCase("GeometryCollection")){
                    //System.out.println("Coordinates:");
                    com.vividsolutions.jts.geom.Coordinate[] c = g.getCoordinates();
                    //for(int k=0;k<c.length;k++){
                    //System.out.println("\t "+c[k].toString());
                    //}
                }
            }
            //System.out.println("********* END TEST "+j+" **********");
        }
        // now a more complex test - using testGML7
        String dataFolder = System.getProperty("dataFolder");
        URL url = new URL("file:///"+dataFolder+"/testGML7.gml");
        java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream()));
        GMLReader gmlr = new GMLReader(in);
        
        com.vividsolutions.jts.geom.GeometryCollection gc = gmlr.read();
        com.vividsolutions.jts.geom.GeometryCollectionIterator gci = new com.vividsolutions.jts.geom.GeometryCollectionIterator(gc);
        int i=0;
        Geometry g = (Geometry)gci.next(); // eat the first geomcollection
        com.vividsolutions.jts.geom.Polygon box =(com.vividsolutions.jts.geom.Polygon)gci.next();
        com.vividsolutions.jts.geom.Polygon poly = (com.vividsolutions.jts.geom.Polygon)gci.next();
        Point p1 = (Point)gci.next();
        Point p2 = (Point)gci.next();
        Point p3 = (Point)gci.next();
        assertEquals(true,box.equals(poly.getEnvelope()));
        assertEquals(false,poly.contains(p1));
        assertEquals(true,poly.contains(p2));
        assertEquals(false,poly.contains(p3));
        assertEquals(1,poly.getNumInteriorRing());
        
    }
    public void testDataSource() {
        System.out.println("testDataSource");
        try{
            String dataFolder = System.getProperty("dataFolder");
            URL url = new URL("file:///"+dataFolder+"/testGML7.gml");
            System.out.println("Testing ability to load "+url+" as datasource");
            GMLDataSource ds = new GMLDataSource(url);
            
            table = new DefaultFeatureTable(ds);
            
           
            
            EnvelopeExtent r = new EnvelopeExtent();
            r.setBounds(new com.vividsolutions.jts.geom.Envelope(-100, 100, 0, 100.0));
            
            //table.requestExtent(r);
            try {
                //fi = new SimpleIndex(table, "LONGITUDE");
                
                table.getFeatures(r);
               
            }catch(Exception exp) {
                System.out.println("Exception requesting Extent : "+exp.getClass().getName()+" : "+exp.getMessage());
            }

            assertEquals(5,table.getFeatures().length);
            Feature[] features = table.getFeatures();
            
            for(int i=0;i<features.length;i++){
                
                System.out.println("Feature  : "+features[i].getGeometry().toString());
            }
        }catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
            fail("Load failed because of exception "+e.toString());
        }
    }

            
    
}