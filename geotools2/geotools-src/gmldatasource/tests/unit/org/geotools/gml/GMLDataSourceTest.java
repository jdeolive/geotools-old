/*
 * GMLDataSourceTest.java
 * JUnit based test
 *
 * Created on 04 March 2002, 16:09
 */

package org.geotools.gml;

import junit.framework.*;
import org.geotools.datasource.*;
import java.net.*;
import java.io.*;
import com.vividsolutions.jts.geom.*;
import java.util.*;

/**
 *
 * @author ian
 */
public class GMLDataSourceTest extends TestCase {
    
    public GMLDataSourceTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(GMLDataSourceTest.class);
        
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
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            GMLReader gmlr = new GMLReader(in);
            
            GeometryCollection gc = gmlr.read();
            GeometryCollectionIterator gci = new GeometryCollectionIterator(gc);
            int i=0;
            Geometry g = (Geometry)gci.next(); // eat the first geomcollection
            while(gci.hasNext()){
                g=(Geometry)gci.next();
                assertEquals(results[r++],g.getGeometryType());
                //System.out.println("Geometry["+(i++)+"] = "+g.getGeometryType());
                if(!g.getGeometryType().equalsIgnoreCase("GeometryCollection")){
                    //System.out.println("Coordinates:");
                    Coordinate[] c = g.getCoordinates();
                    //for(int k=0;k<c.length;k++){
                        //System.out.println("\t "+c[k].toString());
                    //}
                }
            }
            //System.out.println("********* END TEST "+j+" **********");
        }
        // now a more complex test - using testGML7
        String dataFolder = System.getProperty("dataFolder");
        URL url = new URL("file:///"+dataFolder+"/testGML7.xml");
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        GMLReader gmlr = new GMLReader(in);
        
        GeometryCollection gc = gmlr.read();
        GeometryCollectionIterator gci = new GeometryCollectionIterator(gc);
        int i=0;
        Geometry g = (Geometry)gci.next(); // eat the first geomcollection
        Polygon box =(Polygon)gci.next();
        Polygon poly = (Polygon)gci.next();
        Point p1 = (Point)gci.next();
        Point p2 = (Point)gci.next();
        Point p3 = (Point)gci.next();
        assertEquals(true,box.equals(poly.getEnvelope()));
        assertEquals(false,poly.contains(p1));
        assertEquals(true,poly.contains(p2));
        assertEquals(false,poly.contains(p3));
        assertEquals(1,poly.getNumInteriorRing());
        
    }
    public void xtestDataSource() throws Exception{
        System.out.println("testDataSource");
        String dataFolder = System.getProperty("dataFolder");
        URL url = new URL("file:///"+dataFolder+"/testGML1.gml");
        System.out.println("Testing ability to load "+url+" as datasource");
        GMLDataSource ds = new GMLDataSource(url);
        
        ds.load((Extent)null);
    }
}