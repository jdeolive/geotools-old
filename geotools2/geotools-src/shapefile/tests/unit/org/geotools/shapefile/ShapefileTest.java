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
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData";
        }
        try{
            URL url = new URL("file:///"+dataFolder+"/statepop.shp");
            System.out.println("Testing ability to load "+url);
            Shapefile shapefile = new Shapefile(url);
            GeometryCollection shapes = shapefile.read(new GeometryFactory());
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
    
    
    
    public void testLoadingSamplePointFile() {
        
        String dataFolder = System.getProperty("dataFolder");
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData";
        }
        try{
            URL url = new URL("file:///"+dataFolder+"/pointtest.shp");
            System.out.println("Testing ability to load "+url);
            URLConnection uc = url.openConnection();
            BufferedInputStream in = new BufferedInputStream(uc.getInputStream());
            
            Shapefile shapefile = new Shapefile(url);
            GeometryCollection shapes = shapefile.read(new GeometryFactory());
            assertEquals("Number of Geometries loaded incorect",10,shapes.getNumGeometries());
        }
        catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
            fail("Load failed because of exception "+e.toString());
        }
    }
    
    public void testLoadingSamplePolygonFile() {
        
        String dataFolder = System.getProperty("dataFolder");
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData";
        }
        try{
            URL url = new URL("file:///"+dataFolder+"/polygontest.shp");
            System.out.println("Testing ability to load "+url);
            
            Shapefile shapefile = new Shapefile(url);
            GeometryCollection shapes = shapefile.read(new GeometryFactory());
            assertEquals("Number of Geometries loaded incorect",2,shapes.getNumGeometries());
        }
        catch(Exception e){
            System.out.println("Load failed becaouse of "+e);
            e.printStackTrace();
            fail("Load failed because of exception "+e.toString());
        }
    } 
    
     public void testLoadingTwice() {
        
        String dataFolder = System.getProperty("dataFolder");
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData";
        }
        try{
            URL url = new URL("file:///"+dataFolder+"/polygontest.shp");
            System.out.println("Testing ability to load "+url);
            
            Shapefile shapefile = new Shapefile(url);
            GeometryCollection shapes = shapefile.read(new GeometryFactory());
            shapes = shapefile.read(new GeometryFactory());
            assertEquals("Number of Geometries loaded incorect",2,shapes.getNumGeometries());
        }
        catch(Exception e){
            System.out.println("Load failed becaouse of "+e);
            e.printStackTrace();
            fail("Load failed because of exception "+e.toString());
        }
    }    
    
    /**
     * It is posible for a point in a hole to touch the edge of its containing shell
     * This test checks that such polygons can be loaded ok.
     */
    public void testPolygonHoleTouchAtEdge() {
        
        String dataFolder = System.getProperty("dataFolder");
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData";
        }
        try{
            URL url = new URL("file:///"+dataFolder+"/holeTouchEdge.shp");
            System.out.println("Testing ability to load "+url);
            
            Shapefile shapefile = new Shapefile(url);
            GeometryCollection shapes = shapefile.read(new GeometryFactory());
            assertEquals("Number of Geometries loaded incorect",1,shapes.getNumGeometries());
        }
        catch(Exception e){
            System.out.println("Load failed becaouse of "+e);
            e.printStackTrace();
            fail("Load failed because of exception "+e.toString());
        }
    }    
    /**
     * It is posible for a shapefile to have extra information past the end
     * of the normal feature area, this tests checks that this situation is
     * delt with ok.
     */
    public void testExtraAtEnd() {
        
        String dataFolder = System.getProperty("dataFolder");
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData";
        }
        try{
            URL url = new URL("file:///"+dataFolder+"/extraAtEnd.shp");
            System.out.println("Testing ability to load "+url);
            
            Shapefile shapefile = new Shapefile(url);
            GeometryCollection shapes = shapefile.read(new GeometryFactory());
            assertEquals("Number of Geometries loaded incorect",3,shapes.getNumGeometries());
        }
        catch(Exception e){
            System.out.println("Load failed becaouse of "+e);
            e.printStackTrace();
            fail("Load failed because of exception "+e.toString());
        }
    }    
}
