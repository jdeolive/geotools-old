/*
 * ShapefileTest.java
 * JUnit based test
 *
 * Created on 12 February 2002, 21:27
 */

package org.geotools.shapefile;

import junit.framework.*;
import java.net.*;
import com.vividsolutions.jts.geom.*;
import java.io.*;
import org.geotools.shapefile.shapefile.*;

/**
 *
 * @author James Macgill
 */
public class ShapefileTest extends TestCase {
    
    public ShapefileTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
      if (System.getProperty("dataFolder") == null)
        System.setProperty("dataFolder", ShapefileTest.class.getResource("/testData").getFile());
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
            File f = new File(dataFolder,"statepop.shp");
            System.out.println("Testing ability to load "+f.getAbsolutePath());
            FileInputStream in = new FileInputStream(f);
            ShapefileReader reader = new ShapefileReader(in.getChannel());
            int cnt = 0;
            while (reader.hasNext()) {
              reader.nextRecord().shape();
              cnt++;
            }
            assertEquals("Number of Geometries loaded incorect",49,cnt);
            //Geometry bounds = shapes.getEnvelope();
            //bounds.
            //System.out.pritln(""+bounds.ggetMinX()+" "+bounds.getMinY()+" "+bounds.getMaxX()+" "+bounds.getMaxY()); 
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
            File f = new File(dataFolder,"pointtest.shp");
            System.out.println("Testing ability to load "+f.getAbsolutePath());
            FileInputStream in = new FileInputStream(f);
            ShapefileReader reader = new ShapefileReader(in.getChannel());
            int cnt = 0;
            while (reader.hasNext()) {
              reader.nextRecord().shape();
              cnt++;
            }
            assertEquals("Number of Geometries loaded incorect",10,cnt);
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
            File f = new File(dataFolder,"polygontest.shp");
            System.out.println("Testing ability to load "+f.getAbsolutePath());
            FileInputStream in = new FileInputStream(f);
            ShapefileReader reader = new ShapefileReader(in.getChannel());
            int cnt = 0;
            while (reader.hasNext()) {
              reader.nextRecord().shape();
              cnt++;
            }
            assertEquals("Number of Geometries loaded incorect",2,cnt);
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
            File f = new File(dataFolder,"pointtest.shp");
            System.out.println("Testing ability to load "+f.getAbsolutePath());
            FileInputStream in = new FileInputStream(f);
            ShapefileReader reader = new ShapefileReader(in.getChannel());
            int cnt = 0;
            while (reader.hasNext()) {
              reader.nextRecord().shape();
              cnt++;
            }
            assertEquals("Number of Geometries loaded incorect",10,cnt);
            
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
          File f = new File(dataFolder,"holeTouchEdge.shp");
            System.out.println("Testing ability to load "+f.getAbsolutePath());
            FileInputStream in = new FileInputStream(f);
            ShapefileReader reader = new ShapefileReader(in.getChannel());
            int cnt = 0;
            while (reader.hasNext()) {
              reader.nextRecord().shape();
              cnt++;
            }
            assertEquals("Number of Geometries loaded incorect",1,cnt);
            
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
          File f = new File(dataFolder,"extraAtEnd.shp");
            System.out.println("Testing ability to load "+f.getAbsolutePath());
            FileInputStream in = new FileInputStream(f);
            ShapefileReader reader = new ShapefileReader(in.getChannel());
            int cnt = 0;
            while (reader.hasNext()) {
              reader.nextRecord().shape();
              cnt++;
            }
            assertEquals("Number of Geometries loaded incorect",3,cnt);
          
        }
        catch(Exception e){
            System.out.println("Load failed becaouse of "+e);
            e.printStackTrace();
            fail("Load failed because of exception "+e.toString());
        }
    }    
}
