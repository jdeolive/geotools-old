/*
 * PickleDataSource.java
 *
 * Created on June 4, 2003, 8:33 AM
 */

package org.geotools.data.pickle;

import java.io.*;
import java.util.*;
import org.geotools.data.*;
import org.geotools.feature.*;

/**
 *
 * @author  Ian Schneider
 */
public class PickleDataSource extends AbstractDataSource {
  
  final File objectFile;
  final File classFile;
  
  /** Creates a new instance of PickleDataSource */
  public PickleDataSource(File parent,String name) {
    this.objectFile = new File(parent, name + ".obj");
    this.classFile  = new File(parent, name + ".clz");
  }
  
  protected DataSourceMetaData createMetaData() {
    MetaDataSupport mds = new MetaDataSupport();
    mds.setSupportsSetFeatures(true);
    return mds;
  }
  
  public void setFeatures(FeatureCollection collection) throws DataSourceException {
    FileOutputStream obj = null;
    FileOutputStream clz = null;
    try {
      obj = new FileOutputStream(objectFile);
      clz = new FileOutputStream(classFile);
      PickledFeatureProtocol protocol = PickledFeatureProtocol.getWriter(obj,clz);
      protocol.write(collection);
    } catch (IOException ioe) {
      throw new DataSourceException("IOError",ioe);
    } finally {
      try {
        if (obj != null)
          obj.close();
        if (clz != null)
          clz.close();
      } catch (Exception e) {}
    }
  }
  
  
  public void getFeatures(FeatureCollection collection, Query query) throws DataSourceException {
    try {
      FileInputStream obj = new FileInputStream(objectFile);
      FileInputStream clz = new FileInputStream(classFile);
      PickledFeatureProtocol protocol = PickledFeatureProtocol.getReader(obj,clz);
      protocol.read(collection);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public Feature getFeature(int idx) throws DataSourceException {
    FileInputStream obj = null;
    FileInputStream clz = null;
    try {
      obj = new FileInputStream(objectFile);
      clz = new FileInputStream(classFile);
      PickledFeatureProtocol protocol = PickledFeatureProtocol.getReader(obj,clz);
      return protocol.read(idx);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (obj != null)
          obj.close();
        if (clz != null)
          clz.close();
      } catch (Exception e) {}
    }
    return null;
  }
  
  public FeatureType getSchema() throws DataSourceException {
    return null;
  }
  
  
  
  public static final void main(String[] args) throws Exception {
    String tmpDir = System.getProperty("java.io.tmpdir");
    java.net.URL url = new java.net.URL("file:" + args[0]);
    java.net.URL tmpShp = new java.net.URL("file:" + tmpDir + "/tmpshp_delete_me.shp");
    java.util.logging.Logger.getLogger("org.geotools.data.shapefile").setLevel(java.util.logging.Level.OFF);
    long[] times = new long[4];
    final int trials = 10;
    Compiler.compileClass(PickleDataSource.class);
    for (int i = 0; i < trials; i++) {
      System.gc();
      long time = System.currentTimeMillis();
      org.geotools.data.shapefile.ShapefileDataSource sds = new org.geotools.data.shapefile.ShapefileDataSource(url);
      FeatureCollection fc = sds.getFeatures();
      System.out.println("read shapefile");
      if (i > 0)
      times[0] += (System.currentTimeMillis() - time);
      
      System.gc();
      time = System.currentTimeMillis();
      sds = new org.geotools.data.shapefile.ShapefileDataSource(tmpShp);
      sds.setFeatures(fc);
      System.out.println("wrote shapefile");
      if (i > 0)
      times[1] += (System.currentTimeMillis() - time);
      
      System.gc();
      time = System.currentTimeMillis();
      PickleDataSource pds = new PickleDataSource(new File(tmpDir), "pickletest_deleteme");
      pds.setFeatures(fc);
      System.out.println("pickled");
      if (i > 0)
      times[2] += (System.currentTimeMillis() - time);
      
      System.gc();
      time = System.currentTimeMillis();
      fc = pds.getFeatures();
      System.out.println("unpickled");
      if (i > 0)
      times[3] += (System.currentTimeMillis() - time);
      
      // uncomment to test serial access
//      
//            long total = 0;
//            for (int j = 0, jj = fc.size(); j < jj; j++) {
//      
//              time = System.currentTimeMillis();
//              if (pds.getFeature(j) == null)
//                System.out.println("FAILURE");
//              total += System.currentTimeMillis() - time;
//            }
//            System.out.println("random access average (" + fc.size() + ") "  + (double)total / fc.size());
    }
    System.out.println("shapefile read  : " + (times[0] / (double)trials));
    System.out.println("shapefile write : " + (times[1] / (double)trials));
    System.out.println("pickle          : " + (times[2] / (double)trials));
    System.out.println("unpickle        : " + (times[3] / (double)trials));
  }
  
}
