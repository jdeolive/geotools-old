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
import java.util.*;
import org.geotools.data.shapefile.shp.*;
import org.geotools.feature.*;

/**
 * @author Ian Schneider
 * @author James Macgill
 */
public class ShapefileTest extends TestCaseSupport {
  
  final String STATEPOP = "statepop.shp";
  final String STATEPOP_IDX = "statepop.shx";
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
    loadMemoryMapped(STATEPOP,49);
  }
  
  public void testLoadingSamplePointFile() throws Exception {
    loadShapes(POINTTEST, 10);
    loadMemoryMapped(POINTTEST,10);
  }
  
  public void testLoadingSamplePolygonFile() throws Exception {
    loadShapes(POLYGONTEST, 2);
    loadMemoryMapped(POLYGONTEST,2);
  }
  
  public void testLoadingTwice() throws Exception {
    loadShapes(POINTTEST, 10);
    loadShapes(POINTTEST, 10);
    loadShapes(STATEPOP,49);
    loadShapes(STATEPOP,49);
    loadShapes(POLYGONTEST, 2);
    loadShapes(POLYGONTEST, 2);
  }
  
  /**
   * It is posible for a point in a hole to touch the edge of its containing shell
   * This test checks that such polygons can be loaded ok.
   */
  public void testPolygonHoleTouchAtEdge() throws Exception {
    loadShapes(HOLETOUCHEDGE, 1);
    loadMemoryMapped(HOLETOUCHEDGE,1);
  }
  /**
   * It is posible for a shapefile to have extra information past the end
   * of the normal feature area, this tests checks that this situation is
   * delt with ok.
   */
  public void testExtraAtEnd() throws Exception {
    loadShapes(EXTRAATEND,3);
    loadMemoryMapped(EXTRAATEND,3);
  }
  
  public void testIndexFile() throws Exception {
    ShapefileReader reader1 = new ShapefileReader(getTestResourceChannel(STATEPOP));
    ShapefileReader reader2 = new ShapefileReader(getReadableFileChannel(STATEPOP));
    IndexFile index = new IndexFile(getTestResourceChannel(STATEPOP_IDX));
    for (int i = 0; i < index.getRecordCount(); i++) {
      if (reader1.hasNext()) {

        Geometry g1 = (Geometry) reader1.nextRecord().shape();
        Geometry g2 = (Geometry) reader2.shapeAt(2 * (index.getOffset(i) - 50));
        assertTrue(g1.equalsExact(g2));
        
      } else {
        fail("uneven number of records");
      }
      //assertEquals(reader1.nextRecord().offset(),index.getOffset(i));
    }
  }
  
  public void testHolyPolygons() throws Exception {
    Geometry g = readGeometry("holyPoly");
    
    FeatureTypeFactory factory = FeatureTypeFactory.newInstance("junk");
    factory.addType(AttributeTypeFactory.newAttributeType("a",Geometry.class));
    FeatureType type = factory.getFeatureType();
    FeatureCollection features = FeatureCollections.newCollection();
    features.add(type.create(new Object[] {g}));
    
    File tmpFile = getTempFile();
    tmpFile.delete();
    
    // write features
    ShapefileDataSource s = new ShapefileDataSource(tmpFile.toURL());
    s.setFeatures(features);
    
    s = new ShapefileDataSource(tmpFile.toURL());
    FeatureCollection fc = s.getFeatures();
    
    ShapefileReadWriteTest.compare(features,fc);
  }
  
  public void testSkippingRecords() throws Exception {
    ShapefileReader r = new ShapefileReader(getTestResourceChannel(STATEPOP));
    int idx = 0;
    while (r.hasNext()) {
        idx++;
        r.nextRecord();
    }
    assertEquals(49,idx);
  }
  
  public void testShapefileReaderRecord() throws Exception {
    ShapefileReader reader = new ShapefileReader(getTestResourceChannel(STATEPOP));
    ArrayList offsets = new ArrayList();
    while (reader.hasNext()) {
      ShapefileReader.Record record = reader.nextRecord();
      offsets.add(new Integer(record.offset()));
      Geometry geom = (Geometry) record.shape();
      assertEquals(new Envelope(record.minX,record.maxX,record.minY,record.maxY), geom.getEnvelopeInternal());
      record.toString();
    }
    reader = new ShapefileReader(getReadableFileChannel(STATEPOP));
    for (int i = 0, ii = offsets.size(); i < ii; i++) {
      reader.shapeAt( ((Integer)offsets.get(i)).intValue() ); 
    }
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
  
  private void loadMemoryMapped(String resource,int expected) throws Exception {
    ShapefileReader reader = new ShapefileReader(getReadableFileChannel(resource));
    int cnt = 0;
    while (reader.hasNext()) {
      reader.nextRecord().shape();
      cnt++;
    }
    assertEquals("Number of Geometries loaded incorect for : " + resource,expected,cnt);
  }

}
