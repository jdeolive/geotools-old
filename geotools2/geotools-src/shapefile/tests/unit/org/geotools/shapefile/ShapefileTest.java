/*
 * ShapefileTest.java
 * JUnit based test
 *
 * Created on 12 February 2002, 21:27
 */

package org.geotools.shapefile;

import junit.framework.*;
import java.net.*;
import cmp.LEDataStream.*;
import com.vividsolutions.jts.geom.*;
import java.io.*;

/**
 *
 * @author James Macgill
 */
public class ShapefileTest extends TestCase {
    
    public ShapefileTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ShapefileTest.class);
        return suite;
    }
    
    public void testLoadingStatePop() {
        
        String dataFolder = System.getProperty("dataFolder");
        try{
            URL url = new URL("file:///"+dataFolder+"/statePop.shp");
            System.out.println("Testing ability to load "+url);
            URLConnection uc = url.openConnection();
            BufferedInputStream in = new BufferedInputStream(uc.getInputStream());
             System.out.println("state pop length "+uc.getContentLength());
            LEDataInputStream sfile = new LEDataInputStream(in);
            GeometryCollection shapes = Shapefile.read(sfile, new GeometryFactory());
            assertEquals("Number of Geometries loaded incorect",49,shapes.getNumGeometries());
            //Geometry bounds = shapes.getEnvelope();
            //bounds.
            //System.out.println(""+bounds.getMinX()+" "+bounds.getMinY()+" "+bounds.getMaxX()+" "+bounds.getMaxY()); 
        }
        catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
            fail("Load failed because of exception "+e.toString());
        }
    }
    /*
    public void testLoadingSamplePointFile() {
        
        String dataFolder = System.getProperty("dataFolder");
        try{
            URL url = new URL("file:///"+dataFolder+"/pointTest.shp");
            System.out.println("Testing ability to load "+url);
            URLConnection uc = url.openConnection();
            BufferedInputStream in = new BufferedInputStream(uc.getInputStream());
            
            LEDataInputStream sfile = new LEDataInputStream(in);
            GeometryCollection shapes = Shapefile.read(sfile, new GeometryFactory());
            assertEquals("Number of Geometries loaded incorect",10,shapes.getNumGeometries());
        }
        catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
            fail("Load failed because of exception "+e.toString());
        }
    }*/
    /*
    public void testLoadingSamplePolygonFile() {
        
        String dataFolder = System.getProperty("dataFolder");
        try{
            URL url = new URL("file:///"+dataFolder+"/polygonTest.shp");
            System.out.println("Testing ability to load "+url);
            URLConnection uc = url.openConnection();
            BufferedInputStream in = new BufferedInputStream(uc.getInputStream());
            System.out.println("polygon file length "+uc.getContentLength());
            LEDataInputStream sfile = new LEDataInputStream(in);
            GeometryCollection shapes = Shapefile.read(sfile, new GeometryFactory());
            assertEquals("Number of Geometries loaded incorect",2,shapes.getNumGeometries());
        }
        catch(Exception e){
            System.out.println("Load failed becaouse of "+e);
            e.printStackTrace();
            fail("Load failed because of exception "+e.toString());
        }
    }*/
    
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
}
