/*
 * ShapefileTest.java
 * JUnit based test
 *
 * Created on 12 February 2002, 21:27
 */

package org.geotools.data.shapefile;

import junit.framework.*;
import java.net.*;
import com.vividsolutions.jts.geom.*;
import java.io.*;
import org.geotools.data.shapefile.shp.*;

/**
 * @author Ian Schneider
 * @author James Macgill
 */
public class ShapefileTest extends TestCaseSupport {
  
  final String STATEPOP = "statepop.shp";
  final String POINTTEST = "pointtest.shp";
  final String POLYGONTEST = "polygontest.shp";
  final String HOLETOUCHEDGE = "holeTouchEdge.shp";
  final String EXTRAATEND = "extraAtEnd.shp";
  
  public ShapefileTest(String testName) {
    super(testName);
  }
  
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite(ShapefileTest.class));
  }
  
  public void testLoadingStatePop() throws Exception {
    loadShapes(STATEPOP,49);
  }
  
  public void testLoadingSamplePointFile() throws Exception {
    loadShapes(POINTTEST, 10);
  }
  
  public void testLoadingSamplePolygonFile() throws Exception {
    loadShapes(POLYGONTEST, 2);
  }
  
  public void testLoadingTwice() throws Exception {
    loadShapes(POINTTEST, 10);
    loadShapes(POINTTEST, 10);
  }
  
  /**
   * It is posible for a point in a hole to touch the edge of its containing shell
   * This test checks that such polygons can be loaded ok.
   */
  public void testPolygonHoleTouchAtEdge() throws Exception {
    loadShapes(HOLETOUCHEDGE, 1);
  }
  /**
   * It is posible for a shapefile to have extra information past the end
   * of the normal feature area, this tests checks that this situation is
   * delt with ok.
   */
  public void testExtraAtEnd() throws Exception {
    loadShapes(EXTRAATEND,3);
  }
  
  private void loadShapes(String resource,int expected) throws Exception {
    ShapefileReader reader = new ShapefileReader(getTestResourceChannel(resource));
    int cnt = 0;
    while (reader.hasNext()) {
      reader.nextRecord().shape();
      cnt++;
    }
    assertEquals("Number of Geometries loaded incorect for : " + resource,expected,cnt);
  }

}
