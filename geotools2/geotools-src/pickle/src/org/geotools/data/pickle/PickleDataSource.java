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
      PickledFeatureProtocol protocol = PickledFeatureProtocol.defaultProtocol();
      protocol.setOutput(obj, clz);
      protocol.write(collection);
      obj.flush();
      clz.flush();
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
      PickledFeatureProtocol protocol = PickledFeatureProtocol.defaultProtocol();
      FileInputStream obj = new FileInputStream(objectFile);
      FileInputStream clz = new FileInputStream(classFile);
      protocol.setInput(obj,clz);
      protocol.read(collection);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public Feature getFeature(int idx) throws DataSourceException {
    FileInputStream obj = null;
    FileInputStream clz = null;
    try {
      PickledFeatureProtocol protocol = PickledFeatureProtocol.defaultProtocol();
      obj = new FileInputStream(objectFile);
      clz = new FileInputStream(classFile);
      protocol.setInput(obj,clz);
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
    for (int i = 0, ii = 3; i < ii; i++) {
      long time = System.currentTimeMillis();
      org.geotools.data.shapefile.ShapefileDataSource sds = new org.geotools.data.shapefile.ShapefileDataSource(url);
      FeatureCollection fc = sds.getFeatures();
      System.out.println("read shapefile in " + (System.currentTimeMillis() - time));
      
      time = System.currentTimeMillis();
      sds = new org.geotools.data.shapefile.ShapefileDataSource(tmpShp);
      sds.setFeatures(fc);
      System.out.println("wrote shapefile in " + (System.currentTimeMillis() - time));
      
      time = System.currentTimeMillis();
      PickleDataSource pds = new PickleDataSource(new File(tmpDir), "junkyCrap");
      pds.setFeatures(fc);
      System.out.println("pickled in " + (System.currentTimeMillis() - time));
  
      time = System.currentTimeMillis();
      fc = pds.getFeatures();
      System.out.println("unpickled in " + (System.currentTimeMillis() - time));
      
      // uncomment to test serial access
      
//      long total = 0;
//      pds = new PickleDataSource(new File("/tmp"), "junkyCrap");
//      for (int j = 0, jj = fc.size(); j < jj; j++) {
//        
//        time = System.currentTimeMillis();
//        if (pds.getFeature(j) == null)
//          System.out.println("FAILURE");
//        total += System.currentTimeMillis() - time;
//      }
//      System.out.println("random access average (" + fc.size() + ") "  + (double)total / fc.size());
    }
  }
}
